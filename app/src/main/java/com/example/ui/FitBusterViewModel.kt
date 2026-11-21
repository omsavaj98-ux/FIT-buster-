package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BackupBundle
import com.example.data.BackupJsonSerializer
import com.example.data.FitBusterRepository
import com.example.data.PrivateSignatureStore
import com.example.data.RewardEngine
import com.example.model.CertificateRecord
import com.example.model.DisciplineExercise
import com.example.model.DisciplineLog
import com.example.model.DisciplineStatus
import com.example.model.FitBusterCatalog
import com.example.model.MealEntry
import com.example.model.MealType
import com.example.model.MedalRecord
import com.example.model.MissionProgress
import com.example.model.MissionStatus
import com.example.model.RestLog
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import com.example.model.WorkoutType
import com.example.notifications.FitBusterNotificationHelper
import com.example.notifications.SoundAlertHelper
import com.example.util.CertificatePdfGenerator
import com.example.util.FitBusterDateUtil
import com.example.util.FitnessCalculator
import com.example.util.LocationTracker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class FitBusterDashboardData(
    val profile: UserProfile = UserProfile(),
    val workouts: List<WorkoutSession> = emptyList(),
    val sprints: List<SprintRecord> = emptyList(),
    val restLogs: List<RestLog> = emptyList(),
    val meals: List<MealEntry> = emptyList(),
    val waterLogs: List<WaterLog> = emptyList(),
    val missions: List<MissionProgress> = emptyList(),
    val medals: List<MedalRecord> = emptyList(),
    val certificates: List<CertificateRecord> = emptyList(),
    val disciplineLogs: List<DisciplineLog> = emptyList()
)

data class ActiveWorkoutState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val workoutType: WorkoutType = WorkoutType.OUTDOOR_RUN,
    val customTitle: String = "",
    val customMet: Double = 5.5,
    val elapsedSeconds: Long = 0L,
    val gpsPermissionGranted: Boolean = false,
    val distanceMeters: Double = 0.0,
    val paceSecPerKm: Double = 0.0,
    val gpsPointCount: Int = 0,
    val hasGpsFix: Boolean = false,
    val estimatedCalories: Long = 0L,
    val lastCompletedSession: WorkoutSession? = null
)

data class SprintTimerState(
    val selectedDistanceMeters: Int = 100,
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val lastCompletedSprint: SprintRecord? = null,
    val newPersonalBest: Boolean = false
)

data class RestTimerState(
    val plannedSeconds: Int = 60,
    val remainingSeconds: Int = 60,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val completedBannerVisible: Boolean = false
)

data class DisciplineSessionState(
    val selectedExercise: DisciplineExercise = DisciplineExercise.UTHAK_BAITHAK,
    val currentReps: Int = 0,
    val targetReps: Int = 12,
    val timerRemainingSec: Int = 45,
    val timerRunning: Boolean = false
)

data class UnlockCelebration(
    val title: String,
    val subtitle: String,
    val badgeType: String
)

class FitBusterServices(context: Context) {
    val appContext: Context = context.applicationContext
    val repository = FitBusterRepository(appContext)
    val signatureStore = PrivateSignatureStore(appContext)
    val locationTracker = LocationTracker(appContext)
    val rewardEngine = RewardEngine(repository)
}

