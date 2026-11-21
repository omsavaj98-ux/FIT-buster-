package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.MissionCategory
import com.example.model.MissionProgress
import com.example.model.MissionStatus
import com.example.model.MissionTimeframe
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.MetricTile
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.GoldMedal
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil
import com.example.util.FitnessCalculator

@Composable
fun MissionsAndStreaksScreen(
    data: FitBusterDashboardData,
    onSkipMission: (MissionProgress) -> Unit,
    onToggleRecoveryDay: () -> Unit,
    onOpenMedals: () -> Unit,
    onOpenCertificates: () -> Unit
) {
    var selectedTimeframe by remember { mutableStateOf<MissionTimeframe?>(null) }
    var selectedCategory by remember { mutableStateOf<MissionCategory?>(null) }

    val today = remember { FitBusterDateUtil.todayKey() }
    val isRecoveryToday = data.profile.recoveryDates.contains(today)
    val last14Days = remember { FitBusterDateUtil.lastNDaysKeys(14) }
    val weekActiveCount = remember(data.profile.activeDates, data.profile.recoveryDates) {
        val last7 = FitBusterDateUtil.lastNDaysKeys(7).toSet()
        (data.profile.activeDates + data.profile.recoveryDates).distinct().count { it in last7 }
    }
    val monthActiveCount = remember(data.profile.activeDates, data.profile.recoveryDates) {
        val monthPrefix = FitBusterDateUtil.monthKey()
        (data.profile.activeDates + data.profile.recoveryDates).distinct().count { it.startsWith(monthPrefix) }
    }

    val (lvlProg, lvlSpan) = remember(data.profile.challengePoints) {
        FitnessCalculator.pointsProgressInLevel(data.profile.challengePoints)
    }

    val filteredMissions = remember(data.missions, selectedTimeframe, selectedCategory) {
        data.missions.filter { m ->
            (selectedTimeframe == null || m.timeframe == selectedTimeframe!!.name) &&
                (selectedCategory == null || m.category == selectedCategory!!.name)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick navigation to Medals & Certificates
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = false,
                    onClick = onOpenMedals,
                    label = { Text("Medals (${data.medals.size}/11)") },
                    leadingIcon = { Icon(Icons.Filled.EmojiEvents, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = GoldMedal
                    ),
                    modifier = Modifier.testTag("missions_open_medals_chip")
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenCertificates,
                    label = { Text("Certificates (${data.certificates.size}/10)") },
                    leadingIcon = { Icon(Icons.Filled.WorkspacePremium, null, Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("missions_open_certs_chip")
                )
            }
        }

        // Challenge & Level System Card (Level 1 to 10)
        item {
            FitBusterCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CHALLENGE LEVEL ${data.profile.challengeLevel} / 10",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${data.profile.challengePoints} Challenge Points",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = if (data.profile.challengeLevel >= 10L) "MAX" else "$lvlProg / $lvlSpan",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeonProgressBar(progress = lvlProg.toFloat() / lvlSpan.toFloat())
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Complete Daily, Weekly & Monthly missions to earn automatic rewards and level unlocks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Streak System Card
        item {
            SectionHeader(title = "Streak System", subtitle = "Daily, Weekly & Monthly consistency")
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("CURRENT STREAK", "${data.profile.currentStreak}", "days", Icons.Filled.LocalFireDepartment, FlameAmber, Modifier.weight(1f))
                    MetricTile("LONGEST STREAK", "${data.profile.longestStreak}", "days", Icons.Filled.EmojiEvents, GoldMedal, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile("WEEKLY STREAK", "$weekActiveCount", "/ 7 days", Icons.Filled.CheckCircle, SuccessGreen, Modifier.weight(1f))
                    MetricTile("MONTHLY ACTIVE", "$monthActiveCount", "days", Icons.Filled.Hotel, HydrationCyan, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            FitBusterCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Streak History (Last 14 Days)", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (isRecoveryToday) "Today: Planned Recovery Day" else "Protect streak on rest days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onToggleRecoveryDay) {
                        Text(if (isRecoveryToday) "Recovery On" else "Recovery Day")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    last14Days.forEach { key ->
                        val active = data.profile.activeDates.contains(key)
                        val recovery = data.profile.recoveryDates.contains(key)
                        val dotColor = when {
                            active -> MaterialTheme.colorScheme.primary
                            recovery -> HydrationCyan
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }

        // Fitness Mission System Filters
        item {
            SectionHeader(
                title = "Fitness Missions",
                subtitle = "${data.missions.count { it.status == MissionStatus.COMPLETED.name }} completed • auto-tracked"
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedTimeframe == null,
                        onClick = { selectedTimeframe = null },
                        label = { Text("All") }
                    )
                }
                items(MissionTimeframe.entries) { tf ->
                    FilterChip(
                        selected = selectedTimeframe == tf,
                        onClick = { selectedTimeframe = tf },
                        label = { Text(tf.label) }
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Types") }
                    )
                }
                items(MissionCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label) }
                    )
                }
            }
        }

        if (filteredMissions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Flag,
                    title = "No missions match filter",
                    description = "Clear filters or complete a workout/hydration log to refresh mission progress."
                )
            }
        } else {
            items(filteredMissions, key = { it.id }) { mission ->
                MissionItemCard(mission = mission, onSkip = { onSkipMission(mission) })
            }
        }
    }
}

@Composable
private fun MissionItemCard(
    mission: MissionProgress,
    onSkip: () -> Unit
) {
    val completed = mission.status == MissionStatus.COMPLETED.name
    val skipped = mission.status == MissionStatus.SKIPPED.name
    val progress = (mission.currentValue.toFloat() / mission.targetValue.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

    FitBusterCard(highlighted = completed) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${mission.timeframe} • ${mission.category} • ${mission.difficulty}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (completed) SuccessGreen else MaterialTheme.colorScheme.primary
                )
                Text(mission.title, style = MaterialTheme.typography.titleMedium)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (completed) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = if (completed) "DONE +${mission.rewardPoints}" else if (skipped) "SKIPPED" else "+${mission.rewardPoints} pts",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (completed) SuccessGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = mission.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        NeonProgressBar(
            progress = progress,
            color = if (completed) SuccessGreen else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress: ${mission.currentValue} / ${mission.targetValue} • Deadline: ${FitBusterDateUtil.formatDisplayDate(mission.deadlineKey)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (mission.status == MissionStatus.ACTIVE.name) {
                TextButton(onClick = onSkip) {
                    Text("Skip", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
