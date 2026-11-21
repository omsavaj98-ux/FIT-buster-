package com.example.data

import com.example.model.CertificateRecord
import com.example.model.DisciplineLog
import com.example.model.DisciplineStatus
import com.example.model.FitBusterCatalog
import com.example.model.MedalRecord
import com.example.model.MissionProgress
import com.example.model.MissionStatus
import com.example.model.MissionTimeframe
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import com.example.model.WorkoutType
import com.example.util.FitBusterDateUtil
import com.example.util.FitnessCalculator
import java.util.Locale

data class EvaluationSummary(
    val newlyCompletedMissions: List<MissionProgress> = emptyList(),
    val newlyUnlockedMedals: List<MedalRecord> = emptyList(),
    val newlyUnlockedCertificates: List<CertificateRecord> = emptyList(),
    val totalPointsEarned: Long = 0L,
    val newLevel: Int = 1
)

class RewardEngine(private val repository: FitBusterRepository) {

    suspend fun syncMissionsAndRewards(
        userId: String,
        profile: UserProfile,
        workouts: List<WorkoutSession>,
        sprints: List<SprintRecord>,
        waterLogs: List<WaterLog>,
        existingMissions: List<MissionProgress>,
        existingMedals: List<MedalRecord>,
        existingCertificates: List<CertificateRecord>,
        disciplineLogs: List<DisciplineLog>
    ): EvaluationSummary {
        val today = FitBusterDateUtil.todayKey()
        val weekStart = FitBusterDateUtil.weekStartKey()
        val weekEnd = FitBusterDateUtil.weekEndKey()
        val monthPrefix = FitBusterDateUtil.monthKey(today)
        val monthEnd = FitBusterDateUtil.monthEndKey()

        val existingById = existingMissions.associateBy { it.id }
        val newlyCompleted = mutableListOf<MissionProgress>()
        var pointsEarned = 0L

        for (template in FitBusterCatalog.missionTemplates) {
            val missionId = when (template.timeframe) {
                MissionTimeframe.DAILY -> "daily_${today}_${template.key}"
                MissionTimeframe.WEEKLY -> "weekly_${weekStart}_${template.key}"
                MissionTimeframe.MONTHLY -> "monthly_${monthPrefix}_${template.key}"
            }
            val deadlineKey = when (template.timeframe) {
                MissionTimeframe.DAILY -> today
                MissionTimeframe.WEEKLY -> weekEnd
                MissionTimeframe.MONTHLY -> monthEnd
            }

            val currentRecord = existingById[missionId]
            if (currentRecord != null && currentRecord.status != MissionStatus.ACTIVE.name) {
                continue // Already completed or skipped
            }

            val progressVal = computeTemplateProgress(
                templateKey = template.key,
                today = today,
                weekStart = weekStart,
                monthPrefix = monthPrefix,
                workouts = workouts,
                sprints = sprints,
                waterLogs = waterLogs,
                disciplineLogs = disciplineLogs
            )

            val reached = progressVal >= template.targetValue
            val newStatus = if (reached) MissionStatus.COMPLETED.name else MissionStatus.ACTIVE.name
            val completedDate = if (reached) today else ""

            val updatedMission = MissionProgress(
                id = missionId,
                userId = userId,
                missionKey = template.key,
                title = template.title,
                description = template.description,
                timeframe = template.timeframe.name,
                category = template.category.name,
                difficulty = template.difficulty.name,
                targetValue = template.targetValue,
                currentValue = progressVal.coerceAtMost(template.targetValue),
                rewardPoints = template.rewardPoints,
                deadlineKey = deadlineKey,
                status = newStatus,
                completedDateKey = completedDate
            )

            val needsWrite = currentRecord == null ||
                currentRecord.currentValue != updatedMission.currentValue ||
                currentRecord.status != updatedMission.status

            if (needsWrite) {
                val res = repository.upsertMission(missionId, updatedMission)
                if (res.isSuccess && reached && currentRecord?.status != MissionStatus.COMPLETED.name) {
                    newlyCompleted.add(updatedMission)
                    pointsEarned += template.rewardPoints
                }
            }
        }

        val updatedTotalPoints = profile.challengePoints + pointsEarned
        val updatedLevel = FitnessCalculator.levelForPoints(updatedTotalPoints)
        if (pointsEarned > 0L || updatedLevel.toLong() != profile.challengeLevel) {
            repository.updateProfileFields(
                mapOf(
                    "challengePoints" to updatedTotalPoints,
                    "challengeLevel" to updatedLevel.toLong()
                )
            )
        }

        val allMissionsAfter = existingMissions.filter { m -> newlyCompleted.none { it.id == m.id } } + newlyCompleted
        val disciplineCompleted = disciplineLogs.count { it.status == DisciplineStatus.COMPLETED.name }
        val updatedProfile = profile.copy(
            challengePoints = updatedTotalPoints,
            challengeLevel = updatedLevel.toLong()
        )

        val newlyUnlockedMedals = mutableListOf<MedalRecord>()
        val unlockedKeys = existingMedals.map { it.medalKey }.toMutableSet()

        for (def in FitBusterCatalog.medalDefinitions) {
            if (unlockedKeys.contains(def.key)) continue
            val earned = FitnessCalculator.isMedalEarned(
                medalKey = def.key,
                missions = allMissionsAfter,
                workouts = workouts,
                sprints = sprints,
                waterLogs = waterLogs,
                profile = updatedProfile,
                disciplineCompletedCount = disciplineCompleted,
                unlockedMedals = existingMedals + newlyUnlockedMedals
            )
            if (earned) {
                val record = MedalRecord(
                    id = def.key,
                    userId = userId,
                    medalKey = def.key,
                    title = def.title,
                    tier = def.tier.name,
                    description = def.description,
                    unlockedDateKey = today
                )
                val res = repository.unlockMedalOnce(record)
                if (res.getOrNull() == true) {
                    newlyUnlockedMedals.add(record)
                    unlockedKeys.add(def.key)
                }
            }
        }

        val newlyUnlockedCertificates = mutableListOf<CertificateRecord>()
        val existingCertIds = existingCertificates.map { it.id }.toSet()
        val recipientAccountName = profile.displayName.trim().ifBlank { "FIT BUSTER Athlete" }
        val monthlyCompletedCount = allMissionsAfter.count {
            it.timeframe == MissionTimeframe.MONTHLY.name && it.status == MissionStatus.COMPLETED.name
        }

        val certConditions = mapOf(
            "cert_fitness_beginner" to unlockedKeys.contains("bronze_fitness_beginner"),
            "cert_fitness_warrior" to unlockedKeys.contains("silver_fitness_warrior"),
            "cert_fitness_champion" to unlockedKeys.contains("gold_fitness_champion"),
            "cert_running_champion" to unlockedKeys.contains("running_star_medal"),
            "cert_sprint_champion" to unlockedKeys.contains("sprint_champion_medal"),
            "cert_strength_warrior" to unlockedKeys.contains("strength_master_medal"),
            "cert_7_day_consistency" to unlockedKeys.contains("fitness_consistency_medal"),
            "cert_monthly_champion" to (monthlyCompletedCount >= 1 || updatedProfile.longestStreak >= 30L),
            "cert_discipline_achievement" to unlockedKeys.contains("discipline_medal"),
            "cert_ultimate_fit_buster" to unlockedKeys.contains("ultimate_fitness_champion_medal")
        )

        for (certDef in FitBusterCatalog.certificateDefinitions) {
            if (existingCertIds.contains(certDef.key)) continue
            if (certConditions[certDef.key] == true) {
                val suffix = (userId.hashCode() and 0xFFFF).toString(16).uppercase(Locale.US).padStart(4, '0')
                val code = "FB-${today.replace("-", "")}-${certDef.key.takeLast(4).uppercase(Locale.US)}-$suffix"
                val cert = CertificateRecord(
                    id = certDef.key,
                    userId = userId,
                    certificateCode = code,
                    certificateName = certDef.name,
                    recipientName = recipientAccountName,
                    achievementDescription = certDef.description,
                    medalEarned = certDef.medalTitle,
                    completionDateKey = today
                )
                val res = repository.unlockCertificateOnce(certDef.key, cert)
                if (res.getOrNull() == true) {
                    newlyUnlockedCertificates.add(cert)
                }
            }
        }

        return EvaluationSummary(
            newlyCompletedMissions = newlyCompleted,
            newlyUnlockedMedals = newlyUnlockedMedals,
            newlyUnlockedCertificates = newlyUnlockedCertificates,
            totalPointsEarned = pointsEarned,
            newLevel = updatedLevel
        )
    }

