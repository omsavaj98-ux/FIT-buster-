package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FitBusterDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun observeUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeWorkouts(userId: String): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutSessionEntity)

    @Query("SELECT * FROM sprints WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeSprints(userId: String): Flow<List<SprintRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprint(sprint: SprintRecordEntity)

    @Query("SELECT * FROM rest_logs WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeRestLogs(userId: String): Flow<List<RestLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestLog(log: RestLogEntity)

    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeMeals(userId: String): Flow<List<MealEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntryEntity)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: String)

    @Query("SELECT * FROM water_logs WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeWaterLogs(userId: String): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity)

    @Query("SELECT * FROM missions WHERE userId = :userId")
    fun observeMissions(userId: String): Flow<List<MissionProgressEntity>>

    @Query("SELECT * FROM missions WHERE id = :missionId LIMIT 1")
    suspend fun getMissionOnce(missionId: String): MissionProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMission(mission: MissionProgressEntity)

    @Query("SELECT * FROM medals WHERE userId = :userId ORDER BY unlockedDateKey DESC")
    fun observeMedals(userId: String): Flow<List<MedalRecordEntity>>

    @Query("SELECT COUNT(*) FROM medals WHERE id = :medalId")
    suspend fun countMedalsById(medalId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedalIfAbsent(medal: MedalRecordEntity): Long

    @Query("SELECT * FROM certificates WHERE userId = :userId ORDER BY completionDateKey DESC")
    fun observeCertificates(userId: String): Flow<List<CertificateRecordEntity>>

    @Query("SELECT COUNT(*) FROM certificates WHERE id = :certId")
    suspend fun countCertificatesById(certId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCertificateIfAbsent(cert: CertificateRecordEntity): Long

    @Query("SELECT * FROM discipline_logs WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeDisciplineLogs(userId: String): Flow<List<DisciplineLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisciplineLog(log: DisciplineLogEntity)

    @Query("DELETE FROM workouts WHERE userId = :userId")
    suspend fun deleteWorkoutsForUser(userId: String)

    @Query("DELETE FROM sprints WHERE userId = :userId")
    suspend fun deleteSprintsForUser(userId: String)

    @Query("DELETE FROM rest_logs WHERE userId = :userId")
    suspend fun deleteRestLogsForUser(userId: String)

    @Query("DELETE FROM meals WHERE userId = :userId")
    suspend fun deleteMealsForUser(userId: String)

    @Query("DELETE FROM water_logs WHERE userId = :userId")
    suspend fun deleteWaterLogsForUser(userId: String)

    @Query("DELETE FROM missions WHERE userId = :userId")
    suspend fun deleteMissionsForUser(userId: String)

    @Query("DELETE FROM medals WHERE userId = :userId")
    suspend fun deleteMedalsForUser(userId: String)

    @Query("DELETE FROM certificates WHERE userId = :userId")
    suspend fun deleteCertificatesForUser(userId: String)

    @Query("DELETE FROM discipline_logs WHERE userId = :userId")
    suspend fun deleteDisciplineLogsForUser(userId: String)

    @Transaction
    suspend fun clearAllUserProgress(userId: String) {
        deleteWorkoutsForUser(userId)
        deleteSprintsForUser(userId)
        deleteRestLogsForUser(userId)
        deleteMealsForUser(userId)
        deleteWaterLogsForUser(userId)
        deleteMissionsForUser(userId)
        deleteMedalsForUser(userId)
        deleteCertificatesForUser(userId)
        deleteDisciplineLogsForUser(userId)
    }
}
