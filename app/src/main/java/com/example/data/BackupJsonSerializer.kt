package com.example.data

import com.example.model.CertificateRecord
import com.example.model.DisciplineLog
import com.example.model.MealEntry
import com.example.model.MedalRecord
import com.example.model.MissionProgress
import com.example.model.RestLog
import com.example.model.SprintRecord
import com.example.model.UserProfile
import com.example.model.WaterLog
import com.example.model.WorkoutSession
import org.json.JSONArray
import org.json.JSONObject

data class BackupBundle(
    val profile: UserProfile?,
    val workouts: List<WorkoutSession>,
    val sprints: List<SprintRecord>,
    val restLogs: List<RestLog>,
    val meals: List<MealEntry>,
    val waterLogs: List<WaterLog>,
    val missions: List<MissionProgress>,
    val medals: List<MedalRecord>,
    val certificates: List<CertificateRecord>,
    val disciplineLogs: List<DisciplineLog>
)

object BackupJsonSerializer {
    fun exportToJson(bundle: BackupBundle): String {
        val root = JSONObject()
        root.put("app", "FIT_BUSTER")
        root.put("version", 1)
        bundle.profile?.let { p ->
            root.put("profile", JSONObject().apply {
                put("displayName", p.displayName)
                put("age", p.age)
                put("fitnessGoal", p.fitnessGoal)
                put("preferredWorkout", p.preferredWorkout)
                put("dailyCalorieGoal", p.dailyCalorieGoal)
                put("dailyWaterGoalMl", p.dailyWaterGoalMl)
                put("dailyProteinGoalG", p.dailyProteinGoalG)
                put("vegetarianDefault", p.vegetarianDefault)
                put("challengePoints", p.challengePoints)
                put("disciplinePoints", p.disciplinePoints)
                put("challengeLevel", p.challengeLevel)
                put("punishmentLevel", p.punishmentLevel)
                put("currentStreak", p.currentStreak)
                put("longestStreak", p.longestStreak)
                put("lastActiveDate", p.lastActiveDate)
                put("recoveryDates", JSONArray(p.recoveryDates))
                put("activeDates", JSONArray(p.activeDates))
                put("themeAccent", p.themeAccent)
            })
        }

        root.put("workouts", JSONArray().apply {
            bundle.workouts.forEach { w ->
                put(JSONObject().apply {
                    put("id", w.id)
                    put("workoutType", w.workoutType)
                    put("customTitle", w.customTitle)
                    put("durationSeconds", w.durationSeconds)
                    put("distanceMeters", w.distanceMeters)
                    put("averagePaceSecPerKm", w.averagePaceSecPerKm)
                    put("caloriesBurned", w.caloriesBurned)
                    put("gpsTracked", w.gpsTracked)
                    put("gpsPointCount", w.gpsPointCount)
                    put("dateKey", w.dateKey)
                    put("notes", w.notes)
                })
            }
        })

        root.put("sprints", JSONArray().apply {
            bundle.sprints.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("distanceMeters", s.distanceMeters)
                    put("timeMillis", s.timeMillis)
                    put("manualEntry", s.manualEntry)
                    put("personalBest", s.personalBest)
                    put("dateKey", s.dateKey)
                })
            }
        })

        root.put("meals", JSONArray().apply {
            bundle.meals.forEach { m ->
                put(JSONObject().apply {
                    put("id", m.id)
                    put("foodName", m.foodName)
                    put("mealType", m.mealType)
                    put("calories", m.calories)
                    put("proteinG", m.proteinG)
                    put("carbsG", m.carbsG)
                    put("fatsG", m.fatsG)
                    put("vegetarian", m.vegetarian)
                    put("dateKey", m.dateKey)
                })
            }
        })

        root.put("waterLogs", JSONArray().apply {
            bundle.waterLogs.forEach { w ->
                put(JSONObject().apply {
                    put("id", w.id)
                    put("amountMl", w.amountMl)
                    put("dateKey", w.dateKey)
                })
            }
        })

        root.put("medals", JSONArray().apply {
            bundle.medals.forEach { m ->
                put(JSONObject().apply {
                    put("medalKey", m.medalKey)
                    put("title", m.title)
                    put("tier", m.tier)
                    put("description", m.description)
                    put("unlockedDateKey", m.unlockedDateKey)
                })
            }
        })

        root.put("certificates", JSONArray().apply {
            bundle.certificates.forEach { c ->
                put(JSONObject().apply {
                    put("id", c.id)
                    put("certificateCode", c.certificateCode)
                    put("certificateName", c.certificateName)
                    put("recipientName", c.recipientName)
                    put("achievementDescription", c.achievementDescription)
                    put("medalEarned", c.medalEarned)
                    put("completionDateKey", c.completionDateKey)
                })
            }
        })

        root.put("disciplineLogs", JSONArray().apply {
            bundle.disciplineLogs.forEach { d ->
                put(JSONObject().apply {
                    put("id", d.id)
                    put("exerciseType", d.exerciseType)
                    put("level", d.level)
                    put("repsCompleted", d.repsCompleted)
                    put("durationSeconds", d.durationSeconds)
                    put("status", d.status)
                    put("pointsAwarded", d.pointsAwarded)
                    put("dateKey", d.dateKey)
                })
            }
        })

        return root.toString(2)
    }

    fun parseFromJson(jsonText: String, userId: String): BackupBundle {
        val root = JSONObject(jsonText)
        require(root.optString("app") == "FIT_BUSTER") { "Invalid FIT BUSTER backup file." }

        val pObj = root.optJSONObject("profile")
        val profile = pObj?.let {
            val recList = mutableListOf<String>()
            val recArr = it.optJSONArray("recoveryDates")
            if (recArr != null) {
                for (i in 0 until recArr.length()) recList.add(recArr.optString(i))
            }
            val actList = mutableListOf<String>()
            val actArr = it.optJSONArray("activeDates")
            if (actArr != null) {
                for (i in 0 until actArr.length()) actList.add(actArr.optString(i))
            }
            UserProfile(
                userId = userId,
                displayName = it.optString("displayName", "Athlete").ifBlank { "Athlete" },
                age = it.optLong("age", 25L),
                fitnessGoal = it.optString("fitnessGoal", "Build Endurance"),
                preferredWorkout = it.optString("preferredWorkout", "OUTDOOR_RUN"),
                dailyCalorieGoal = it.optLong("dailyCalorieGoal", 2200L),
                dailyWaterGoalMl = it.optLong("dailyWaterGoalMl", 2500L),
                dailyProteinGoalG = it.optLong("dailyProteinGoalG", 110L),
                vegetarianDefault = it.optBoolean("vegetarianDefault", true),
                challengePoints = it.optLong("challengePoints", 0L),
                disciplinePoints = it.optLong("disciplinePoints", 0L),
                challengeLevel = it.optLong("challengeLevel", 1L).coerceIn(1L, 10L),
                punishmentLevel = it.optLong("punishmentLevel", 1L).coerceIn(1L, 10L),
                currentStreak = it.optLong("currentStreak", 0L),
                longestStreak = it.optLong("longestStreak", 0L),
                lastActiveDate = it.optString("lastActiveDate", ""),
                recoveryDates = recList,
                activeDates = actList,
                themeAccent = it.optString("themeAccent", "NEON_LIME")
            )
        }

        val workouts = mutableListOf<WorkoutSession>()
        val wArr = root.optJSONArray("workouts")
        if (wArr != null) {
            for (i in 0 until wArr.length()) {
                val o = wArr.getJSONObject(i)
                workouts.add(
                    WorkoutSession(
                        id = o.optString("id"),
                        userId = userId,
                        workoutType = o.optString("workoutType", "OUTDOOR_RUN"),
                        customTitle = o.optString("customTitle", ""),
                        durationSeconds = o.optLong("durationSeconds", 0L),
                        distanceMeters = o.optDouble("distanceMeters", 0.0),
                        averagePaceSecPerKm = o.optDouble("averagePaceSecPerKm", 0.0),
                        caloriesBurned = o.optLong("caloriesBurned", 0L),
                        gpsTracked = o.optBoolean("gpsTracked", false),
                        gpsPointCount = o.optLong("gpsPointCount", 0L),
                        dateKey = o.optString("dateKey", ""),
                        notes = o.optString("notes", "")
                    )
                )
            }
        }

        val sprints = mutableListOf<SprintRecord>()
        val sArr = root.optJSONArray("sprints")
        if (sArr != null) {
            for (i in 0 until sArr.length()) {
                val o = sArr.getJSONObject(i)
                sprints.add(
                    SprintRecord(
                        id = o.optString("id"),
                        userId = userId,
                        distanceMeters = o.optLong("distanceMeters", 100L),
                        timeMillis = o.optLong("timeMillis", 15000L),
                        manualEntry = o.optBoolean("manualEntry", false),
                        personalBest = o.optBoolean("personalBest", false),
                        dateKey = o.optString("dateKey", "")
                    )
                )
            }
        }

        val meals = mutableListOf<MealEntry>()
        val mArr = root.optJSONArray("meals")
        if (mArr != null) {
            for (i in 0 until mArr.length()) {
                val o = mArr.getJSONObject(i)
                meals.add(
                    MealEntry(
                        id = o.optString("id"),
                        userId = userId,
                        foodName = o.optString("foodName", "Meal"),
                        mealType = o.optString("mealType", "LUNCH"),
                        calories = o.optLong("calories", 0L),
                        proteinG = o.optLong("proteinG", 0L),
                        carbsG = o.optLong("carbsG", 0L),
                        fatsG = o.optLong("fatsG", 0L),
                        vegetarian = o.optBoolean("vegetarian", true),
                        dateKey = o.optString("dateKey", "")
                    )
                )
            }
        }

        val waterLogs = mutableListOf<WaterLog>()
        val wlArr = root.optJSONArray("waterLogs")
        if (wlArr != null) {
            for (i in 0 until wlArr.length()) {
                val o = wlArr.getJSONObject(i)
                waterLogs.add(
                    WaterLog(
                        id = o.optString("id"),
                        userId = userId,
                        amountMl = o.optLong("amountMl", 250L),
                        dateKey = o.optString("dateKey", "")
                    )
                )
            }
        }

        val medals = mutableListOf<MedalRecord>()
        val medArr = root.optJSONArray("medals")
        if (medArr != null) {
            for (i in 0 until medArr.length()) {
                val o = medArr.getJSONObject(i)
                medals.add(
                    MedalRecord(
                        id = o.optString("medalKey"),
                        userId = userId,
                        medalKey = o.optString("medalKey"),
                        title = o.optString("title"),
                        tier = o.optString("tier", "BRONZE"),
                        description = o.optString("description"),
                        unlockedDateKey = o.optString("unlockedDateKey")
                    )
                )
            }
        }

        val certificates = mutableListOf<CertificateRecord>()
        val cArr = root.optJSONArray("certificates")
        if (cArr != null) {
            for (i in 0 until cArr.length()) {
                val o = cArr.getJSONObject(i)
                certificates.add(
                    CertificateRecord(
                        id = o.optString("id"),
                        userId = userId,
                        certificateCode = o.optString("certificateCode"),
                        certificateName = o.optString("certificateName"),
                        recipientName = o.optString("recipientName"),
                        achievementDescription = o.optString("achievementDescription"),
                        medalEarned = o.optString("medalEarned"),
                        completionDateKey = o.optString("completionDateKey")
                    )
                )
            }
        }

        val disciplineLogs = mutableListOf<DisciplineLog>()
        val dArr = root.optJSONArray("disciplineLogs")
        if (dArr != null) {
            for (i in 0 until dArr.length()) {
                val o = dArr.getJSONObject(i)
                disciplineLogs.add(
                    DisciplineLog(
                        id = o.optString("id"),
                        userId = userId,
                        exerciseType = o.optString("exerciseType", "UTHAK_BAITHAK"),
                        level = o.optLong("level", 1L),
                        repsCompleted = o.optLong("repsCompleted", 0L),
                        durationSeconds = o.optLong("durationSeconds", 0L),
                        status = o.optString("status", "COMPLETED"),
                        pointsAwarded = o.optLong("pointsAwarded", 0L),
                        dateKey = o.optString("dateKey", "")
                    )
                )
            }
        }

        return BackupBundle(
            profile = profile,
            workouts = workouts,
            sprints = sprints,
            restLogs = emptyList(),
            meals = meals,
            waterLogs = waterLogs,
            missions = emptyList(),
            medals = medals,
            certificates = certificates,
            disciplineLogs = disciplineLogs
        )
    }
}
