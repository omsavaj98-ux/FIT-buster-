package com.example.util

import android.location.Location
import com.example.model.FitBusterCatalog
import com.example.model.MedalRecord
import com.example.model.MissionProgress
import com.example.model.MissionStatus
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import com.example.model.WorkoutType
import kotlin.math.roundToLong

object FitnessCalculator {

    fun estimateCalories(
        workoutType: WorkoutType,
        durationSeconds: Long,
        weightKg: Double = 70.0
    ): Long {
        if (durationSeconds <= 0) return 0L
        val hours = durationSeconds / 3600.0
        return (workoutType.metValue * weightKg * hours).roundToLong().coerceAtLeast(1L)
    }

    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }

    fun calculatePaceSecPerKm(durationSeconds: Long, distanceMeters: Double): Double {
        if (distanceMeters < 20.0 || durationSeconds <= 0) return 0.0
        val km = distanceMeters / 1000.0
        return durationSeconds / km
    }

    fun levelForPoints(points: Long): Int {
        val thresholds = FitBusterCatalog.challengeLevelThresholds
        var level = 1
        for (i in thresholds.indices) {
            if (points >= thresholds[i]) {
                level = i + 1
            }
        }
        return level.coerceIn(1, 10)
    }

    fun pointsProgressInLevel(points: Long): Pair<Long, Long> {
        val lvl = levelForPoints(points)
        if (lvl >= 10) return Pair(points, FitBusterCatalog.challengeLevelThresholds.last())
        val floor = FitBusterCatalog.challengeLevelThresholds[lvl - 1]
        val ceil = FitBusterCatalog.challengeLevelThresholds[lvl]
        return Pair((points - floor).coerceAtLeast(0L), (ceil - floor).coerceAtLeast(1L))
    }

    fun computeStreaks(activeDates: List<String>, recoveryDates: List<String>, todayKey: String): Pair<Long, Long> {
        val allHealthyDays = (activeDates + recoveryDates).toSet()
        if (allHealthyDays.isEmpty()) return Pair(0L, 0L)

        // Current streak ending today or yesterday (grace for today not yet completed)
        var current = 0L
        val startOffset = if (allHealthyDays.contains(todayKey)) 0 else 1
        for (offset in startOffset until 366) {
            val key = FitBusterDateUtil.dateKeyDaysAgo(offset)
            if (allHealthyDays.contains(key)) {
                current++
            } else {
                break
            }
        }

        // Longest streak across last 365 days
        var longest = 0L
        var running = 0L
        for (offset in 365 downTo 0) {
            val key = FitBusterDateUtil.dateKeyDaysAgo(offset)
            if (allHealthyDays.contains(key)) {
                running++
                if (running > longest) longest = running
            } else {
                running = 0L
            }
        }
        return Pair(current, maxOf(longest, current))
    }

    fun evaluateMedalProgress(
        medalKey: String,
        missions: List<MissionProgress>,
        workouts: List<WorkoutSession>,
        sprints: List<SprintRecord>,
        waterLogs: List<WaterLog>,
        profile: UserProfile,
        disciplineCompletedCount: Int,
        unlockedMedalKeys: Set<String>
    ): Int {
        val completedMissions = missions.count { it.status == MissionStatus.COMPLETED.name }
        val runCount = workouts.count { it.workoutType == WorkoutType.OUTDOOR_RUN.name }
        val runDistanceKm = workouts.filter {
            it.workoutType == WorkoutType.OUTDOOR_RUN.name || it.workoutType == WorkoutType.OUTDOOR_WALK.name
        }.sumOf { it.distanceMeters } / 1000.0
        val strengthCount = workouts.count { it.workoutType == WorkoutType.FUNCTIONAL_STRENGTH.name }
        val waterGoal = profile.dailyWaterGoalMl.coerceAtLeast(500L)
        val hydratedDays = waterLogs.groupBy { it.dateKey }.count { (_, logs) ->
            logs.sumOf { it.amountMl } >= waterGoal
        }

        return when (medalKey) {
            "bronze_fitness_beginner",
            "silver_fitness_warrior",
            "gold_fitness_champion",
            "platinum_fitness_legend" -> completedMissions
            "running_star_medal" -> maxOf(runCount, (runDistanceKm / 2.5).toInt())
            "sprint_champion_medal" -> sprints.size
            "strength_master_medal" -> strengthCount
            "hydration_champion_medal" -> hydratedDays
            "fitness_consistency_medal" -> profile.longestStreak.toInt()
            "discipline_medal" -> disciplineCompletedCount
            "ultimate_fitness_champion_medal" -> {
                if (profile.challengeLevel >= 10L) 8 else unlockedMedalKeys.count { it != "ultimate_fitness_champion_medal" }
            }
            else -> 0
        }
    }

    fun isMedalEarned(
        medalKey: String,
        missions: List<MissionProgress>,
        workouts: List<WorkoutSession>,
        sprints: List<SprintRecord>,
        waterLogs: List<WaterLog>,
        profile: UserProfile,
        disciplineCompletedCount: Int,
        unlockedMedals: List<MedalRecord>
    ): Boolean {
        val def = FitBusterCatalog.medalDefinitions.firstOrNull { it.key == medalKey } ?: return false
        val unlockedKeys = unlockedMedals.map { it.medalKey }.toSet()
        if (medalKey == "running_star_medal") {
            val runCount = workouts.count { it.workoutType == WorkoutType.OUTDOOR_RUN.name }
            val runDistMeters = workouts.filter { it.workoutType == WorkoutType.OUTDOOR_RUN.name }.sumOf { it.distanceMeters }
            return runCount >= 10 || runDistMeters >= 25_000.0
        }
        val progress = evaluateMedalProgress(
            medalKey, missions, workouts, sprints, waterLogs, profile, disciplineCompletedCount, unlockedKeys
        )
        return progress >= def.targetCount
    }
}
