package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.MissionStatus
import com.example.model.WorkoutType
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.BarChartPoint
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.LineProgressChart
import com.example.ui.components.MetricTile
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.GoldMedal
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil
import java.util.Locale

@Composable
fun AnalyticsScreen(
    data: FitBusterDashboardData,
    onOpenProfileSettings: () -> Unit
) {
    val totalWorkouts = data.workouts.size
    val totalMinutes = remember(data.workouts) { data.workouts.sumOf { it.durationSeconds } / 60L }
    val totalDistanceKm = remember(data.workouts) { data.workouts.sumOf { it.distanceMeters } / 1000.0 }
    val totalCalories = remember(data.workouts) { data.workouts.sumOf { it.caloriesBurned } }

    val last7Keys = remember { FitBusterDateUtil.lastNDaysKeys(7) }
    val weeklyMinutesPoints = remember(data.workouts, last7Keys) {
        last7Keys.map { k ->
            BarChartPoint(FitBusterDateUtil.shortDayLabel(k), data.workouts.filter { it.dateKey == k }.sumOf { it.durationSeconds } / 60f)
        }
    }

    val last4WeeksPoints = remember(data.workouts) {
        (3 downTo 0).map { weekOffset ->
            val endDay = weekOffset * 7
            val keys = (endDay + 6 downTo endDay).map { FitBusterDateUtil.dateKeyDaysAgo(it) }.toSet()
            val mins = data.workouts.filter { it.dateKey in keys }.sumOf { it.durationSeconds } / 60f
            BarChartPoint("W-${weekOffset}", mins)
        }
    }

    val runningDistancePoints = remember(data.workouts) {
        data.workouts.filter { it.workoutType == WorkoutType.OUTDOOR_RUN.name || it.workoutType == WorkoutType.OUTDOOR_WALK.name }
            .take(8).reversed()
            .mapIndexed { idx, w -> BarChartPoint("#${idx + 1}", (w.distanceMeters / 1000.0).toFloat()) }
    }

    val sprintProgressPoints = remember(data.sprints) {
        data.sprints.take(8).reversed().mapIndexed { idx, s -> BarChartPoint("${s.distanceMeters}m", s.timeMillis / 1000f) }
    }

    val pb100 = remember(data.sprints) { data.sprints.filter { it.distanceMeters == 100L }.minByOrNull { it.timeMillis } }
    val pb60 = remember(data.sprints) { data.sprints.filter { it.distanceMeters == 60L }.minByOrNull { it.timeMillis } }
    val pb30 = remember(data.sprints) { data.sprints.filter { it.distanceMeters == 30L }.minByOrNull { it.timeMillis } }

    val strengthWorkouts = remember(data.workouts) { data.workouts.filter { it.workoutType == WorkoutType.FUNCTIONAL_STRENGTH.name } }
    val completedMissions = remember(data.missions) { data.missions.count { it.status == MissionStatus.COMPLETED.name } }

    val thisWeekKeys = remember { FitBusterDateUtil.lastNDaysKeys(7).toSet() }
    val prevWeekKeys = remember { (13 downTo 7).map { FitBusterDateUtil.dateKeyDaysAgo(it) }.toSet() }
    val thisWeekMinutes = remember(data.workouts, thisWeekKeys) { data.workouts.filter { it.dateKey in thisWeekKeys }.sumOf { it.durationSeconds } / 60L }
    val prevWeekMinutes = remember(data.workouts, prevWeekKeys) { data.workouts.filter { it.dateKey in prevWeekKeys }.sumOf { it.durationSeconds } / 60L }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onOpenProfileSettings,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = ObsidianBlack),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_profile_settings_btn")
            ) {
                Icon(Icons.Filled.Settings, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Profile, Signature, Alarms & Backup", fontWeight = FontWeight.Bold)
            }
        }

        // Core Totals
        item {
            SectionHeader(title = "Stats & Analytics", subtitle = "Genuine metrics across all logged sessions")
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("TOTAL WORKOUTS", "$totalWorkouts", "sessions", Icons.Filled.FitnessCenter, modifier = Modifier.weight(1f))
                    MetricTile("TOTAL MINUTES", "$totalMinutes", "min", Icons.Filled.Timer, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("TOTAL DISTANCE", String.format(Locale.US, "%.2f", totalDistanceKm), "km", Icons.AutoMirrored.Filled.DirectionsRun, HydrationCyan, Modifier.weight(1f))
                    MetricTile("EST. CALORIES", "$totalCalories", "kcal", Icons.Filled.LocalFireDepartment, FlameAmber, Modifier.weight(1f))
                }
            }
        }

        // Workout Comparison & Strength Progress
        item {
            FitBusterCard {
                Text("Workout Comparison (7d vs Previous 7d)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("This Week: $thisWeekMinutes min  •  Previous Week: $prevWeekMinutes min", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                val diff = thisWeekMinutes - prevWeekMinutes
                Text(
                    text = if (diff >= 0) "+$diff min more active than last week" else "${diff} min vs last week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (diff >= 0) SuccessGreen else FlameAmber
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Strength Progress: ${strengthWorkouts.size} Functional Strength workouts • ${strengthWorkouts.sumOf { it.durationSeconds } / 60L} min", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Weekly Graph & Monthly Graph
        item {
            SectionHeader(title = "Weekly Workout Graph", subtitle = "Minutes per day (7 days)")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                WeeklyBarChart(points = weeklyMinutesPoints, unitSuffix = "m")
            }
        }

        item {
            SectionHeader(title = "Monthly Trend Graph", subtitle = "Minutes per week (4 weeks)")
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                WeeklyBarChart(points = last4WeeksPoints, barColor = HydrationCyan, unitSuffix = "m")
            }
        }

        // Running & Sprint Progress
        item {
            SectionHeader(title = "Running & Sprint Progress", subtitle = "Personal Bests: 30m ${pb30?.let { FitBusterDateUtil.formatSprintMillis(it.timeMillis) } ?: "--"} • 60m ${pb60?.let { FitBusterDateUtil.formatSprintMillis(it.timeMillis) } ?: "--"} • 100m ${pb100?.let { FitBusterDateUtil.formatSprintMillis(it.timeMillis) } ?: "--"}")
            Spacer(modifier = Modifier.height(8.dp))
            if (runningDistancePoints.isEmpty() && sprintProgressPoints.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.Speed,
                    title = "No running or sprint charts yet",
                    description = "Complete an Outdoor Run or Sprint session to plot real progress curves."
                )
            } else {
                if (runningDistancePoints.isNotEmpty()) {
                    FitBusterCard {
                        Text("Running Distance (km)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        LineProgressChart(points = runningDistancePoints)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                if (sprintProgressPoints.isNotEmpty()) {
                    FitBusterCard {
                        Text("Sprint Timing History (s)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        LineProgressChart(points = sprintProgressPoints, lineColor = GoldMedal)
                    }
                }
            }
        }

        // Mission, Medal & Streak Statistics
        item {
            FitBusterCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Flag, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Mission Completion Stats", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("$completedMissions / ${data.missions.size}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                NeonProgressBar(progress = if (data.missions.isEmpty()) 0f else completedMissions.toFloat() / data.missions.size.toFloat())
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = GoldMedal)
                        Text("Medal Collection Progress", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("${data.medals.size} / 11", style = MaterialTheme.typography.titleMedium, color = GoldMedal)
                }
                Spacer(modifier = Modifier.height(8.dp))
                NeonProgressBar(progress = data.medals.size / 11f, color = GoldMedal)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Streak History: Current ${data.profile.currentStreak} days • Longest ${data.profile.longestStreak} days • Active days logged: ${data.profile.activeDates.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
