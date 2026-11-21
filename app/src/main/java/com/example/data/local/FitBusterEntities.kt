package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.CertificateRecord
import com.example.model.DisciplineLog
import com.example.model.MealEntry
import com.example.model.MedalRecord
import com.example.model.MissionProgress
import com.example.model.RestLog
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import org.json.JSONArray

class FitBusterTypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        val array = JSONArray()
        value?.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { idx -> array.optString(idx) }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val displayName: String,
    val email: String,
    val age: Long,
    val fitnessGoal: String,
    val preferredWorkout: String,
    val dailyCalorieGoal: Long,
    val dailyWaterGoalMl: Long,
    val dailyProteinGoalG: Long,
    val vegetarianDefault: Boolean,
    val challengePoints: Long,
    val disciplinePoints: Long,
    val challengeLevel: Long,
    val punishmentLevel: Long,
    val currentStreak: Long,
    val longestStreak: Long,
    val lastActiveDate: String,
    val recoveryDates: List<String>,
    val activeDates: List<String>,
    val notificationsEnabled: Boolean,
    val workoutReminderEnabled: Boolean,
    val workoutReminderHour: Long,
    val workoutReminderMinute: Long,
    val waterReminderEnabled: Boolean,
    val missionReminderEnabled: Boolean,
    val streakReminderEnabled: Boolean,
    val themeAccent: String,
    val profilePrivate: Boolean,
    val hasSignature: Boolean,
    val signatureUpdatedAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    userId = userId,
    displayName = displayName,
    email = email,
    age = age,
    fitnessGoal = fitnessGoal,
    preferredWorkout = preferredWorkout,
    dailyCalorieGoal = dailyCalorieGoal,
    dailyWaterGoalMl = dailyWaterGoalMl,
    dailyProteinGoalG = dailyProteinGoalG,
    vegetarianDefault = vegetarianDefault,
    challengePoints = challengePoints,
    disciplinePoints = disciplinePoints,
    challengeLevel = challengeLevel,
    punishmentLevel = punishmentLevel,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActiveDate = lastActiveDate,
    recoveryDates = recoveryDates,
    activeDates = activeDates,
    notificationsEnabled = notificationsEnabled,
    workoutReminderEnabled = workoutReminderEnabled,
    workoutReminderHour = workoutReminderHour,
    workoutReminderMinute = workoutReminderMinute,
    waterReminderEnabled = waterReminderEnabled,
    missionReminderEnabled = missionReminderEnabled,
    streakReminderEnabled = streakReminderEnabled,
    themeAccent = themeAccent,
    profilePrivate = profilePrivate,
    hasSignature = hasSignature,
    signatureUpdatedAtMillis = signatureUpdatedAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id.ifBlank { userId },
    userId = userId,
    displayName = displayName,
    email = email,
    age = age,
    fitnessGoal = fitnessGoal,
    preferredWorkout = preferredWorkout,
    dailyCalorieGoal = dailyCalorieGoal,
    dailyWaterGoalMl = dailyWaterGoalMl,
    dailyProteinGoalG = dailyProteinGoalG,
    vegetarianDefault = vegetarianDefault,
    challengePoints = challengePoints,
    disciplinePoints = disciplinePoints,
    challengeLevel = challengeLevel,
    punishmentLevel = punishmentLevel,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActiveDate = lastActiveDate,
    recoveryDates = recoveryDates,
    activeDates = activeDates,
    notificationsEnabled = notificationsEnabled,
    workoutReminderEnabled = workoutReminderEnabled,
    workoutReminderHour = workoutReminderHour,
    workoutReminderMinute = workoutReminderMinute,
    waterReminderEnabled = waterReminderEnabled,
    missionReminderEnabled = missionReminderEnabled,
    streakReminderEnabled = streakReminderEnabled,
    themeAccent = themeAccent,
    profilePrivate = profilePrivate,
    hasSignature = hasSignature,
    signatureUpdatedAtMillis = signatureUpdatedAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "workouts")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val workoutType: String,
    val customTitle: String,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val averagePaceSecPerKm: Double,
    val caloriesBurned: Long,
    val gpsTracked: Boolean,
    val gpsPointCount: Long,
    val dateKey: String,
    val notes: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    userId = userId,
    workoutType = workoutType,
    customTitle = customTitle,
    durationSeconds = durationSeconds,
    distanceMeters = distanceMeters,
    averagePaceSecPerKm = averagePaceSecPerKm,
    caloriesBurned = caloriesBurned,
    gpsTracked = gpsTracked,
    gpsPointCount = gpsPointCount,
    dateKey = dateKey,
    notes = notes,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun WorkoutSession.toEntity(generatedId: String): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    workoutType = workoutType,
    customTitle = customTitle,
    durationSeconds = durationSeconds,
    distanceMeters = distanceMeters,
    averagePaceSecPerKm = averagePaceSecPerKm,
    caloriesBurned = caloriesBurned,
    gpsTracked = gpsTracked,
    gpsPointCount = gpsPointCount,
    dateKey = dateKey,
    notes = notes,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "sprints")
