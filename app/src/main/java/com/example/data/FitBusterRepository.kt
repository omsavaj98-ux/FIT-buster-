package com.example.data

import android.content.Context
import com.example.data.local.FitBusterDao
import com.example.data.local.FitBusterDatabase
import com.example.data.local.UserProfileEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.model.CertificateRecord
import com.example.model.DisciplineLog
import com.example.model.MealEntry
import com.example.model.MedalRecord
import com.example.model.MissionProgress
import com.example.model.MissionStatus
import com.example.model.RestLog
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

const val LOCAL_USER_ID = "local_athlete"

class FitBusterRepository(
    private val dao: FitBusterDao,
    private val activeUserId: String = LOCAL_USER_ID
) {
    constructor(context: Context) : this(
        dao = FitBusterDatabase.getInstance(context.applicationContext).fitBusterDao(),
        activeUserId = LOCAL_USER_ID
    )

    private fun newId(): String = UUID.randomUUID().toString()

    // --- Real-time Local Observation Flows ---
    fun observeUserProfile(userId: String = activeUserId): Flow<UserProfile?> =
        dao.observeUserProfile(userId).map { it?.toDomain() }

    fun observeWorkouts(userId: String = activeUserId): Flow<List<WorkoutSession>> =
        dao.observeWorkouts(userId).map { list -> list.map { it.toDomain() } }

    fun observeSprints(userId: String = activeUserId): Flow<List<SprintRecord>> =
        dao.observeSprints(userId).map { list -> list.map { it.toDomain() } }

    fun observeRestLogs(userId: String = activeUserId): Flow<List<RestLog>> =
        dao.observeRestLogs(userId).map { list -> list.map { it.toDomain() } }

    fun observeMeals(userId: String = activeUserId): Flow<List<MealEntry>> =
        dao.observeMeals(userId).map { list -> list.map { it.toDomain() } }

    fun observeWaterLogs(userId: String = activeUserId): Flow<List<WaterLog>> =
        dao.observeWaterLogs(userId).map { list -> list.map { it.toDomain() } }

    fun observeMissions(userId: String = activeUserId): Flow<List<MissionProgress>> =
        dao.observeMissions(userId).map { list -> list.map { it.toDomain() } }

    fun observeMedals(userId: String = activeUserId): Flow<List<MedalRecord>> =
        dao.observeMedals(userId).map { list -> list.map { it.toDomain() } }

    fun observeCertificates(userId: String = activeUserId): Flow<List<CertificateRecord>> =
        dao.observeCertificates(userId).map { list -> list.map { it.toDomain() } }

    fun observeDisciplineLogs(userId: String = activeUserId): Flow<List<DisciplineLog>> =
        dao.observeDisciplineLogs(userId).map { list -> list.map { it.toDomain() } }

    // --- Profile Creation & Updates ---
    suspend fun ensureUserProfile(defaultName: String, email: String = ""): Result<Unit> {
        return try {
            val existing = dao.getUserProfileOnce(activeUserId)
            if (existing == null) {
                val now = System.currentTimeMillis()
                val cleanName = defaultName.trim().take(60).ifBlank { "FIT BUSTER Athlete" }
                val cleanEmail = email.trim().take(120)
                val defaultProfile = UserProfile(
                    id = activeUserId,
                    userId = activeUserId,
                    displayName = cleanName,
                    email = cleanEmail,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
                dao.upsertUserProfile(defaultProfile.toEntity())
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileFields(fields: Map<String, Any>): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val current = dao.getUserProfileOnce(activeUserId) ?: UserProfile(
                id = activeUserId,
                userId = activeUserId,
                displayName = "FIT BUSTER Athlete",
                createdAtMillis = now,
                updatedAtMillis = now
            ).toEntity()

            val updated = applyProfileUpdates(current, fields, now)
            dao.upsertUserProfile(updated)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun applyProfileUpdates(
        current: UserProfileEntity,
        fields: Map<String, Any>,
        now: Long
    ): UserProfileEntity {
        var next = current.copy(updatedAtMillis = now)
        for ((key, value) in fields) {
            when (key) {
                "displayName" -> (value as? String)?.let {
                    next = next.copy(displayName = it.trim().take(60).ifBlank { next.displayName })
                }
                "email" -> (value as? String)?.let {
                    next = next.copy(email = it.trim().take(120))
                }
                "age" -> (value as? Number)?.let {
                    next = next.copy(age = it.toLong().coerceIn(10L, 120L))
                }
                "fitnessGoal" -> (value as? String)?.let {
                    next = next.copy(fitnessGoal = it.trim().take(120))
                }
                "preferredWorkout" -> (value as? String)?.let {
                    next = next.copy(preferredWorkout = it.take(60))
                }
                "dailyCalorieGoal" -> (value as? Number)?.let {
                    next = next.copy(dailyCalorieGoal = it.toLong().coerceIn(800L, 8000L))
                }
                "dailyWaterGoalMl" -> (value as? Number)?.let {
                    next = next.copy(dailyWaterGoalMl = it.toLong().coerceIn(500L, 10000L))
                }
                "dailyProteinGoalG" -> (value as? Number)?.let {
                    next = next.copy(dailyProteinGoalG = it.toLong().coerceIn(10L, 500L))
                }
                "vegetarianDefault" -> (value as? Boolean)?.let {
                    next = next.copy(vegetarianDefault = it)
                }
                "challengePoints" -> (value as? Number)?.let {
                    next = next.copy(challengePoints = it.toLong().coerceAtLeast(0L))
                }
                "disciplinePoints" -> (value as? Number)?.let {
                    next = next.copy(disciplinePoints = it.toLong().coerceAtLeast(0L))
                }
                "challengeLevel" -> (value as? Number)?.let {
                    next = next.copy(challengeLevel = it.toLong().coerceIn(1L, 10L))
                }
                "punishmentLevel" -> (value as? Number)?.let {
                    next = next.copy(punishmentLevel = it.toLong().coerceIn(1L, 10L))
                }
                "currentStreak" -> (value as? Number)?.let {
                    next = next.copy(currentStreak = it.toLong().coerceAtLeast(0L))
                }
                "longestStreak" -> (value as? Number)?.let {
                    next = next.copy(longestStreak = it.toLong().coerceAtLeast(0L))
                }
                "lastActiveDate" -> (value as? String)?.let {
                    next = next.copy(lastActiveDate = it)
                }
                "recoveryDates" -> @Suppress("UNCHECKED_CAST") (value as? List<*>)?.let { raw ->
                    next = next.copy(recoveryDates = raw.filterIsInstance<String>().take(366))
                }
                "activeDates" -> @Suppress("UNCHECKED_CAST") (value as? List<*>)?.let { raw ->
                    next = next.copy(activeDates = raw.filterIsInstance<String>().take(366))
                }
                "notificationsEnabled" -> (value as? Boolean)?.let {
                    next = next.copy(notificationsEnabled = it)
                }
                "workoutReminderEnabled" -> (value as? Boolean)?.let {
                    next = next.copy(workoutReminderEnabled = it)
                }
                "workoutReminderHour" -> (value as? Number)?.let {
                    next = next.copy(workoutReminderHour = it.toLong().coerceIn(0L, 23L))
                }
                "workoutReminderMinute" -> (value as? Number)?.let {
                    next = next.copy(workoutReminderMinute = it.toLong().coerceIn(0L, 59L))
                }
                "waterReminderEnabled" -> (value as? Boolean)?.let {
                    next = next.copy(waterReminderEnabled = it)
                }
                "missionReminderEnabled" -> (value as? Boolean)?.let {
                    next = next.copy(missionReminderEnabled = it)
                }
                "streakReminderEnabled" -> (value as? Boolean)?.let {
                    next = next.copy(streakReminderEnabled = it)
                }
                "themeAccent" -> (value as? String)?.let {
                    next = next.copy(themeAccent = it)
                }
                "profilePrivate" -> (value as? Boolean)?.let {
                    next = next.copy(profilePrivate = it)
                }
                "hasSignature" -> (value as? Boolean)?.let {
                    next = next.copy(hasSignature = it)
                }
                "signatureUpdatedAtMillis" -> (value as? Number)?.let {
                    next = next.copy(signatureUpdatedAtMillis = it.toLong())
                }
            }
        }
        return next
    }

    // --- Workouts, Sprints, Rest ---
    suspend fun addWorkout(session: WorkoutSession): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val sanitized = session.copy(
                id = session.id.ifBlank { newId() },
                userId = activeUserId,
                customTitle = session.customTitle.take(80),
                durationSeconds = session.durationSeconds.coerceIn(0L, 86400L),
                distanceMeters = session.distanceMeters.coerceAtLeast(0.0),
                averagePaceSecPerKm = session.averagePaceSecPerKm.coerceAtLeast(0.0),
                caloriesBurned = session.caloriesBurned.coerceIn(0L, 10000L),
                gpsPointCount = session.gpsPointCount.coerceAtLeast(0L),
                notes = session.notes.take(300),
                createdAtMillis = if (session.createdAtMillis > 0L) session.createdAtMillis else now,
                updatedAtMillis = now
            )
            dao.insertWorkout(sanitized.toEntity(sanitized.id))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSprint(sprint: SprintRecord): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val sanitized = sprint.copy(
                id = sprint.id.ifBlank { newId() },
                userId = activeUserId,
                timeMillis = sprint.timeMillis.coerceIn(1L, 600000L),
                createdAtMillis = if (sprint.createdAtMillis > 0L) sprint.createdAtMillis else now,
                updatedAtMillis = now
            )
            dao.insertSprint(sanitized.toEntity(sanitized.id))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRestLog(log: RestLog): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val sanitized = log.copy(
                id = log.id.ifBlank { newId() },
                userId = activeUserId,
                plannedSeconds = log.plannedSeconds.coerceIn(1L, 3600L),
                actualSeconds = log.actualSeconds.coerceIn(0L, 7200L),
                createdAtMillis = if (log.createdAtMillis > 0L) log.createdAtMillis else now,
                updatedAtMillis = now
            )
            dao.insertRestLog(sanitized.toEntity(sanitized.id))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Diet & Water ---
    suspend fun addMeal(meal: MealEntry): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val sanitized = meal.copy(
                id = meal.id.ifBlank { newId() },
                userId = activeUserId,
                foodName = meal.foodName.trim().take(100),
                calories = meal.calories.coerceIn(0L, 10000L),
                proteinG = meal.proteinG.coerceIn(0L, 500L),
                carbsG = meal.carbsG.coerceIn(0L, 1000L),
                fatsG = meal.fatsG.coerceIn(0L, 500L),
                createdAtMillis = if (meal.createdAtMillis > 0L) meal.createdAtMillis else now,
                updatedAtMillis = now
            )
            dao.insertMeal(sanitized.toEntity(sanitized.id))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMeal(mealId: String): Result<Unit> {
        return try {
            dao.deleteMealById(mealId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addWaterLog(amountMl: Long, dateKey: String, customId: String = ""): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val id = customId.ifBlank { newId() }
            val log = WaterLog(
                id = id,
                userId = activeUserId,
                amountMl = amountMl.coerceIn(1L, 10000L),
                dateKey = dateKey,
                createdAtMillis = now,
                updatedAtMillis = now
            )
            dao.insertWaterLog(log.toEntity(id))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Missions, Medals, Certificates, Discipline ---
    suspend fun upsertMission(missionId: String, mission: MissionProgress): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val existing = dao.getMissionOnce(missionId)
            if (existing == null) {
                val created = mission.copy(
                    id = missionId,
                    userId = activeUserId,
                    missionKey = mission.missionKey.take(80),
                    title = mission.title.take(100),
                    description = mission.description.take(250),
                    targetValue = mission.targetValue.coerceAtLeast(1L),
                    currentValue = mission.currentValue.coerceAtLeast(0L),
                    rewardPoints = mission.rewardPoints.coerceAtLeast(1L),
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
                dao.upsertMission(created.toEntity(missionId))
            } else if (existing.status == MissionStatus.ACTIVE.name) {
                val updated = existing.copy(
                    currentValue = mission.currentValue.coerceAtLeast(0L),
                    status = mission.status,
                    completedDateKey = mission.completedDateKey,
                    updatedAtMillis = now
                )
                dao.upsertMission(updated)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockMedalOnce(medal: MedalRecord): Result<Boolean> {
        return try {
            val medalId = medal.medalKey.ifBlank { medal.id }
            if (dao.countMedalsById(medalId) > 0) {
                return Result.success(false) // Duplicate prevention!
            }
            val now = System.currentTimeMillis()
            val sanitized = medal.copy(
                id = medalId,
                userId = activeUserId,
                title = medal.title.take(100),
                description = medal.description.take(250),
                createdAtMillis = now,
                updatedAtMillis = now
            )
            val insertedRow = dao.insertMedalIfAbsent(sanitized.toEntity(medalId))
            Result.success(insertedRow != -1L)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockCertificateOnce(certId: String, cert: CertificateRecord): Result<Boolean> {
        return try {
            if (dao.countCertificatesById(certId) > 0) {
                return Result.success(false)
            }
            val now = System.currentTimeMillis()
            val sanitized = cert.copy(
                id = certId,
                userId = activeUserId,
                certificateCode = cert.certificateCode.take(40),
                certificateName = cert.certificateName.take(120),
                recipientName = cert.recipientName.trim().take(80).ifBlank { "FIT BUSTER Athlete" },
                achievementDescription = cert.achievementDescription.take(250),
                medalEarned = cert.medalEarned.take(100),
                createdAtMillis = now,
                updatedAtMillis = now
            )
            val insertedRow = dao.insertCertificateIfAbsent(sanitized.toEntity(certId))
            Result.success(insertedRow != -1L)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addDisciplineLog(log: DisciplineLog): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val sanitized = log.copy(
                id = log.id.ifBlank { newId() },
                userId = activeUserId,
                level = log.level.coerceIn(1L, 10L),
                repsCompleted = log.repsCompleted.coerceIn(0L, 500L),
                durationSeconds = log.durationSeconds.coerceIn(0L, 7200L),
                pointsAwarded = log.pointsAwarded.coerceIn(0L, 1000L),
                createdAtMillis = if (log.createdAtMillis > 0L) log.createdAtMillis else now,
                updatedAtMillis = now
            )
            dao.insertDisciplineLog(sanitized.toEntity(sanitized.id))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Restore & Reset ---
    suspend fun restoreFromBackupBundle(bundle: BackupBundle): Result<Unit> {
        return try {
            ensureUserProfile(bundle.profile?.displayName ?: "FIT BUSTER Athlete")
            bundle.profile?.let { p ->
                updateProfileFields(
                    mapOf(
                        "displayName" to p.displayName.take(60),
                        "age" to p.age.coerceIn(10L, 120L),
                        "fitnessGoal" to p.fitnessGoal.take(120),
                        "preferredWorkout" to p.preferredWorkout.take(60),
                        "dailyCalorieGoal" to p.dailyCalorieGoal.coerceIn(800L, 8000L),
                        "dailyWaterGoalMl" to p.dailyWaterGoalMl.coerceIn(500L, 10000L),
                        "dailyProteinGoalG" to p.dailyProteinGoalG.coerceIn(10L, 500L),
                        "vegetarianDefault" to p.vegetarianDefault,
                        "challengePoints" to p.challengePoints.coerceAtLeast(0L),
                        "disciplinePoints" to p.disciplinePoints.coerceAtLeast(0L),
                        "challengeLevel" to p.challengeLevel.coerceIn(1L, 10L),
                        "punishmentLevel" to p.punishmentLevel.coerceIn(1L, 10L),
                        "currentStreak" to p.currentStreak.coerceAtLeast(0L),
                        "longestStreak" to p.longestStreak.coerceAtLeast(0L),
                        "lastActiveDate" to p.lastActiveDate,
                        "recoveryDates" to p.recoveryDates.take(366),
                        "activeDates" to p.activeDates.take(366),
                        "themeAccent" to p.themeAccent
                    )
                )
            }
            bundle.workouts.forEach { addWorkout(it.copy(userId = activeUserId)) }
            bundle.sprints.forEach { addSprint(it.copy(userId = activeUserId)) }
            bundle.meals.forEach { addMeal(it.copy(userId = activeUserId)) }
            bundle.waterLogs.forEach { addWaterLog(it.amountMl, it.dateKey, it.id) }
            bundle.medals.forEach { unlockMedalOnce(it.copy(userId = activeUserId)) }
            bundle.certificates.forEach { c ->
                val certId = c.id.ifBlank { c.certificateCode.replace(Regex("[^a-zA-Z0-9_-]"), "_") }
                unlockCertificateOnce(certId, c.copy(userId = activeUserId))
            }
            bundle.disciplineLogs.forEach { addDisciplineLog(it.copy(userId = activeUserId)) }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetAllProgress(): Result<Unit> {
        return try {
            dao.clearAllUserProgress(activeUserId)
            updateProfileFields(
                mapOf(
                    "challengePoints" to 0L,
                    "disciplinePoints" to 0L,
                    "challengeLevel" to 1L,
                    "punishmentLevel" to 1L,
                    "currentStreak" to 0L,
                    "longestStreak" to 0L,
                    "lastActiveDate" to "",
                    "recoveryDates" to emptyList<String>(),
                    "activeDates" to emptyList<String>()
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
