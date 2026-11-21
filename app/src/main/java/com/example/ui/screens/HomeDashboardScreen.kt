package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.MissionStatus
import com.example.model.MissionTimeframe
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.BarChartPoint
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.MetricTile
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.CarbonCardBorder
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil
import com.example.util.FitnessCalculator

@Composable
fun HomeDashboardScreen(
    data: FitBusterDashboardData,
    onQuickStartWorkout: () -> Unit,
    onOpenSprint: () -> Unit,
    onOpenRestTimer: () -> Unit,
    onOpenDailyReport: () -> Unit,
    onOpenDiscipline: () -> Unit,
    onOpenWater: () -> Unit,
    onOpenMissions: () -> Unit,
    onAddQuickWater: (Long) -> Unit
) {
    val today = remember { FitBusterDateUtil.todayKey() }
    val todayWorkouts = remember(data.workouts, today) { data.workouts.filter { it.dateKey == today } }
    val todayMinutes = remember(todayWorkouts) { todayWorkouts.sumOf { it.durationSeconds } / 60L }
    val todayCalories = remember(todayWorkouts) { todayWorkouts.sumOf { it.caloriesBurned } }

    val totalWorkouts = data.workouts.size
    val totalMinutes = remember(data.workouts) { data.workouts.sumOf { it.durationSeconds } / 60L }
    val totalCalories = remember(data.workouts) { data.workouts.sumOf { it.caloriesBurned } }

    val todayWaterMl = remember(data.waterLogs, today) {
        data.waterLogs.filter { it.dateKey == today }.sumOf { it.amountMl }
    }
    val waterGoal = data.profile.dailyWaterGoalMl.coerceAtLeast(500L)

    val dailyMissions = remember(data.missions) {
        data.missions.filter { it.timeframe == MissionTimeframe.DAILY.name }
    }
    val dailyMissionsDone = dailyMissions.count { it.status == MissionStatus.COMPLETED.name }

    val last7Days = remember { FitBusterDateUtil.lastNDaysKeys(7) }
    val weeklyChartPoints = remember(data.workouts, last7Days) {
        last7Days.map { dayKey ->
            val mins = data.workouts.filter { it.dateKey == dayKey }.sumOf { it.durationSeconds } / 60f
            BarChartPoint(FitBusterDateUtil.shortDayLabel(dayKey), mins)
        }
    }
    val weeklyMinutesTotal = remember(data.workouts, last7Days) {
        val set = last7Days.toSet()
        data.workouts.filter { it.dateKey in set }.sumOf { it.durationSeconds } / 60L
    }
    val weeklyWorkoutsCount = remember(data.workouts, last7Days) {
        val set = last7Days.toSet()
        data.workouts.count { it.dateKey in set }
    }

    val (lvlProg, lvlSpan) = remember(data.profile.challengePoints) {
        FitnessCalculator.pointsProgressInLevel(data.profile.challengePoints)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Quick Start Banner + Level + Streak
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.large
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_fitness),
                    contentDescription = "FIT BUSTER Track Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBlack.copy(alpha = 0.62f),
                                    ObsidianBlack.copy(alpha = 0.90f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WELCOME BACK, ${data.profile.displayName.uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "TODAY'S FITNESS BRIEF",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FlameAmber.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FlameAmber.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Current Streak",
                                    tint = FlameAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${data.profile.currentStreak}d Streak",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = FlameAmber
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level ${data.profile.challengeLevel} • ${data.profile.challengePoints} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Today: $todayMinutes min • $todayCalories kcal",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    NeonProgressBar(progress = lvlProg.toFloat() / lvlSpan.toFloat())

                    Button(
                        onClick = onQuickStartWorkout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = ObsidianBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("quick_start_workout_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "QUICK START WORKOUT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Quick Tool Shortcuts (Sprint, Rest Timer, Daily Report, Discipline)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPill(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    label = "Sprint",
                    tag = "home_sprint_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenSprint
                )
                QuickActionPill(
                    icon = Icons.Filled.Timer,
                    label = "Rest",
                    tag = "home_rest_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenRestTimer
                )
                QuickActionPill(
                    icon = Icons.Filled.Assessment,
                    label = "Report",
                    tag = "home_report_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDailyReport
                )
                QuickActionPill(
                    icon = Icons.Filled.SelfImprovement,
                    label = "Discipline",
                    tag = "home_discipline_btn",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDiscipline
                )
            }
        }

        // 3. Core Today & All-Time Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        label = "WORKOUT MINUTES",
                        value = "$totalMinutes",
                        unit = "min total",
                        icon = Icons.Filled.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "CALORIES BURNED",
                        value = "$totalCalories",
                        unit = "kcal est.",
                        icon = Icons.Filled.LocalFireDepartment,
                        accentColor = FlameAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricTile(
                        label = "COMPLETED",
                        value = "$totalWorkouts",
                        unit = "workouts",
                        icon = Icons.Filled.FitnessCenter,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "WEEKLY OVERVIEW",
                        value = "$weeklyWorkoutsCount",
                        unit = "${weeklyMinutesTotal}m / 7d",
                        icon = Icons.Filled.Bolt,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Water Intake Progress Card
        item {
            FitBusterCard(onClick = onOpenWater) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = "Water Intake",
                            tint = HydrationCyan
                        )
                        Column {
                            Text(
                                text = "Water Intake Progress",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$todayWaterMl / $waterGoal ml today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        onClick = { onAddQuickWater(250L) },
                        shape = RoundedCornerShape(10.dp),
                        color = HydrationCyan.copy(alpha = 0.18f),
                        modifier = Modifier.testTag("home_add_water_250_btn")
                    ) {
                        Text(
                            text = "+250 ml",
                            style = MaterialTheme.typography.labelLarge,
                            color = HydrationCyan,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeonProgressBar(
                    progress = todayWaterMl.toFloat() / waterGoal.toFloat(),
                    color = HydrationCyan
                )
            }
        }

        // 5. Daily Mission Progress Card
        item {
            FitBusterCard(onClick = onOpenMissions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Daily Missions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Daily Mission Progress",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$dailyMissionsDone of ${dailyMissions.size.coerceAtLeast(4)} missions completed today",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val missionProgress = if (dailyMissions.isEmpty()) 0f
                else dailyMissionsDone.toFloat() / dailyMissions.size.toFloat()
                NeonProgressBar(progress = missionProgress)
            }
        }

        // 6. Weekly Activity Graph
        item {
            SectionHeader(
                title = "Weekly Activity Graph",
                subtitle = "Active workout minutes over the last 7 days"
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (data.workouts.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.FitnessCenter,
                    title = "No workouts recorded yet",
                    description = "Tap Quick Start Workout to track your first real GPS or strength session.",
                    actionLabel = "Start First Workout",
                    onAction = onQuickStartWorkout
                )
            } else {
                FitBusterCard {
                    WeeklyBarChart(
                        points = weeklyChartPoints,
                        barColor = MaterialTheme.colorScheme.primary,
                        unitSuffix = "m"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    tag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonCardBorder),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