class FitBusterViewModel(
    private val services: FitBusterServices,
    private val currentUserId: String,
    private val initialDisplayName: String,
    private val initialEmail: String
) : ViewModel() {

    private val repo = services.repository
    private val rewardEngine = services.rewardEngine
    private val signatureStore = services.signatureStore
    private val locationTracker = services.locationTracker

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _celebration = MutableStateFlow<UnlockCelebration?>(null)
    val celebration: StateFlow<UnlockCelebration?> = _celebration.asStateFlow()

    private val _signatureBitmap = MutableStateFlow<Bitmap?>(signatureStore.loadSignatureBitmap(currentUserId))
    val signatureBitmap: StateFlow<Bitmap?> = _signatureBitmap.asStateFlow()

    private val _profilePhotoBitmap = MutableStateFlow<Bitmap?>(signatureStore.loadProfilePhotoBitmap(currentUserId))
    val profilePhotoBitmap: StateFlow<Bitmap?> = _profilePhotoBitmap.asStateFlow()

    private val _activeWorkout = MutableStateFlow(ActiveWorkoutState())
    val activeWorkout: StateFlow<ActiveWorkoutState> = _activeWorkout.asStateFlow()

    private val _sprintTimer = MutableStateFlow(SprintTimerState())
    val sprintTimer: StateFlow<SprintTimerState> = _sprintTimer.asStateFlow()

    private val _restTimer = MutableStateFlow(RestTimerState())
    val restTimer: StateFlow<RestTimerState> = _restTimer.asStateFlow()

    private val _disciplineState = MutableStateFlow(DisciplineSessionState())
    val disciplineState: StateFlow<DisciplineSessionState> = _disciplineState.asStateFlow()

    private var workoutTimerJob: Job? = null
    private var workoutStartElapsedRealtime: Long = 0L
    private var workoutAccumulatedSeconds: Long = 0L

    private var sprintTimerJob: Job? = null
    private var sprintStartElapsedRealtime: Long = 0L

    private var restTimerJob: Job? = null
    private var disciplineTimerJob: Job? = null

    private var hasInitializedMissions = false

    private val coreStreams = combine(
        repo.observeUserProfile(currentUserId),
        repo.observeWorkouts(currentUserId),
        repo.observeSprints(currentUserId),
        repo.observeRestLogs(currentUserId),
        repo.observeMeals(currentUserId)
    ) { profile, workouts, sprints, restLogs, meals ->
        CoreBundle(profile, workouts, sprints, restLogs, meals)
    }

    private val metaStreams = combine(
        repo.observeWaterLogs(currentUserId),
        repo.observeMissions(currentUserId),
        repo.observeMedals(currentUserId),
        repo.observeCertificates(currentUserId),
        repo.observeDisciplineLogs(currentUserId)
    ) { waterLogs, missions, medals, certs, discipline ->
        MetaBundle(waterLogs, missions, medals, certs, discipline)
    }

    val uiState: StateFlow<UiState<FitBusterDashboardData>> = combine(coreStreams, metaStreams) { core, meta ->
        val safeProfile = core.profile ?: UserProfile(
            userId = currentUserId,
            displayName = initialDisplayName.ifBlank { "FIT BUSTER Athlete" },
            email = initialEmail
        )
        FitBusterDashboardData(
            profile = safeProfile,
            workouts = core.workouts.sortedByDescending { it.createdAtMillis },
            sprints = core.sprints.sortedByDescending { it.createdAtMillis },
            restLogs = core.restLogs.sortedByDescending { it.createdAtMillis },
            meals = core.meals.sortedByDescending { it.createdAtMillis },
            waterLogs = meta.waterLogs.sortedByDescending { it.createdAtMillis },
            missions = meta.missions,
            medals = meta.medals.sortedByDescending { it.unlockedDateKey },
            certificates = meta.certificates.sortedByDescending { it.completionDateKey },
            disciplineLogs = meta.disciplineLogs.sortedByDescending { it.createdAtMillis }
        )
    }
        .let { flow ->
            flow.combine(MutableStateFlow(Unit)) { data, _ ->
                UiState.Success(data) as UiState<FitBusterDashboardData>
            }
        }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w("FitBusterVM", "Error observing user fitness data", error)
            emit(UiState.Error(error.message ?: "Failed to load fitness data"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    init {
        viewModelScope.launch {
            repo.ensureUserProfile(initialDisplayName, initialEmail)
        }
        viewModelScope.launch {
            locationTracker.state.collect { gps ->
                val current = _activeWorkout.value
                if (current.isActive && !current.isPaused && current.workoutType.supportsGps && current.gpsPermissionGranted) {
                    val pace = FitnessCalculator.calculatePaceSecPerKm(current.elapsedSeconds, gps.distanceMeters)
                    _activeWorkout.value = current.copy(
                        distanceMeters = gps.distanceMeters,
                        paceSecPerKm = pace,
                        gpsPointCount = gps.pointCount,
                        hasGpsFix = gps.hasGpsFix
                    )
                }
            }
        }
        viewModelScope.launch {
            uiState.collect { state ->
                if (state is UiState.Success && !hasInitializedMissions) {
                    hasInitializedMissions = true
                    evaluateRewards(state.data)
                    scheduleUserReminders(state.data.profile)
                }
            }
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    // --- Section 2: Live Workout System ---
    fun startWorkout(type: WorkoutType, customTitle: String = "", hasLocationPermission: Boolean) {
        workoutTimerJob?.cancel()
        workoutAccumulatedSeconds = 0L
        workoutStartElapsedRealtime = SystemClock.elapsedRealtime()
        locationTracker.reset()
        if (type.supportsGps && hasLocationPermission) {
            locationTracker.startTracking()
        }
        _activeWorkout.value = ActiveWorkoutState(
            isActive = true,
            isPaused = false,
            workoutType = type,
            customTitle = customTitle.trim(),
            elapsedSeconds = 0L,
            gpsPermissionGranted = hasLocationPermission,
            estimatedCalories = 0L,
            lastCompletedSession = null
        )
        launchWorkoutTicker()
    }

    fun onLocationPermissionGrantedDuringWorkout() {
        val current = _activeWorkout.value
        if (current.isActive && current.workoutType.supportsGps && !current.gpsPermissionGranted) {
            _activeWorkout.value = current.copy(gpsPermissionGranted = true)
            if (!current.isPaused) {
                locationTracker.startTracking()
            }
        }
    }

    fun pauseWorkout() {
        val current = _activeWorkout.value
        if (!current.isActive || current.isPaused) return
        workoutTimerJob?.cancel()
        val delta = (SystemClock.elapsedRealtime() - workoutStartElapsedRealtime) / 1000L
        workoutAccumulatedSeconds += delta.coerceAtLeast(0L)
        locationTracker.stopTracking()
        _activeWorkout.value = current.copy(
            isPaused = true,
            elapsedSeconds = workoutAccumulatedSeconds
        )
    }

    fun resumeWorkout() {
        val current = _activeWorkout.value
        if (!current.isActive || !current.isPaused) return
        workoutStartElapsedRealtime = SystemClock.elapsedRealtime()
        if (current.workoutType.supportsGps && current.gpsPermissionGranted) {
            locationTracker.startTracking()
        }
        _activeWorkout.value = current.copy(isPaused = false)
        launchWorkoutTicker()
    }

    fun finishWorkout(notes: String = "") {
        val current = _activeWorkout.value
        if (!current.isActive) return
        workoutTimerJob?.cancel()
        val totalSeconds = if (current.isPaused) {
            workoutAccumulatedSeconds
        } else {
            workoutAccumulatedSeconds + ((SystemClock.elapsedRealtime() - workoutStartElapsedRealtime) / 1000L).coerceAtLeast(0L)
        }
        locationTracker.stopTracking()

        val safeDuration = totalSeconds.coerceAtLeast(1L)
        val calories = FitnessCalculator.estimateCalories(current.workoutType, safeDuration)
        val pace = FitnessCalculator.calculatePaceSecPerKm(safeDuration, current.distanceMeters)
        val today = FitBusterDateUtil.todayKey()

        val session = WorkoutSession(
            userId = currentUserId,
            workoutType = current.workoutType.name,
            customTitle = current.customTitle,
            durationSeconds = safeDuration,
            distanceMeters = current.distanceMeters,
            averagePaceSecPerKm = pace,
            caloriesBurned = calories,
            gpsTracked = current.workoutType.supportsGps && current.gpsPermissionGranted && current.gpsPointCount > 0,
            gpsPointCount = current.gpsPointCount.toLong(),
            dateKey = today,
            notes = notes.trim()
        )

        _activeWorkout.value = ActiveWorkoutState(lastCompletedSession = session)
        locationTracker.reset()

        viewModelScope.launch {
            val res = repo.addWorkout(session)
            if (res.isSuccess) {
                recordActivityDateAndStreak(today)
                triggerRewardReevaluation(extraWorkout = session)
            } else {
                _statusBanner.value = "Failed to save workout: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun dismissWorkoutReport() {
        _activeWorkout.value = _activeWorkout.value.copy(lastCompletedSession = null)
    }

    private fun launchWorkoutTicker() {
        workoutTimerJob = viewModelScope.launch {
            while (isActive) {
                val cur = _activeWorkout.value
                if (!cur.isActive || cur.isPaused) break
                val total = workoutAccumulatedSeconds +
                    ((SystemClock.elapsedRealtime() - workoutStartElapsedRealtime) / 1000L).coerceAtLeast(0L)
                val kcal = FitnessCalculator.estimateCalories(cur.workoutType, total)
                val pace = FitnessCalculator.calculatePaceSecPerKm(total, cur.distanceMeters)
                _activeWorkout.value = cur.copy(
                    elapsedSeconds = total,
                    estimatedCalories = kcal,
                    paceSecPerKm = pace
                )
                delay(500L)
            }
        }
    }

    // --- Section 3: Sprint Training ---
    fun selectSprintDistance(meters: Int) {
        if (_sprintTimer.value.isRunning) return
        _sprintTimer.value = _sprintTimer.value.copy(
            selectedDistanceMeters = meters,
            elapsedMillis = 0L,
            lastCompletedSprint = null,
            newPersonalBest = false
        )
    }

    fun startSprintStopwatch() {
        sprintTimerJob?.cancel()
        sprintStartElapsedRealtime = SystemClock.elapsedRealtime()
        _sprintTimer.value = _sprintTimer.value.copy(
            isRunning = true,
            elapsedMillis = 0L,
            lastCompletedSprint = null,
            newPersonalBest = false
        )
        sprintTimerJob = viewModelScope.launch {
            while (isActive) {
                val ms = (SystemClock.elapsedRealtime() - sprintStartElapsedRealtime).coerceAtLeast(0L)
                _sprintTimer.value = _sprintTimer.value.copy(elapsedMillis = ms)
                delay(40L)
            }
        }
    }

    fun stopAndSaveSprintStopwatch() {
        val current = _sprintTimer.value
        if (!current.isRunning) return
        sprintTimerJob?.cancel()
        val finalMs = (SystemClock.elapsedRealtime() - sprintStartElapsedRealtime).coerceAtLeast(100L)
        saveSprintRecord(current.selectedDistanceMeters, finalMs, manualEntry = false)
    }

    fun saveManualSprint(distanceMeters: Int, secondsInput: Double) {
        if (secondsInput <= 0.5 || secondsInput > 600.0) {
            _statusBanner.value = "Enter a valid sprint time between 0.5 and 600 seconds."
            return
        }
        saveSprintRecord(distanceMeters, (secondsInput * 1000.0).toLong(), manualEntry = true)
    }

    private fun saveSprintRecord(distanceMeters: Int, timeMillis: Long, manualEntry: Boolean) {
        val data = (uiState.value as? UiState.Success)?.data
        val existingBest = data?.sprints
            ?.filter { it.distanceMeters == distanceMeters.toLong() }
            ?.minByOrNull { it.timeMillis }
            ?.timeMillis
        val isPb = existingBest == null || timeMillis < existingBest
        val today = FitBusterDateUtil.todayKey()

        val record = SprintRecord(
            userId = currentUserId,
            distanceMeters = distanceMeters.toLong(),
            timeMillis = timeMillis,
            manualEntry = manualEntry,
            personalBest = isPb,
            dateKey = today
        )

        _sprintTimer.value = _sprintTimer.value.copy(
            isRunning = false,
            elapsedMillis = timeMillis,
            lastCompletedSprint = record,
            newPersonalBest = isPb
        )

        viewModelScope.launch {
            val res = repo.addSprint(record)
            if (res.isSuccess) {
                // Also record a short sprint workout entry for calories/distance metrics
                val sprintWorkout = WorkoutSession(
                    userId = currentUserId,
                    workoutType = WorkoutType.SPRINT_TRAINING.name,
                    customTitle = "${distanceMeters}m Sprint (${FitBusterDateUtil.formatSprintMillis(timeMillis)})",
                    durationSeconds = (timeMillis / 1000L).coerceAtLeast(5L),
                    distanceMeters = distanceMeters.toDouble(),
                    caloriesBurned = FitnessCalculator.estimateCalories(
                        WorkoutType.SPRINT_TRAINING,
                        (timeMillis / 1000L).coerceAtLeast(15L)
                    ),
                    dateKey = today
                )
                repo.addWorkout(sprintWorkout)
                recordActivityDateAndStreak(today)
                if (isPb && data?.profile?.notificationsEnabled == true) {
                    SoundAlertHelper.playReminderSound(services.appContext, isAlarmOrTimer = false)
                    FitBusterNotificationHelper.showNotification(
                        services.appContext,
                        FitBusterNotificationHelper.CHANNEL_TIMERS,
                        3001,
                        "New ${distanceMeters}m Personal Best!",
                        "You clocked ${FitBusterDateUtil.formatSprintMillis(timeMillis)} in FIT BUSTER!"
                    )
                }
                triggerRewardReevaluation(extraSprint = record)
            } else {
                _statusBanner.value = "Failed to save sprint: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    // --- Section 4: Rest Timer ---
    fun setRestDuration(seconds: Int) {
        restTimerJob?.cancel()
        val safe = seconds.coerceIn(5, 3600)
        _restTimer.value = RestTimerState(
            plannedSeconds = safe,
            remainingSeconds = safe,
            isRunning = false,
            isPaused = false,
            completedBannerVisible = false
        )
    }

    fun startOrResumeRestTimer() {
        val current = _restTimer.value
        if (current.isRunning) return
        if (current.remainingSeconds <= 0) {
            _restTimer.value = current.copy(remainingSeconds = current.plannedSeconds, completedBannerVisible = false)
        }
        _restTimer.value = _restTimer.value.copy(isRunning = true, isPaused = false, completedBannerVisible = false)
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val state = _restTimer.value
                if (!state.isRunning) break
                val next = state.remainingSeconds - 1
                if (next <= 0) {
                    _restTimer.value = state.copy(
                        remainingSeconds = 0,
                        isRunning = false,
                        isPaused = false,
                        completedBannerVisible = true
                    )
                    onRestFinished(state.plannedSeconds, state.plannedSeconds, "COMPLETED")
                    break
                } else {
                    _restTimer.value = state.copy(remainingSeconds = next)
                }
            }
        }
    }

    fun pauseRestTimer() {
        restTimerJob?.cancel()
        _restTimer.value = _restTimer.value.copy(isRunning = false, isPaused = true)
    }

    fun resetRestTimer() {
        restTimerJob?.cancel()
        val planned = _restTimer.value.plannedSeconds
        _restTimer.value = RestTimerState(plannedSeconds = planned, remainingSeconds = planned)
    }

    fun extendRestTimer(extraSeconds: Int) {
        val cur = _restTimer.value
        _restTimer.value = cur.copy(
            plannedSeconds = (cur.plannedSeconds + extraSeconds).coerceAtMost(3600),
            remainingSeconds = (cur.remainingSeconds + extraSeconds).coerceAtMost(3600),
            completedBannerVisible = false
        )
    }

    fun skipRestTimer() {
        val cur = _restTimer.value
        restTimerJob?.cancel()
        val elapsed = (cur.plannedSeconds - cur.remainingSeconds).coerceAtLeast(0)
        _restTimer.value = RestTimerState(plannedSeconds = cur.plannedSeconds, remainingSeconds = cur.plannedSeconds)
        viewModelScope.launch {
            repo.addRestLog(
                RestLog(
                    userId = currentUserId,
                    plannedSeconds = cur.plannedSeconds.toLong(),
                    actualSeconds = elapsed.toLong(),
                    status = "SKIPPED",
                    dateKey = FitBusterDateUtil.todayKey()
                )
            )
        }
    }

    private fun onRestFinished(planned: Int, actual: Int, status: String) {
        val profile = (uiState.value as? UiState.Success)?.data?.profile
        if (profile?.notificationsEnabled != false) {
            SoundAlertHelper.playReminderSound(services.appContext, isAlarmOrTimer = true)
            FitBusterNotificationHelper.showNotification(
                services.appContext,
                FitBusterNotificationHelper.CHANNEL_TIMERS,
                3002,
                "Rest Complete — Next Set!",
                "Your ${planned}s recovery countdown finished. Get back to work!"
            )
        }
        viewModelScope.launch {
            repo.addRestLog(
                RestLog(
                    userId = currentUserId,
                    plannedSeconds = planned.toLong(),
                    actualSeconds = actual.toLong(),
                    status = status,
                    dateKey = FitBusterDateUtil.todayKey()
                )
            )
        }
    }

    // --- Section 5 & 6: Diet & Water ---
    fun logMeal(
        foodName: String,
        mealType: MealType,
        calories: Long,
        proteinG: Long,
        carbsG: Long,
        fatsG: Long,
        vegetarian: Boolean
    ) {
        if (foodName.isBlank()) {
            _statusBanner.value = "Please enter a food name."
            return
        }
        if (calories <= 0L || calories > 10000L) {
            _statusBanner.value = "Calories must be between 1 and 10,000."
            return
        }
        val today = FitBusterDateUtil.todayKey()
        val entry = MealEntry(
            userId = currentUserId,
            foodName = foodName.trim(),
            mealType = mealType.name,
            calories = calories,
            proteinG = proteinG.coerceIn(0L, 500L),
            carbsG = carbsG.coerceIn(0L, 1000L),
            fatsG = fatsG.coerceIn(0L, 500L),
            vegetarian = vegetarian,
            dateKey = today
        )
        viewModelScope.launch {
            val res = repo.addMeal(entry)
            if (res.isSuccess) {
                _statusBanner.value = "Logged ${entry.foodName} (${entry.calories} kcal)"
            } else {
                _statusBanner.value = "Failed to log meal."
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repo.deleteMeal(mealId)
        }
    }

    fun logWater(amountMl: Long) {
        if (amountMl <= 0L || amountMl > 10000L) {
            _statusBanner.value = "Enter a valid water amount between 1 and 10,000 ml."
            return
        }
        val today = FitBusterDateUtil.todayKey()
        viewModelScope.launch {
            val res = repo.addWaterLog(amountMl, today)
            if (res.isSuccess) {
                _statusBanner.value = "Added +${amountMl} ml hydration"
                triggerRewardReevaluation(extraWater = WaterLog(userId = currentUserId, amountMl = amountMl, dateKey = today))
            } else {
                _statusBanner.value = "Failed to log water."
            }
        }
    }

    fun updateNutritionAndWaterGoals(calorieGoal: Long, waterGoalMl: Long, proteinGoalG: Long, vegDefault: Boolean) {
        if (calorieGoal !in 800L..8000L || waterGoalMl !in 500L..10000L || proteinGoalG !in 10L..500L) {
            _statusBanner.value = "Please enter valid daily goal ranges."
            return
        }
        viewModelScope.launch {
            val res = repo.updateProfileFields(
                mapOf(
                    "dailyCalorieGoal" to calorieGoal,
                    "dailyWaterGoalMl" to waterGoalMl,
                    "dailyProteinGoalG" to proteinGoalG,
                    "vegetarianDefault" to vegDefault
                )
            )
            _statusBanner.value = if (res.isSuccess) "Goals updated!" else "Failed to update goals."
        }
    }

    // --- Section 8, 9, 12, 13: Streaks, Missions, Discipline ---
    fun togglePlannedRecoveryDayToday() {
        val data = (uiState.value as? UiState.Success)?.data ?: return
        val today = FitBusterDateUtil.todayKey()
        val updatedRecovery = data.profile.recoveryDates.toMutableList()
        val added: Boolean
        if (updatedRecovery.contains(today)) {
            updatedRecovery.remove(today)
            added = false
        } else {
            updatedRecovery.add(today)
            added = true
        }
        val (curStreak, longStreak) = FitnessCalculator.computeStreaks(
            data.profile.activeDates,
            updatedRecovery,
            today
        )
        viewModelScope.launch {
            repo.updateProfileFields(
                mapOf(
                    "recoveryDates" to updatedRecovery.distinct().takeLast(366),
                    "currentStreak" to curStreak,
                    "longestStreak" to maxOf(longStreak, data.profile.longestStreak)
                )
            )
            if (added) {
                repo.addDisciplineLog(
                    DisciplineLog(
                        userId = currentUserId,
                        exerciseType = DisciplineExercise.LIGHT_WALKING.name,
                        level = data.profile.punishmentLevel,
                        status = DisciplineStatus.RECOVERY_DAY.name,
                        dateKey = today
                    )
                )
            }
            _statusBanner.value = if (added) "Planned Recovery Day enabled — streak protected." else "Recovery Day removed."
        }
    }

    fun skipMission(mission: MissionProgress) {
        if (mission.status != MissionStatus.ACTIVE.name) return
        viewModelScope.launch {
            val res = repo.upsertMission(
                mission.id,
                mission.copy(status = MissionStatus.SKIPPED.name)
            )
            if (res.isSuccess) {
                _statusBanner.value = "Mission skipped safely."
            }
        }
    }

    fun selectDisciplineExercise(exercise: DisciplineExercise) {
        disciplineTimerJob?.cancel()
        val level = ((uiState.value as? UiState.Success)?.data?.profile?.punishmentLevel ?: 1L).toInt()
        val scale = (level - 1) * 2
        val targetReps = if (exercise.defaultReps > 0) (exercise.defaultReps + scale).coerceAtMost(35) else 0
        val targetSecs = exercise.defaultSeconds
        _disciplineState.value = DisciplineSessionState(
            selectedExercise = exercise,
            currentReps = 0,
            targetReps = targetReps,
            timerRemainingSec = targetSecs,
            timerRunning = false
        )
    }

    fun incrementDisciplineRep(delta: Int = 1) {
        val cur = _disciplineState.value
        _disciplineState.value = cur.copy(currentReps = (cur.currentReps + delta).coerceIn(0, 500))
    }

    fun toggleDisciplineTimer() {
        val cur = _disciplineState.value
        if (cur.timerRunning) {
            disciplineTimerJob?.cancel()
            _disciplineState.value = cur.copy(timerRunning = false)
        } else {
            _disciplineState.value = cur.copy(timerRunning = true)
            disciplineTimerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000L)
                    val s = _disciplineState.value
                    if (!s.timerRunning) break
                    if (s.timerRemainingSec <= 1) {
                        _disciplineState.value = s.copy(timerRemainingSec = 0, timerRunning = false)
                        SoundAlertHelper.playReminderSound(services.appContext, isAlarmOrTimer = true)
                        _statusBanner.value = "Discipline timer finished! Tap Complete when ready."
                        break
                    } else {
                        _disciplineState.value = s.copy(timerRemainingSec = s.timerRemainingSec - 1)
                    }
                }
            }
        }
    }

    fun completeDisciplineChallenge() {
        val data = (uiState.value as? UiState.Success)?.data ?: return
        val cur = _disciplineState.value
        disciplineTimerJob?.cancel()
        val today = FitBusterDateUtil.todayKey()
        val points = 30L + (data.profile.punishmentLevel * 5L)
        val newDiscPoints = data.profile.disciplinePoints + points
        val newPunishmentLevel = (1 + (newDiscPoints / 120L).toInt()).coerceIn(1, 10).toLong()

        val log = DisciplineLog(
            userId = currentUserId,
            exerciseType = cur.selectedExercise.name,
            level = data.profile.punishmentLevel,
            repsCompleted = cur.currentReps.toLong(),
            durationSeconds = (cur.selectedExercise.defaultSeconds - cur.timerRemainingSec).coerceAtLeast(10).toLong(),
            status = DisciplineStatus.COMPLETED.name,
            pointsAwarded = points,
            dateKey = today
        )

        viewModelScope.launch {
            val res = repo.addDisciplineLog(log)
            if (res.isSuccess) {
                repo.updateProfileFields(
                    mapOf(
                        "disciplinePoints" to newDiscPoints,
                        "punishmentLevel" to newPunishmentLevel,
                        "challengePoints" to (data.profile.challengePoints + points)
                    )
                )
                recordActivityDateAndStreak(today)
                _disciplineState.value = cur.copy(currentReps = 0, timerRunning = false, timerRemainingSec = cur.selectedExercise.defaultSeconds)
                _statusBanner.value = "Discipline completed! +$points pts (Level $newPunishmentLevel)"
                triggerRewardReevaluation(extraDiscipline = log)
            }
        }
    }

    fun skipDisciplineChallenge() {
        val data = (uiState.value as? UiState.Success)?.data ?: return
        val cur = _disciplineState.value
        disciplineTimerJob?.cancel()
        val log = DisciplineLog(
            userId = currentUserId,
            exerciseType = cur.selectedExercise.name,
            level = data.profile.punishmentLevel,
            status = DisciplineStatus.SKIPPED.name,
            pointsAwarded = 0L,
            dateKey = FitBusterDateUtil.todayKey()
        )
        viewModelScope.launch {
            repo.addDisciplineLog(log)
            _disciplineState.value = cur.copy(currentReps = 0, timerRunning = false, timerRemainingSec = cur.selectedExercise.defaultSeconds)
            _statusBanner.value = "Challenge skipped safely. Listen to your body!"
        }
    }

    // --- Section 11, 15, 16, 17, 18: Signature, PDF, Profile, Notifications, Backup/Restore, Reset ---
    fun uploadSignatureImage(uri: Uri) {
        viewModelScope.launch {
            val saveRes = signatureStore.saveSignatureFromUri(currentUserId, uri)
            if (saveRes.isFailure) {
                _statusBanner.value = saveRes.exceptionOrNull()?.localizedMessage ?: "Failed to read signature."
                return@launch
            }
            saveRes.getOrThrow()
            _signatureBitmap.value = signatureStore.loadSignatureBitmap(currentUserId)
            repo.updateProfileFields(
                mapOf(
                    "hasSignature" to true,
                    "signatureUpdatedAtMillis" to System.currentTimeMillis()
                )
            )
            _statusBanner.value = "Authorized signature saved securely (untouched original)."
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val saveRes = signatureStore.saveProfilePhotoFromUri(currentUserId, uri)
            if (saveRes.isFailure) {
                _statusBanner.value = "Failed to load profile photo."
                return@launch
            }
            saveRes.getOrThrow()
            _profilePhotoBitmap.value = signatureStore.loadProfilePhotoBitmap(currentUserId)
            _statusBanner.value = "Profile photo updated!"
        }
    }

    fun removeSignature() {
        signatureStore.deleteSignature(currentUserId)
        _signatureBitmap.value = null
        viewModelScope.launch {
            repo.updateProfileFields(mapOf("hasSignature" to false))
            _statusBanner.value = "Signature removed from device."
        }
    }

    fun downloadOrShareCertificatePdf(context: Context, cert: CertificateRecord, shareAfter: Boolean) {
        viewModelScope.launch {
            val sig = _signatureBitmap.value ?: signatureStore.loadSignatureBitmap(currentUserId)
            val res = CertificatePdfGenerator.generateCertificatePdf(context, cert, sig)
            if (res.isSuccess) {
                val exported = res.getOrThrow()
                if (shareAfter) {
                    CertificatePdfGenerator.shareCertificatePdf(context, exported.shareUri, cert.certificateName)
                } else {
                    _statusBanner.value = "PDF saved: ${exported.displayLocation}"
                }
            } else {
                _statusBanner.value = "PDF export failed: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateProfileDetails(
        displayName: String,
        age: Long,
        fitnessGoal: String,
        preferredWorkout: String,
        themeAccent: String,
        profilePrivate: Boolean
    ) {
        if (displayName.isBlank()) {
            _statusBanner.value = "Account name cannot be empty."
            return
        }
        viewModelScope.launch {
            val res = repo.updateProfileFields(
                mapOf(
                    "displayName" to displayName.trim().take(60),
                    "age" to age.coerceIn(10L, 120L),
                    "fitnessGoal" to fitnessGoal.trim().take(120).ifBlank { "General Fitness" },
                    "preferredWorkout" to preferredWorkout,
                    "themeAccent" to themeAccent,
                    "profilePrivate" to profilePrivate
                )
            )
            _statusBanner.value = if (res.isSuccess) "Profile updated!" else "Failed to save profile."
        }
    }

    fun updateNotificationSettings(
        masterEnabled: Boolean,
        workoutEnabled: Boolean,
        hour: Int,
        minute: Int,
        waterEnabled: Boolean,
        missionEnabled: Boolean,
        streakEnabled: Boolean
    ) {
        viewModelScope.launch {
            val res = repo.updateProfileFields(
                mapOf(
                    "notificationsEnabled" to masterEnabled,
                    "workoutReminderEnabled" to workoutEnabled,
                    "workoutReminderHour" to hour.toLong().coerceIn(0L, 23L),
                    "workoutReminderMinute" to minute.toLong().coerceIn(0L, 59L),
                    "waterReminderEnabled" to waterEnabled,
                    "missionReminderEnabled" to missionEnabled,
                    "streakReminderEnabled" to streakEnabled
                )
            )
            if (res.isSuccess) {
                FitBusterNotificationHelper.scheduleReminders(
                    services.appContext,
                    masterEnabled,
                    workoutEnabled,
                    hour,
                    minute,
                    waterEnabled,
                    missionEnabled,
                    streakEnabled
                )
                _statusBanner.value = "Notification & alarm schedule updated."
            }
        }
    }

    private fun scheduleUserReminders(profile: UserProfile) {
        FitBusterNotificationHelper.scheduleReminders(
            services.appContext,
            profile.notificationsEnabled,
            profile.workoutReminderEnabled,
            profile.workoutReminderHour.toInt(),
            profile.workoutReminderMinute.toInt(),
            profile.waterReminderEnabled,
            profile.missionReminderEnabled,
            profile.streakReminderEnabled
        )
    }

    fun testReminderSound() {
        SoundAlertHelper.playReminderSound(services.appContext, isAlarmOrTimer = true)
        FitBusterNotificationHelper.showNotification(
            services.appContext,
            FitBusterNotificationHelper.CHANNEL_REMINDERS,
            3099,
            "FIT BUSTER Reminder Sound",
            "Your workout, hydration, and timer sound alerts are active."
        )
        _statusBanner.value = "Reminder sound played!"
    }

    fun exportBackupJson(context: Context, targetUri: Uri) {
        val data = (uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            try {
                val json = BackupJsonSerializer.exportToJson(
                    BackupBundle(
                        profile = data.profile,
                        workouts = data.workouts,
                        sprints = data.sprints,
                        restLogs = data.restLogs,
                        meals = data.meals,
                        waterLogs = data.waterLogs,
                        missions = data.missions,
                        medals = data.medals,
                        certificates = data.certificates,
                        disciplineLogs = data.disciplineLogs
                    )
                )
                context.contentResolver.openOutputStream(targetUri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                _statusBanner.value = "Backup exported successfully!"
            } catch (e: Exception) {
                _statusBanner.value = "Backup failed: ${e.localizedMessage}"
            }
        }
    }

    fun restoreFromBackupUri(context: Context, sourceUri: Uri) {
        viewModelScope.launch {
            try {
                val text = context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.use { it.readText() }
                    ?: throw IllegalArgumentException("Empty backup file")
                val bundle = BackupJsonSerializer.parseFromJson(text, currentUserId)
                val res = repo.restoreFromBackupBundle(bundle)
                _statusBanner.value = if (res.isSuccess) "Data restored on this device!" else "Restore error."
            } catch (e: Exception) {
                _statusBanner.value = "Invalid backup file: ${e.localizedMessage}"
            }
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            val res = repo.resetAllProgress()
            _statusBanner.value = if (res.isSuccess) "All workout & achievement progress reset." else "Failed to reset progress."
        }
    }

    private suspend fun recordActivityDateAndStreak(dateKey: String) {
        val data = (uiState.value as? UiState.Success)?.data ?: return
        val updatedActive = (data.profile.activeDates + dateKey).distinct().takeLast(366)
        val (curStreak, longStreak) = FitnessCalculator.computeStreaks(
            updatedActive,
            data.profile.recoveryDates,
            dateKey
        )
        repo.updateProfileFields(
            mapOf(
                "activeDates" to updatedActive,
                "lastActiveDate" to dateKey,
                "currentStreak" to curStreak,
                "longestStreak" to maxOf(longStreak, data.profile.longestStreak)
            )
        )
    }

    private suspend fun triggerRewardReevaluation(
        extraWorkout: WorkoutSession? = null,
        extraSprint: SprintRecord? = null,
        extraWater: WaterLog? = null,
        extraDiscipline: DisciplineLog? = null
    ) {
        val data = (uiState.value as? UiState.Success)?.data ?: return
        evaluateRewards(
            data.copy(
                workouts = if (extraWorkout != null) listOf(extraWorkout) + data.workouts else data.workouts,
                sprints = if (extraSprint != null) listOf(extraSprint) + data.sprints else data.sprints,
                waterLogs = if (extraWater != null) listOf(extraWater) + data.waterLogs else data.waterLogs,
                disciplineLogs = if (extraDiscipline != null) listOf(extraDiscipline) + data.disciplineLogs else data.disciplineLogs
            )
        )
    }

    private suspend fun evaluateRewards(data: FitBusterDashboardData) {
        val summary = rewardEngine.syncMissionsAndRewards(
            userId = currentUserId,
            profile = data.profile,
            workouts = data.workouts,
            sprints = data.sprints,
            waterLogs = data.waterLogs,
            existingMissions = data.missions,
            existingMedals = data.medals,
            existingCertificates = data.certificates,
            disciplineLogs = data.disciplineLogs
        )

        val notifyEnabled = data.profile.notificationsEnabled
        val firstCert = summary.newlyUnlockedCertificates.firstOrNull()
        val firstMedal = summary.newlyUnlockedMedals.firstOrNull()
        val firstMission = summary.newlyCompletedMissions.firstOrNull()

        when {
            firstCert != null -> {
                _celebration.value = UnlockCelebration(
                    title = "Certificate Unlocked!",
                    subtitle = "${firstCert.certificateName} • ${firstCert.medalEarned}",
                    badgeType = "CERTIFICATE"
                )
                if (notifyEnabled) {
                    SoundAlertHelper.playReminderSound(services.appContext, isAlarmOrTimer = false)
                    FitBusterNotificationHelper.showNotification(
                        services.appContext,
                        FitBusterNotificationHelper.CHANNEL_ACHIEVEMENTS,
                        4001,
                        "Certificate Unlocked!",
                        "${firstCert.certificateName} is ready in your gallery."
                    )
                }
            }
            firstMedal != null -> {
                _celebration.value = UnlockCelebration(
                    title = "Medal Unlocked!",
                    subtitle = "${firstMedal.title} (${firstMedal.tier})",
                    badgeType = "MEDAL"
                )
                if (notifyEnabled) {
                    SoundAlertHelper.playReminderSound(services.appContext, isAlarmOrTimer = false)
                    FitBusterNotificationHelper.showNotification(
                        services.appContext,
                        FitBusterNotificationHelper.CHANNEL_ACHIEVEMENTS,
                        4002,
                        "Medal Unlocked!",
                        "You earned ${firstMedal.title}!"
                    )
                }
            }
            firstMission != null -> {
                _celebration.value = UnlockCelebration(
                    title = "Mission Complete!",
                    subtitle = "${firstMission.title} • +${summary.totalPointsEarned} pts",
                    badgeType = "MISSION"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        workoutTimerJob?.cancel()
        sprintTimerJob?.cancel()
        restTimerJob?.cancel()
        disciplineTimerJob?.cancel()
        locationTracker.stopTracking()
    }

    private data class CoreBundle(
        val profile: UserProfile?,
        val workouts: List<WorkoutSession>,
        val sprints: List<SprintRecord>,
        val restLogs: List<RestLog>,
        val meals: List<MealEntry>
    )

    private data class MetaBundle(
        val waterLogs: List<WaterLog>,
        val missions: List<MissionProgress>,
        val medals: List<MedalRecord>,
        val certificates: List<CertificateRecord>,
        val disciplineLogs: List<DisciplineLog>
    )
}
