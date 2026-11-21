package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.WorkoutSession
import com.example.model.WorkoutType
import com.example.ui.ActiveWorkoutState
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil
import java.util.Locale

@Composable
fun WorkoutHubScreen(
    data: FitBusterDashboardData,
    activeWorkout: ActiveWorkoutState,
    onStartWorkout: (WorkoutType, String, Boolean) -> Unit,
    onPermissionGrantedDuringWorkout: () -> Unit,
    onPauseWorkout: () -> Unit,
    onResumeWorkout: () -> Unit,
    onFinishWorkout: (String) -> Unit,
    onDismissReport: () -> Unit,
    onOpenSprint: () -> Unit,
    onOpenRestTimer: () -> Unit,
    onOpenDiscipline: () -> Unit
) {
    val context = LocalContext.current
    var pendingType by remember { mutableStateOf<WorkoutType?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var customTitleInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val typeToStart = pendingType
        if (typeToStart != null) {
            pendingType = null
            onStartWorkout(typeToStart, customTitleInput, granted)
        } else if (granted) {
            onPermissionGrantedDuringWorkout()
        }
    }

    fun launchWorkout(type: WorkoutType, customTitle: String = "") {
        if (type.supportsGps && !hasLocationPermission()) {
            pendingType = type
            customTitleInput = customTitle
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            onStartWorkout(type, customTitle, hasLocationPermission())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Pills for Sprint, Rest Timer, Discipline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onOpenSprint,
                    label = { Text("Sprint Lab") },
                    leadingIcon = { Icon(Icons.Filled.FlashOn, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("train_sprint_chip")
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenRestTimer,
                    label = { Text("Rest Timer") },
                    leadingIcon = { Icon(Icons.Filled.Timer, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = HydrationCyan
                    ),
                    modifier = Modifier.testTag("train_rest_chip")
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenDiscipline,
                    label = { Text("Discipline") },
                    leadingIcon = { Icon(Icons.Filled.SelfImprovement, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = FlameAmber
                    ),
                    modifier = Modifier.testTag("train_discipline_chip")
                )
            }
        }

        // Live Active Workout Panel
        if (activeWorkout.isActive) {
            item {
                FitBusterCard(highlighted = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (activeWorkout.isPaused) "WORKOUT PAUSED" else "LIVE SESSION ACTIVE",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (activeWorkout.isPaused) FlameAmber else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activeWorkout.customTitle.ifBlank { activeWorkout.workoutType.displayName },
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (activeWorkout.workoutType.supportsGps) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (activeWorkout.hasGpsFix) SuccessGreen.copy(alpha = 0.16f) else FlameAmber.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (activeWorkout.gpsPermissionGranted) Icons.Filled.GpsFixed else Icons.Filled.GpsOff,
                                        contentDescription = "GPS status",
                                        tint = if (activeWorkout.hasGpsFix) SuccessGreen else FlameAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = when {
                                            !activeWorkout.gpsPermissionGranted -> "No GPS Perm"
                                            activeWorkout.hasGpsFix -> "GPS Locked"
                                            else -> "Acquiring GPS"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (activeWorkout.hasGpsFix) SuccessGreen else FlameAmber
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = FitBusterDateUtil.formatDurationHms(activeWorkout.elapsedSeconds),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val km = activeWorkout.distanceMeters / 1000.0
                            Text(
                                text = String.format(Locale.US, "%.2f km", km),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column {
                            Text("PACE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = FitBusterDateUtil.formatPace(activeWorkout.paceSecPerKm),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column {
                            Text("EST. CALORIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${activeWorkout.estimatedCalories} kcal",
                                style = MaterialTheme.typography.titleLarge,
                                color = FlameAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Session Notes (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (activeWorkout.isPaused) {
                            Button(
                                onClick = onResumeWorkout,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = ObsidianBlack
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("resume_workout_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onPauseWorkout,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("pause_workout_button")
                            ) {
                                Icon(Icons.Filled.Pause, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause")
                            }
                        }

                        Button(
                            onClick = {
                                val n = notesInput
                                notesInput = ""
                                onFinishWorkout(n)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertCrimson,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("finish_workout_button")
                        ) {
                            Icon(Icons.Filled.Stop, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Workout Mode Selection Cards
            item {
                SectionHeader(
                    title = "Start a Workout",
                    subtitle = "Choose a discipline — genuine GPS & live timer tracking"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WorkoutTypeCard(
                            title = "Outdoor Walk",
                            subtitle = "GPS + Pace",
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            tag = "start_walk_btn",
                            modifier = Modifier.weight(1f),
                            onClick = { launchWorkout(WorkoutType.OUTDOOR_WALK) }
                        )
                        WorkoutTypeCard(
                            title = "Outdoor Run",
                            subtitle = "GPS + Pace",
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            tag = "start_run_btn",
                            modifier = Modifier.weight(1f),
                            onClick = { launchWorkout(WorkoutType.OUTDOOR_RUN) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WorkoutTypeCard(
                            title = "Outdoor Rowing",
                            subtitle = "GPS + Endurance",
                            icon = Icons.Filled.Rowing,
                            tag = "start_rowing_btn",
                            modifier = Modifier.weight(1f),
                            onClick = { launchWorkout(WorkoutType.OUTDOOR_ROWING) }
                        )
                        WorkoutTypeCard(
                            title = "Functional Strength",
                            subtitle = "Power & Reps",
                            icon = Icons.Filled.FitnessCenter,
                            tag = "start_strength_btn",
                            modifier = Modifier.weight(1f),
                            onClick = { launchWorkout(WorkoutType.FUNCTIONAL_STRENGTH) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WorkoutTypeCard(
                            title = "Sprint Training",
                            subtitle = "High Intensity",
                            icon = Icons.Filled.FlashOn,
                            tag = "start_sprint_workout_btn",
                            modifier = Modifier.weight(1f),
                            onClick = { launchWorkout(WorkoutType.SPRINT_TRAINING) }
                        )
                        WorkoutTypeCard(
                            title = "Custom Workout",
                            subtitle = "Your Own Name",
                            icon = Icons.Filled.Add,
                            tag = "start_custom_btn",
                            modifier = Modifier.weight(1f),
                            onClick = { showCustomDialog = true }
                        )
                    }
                }
            }
        }

        // Workout History
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Workout History",
                subtitle = "${data.workouts.size} completed sessions stored permanently"
            )
        }

        if (data.workouts.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.FitnessCenter,
                    title = "No completed workouts yet",
                    description = "Select any workout mode above and tap Finish when done to generate your completion report."
                )
            }
        } else {
            items(data.workouts, key = { it.id.ifBlank { it.hashCode().toString() } }) { session ->
                WorkoutHistoryItem(session)
            }
        }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom Workout") },
            text = {
                OutlinedTextField(
                    value = customTitleInput,
                    onValueChange = { customTitleInput = it },
                    label = { Text("Workout Name (e.g. Kettlebell HIIT)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = customTitleInput.ifBlank { "Custom Workout" }
                        showCustomDialog = false
                        customTitleInput = ""
                        launchWorkout(WorkoutType.CUSTOM, title)
                    }
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Workout Completion Report Dialog
    activeWorkout.lastCompletedSession?.let { completed ->
        AlertDialog(
            onDismissRequest = onDismissReport,
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Workout Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Workout Completion Report",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = completed.customTitle.ifBlank { WorkoutType.fromRaw(completed.workoutType).displayName },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Duration: ${FitBusterDateUtil.formatDurationHms(completed.durationSeconds)}")
                    Text("Distance: ${String.format(Locale.US, "%.2f km", completed.distanceMeters / 1000.0)}")
                    Text("Pace: ${FitBusterDateUtil.formatPace(completed.averagePaceSecPerKm)}")
                    Text("Estimated Burn: ${completed.caloriesBurned} kcal")
                    Text("GPS Tracked: ${if (completed.gpsTracked) "Yes (${completed.gpsPointCount} fixes)" else "No (Timer-only)"}")
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissReport,
                    modifier = Modifier.testTag("close_workout_report_btn")
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun WorkoutTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FitBusterCard(
        modifier = modifier.testTag(tag),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(session: WorkoutSession) {
    val type = WorkoutType.fromRaw(session.workoutType)
    FitBusterCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.customTitle.ifBlank { type.displayName },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${FitBusterDateUtil.formatDisplayDate(session.dateKey)} • ${FitBusterDateUtil.formatDurationHms(session.durationSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.distanceMeters > 0) {
                    Text(
                        text = "${String.format(Locale.US, "%.2f km", session.distanceMeters / 1000.0)} • ${FitBusterDateUtil.formatPace(session.averagePaceSecPerKm)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HydrationCyan
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = FlameAmber.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "${session.caloriesBurned} kcal",
                    style = MaterialTheme.typography.labelLarge,
                    color = FlameAmber,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
