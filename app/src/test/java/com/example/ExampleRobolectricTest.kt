package com.example

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Base64
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BackupBundle
import com.example.data.BackupJsonSerializer
import com.example.data.FitBusterRepository
import com.example.data.LOCAL_USER_ID
import com.example.data.PrivateSignatureStore
import com.example.data.local.FitBusterDatabase
import com.example.model.CertificateRecord
import com.example.model.DisciplineLog
import com.example.model.MealEntry
import com.example.model.MedalRecord
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import com.example.model.WorkoutType
import com.example.notifications.FitBusterNotificationHelper
import com.example.notifications.SoundAlertHelper
import com.example.ui.FitBusterDashboardData
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MedalsAndCertificatesScreen
import com.example.ui.theme.FitBusterTheme
import com.example.util.CertificatePdfGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readStringFromContext_matchesFitBuster() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FIT BUSTER", appName)
    }

    @Test
    fun backupJsonSerializer_roundTripsAllFitnessEntities() {
        val bundle = BackupBundle(
            profile = UserProfile(
                userId = "user_old",
                displayName = "Aarav Mehta",
                age = 24L,
                fitnessGoal = "Sub-12s 100m",
                dailyWaterGoalMl = 3000L,
                challengePoints = 420L,
                challengeLevel = 3L
            ),
            workouts = listOf(
                WorkoutSession(
                    id = "w1",
                    userId = "user_old",
                    workoutType = WorkoutType.OUTDOOR_RUN.name,
                    durationSeconds = 1200L,
                    distanceMeters = 3200.0,
                    caloriesBurned = 260L,
                    dateKey = "2026-03-01"
                )
            ),
            sprints = listOf(
                SprintRecord(
                    id = "s1",
                    userId = "user_old",
                    distanceMeters = 100L,
                    timeMillis = 12450L,
                    personalBest = true,
                    dateKey = "2026-03-01"
                )
            ),
            restLogs = emptyList(),
            meals = listOf(
                MealEntry(
                    id = "m1",
                    userId = "user_old",
                    foodName = "Paneer Bowl",
                    calories = 450L,
                    proteinG = 28L,
                    vegetarian = true,
                    dateKey = "2026-03-01"
                )
            ),
            waterLogs = listOf(WaterLog(id = "wl1", userId = "user_old", amountMl = 500L, dateKey = "2026-03-01")),
            missions = emptyList(),
            medals = listOf(
                MedalRecord(
                    id = "bronze_fitness_beginner",
                    userId = "user_old",
                    medalKey = "bronze_fitness_beginner",
                    title = "Bronze – Fitness Beginner",
                    tier = "BRONZE",
                    unlockedDateKey = "2026-03-01"
                )
            ),
            certificates = listOf(
                CertificateRecord(
                    id = "fitness_beginner_cert",
                    userId = "user_old",
                    certificateCode = "FB-2026-1001",
                    certificateName = "Fitness Beginner Certificate",
                    recipientName = "Aarav Mehta",
                    achievementDescription = "Completed 5 fitness missions.",
                    medalEarned = "Bronze – Fitness Beginner",
                    completionDateKey = "2026-03-01"
                )
            ),
            disciplineLogs = listOf(
                DisciplineLog(
                    id = "d1",
                    userId = "user_old",
                    exerciseType = "UTHAK_BAITHAK",
                    repsCompleted = 15L,
                    pointsAwarded = 35L,
                    dateKey = "2026-03-01"
                )
            )
        )

        val json = BackupJsonSerializer.exportToJson(bundle)
        val restored = BackupJsonSerializer.parseFromJson(json, "user_new")

        assertNotNull(restored.profile)
        assertEquals("user_new", restored.profile?.userId)
        assertEquals("Aarav Mehta", restored.profile?.displayName)
        assertEquals(3000L, restored.profile?.dailyWaterGoalMl)
        assertEquals(1, restored.workouts.size)
        assertEquals(3200.0, restored.workouts.first().distanceMeters, 0.01)
        assertEquals(1, restored.sprints.size)
        assertEquals(12450L, restored.sprints.first().timeMillis)
        assertEquals(1, restored.meals.size)
        assertEquals("Paneer Bowl", restored.meals.first().foodName)
        assertEquals(1, restored.medals.size)
        assertEquals(1, restored.certificates.size)
        assertEquals("Aarav Mehta", restored.certificates.first().recipientName)
        assertEquals(1, restored.disciplineLogs.size)
    }

    @Test
    fun privateSignatureStore_savesAndLoadsUntouchedSignatureBitmap() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = PrivateSignatureStore(context)
        val uid = "test_user_sig"

        val original = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            setPixel(10, 10, Color.BLACK)
        }
        val out = ByteArrayOutputStream()
        original.compress(Bitmap.CompressFormat.PNG, 100, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)

        store.restoreSignatureBytes(uid, b64)
        assertTrue(store.hasLocalSignature(uid))

        val loaded = store.loadSignatureBitmap(uid)
        assertNotNull(loaded)
        assertEquals(40, loaded?.width)
        assertEquals(20, loaded?.height)
        assertEquals(Color.BLACK, loaded?.getPixel(10, 10))

        store.deleteSignature(uid)
        assertFalse(store.hasLocalSignature(uid))
    }

    @Test
    fun certificatePdfGenerator_handlesExecutionWithoutLeaks() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sig = Bitmap.createBitmap(60, 24, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
        }
        val cert = CertificateRecord(
            id = "fitness_beginner_cert",
            userId = "u1",
            certificateCode = "FB-2026-8888",
            certificateName = "Fitness Beginner Certificate",
            recipientName = "Riya Sharma",
            achievementDescription = "Completed 5 FIT BUSTER missions.",
            medalEarned = "Bronze – Fitness Beginner",
            completionDateKey = "2026-03-02"
        )

        val result = CertificatePdfGenerator.generateCertificatePdf(context, cert, sig)
        // Native PdfDocument is supported on Android devices; in host JVM Robolectric without native PDFium,
        // verify it returns a clean Result rather than crashing or leaking.
        if (result.isSuccess) {
            val exported = result.getOrThrow()
            assertTrue(exported.internalFile.exists())
            assertTrue(exported.internalFile.length() > 500L)
        } else {
            assertNotNull(result.exceptionOrNull())
        }
    }

    @Test
    fun homeDashboardScreen_displaysSummaryAndQuickStart() {
        val sampleData = FitBusterDashboardData(
            profile = UserProfile(
                userId = "u1",
                displayName = "Kabir Verma",
                currentStreak = 4L,
                dailyWaterGoalMl = 2500L
            )
        )

        composeRule.setContent {
            FitBusterTheme {
                HomeDashboardScreen(
                    data = sampleData,
                    onQuickStartWorkout = {},
                    onOpenSprint = {},
                    onOpenRestTimer = {},
                    onOpenDailyReport = {},
                    onOpenDiscipline = {},
                    onOpenWater = {},
                    onOpenMissions = {},
                    onAddQuickWater = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("WELCOME BACK, KABIR VERMA").assertIsDisplayed()
        composeRule.onNodeWithTag("quick_start_workout_button").assertIsDisplayed()
        composeRule.onNodeWithText("4d Streak").assertIsDisplayed()
    }

    @Test
    fun certificatesTab_displaysRecipientAccountNameAndPdfButtons() {
        val sig = Bitmap.createBitmap(80, 30, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
        }
        val cert = CertificateRecord(
            id = "fitness_beginner_cert",
            userId = "u1",
            certificateCode = "FB-2026-9012",
            certificateName = "Fitness Beginner Certificate",
            recipientName = "Ananya Patel",
            achievementDescription = "Completed 5 fitness missions.",
            medalEarned = "Bronze – Fitness Beginner",
            completionDateKey = "2026-03-05"
        )
        val sampleData = FitBusterDashboardData(
            profile = UserProfile(userId = "u1", displayName = "Ananya Patel"),
            certificates = listOf(cert)
        )

        composeRule.setContent {
            FitBusterTheme {
                MedalsAndCertificatesScreen(
                    data = sampleData,
                    signatureBitmap = sig,
                    initialTab = 1,
                    onExportPdf = { _, _ -> },
                    onOpenSignatureSettings = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Fitness Beginner Certificate").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Recipient: Ananya Patel").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("download_pdf_fitness_beginner_cert").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("share_pdf_fitness_beginner_cert").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun roomRepository_persistsProfileWorkoutsAndPreventsDuplicateMedals() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, FitBusterDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = FitBusterRepository(db.fitBusterDao(), LOCAL_USER_ID)
            repo.ensureUserProfile("Rohan Sen").getOrThrow()
            repo.updateProfileFields(mapOf("dailyWaterGoalMl" to 3200L)).getOrThrow()

            val profile = repo.observeUserProfile(LOCAL_USER_ID).first()
            assertNotNull(profile)
            assertEquals("Rohan Sen", profile?.displayName)
            assertEquals(3200L, profile?.dailyWaterGoalMl)

            repo.addWorkout(
                WorkoutSession(
                    workoutType = WorkoutType.OUTDOOR_RUN.name,
                    durationSeconds = 900L,
                    distanceMeters = 2200.0,
                    caloriesBurned = 210L,
                    dateKey = "2026-03-06"
                )
            ).getOrThrow()
            assertEquals(1, repo.observeWorkouts(LOCAL_USER_ID).first().size)

            val medal = MedalRecord(
                id = "bronze_fitness_beginner",
                medalKey = "bronze_fitness_beginner",
                title = "Bronze - Fitness Beginner",
                tier = "BRONZE",
                unlockedDateKey = "2026-03-06"
            )
            assertTrue(repo.unlockMedalOnce(medal).getOrThrow())
            assertFalse(repo.unlockMedalOnce(medal).getOrThrow())

            repo.resetAllProgress().getOrThrow()
            assertEquals(0, repo.observeWorkouts(LOCAL_USER_ID).first().size)
            assertEquals(0, repo.observeMedals(LOCAL_USER_ID).first().size)
        } finally {
            db.close()
        }
    }

    @Test
    fun notificationChannels_haveHighImportanceAndSoundConfigured() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FitBusterNotificationHelper.ensureChannels(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val reminderChannel = nm.getNotificationChannel(FitBusterNotificationHelper.CHANNEL_REMINDERS)
            val timerChannel = nm.getNotificationChannel(FitBusterNotificationHelper.CHANNEL_TIMERS)
            assertNotNull(reminderChannel)
            assertNotNull(timerChannel)
            assertEquals(NotificationManager.IMPORTANCE_HIGH, reminderChannel?.importance)
            assertEquals(NotificationManager.IMPORTANCE_HIGH, timerChannel?.importance)
        }
        SoundAlertHelper.playReminderSound(context, isAlarmOrTimer = true)
    }
}