    private fun computeTemplateProgress(
        templateKey: String,
        today: String,
        weekStart: String,
        monthPrefix: String,
        workouts: List<WorkoutSession>,
        sprints: List<SprintRecord>,
        waterLogs: List<WaterLog>,
        disciplineLogs: List<DisciplineLog>
    ): Long {
        return when (templateKey) {
            "daily_workout_1" -> workouts.count { it.dateKey == today && it.durationSeconds >= 300L }.toLong()
            "daily_hydration_goal" -> waterLogs.filter { it.dateKey == today }.sumOf { it.amountMl }
            "daily_run_distance" -> workouts.filter {
                it.dateKey == today && (it.workoutType == WorkoutType.OUTDOOR_RUN.name ||
                    it.workoutType == WorkoutType.OUTDOOR_WALK.name ||
                    it.workoutType == WorkoutType.SPRINT_TRAINING.name)
            }.sumOf { it.distanceMeters }.toLong()
            "daily_sprint_drill" -> sprints.count { it.dateKey == today }.toLong()
            "weekly_workouts_4" -> workouts.count { it.dateKey >= weekStart }.toLong()
            "weekly_strength_2" -> workouts.count {
                it.dateKey >= weekStart && it.workoutType == WorkoutType.FUNCTIONAL_STRENGTH.name
            }.toLong()
            "weekly_sprint_5" -> sprints.count { it.dateKey >= weekStart }.toLong()
            "weekly_hydration_5days" -> waterLogs.filter { it.dateKey >= weekStart }.map { it.dateKey }.distinct().size.toLong()
            "monthly_minutes_300" -> workouts.filter { it.dateKey.startsWith(monthPrefix) }.sumOf { it.durationSeconds } / 60L
            "monthly_distance_25km" -> workouts.filter { it.dateKey.startsWith(monthPrefix) }.sumOf { it.distanceMeters }.toLong()
            "monthly_challenge_discipline" -> disciplineLogs.count {
                it.dateKey.startsWith(monthPrefix) && it.status == DisciplineStatus.COMPLETED.name
            }.toLong()
            else -> 0L
        }
    }
}
