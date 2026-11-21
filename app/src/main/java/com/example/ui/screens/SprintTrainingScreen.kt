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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.MissionCategory
import com.example.model.SprintRecord
import com.example.ui.FitBusterDashboardData
import com.example.ui.SprintTimerState
import com.example.ui.components.BarChartPoint
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.LineProgressChart
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.GoldMedal
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.util.FitBusterDateUtil
import java.util.Locale

@Composable
fun SprintTrainingScreen(
    data: FitBusterDashboardData,
    sprintTimer: SprintTimerState,
    onSelectDistance: (Int) -> Unit,
    onStartStopwatch: () -> Unit,
    onStopAndSaveStopwatch: () -> Unit,
    onSaveManualSprint: (Int, Double) -> Unit
) {
    var manualSecondsText by remember { mutableStateOf("") }
    var warmupExpanded by remember { mutableStateOf(true) }

    val pb30 = remember(data.sprints) { data.sprints.filter { it.distanceMeters == 30L }.minByOrNull { it.timeMillis } }
    val pb60 = remember(data.sprints) { data.sprints.filter { it.distanceMeters == 60L }.minByOrNull { it.timeMillis } }
    val pb100 = remember(data.sprints) { data.sprints.filter { it.distanceMeters == 100L }.minByOrNull { it.timeMillis } }

    val selectedRecords = remember(data.sprints, sprintTimer.selectedDistanceMeters) {
        data.sprints.filter { it.distanceMeters == sprintTimer.selectedDistanceMeters.toLong() }
    }
    val chartPoints = remember(selectedRecords) {
        selectedRecords.take(8).reversed().mapIndexed { idx, rec ->
            BarChartPoint("#${idx + 1}", rec.timeMillis / 1000f)
        }
    }
    val sprintMissions = remember(data.missions) {
        data.missions.filter { it.category == MissionCategory.SPRINT.name }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Personal Best Records (30m, 60m, 100m)
        item {
            SectionHeader(
                title = "Personal Best Records",
                subtitle = "Automatic PB detection across 30m, 60m & 100m sprints"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SprintPbCard("30m PB", pb30, Modifier.weight(1f))
                SprintPbCard("60m PB", pb60, Modifier.weight(1f))
                SprintPbCard("100m PB", pb100, Modifier.weight(1f))
            }
        }

        // 2. Sprint Distance Selector & Live Stopwatch
        item {
            FitBusterCard(highlighted = sprintTimer.isRunning) {
                Text(
                    text = "Sprint Stopwatch",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 100).forEach { dist ->
                        FilterChip(
                            selected = sprintTimer.selectedDistanceMeters == dist,
                            onClick = { onSelectDistance(dist) },
                            label = { Text("${dist}m Sprint") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = ObsidianBlack
                            ),
                            modifier = Modifier.testTag("sprint_dist_$dist")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = String.format(Locale.US, "%.2f s", sprintTimer.elapsedMillis / 1000.0),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (sprintTimer.isRunning) {
                    Button(
                        onClick = onStopAndSaveStopwatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertCrimson,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("stop_sprint_btn")
                    ) {
                        Icon(Icons.Filled.Stop, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STOP & SAVE ${sprintTimer.selectedDistanceMeters}m", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStartStopwatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_sprint_btn")
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START ${sprintTimer.selectedDistanceMeters}m SPRINT", fontWeight = FontWeight.Bold)
                    }
                }

                sprintTimer.lastCompletedSprint?.let { last ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (sprintTimer.newPersonalBest) GoldMedal.copy(alpha = 0.18f) else HydrationCyan.copy(alpha = 0.14f)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(
                                text = if (sprintTimer.newPersonalBest) "NEW PERSONAL BEST RECORD!" else "Sprint Performance Report",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (sprintTimer.newPersonalBest) GoldMedal else HydrationCyan
                            )
                            val speedMps = last.distanceMeters / (last.timeMillis / 1000.0).coerceAtLeast(0.1)
                            Text(
                                text = "${last.distanceMeters}m in ${FitBusterDateUtil.formatSprintMillis(last.timeMillis)} • Avg speed: ${String.format(Locale.US, "%.2f m/s", speedMps)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 3. Manual Timing Entry
        item {
            FitBusterCard {
                Text(
                    text = "Manual Timing Entry (${sprintTimer.selectedDistanceMeters}m)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = manualSecondsText,
                        onValueChange = { manualSecondsText = it },
                        label = { Text("Seconds (e.g. 12.45)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("manual_sprint_input")
                    )
                    Button(
                        onClick = {
                            val parsed = manualSecondsText.toDoubleOrNull() ?: 0.0
                            manualSecondsText = ""
                            onSaveManualSprint(sprintTimer.selectedDistanceMeters, parsed)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier.height(54.dp).testTag("save_manual_sprint_btn")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Warm-up & Recovery Reminders
        item {
            FitBusterCard(onClick = { warmupExpanded = !warmupExpanded }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Whatshot, contentDescription = "Warm-up", tint = FlameAmber)
                    Text("Warm-up & Recovery Reminders", style = MaterialTheme.typography.titleMedium)
                }
                if (warmupExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Warm-up (5–8 min): Light jog, leg swings, ankle mobility, and 2 progressive build-up strides before max sprints.\n• Recovery: Rest 90–180 seconds between sprints and hydrate afterward.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Running & Sprint Progress Graph
        item {
            SectionHeader(
                title = "${sprintTimer.selectedDistanceMeters}m Progress Graph",
                subtitle = "Recent sprint timings in seconds (lower is faster)"
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (chartPoints.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.FlashOn,
                    title = "No ${sprintTimer.selectedDistanceMeters}m sprints yet",
                    description = "Run a live sprint or log a manual time to track your speed progression."
                )
            } else {
                FitBusterCard {
                    LineProgressChart(points = chartPoints)
                }
            }
        }

        // 6. Sprint Practice Missions
        if (sprintMissions.isNotEmpty()) {
            item {
                SectionHeader(title = "Sprint Practice Missions")
            }
            items(sprintMissions, key = { it.id }) { m ->
                FitBusterCard {
                    Text(m.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${m.currentValue} / ${m.targetValue} • ${m.status} (+${m.rewardPoints} pts)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 7. Previous Timing History
        item {
            SectionHeader(
                title = "Previous Timing History",
                subtitle = "${data.sprints.size} sprint logs"
            )
        }
        items(data.sprints.take(15), key = { it.id.ifBlank { it.hashCode().toString() } }) { rec ->
            FitBusterCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${rec.distanceMeters}m Sprint • ${FitBusterDateUtil.formatSprintMillis(rec.timeMillis)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${FitBusterDateUtil.formatDisplayDate(rec.dateKey)} • ${if (rec.manualEntry) "Manual" else "Stopwatch"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (rec.personalBest) {
                        Surface(shape = RoundedCornerShape(8.dp), color = GoldMedal.copy(alpha = 0.2f)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.EmojiEvents, null, tint = GoldMedal, modifier = androidx.compose.ui.Modifier.height(14.dp))
                                Text("PB", style = MaterialTheme.typography.labelSmall, color = GoldMedal)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SprintPbCard(title: String, record: SprintRecord?, modifier: Modifier = Modifier) {
    FitBusterCard(modifier = modifier, highlighted = record != null) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = record?.let { FitBusterDateUtil.formatSprintMillis(it.timeMillis) } ?: "--",
            style = MaterialTheme.typography.headlineMedium,
            color = if (record != null) GoldMedal else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
