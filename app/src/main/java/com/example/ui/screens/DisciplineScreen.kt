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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DisciplineExercise
import com.example.model.DisciplineStatus
import com.example.model.FitBusterCatalog
import com.example.model.MissionStatus
import com.example.model.MissionTimeframe
import com.example.ui.DisciplineSessionState
import com.example.ui.FitBusterDashboardData
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FitBusterCard
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.HydrationCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SuccessGreen
import com.example.util.FitBusterDateUtil

@Composable
fun DisciplineScreen(
    data: FitBusterDashboardData,
    session: DisciplineSessionState,
    onSelectExercise: (DisciplineExercise) -> Unit,
    onIncrementRep: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onCompleteChallenge: () -> Unit,
    onSkipChallenge: () -> Unit,
    onToggleRecoveryDay: () -> Unit
) {
    val today = remember { FitBusterDateUtil.todayKey() }
    val isRecoveryToday = data.profile.recoveryDates.contains(today)
    val activeDailyMissions = data.missions.count {
        it.timeframe == MissionTimeframe.DAILY.name && it.status == MissionStatus.ACTIVE.name
    }
    val levelIndex = (data.profile.punishmentLevel.toInt() - 1).coerceIn(0, 9)
    val levelName = FitBusterCatalog.disciplineLevelNames[levelIndex]
    val pointsInLevel = data.profile.disciplinePoints % 120L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Punishment & Discipline Level Header
        item {
            FitBusterCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PUNISHMENT LEVEL ${data.profile.punishmentLevel} / 10",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = levelName,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "${data.profile.disciplinePoints} Disc. Pts",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeonProgressBar(progress = pointsInLevel / 120f)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Next level reward: +30 bonus challenge pts & Discipline Medal / Certificate progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Missed Mission Warning & Recovery Day Option
        if (activeDailyMissions > 0 && !isRecoveryToday) {
            item {
                FitBusterCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = "Missed Mission Notice", tint = FlameAmber)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pending Daily Missions ($activeDailyMissions)", style = MaterialTheme.typography.titleMedium, color = FlameAmber)
                            Text(
                                text = "Complete an optional safe discipline set below for bonus points, or mark a Planned Recovery Day if resting.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            FitBusterCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Hotel, contentDescription = "Recovery Day", tint = HydrationCyan)
                        Column {
                            Text("Recovery Day Option", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (isRecoveryToday) "Recovery Day active — rest respected & streak protected"
                                else "Ill, injured, or resting? Enable Recovery Day anytime.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onToggleRecoveryDay,
                        modifier = Modifier.testTag("discipline_recovery_btn")
                    ) {
                        Text(if (isRecoveryToday) "Active" else "Enable")
                    }
                }
            }
        }

        // 3. Exercise Selection (All 7 Exercises)
        item {
            SectionHeader(
                title = "Exercise Selection",
                subtitle = "Choose a safe movement challenge"
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DisciplineExercise.entries) { ex ->
                    FilterChip(
                        selected = session.selectedExercise == ex,
                        onClick = { onSelectExercise(ex) },
                        label = { Text(ex.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = ObsidianBlack
                        ),
                        modifier = Modifier.testTag("discipline_ex_${ex.name}")
                    )
                }
            }
        }

        // 4. Interactive Counter, Countdown Timer, Complete & Skip Buttons
        item {
            FitBusterCard(highlighted = true) {
                Text(
                    text = session.selectedExercise.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = session.selectedExercise.safeInstruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Rep Counter (if rep-based)
                if (session.targetReps > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("EXERCISE COUNTER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${session.currentReps} / ${session.targetReps} reps",
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onIncrementRep(-1) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease rep")
                            }
                            IconButton(
                                onClick = { onIncrementRep(1) },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                    .testTag("discipline_add_rep_btn")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add rep", tint = ObsidianBlack)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NeonProgressBar(progress = session.currentReps.toFloat() / session.targetReps.toFloat())
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Countdown Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("COUNTDOWN TIMER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FitBusterDateUtil.formatDurationHms(session.timerRemainingSec.toLong()),
                            style = MaterialTheme.typography.displayMedium,
                            color = HydrationCyan
                        )
                    }
                    OutlinedButton(
                        onClick = onToggleTimer,
                        modifier = Modifier.height(48.dp).testTag("discipline_timer_btn")
                    ) {
                        Icon(if (session.timerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (session.timerRunning) "Pause" else "Start Timer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCompleteChallenge,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = ObsidianBlack
                        ),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("complete_discipline_btn")
                    ) {
                        Icon(Icons.Filled.CheckCircle, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Complete", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSkipChallenge,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("skip_discipline_btn")
                    ) {
                        Icon(Icons.Filled.SkipNext, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skip Safely")
                    }
                }
            }
        }

        // 5. Safety First Notice
        item {
            FitBusterCard {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.HealthAndSafety, contentDescription = "Safety Guidelines", tint = SuccessGreen)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Safe Exercise Guidelines", style = MaterialTheme.typography.titleMedium, color = SuccessGreen)
                        Text(
                            text = "• No forced exercise or excessive repetitions.\n• Never exercise through sharp pain, dizziness, illness, or injury.\n• Respect rest and recovery days — use Skip Safely anytime.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 6. Punishment / Discipline History
        item {
            SectionHeader(title = "Discipline History", subtitle = "${data.disciplineLogs.size} logs")
        }
        if (data.disciplineLogs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.HealthAndSafety,
                    title = "No discipline logs yet",
                    description = "Complete an optional Uthak-Baithak, sit-ups, or mobility challenge to earn Discipline Points, Medal & Certificate."
                )
            }
        } else {
            items(data.disciplineLogs.take(15), key = { it.id.ifBlank { it.hashCode().toString() } }) { log ->
                val ex = DisciplineExercise.fromRaw(log.exerciseType)
                FitBusterCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ex.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Lvl ${log.level} • ${FitBusterDateUtil.formatDisplayDate(log.dateKey)} • ${log.repsCompleted} reps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (log.status == DisciplineStatus.COMPLETED.name) "+${log.pointsAwarded} pts" else log.status,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (log.status == DisciplineStatus.COMPLETED.name) SuccessGreen else HydrationCyan
                        )
                    }
                }
            }
        }
    }
}
