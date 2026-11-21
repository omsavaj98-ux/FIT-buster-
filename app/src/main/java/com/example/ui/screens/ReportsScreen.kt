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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.MissionStatus
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.FitBusterCard
import com.example.ui.components.MetricTile
import com.example.ui.components.SectionHeader
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil
import java.util.Locale

@Composable
fun ReportsScreen(
    data: FitBusterDashboardData
) {
    val last14Days = remember { FitBusterDateUtil.lastNDaysKeys(14).reversed() }
    var selectedDate by remember { mutableStateOf(last14Days.first()) }

    val dayWorkouts = remember(data.workouts, selectedDate) { data.workouts.filter { it.dateKey == selectedDate } }
    val dayMinutes = remember(dayWorkouts) { dayWorkouts.sumOf { it.durationSeconds } / 60L }
    val dayBurned = remember(dayWorkouts) { dayWorkouts.sumOf { it.caloriesBurned } }
    val dayWater = remember(data.waterLogs, selectedDate) { data.waterLogs.filter { it.dateKey == selectedDate }.sumOf { it.amountMl } }
    val dayCaloriesIn = remember(data.meals, selectedDate) { data.meals.filter { it.dateKey == selectedDate }.sumOf { it.calories } }
    val dayMissionsDone = remember(data.missions, selectedDate) {
        data.missions.count { it.status == MissionStatus.COMPLETED.name && it.completedDateKey == selectedDate }
    }
    val dayActive = data.profile.activeDates.contains(selectedDate)
    val dayRecovery = data.profile.recoveryDates.contains(selectedDate)

    // Monthly Report totals
    val currentMonth = remember { FitBusterDateUtil.monthKey() }
    val monthWorkouts = remember(data.workouts, currentMonth) { data.workouts.filter { it.dateKey.startsWith(currentMonth) } }
    val monthMinutes = remember(monthWorkouts) { monthWorkouts.sumOf { it.durationSeconds } / 60L }
    val monthCalories = remember(monthWorkouts) { monthWorkouts.sumOf { it.caloriesBurned } }
    val monthDistanceKm = remember(monthWorkouts) { monthWorkouts.sumOf { it.distanceMeters } / 1000.0 }
    val monthWaterL = remember(data.waterLogs, currentMonth) {
        data.waterLogs.filter { it.dateKey.startsWith(currentMonth) }.sumOf { it.amountMl } / 1000.0
    }
    val monthMissions = remember(data.missions, currentMonth) {
        data.missions.count { it.status == MissionStatus.COMPLETED.name && it.completedDateKey.startsWith(currentMonth) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Date-Wise Daily Report",
                subtitle = "Select today or any previous day to inspect full metrics"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(last14Days) { key ->
                    FilterChip(
                        selected = selectedDate == key,
                        onClick = { selectedDate = key },
                        label = { Text(if (key == FitBusterDateUtil.todayKey()) "Today" else FitBusterDateUtil.formatDisplayDate(key)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = ObsidianBlack
                        )
                    )
                }
            }
        }

        // Daily Progress Summary
        item {
            FitBusterCard(highlighted = true) {
                Text(
                    text = FitBusterDateUtil.formatDisplayDate(selectedDate),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        dayActive -> "Streak Status: Active Day Recorded (${data.profile.currentStreak}d current streak)"
                        dayRecovery -> "Streak Status: Planned Recovery Day (Streak Protected)"
                        else -> "Streak Status: Rest / No Activity Logged"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dayActive || dayRecovery) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("WORKOUTS", "${dayWorkouts.size}", "done", Icons.Filled.FitnessCenter, modifier = Modifier.weight(1f))
                    MetricTile("DURATION", "$dayMinutes", "min", Icons.Filled.Timer, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("BURNED EST.", "$dayBurned", "kcal", Icons.Filled.LocalFireDepartment, FlameAmber, Modifier.weight(1f))
                    MetricTile("WATER INTAKE", "$dayWater", "ml", Icons.Filled.WaterDrop, HydrationCyan, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("MISSIONS DONE", "$dayMissionsDone", "completed", Icons.Filled.Flag, SuccessGreen, Modifier.weight(1f))
                    MetricTile("FOOD INTAKE", "$dayCaloriesIn", "kcal", Icons.Filled.CalendarMonth, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            }
        }

        // Monthly Report
        item {
            SectionHeader(
                title = "Monthly Report ($currentMonth)",
                subtitle = "Cumulative monthly training & nutrition totals"
            )
            Spacer(modifier = Modifier.height(8.dp))
            FitBusterCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportLine("Workouts Completed", "${monthWorkouts.size}")
                    ReportLine("Total Workout Duration", "$monthMinutes min")
                    ReportLine("Total Distance Covered", String.format(Locale.US, "%.2f km", monthDistanceKm))
                    ReportLine("Calories Burned Estimate", "$monthCalories kcal")
                    ReportLine("Water Intake Total", String.format(Locale.US, "%.1f L", monthWaterL))
                    ReportLine("Missions Completed", "$monthMissions")
                    ReportLine("Current / Longest Streak", "${data.profile.currentStreak}d / ${data.profile.longestStreak}d")
                }
            }
        }
    }
}

@Composable
private fun ReportLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
