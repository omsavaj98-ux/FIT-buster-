package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.LOCAL_USER_ID
import com.example.ui.components.ErrorStateScreen
import com.example.ui.components.LoadingStateScreen
import com.example.ui.components.UnlockCelebrationDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DietNutritionScreen
import com.example.ui.screens.DisciplineScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MedalsAndCertificatesScreen
import com.example.ui.screens.MissionsAndStreaksScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RestTimerScreen
import com.example.ui.screens.SprintTrainingScreen
import com.example.ui.screens.WaterTrackerScreen
import com.example.ui.screens.WorkoutHubScreen
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.FitBusterTheme
import com.example.ui.theme.ObsidianBlack
import com.example.util.FitBusterDateUtil

object FitBusterRoutes {
    const val HOME = "home"
    const val TRAIN = "train"
    const val FUEL = "fuel"
    const val GOALS = "goals"
    const val STATS = "stats"
    const val SPRINT = "sprint"
    const val REST_TIMER = "rest_timer"
    const val DISCIPLINE = "discipline"
    const val WATER = "water"
    const val REPORTS = "reports"
    const val MEDALS = "medals"
    const val CERTIFICATES = "certificates"
    const val PROFILE = "profile"
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private val bottomNavTabs = listOf(
    NavTabItem(FitBusterRoutes.HOME, "Home", Icons.Filled.Home, "nav_home"),
    NavTabItem(FitBusterRoutes.TRAIN, "Train", Icons.Filled.FitnessCenter, "nav_train"),
    NavTabItem(FitBusterRoutes.FUEL, "Fuel", Icons.Filled.Restaurant, "nav_fuel"),
    NavTabItem(FitBusterRoutes.GOALS, "Goals", Icons.Filled.Flag, "nav_goals"),
    NavTabItem(FitBusterRoutes.STATS, "Stats", Icons.Filled.Analytics, "nav_stats")
)

@Composable
fun FitBusterAppRoot() {
    val context = LocalContext.current
    val services = remember { FitBusterServices(context) }

    val viewModel: FitBusterViewModel = viewModel(
        key = LOCAL_USER_ID,
        factory = viewModelFactory {
            initializer {
                FitBusterViewModel(
                    services = services,
                    currentUserId = LOCAL_USER_ID,
                    initialDisplayName = "FIT BUSTER Athlete",
                    initialEmail = ""
                )
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val sprintTimer by viewModel.sprintTimer.collectAsStateWithLifecycle()
    val restTimer by viewModel.restTimer.collectAsStateWithLifecycle()
    val disciplineState by viewModel.disciplineState.collectAsStateWithLifecycle()
    val signatureBitmap by viewModel.signatureBitmap.collectAsStateWithLifecycle()
    val profilePhotoBitmap by viewModel.profilePhotoBitmap.collectAsStateWithLifecycle()

    val accentPreset = (uiState as? UiState.Success)?.data?.profile?.themeAccent ?: "NEON_LIME"

    FitBusterTheme(accentPreset = accentPreset) {
        when (val state = uiState) {
            is UiState.Loading -> LoadingStateScreen()
            is UiState.Error -> ErrorStateScreen(message = state.message)
            is UiState.Success -> {
                FitBusterMainShell(
                    data = state.data,
                    activeWorkout = activeWorkout,
                    sprintTimer = sprintTimer,
                    restTimer = restTimer,
                    disciplineState = disciplineState,
                    signatureBitmap = signatureBitmap,
                    profilePhotoBitmap = profilePhotoBitmap,
                    statusBanner = statusBanner,
                    celebration = celebration,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FitBusterMainShell(
    data: FitBusterDashboardData,
    activeWorkout: ActiveWorkoutState,
    sprintTimer: SprintTimerState,
    restTimer: RestTimerState,
    disciplineState: DisciplineSessionState,
    signatureBitmap: Bitmap?,
    profilePhotoBitmap: Bitmap?,
    statusBanner: String?,
    celebration: UnlockCelebration?,
    viewModel: FitBusterViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: FitBusterRoutes.HOME
    val snackbarHostState = remember { SnackbarHostState() }

    val topLevelRoutes = remember { bottomNavTabs.map { it.route }.toSet() }
    val isTopLevel = currentRoute in topLevelRoutes

    LaunchedEffect(statusBanner) {
        val message = statusBanner
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearBanner()
        }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun screenTitle(route: String): String = when (route) {
        FitBusterRoutes.HOME -> "FIT BUSTER"
        FitBusterRoutes.TRAIN -> "Workout System"
        FitBusterRoutes.FUEL -> "Diet & Nutrition"
        FitBusterRoutes.GOALS -> "Missions & Streaks"
        FitBusterRoutes.STATS -> "Stats & Analytics"
        FitBusterRoutes.SPRINT -> "Running & Sprint"
        FitBusterRoutes.REST_TIMER -> "Rest Timer"
        FitBusterRoutes.DISCIPLINE -> "Punishment & Discipline"
        FitBusterRoutes.WATER -> "Water Tracker"
        FitBusterRoutes.REPORTS -> "Daily & Monthly Report"
        FitBusterRoutes.MEDALS -> "Fitness Medals"
        FitBusterRoutes.CERTIFICATES -> "Certificates"
        FitBusterRoutes.PROFILE -> "Profile & Settings"
        else -> "FIT BUSTER"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    containerColor = CarbonSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Spacer(modifier = Modifier.weight(0.1f))
                    bottomNavTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = ObsidianBlack,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = ObsidianBlack,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = ObsidianBlack,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        navigationIcon = {
                            if (!isTopLevel) {
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier.testTag("top_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_fit_buster_crest),
                                    contentDescription = "FIT BUSTER Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .border(1.2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(9.dp))
                                )
                            }
                        },
                        title = {
                            Text(
                                text = screenTitle(currentRoute),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (currentRoute == FitBusterRoutes.HOME) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        },
                        actions = {
                            if (activeWorkout.isActive && currentRoute != FitBusterRoutes.TRAIN) {
                                Surface(
                                    onClick = { navigateTopLevel(FitBusterRoutes.TRAIN) },
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .testTag("active_workout_pill")
                                ) {
                                    Text(
                                        text = FitBusterDateUtil.formatDurationHms(activeWorkout.elapsedSeconds),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ObsidianBlack,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { navController.navigate(FitBusterRoutes.MEDALS) { launchSingleTop = true } },
                                modifier = Modifier.testTag("top_medals_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Medals & Certificates",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { navController.navigate(FitBusterRoutes.PROFILE) { launchSingleTop = true } },
                                modifier = Modifier.testTag("top_profile_btn")
                            ) {
                                if (profilePhotoBitmap != null) {
                                    Image(
                                        bitmap = profilePhotoBitmap.asImageBitmap(),
                                        contentDescription = "Profile & Settings",
                                        contentScale = ContentScale.Crop,
                                        filterQuality = FilterQuality.High,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Profile & Settings",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = CarbonSurface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            bottomNavTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(tab.route) },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ObsidianBlack,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = FitBusterRoutes.HOME,
                        modifier = Modifier
                            .widthIn(max = 840.dp)
                            .fillMaxSize()
                    ) {
                        composable(FitBusterRoutes.HOME) {
                            HomeDashboardScreen(
                                data = data,
                                onQuickStartWorkout = { navigateTopLevel(FitBusterRoutes.TRAIN) },
                                onOpenSprint = { navController.navigate(FitBusterRoutes.SPRINT) },
                                onOpenRestTimer = { navController.navigate(FitBusterRoutes.REST_TIMER) },
                                onOpenDailyReport = { navController.navigate(FitBusterRoutes.REPORTS) },
                                onOpenDiscipline = { navController.navigate(FitBusterRoutes.DISCIPLINE) },
                                onOpenWater = { navController.navigate(FitBusterRoutes.WATER) },
                                onOpenMissions = { navigateTopLevel(FitBusterRoutes.GOALS) },
                                onAddQuickWater = { ml -> viewModel.logWater(ml) }
                            )
                        }

                        composable(FitBusterRoutes.TRAIN) {
                            WorkoutHubScreen(
                                data = data,
                                activeWorkout = activeWorkout,
                                onStartWorkout = { type, title, hasLocation ->
                                    viewModel.startWorkout(type, title, hasLocation)
                                },
                                onPermissionGrantedDuringWorkout = {
                                    viewModel.onLocationPermissionGrantedDuringWorkout()
                                },
                                onPauseWorkout = { viewModel.pauseWorkout() },
                                onResumeWorkout = { viewModel.resumeWorkout() },
                                onFinishWorkout = { notes -> viewModel.finishWorkout(notes) },
                                onDismissReport = { viewModel.dismissWorkoutReport() },
                                onOpenSprint = { navController.navigate(FitBusterRoutes.SPRINT) },
                                onOpenRestTimer = { navController.navigate(FitBusterRoutes.REST_TIMER) },
                                onOpenDiscipline = { navController.navigate(FitBusterRoutes.DISCIPLINE) }
                            )
                        }

                        composable(FitBusterRoutes.FUEL) {
                            DietNutritionScreen(
                                data = data,
                                onLogMeal = { name, type, cal, prot, carb, fat, veg ->
                                    viewModel.logMeal(name, type, cal, prot, carb, fat, veg)
                                },
                                onDeleteMeal = { mealId -> viewModel.deleteMeal(mealId) },
                                onUpdateGoals = { cal, water, prot, vegDefault ->
                                    viewModel.updateNutritionAndWaterGoals(cal, water, prot, vegDefault)
                                },
                                onOpenWaterTracker = { navController.navigate(FitBusterRoutes.WATER) },
                                onOpenDailyReport = { navController.navigate(FitBusterRoutes.REPORTS) }
                            )
                        }

                        composable(FitBusterRoutes.GOALS) {
                            MissionsAndStreaksScreen(
                                data = data,
                                onSkipMission = { mission -> viewModel.skipMission(mission) },
                                onToggleRecoveryDay = { viewModel.togglePlannedRecoveryDayToday() },
                                onOpenMedals = { navController.navigate(FitBusterRoutes.MEDALS) },
                                onOpenCertificates = { navController.navigate(FitBusterRoutes.CERTIFICATES) }
                            )
                        }

                        composable(FitBusterRoutes.STATS) {
                            AnalyticsScreen(
                                data = data,
                                onOpenProfileSettings = { navController.navigate(FitBusterRoutes.PROFILE) }
                            )
                        }

                        composable(FitBusterRoutes.SPRINT) {
                            SprintTrainingScreen(
                                data = data,
                                sprintTimer = sprintTimer,
                                onSelectDistance = { dist -> viewModel.selectSprintDistance(dist) },
                                onStartStopwatch = { viewModel.startSprintStopwatch() },
                                onStopAndSaveStopwatch = { viewModel.stopAndSaveSprintStopwatch() },
                                onSaveManualSprint = { dist, secs -> viewModel.saveManualSprint(dist, secs) }
                            )
                        }

                        composable(FitBusterRoutes.REST_TIMER) {
                            RestTimerScreen(
                                data = data,
                                restTimer = restTimer,
                                onSetDuration = { secs -> viewModel.setRestDuration(secs) },
                                onStartOrResume = { viewModel.startOrResumeRestTimer() },
                                onPause = { viewModel.pauseRestTimer() },
                                onReset = { viewModel.resetRestTimer() },
                                onExtend = { secs -> viewModel.extendRestTimer(secs) },
                                onSkip = { viewModel.skipRestTimer() }
                            )
                        }

                        composable(FitBusterRoutes.DISCIPLINE) {
                            DisciplineScreen(
                                data = data,
                                session = disciplineState,
                                onSelectExercise = { ex -> viewModel.selectDisciplineExercise(ex) },
                                onIncrementRep = { delta -> viewModel.incrementDisciplineRep(delta) },
                                onToggleTimer = { viewModel.toggleDisciplineTimer() },
                                onCompleteChallenge = { viewModel.completeDisciplineChallenge() },
                                onSkipChallenge = { viewModel.skipDisciplineChallenge() },
                                onToggleRecoveryDay = { viewModel.togglePlannedRecoveryDayToday() }
                            )
                        }

                        composable(FitBusterRoutes.WATER) {
                            WaterTrackerScreen(
                                data = data,
                                onAddWater = { ml -> viewModel.logWater(ml) },
                                onEditGoal = { newWaterGoal ->
                                    viewModel.updateNutritionAndWaterGoals(
                                        calorieGoal = data.profile.dailyCalorieGoal,
                                        waterGoalMl = newWaterGoal,
                                        proteinGoalG = data.profile.dailyProteinGoalG,
                                        vegDefault = data.profile.vegetarianDefault
                                    )
                                },
                                onToggleWaterReminder = { waterEnabled ->
                                    viewModel.updateNotificationSettings(
                                        masterEnabled = data.profile.notificationsEnabled,
                                        workoutEnabled = data.profile.workoutReminderEnabled,
                                        hour = data.profile.workoutReminderHour.toInt(),
                                        minute = data.profile.workoutReminderMinute.toInt(),
                                        waterEnabled = waterEnabled,
                                        missionEnabled = data.profile.missionReminderEnabled,
                                        streakEnabled = data.profile.streakReminderEnabled
                                    )
                                },
                                onTestReminderSound = { viewModel.testReminderSound() }
                            )
                        }

                        composable(FitBusterRoutes.REPORTS) {
                            ReportsScreen(data = data)
                        }

                        composable(FitBusterRoutes.MEDALS) {
                            MedalsAndCertificatesScreen(
                                data = data,
                                signatureBitmap = signatureBitmap,
                                initialTab = 0,
                                onExportPdf = { cert, shareAfter ->
                                    viewModel.downloadOrShareCertificatePdf(context, cert, shareAfter)
                                },
                                onOpenSignatureSettings = { navController.navigate(FitBusterRoutes.PROFILE) }
                            )
                        }

                        composable(FitBusterRoutes.CERTIFICATES) {
                            MedalsAndCertificatesScreen(
                                data = data,
                                signatureBitmap = signatureBitmap,
                                initialTab = 1,
                                onExportPdf = { cert, shareAfter ->
                                    viewModel.downloadOrShareCertificatePdf(context, cert, shareAfter)
                                },
                                onOpenSignatureSettings = { navController.navigate(FitBusterRoutes.PROFILE) }
                            )
                        }

                        composable(FitBusterRoutes.PROFILE) {
                            ProfileSettingsScreen(
                                data = data,
                                signatureBitmap = signatureBitmap,
                                profilePhotoBitmap = profilePhotoBitmap,
                                onSaveProfile = { name, age, goal, prefWorkout, accent, isPrivate ->
                                    viewModel.updateProfileDetails(name, age, goal, prefWorkout, accent, isPrivate)
                                },
                                onUpdateNotifications = { master, workout, hr, min, water, mission, streak ->
                                    viewModel.updateNotificationSettings(master, workout, hr, min, water, mission, streak)
                                },
                                onUploadSignature = { uri -> viewModel.uploadSignatureImage(uri) },
                                onRemoveSignature = { viewModel.removeSignature() },
                                onUploadProfilePhoto = { uri -> viewModel.uploadProfilePhoto(uri) },
                                onExportBackup = { uri -> viewModel.exportBackupJson(context, uri) },
                                onImportBackup = { uri -> viewModel.restoreFromBackupUri(context, uri) },
                                onResetProgress = { viewModel.resetAllProgress() },
                                onTestReminderSound = { viewModel.testReminderSound() }
                            )
                        }
                    }
                }
            }
        }

        celebration?.let { activeCelebration ->
            UnlockCelebrationDialog(
                celebration = activeCelebration,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}
