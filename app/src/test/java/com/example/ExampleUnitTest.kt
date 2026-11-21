package com.example

import com.example.model.MissionProgress
import com.example.model.MissionStatus
import com.example.model.UserProfile
import com.example.model.WorkoutType
import com.example.util.FitBusterDateUtil
import com.example.util.FitnessCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun calorieEstimation_computesReasonableValues() {
        assertEquals(0L, FitnessCalculator.estimateCalories(WorkoutType.OUTDOOR_RUN, 0L))
        val runCal = FitnessCalculator.estimateCalories(WorkoutType.OUTDOOR_RUN, 1800L, 70.0)
        val walkCal = FitnessCalculator.estimateCalories(WorkoutType.OUTDOOR_WALK, 1800L, 70.0)
        assertTrue(runCal > walkCal)
        assertTrue(runCal in 250L..450L)
    }

    @Test
    fun paceCalculation_handlesShortAndNormalDistances() {
        assertEquals(0.0, FitnessCalculator.calculatePaceSecPerKm(120L, 10.0), 0.001)
        val pace = FitnessCalculator.calculatePaceSecPerKm(300L, 1000.0)
        assertEquals(300.0, pace, 0.001)
    }

    @Test
    fun levelCalculation_coversLevels1Through10() {
        assertEquals(1, FitnessCalculator.levelForPoints(0L))
        assertEquals(2, FitnessCalculator.levelForPoints(150L))
        assertEquals(10, FitnessCalculator.levelForPoints(5200L))
        val (prog, span) = FitnessCalculator.pointsProgressInLevel(200L)
        assertEquals(50L, prog)
        assertEquals(200L, span)
    }

    @Test
    fun streakCalculation_countsActiveAndRecoveryDaysContinuously() {
        val today = FitBusterDateUtil.todayKey()
        val yesterday = FitBusterDateUtil.dateKeyDaysAgo(1)
        val twoDaysAgo = FitBusterDateUtil.dateKeyDaysAgo(2)
        val (cur, longest) = FitnessCalculator.computeStreaks(
            activeDates = listOf(today, twoDaysAgo),
            recoveryDates = listOf(yesterday),
            todayKey = today
        )
        assertEquals(3L, cur)
        assertEquals(3L, longest)
    }

    @Test
    fun medalEvaluation_unlocksBronzeAtFiveCompletedMissions() {
        val missions = (1..5).map { idx ->
            MissionProgress(id = "m_$idx", status = MissionStatus.COMPLETED.name)
        }
        val earnedBronze = FitnessCalculator.isMedalEarned(
            medalKey = "bronze_fitness_beginner",
            missions = missions,
            workouts = emptyList(),
            sprints = emptyList(),
            waterLogs = emptyList(),
            profile = UserProfile(),
            disciplineCompletedCount = 0,
            unlockedMedals = emptyList()
        )
        val earnedSilver = FitnessCalculator.isMedalEarned(
            medalKey = "silver_fitness_warrior",
            missions = missions,
            workouts = emptyList(),
            sprints = emptyList(),
            waterLogs = emptyList(),
            profile = UserProfile(),
            disciplineCompletedCount = 0,
            unlockedMedals = emptyList()
        )
        assertTrue(earnedBronze)
        assertFalse(earnedSilver)
    }

    @Test
    fun dateAndTimeFormatters_produceExpectedStrings() {
        assertEquals("05:09", FitBusterDateUtil.formatDurationHms(309L))
        assertEquals("01:02:03", FitBusterDateUtil.formatDurationHms(3723L))
        assertEquals("11.45s", FitBusterDateUtil.formatSprintMillis(11450L))
        assertEquals("5:00 /km", FitBusterDateUtil.formatPace(300.0))
        assertEquals(7, FitBusterDateUtil.lastNDaysKeys(7).size)
    }
}