data class SprintRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val distanceMeters: Long,
    val timeMillis: Long,
    val manualEntry: Boolean,
    val personalBest: Boolean,
    val dateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun SprintRecordEntity.toDomain(): SprintRecord = SprintRecord(
    id = id,
    userId = userId,
    distanceMeters = distanceMeters,
    timeMillis = timeMillis,
    manualEntry = manualEntry,
    personalBest = personalBest,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun SprintRecord.toEntity(generatedId: String): SprintRecordEntity = SprintRecordEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    distanceMeters = distanceMeters,
    timeMillis = timeMillis,
    manualEntry = manualEntry,
    personalBest = personalBest,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "rest_logs")
data class RestLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val plannedSeconds: Long,
    val actualSeconds: Long,
    val status: String,
    val dateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun RestLogEntity.toDomain(): RestLog = RestLog(
    id = id,
    userId = userId,
    plannedSeconds = plannedSeconds,
    actualSeconds = actualSeconds,
    status = status,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun RestLog.toEntity(generatedId: String): RestLogEntity = RestLogEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    plannedSeconds = plannedSeconds,
    actualSeconds = actualSeconds,
    status = status,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "meals")
data class MealEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val foodName: String,
    val mealType: String,
    val calories: Long,
    val proteinG: Long,
    val carbsG: Long,
    val fatsG: Long,
    val vegetarian: Boolean,
    val dateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun MealEntryEntity.toDomain(): MealEntry = MealEntry(
    id = id,
    userId = userId,
    foodName = foodName,
    mealType = mealType,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatsG = fatsG,
    vegetarian = vegetarian,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun MealEntry.toEntity(generatedId: String): MealEntryEntity = MealEntryEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    foodName = foodName,
    mealType = mealType,
    calories = calories,
    proteinG = proteinG,
    carbsG = carbsG,
    fatsG = fatsG,
    vegetarian = vegetarian,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountMl: Long,
    val dateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun WaterLogEntity.toDomain(): WaterLog = WaterLog(
    id = id,
    userId = userId,
    amountMl = amountMl,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun WaterLog.toEntity(generatedId: String): WaterLogEntity = WaterLogEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    amountMl = amountMl,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "missions")
data class MissionProgressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val missionKey: String,
    val title: String,
    val description: String,
    val timeframe: String,
    val category: String,
    val difficulty: String,
    val targetValue: Long,
    val currentValue: Long,
    val rewardPoints: Long,
    val deadlineKey: String,
    val status: String,
    val completedDateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun MissionProgressEntity.toDomain(): MissionProgress = MissionProgress(
    id = id,
    userId = userId,
    missionKey = missionKey,
    title = title,
    description = description,
    timeframe = timeframe,
    category = category,
    difficulty = difficulty,
    targetValue = targetValue,
    currentValue = currentValue,
    rewardPoints = rewardPoints,
    deadlineKey = deadlineKey,
    status = status,
    completedDateKey = completedDateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun MissionProgress.toEntity(generatedId: String): MissionProgressEntity = MissionProgressEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    missionKey = missionKey,
    title = title,
    description = description,
    timeframe = timeframe,
    category = category,
    difficulty = difficulty,
    targetValue = targetValue,
    currentValue = currentValue,
    rewardPoints = rewardPoints,
    deadlineKey = deadlineKey,
    status = status,
    completedDateKey = completedDateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "medals")
data class MedalRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val medalKey: String,
    val title: String,
    val tier: String,
    val description: String,
    val unlockedDateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun MedalRecordEntity.toDomain(): MedalRecord = MedalRecord(
    id = id,
    userId = userId,
    medalKey = medalKey,
    title = title,
    tier = tier,
    description = description,
    unlockedDateKey = unlockedDateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun MedalRecord.toEntity(generatedId: String): MedalRecordEntity = MedalRecordEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    medalKey = medalKey,
    title = title,
    tier = tier,
    description = description,
    unlockedDateKey = unlockedDateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "certificates")
data class CertificateRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val certificateCode: String,
    val certificateName: String,
    val recipientName: String,
    val achievementDescription: String,
    val medalEarned: String,
    val completionDateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun CertificateRecordEntity.toDomain(): CertificateRecord = CertificateRecord(
    id = id,
    userId = userId,
    certificateCode = certificateCode,
    certificateName = certificateName,
    recipientName = recipientName,
    achievementDescription = achievementDescription,
    medalEarned = medalEarned,
    completionDateKey = completionDateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun CertificateRecord.toEntity(generatedId: String): CertificateRecordEntity = CertificateRecordEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    certificateCode = certificateCode,
    certificateName = certificateName,
    recipientName = recipientName,
    achievementDescription = achievementDescription,
    medalEarned = medalEarned,
    completionDateKey = completionDateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

@Entity(tableName = "discipline_logs")
data class DisciplineLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val exerciseType: String,
    val level: Long,
    val repsCompleted: Long,
    val durationSeconds: Long,
    val status: String,
    val pointsAwarded: Long,
    val dateKey: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun DisciplineLogEntity.toDomain(): DisciplineLog = DisciplineLog(
    id = id,
    userId = userId,
    exerciseType = exerciseType,
    level = level,
    repsCompleted = repsCompleted,
    durationSeconds = durationSeconds,
    status = status,
    pointsAwarded = pointsAwarded,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun DisciplineLog.toEntity(generatedId: String): DisciplineLogEntity = DisciplineLogEntity(
    id = id.ifBlank { generatedId },
    userId = userId,
    exerciseType = exerciseType,
    level = level,
    repsCompleted = repsCompleted,
    durationSeconds = durationSeconds,
    status = status,
    pointsAwarded = pointsAwarded,
    dateKey = dateKey,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)
