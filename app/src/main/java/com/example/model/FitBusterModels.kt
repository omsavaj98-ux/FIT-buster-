package com.example.model

enum class WorkoutType(
    val displayName: String,
    val metValue: Double,
    val supportsGps: Boolean
) {
    OUTDOOR_WALK("Outdoor Walk", 3.8, true),
    OUTDOOR_RUN("Outdoor Run", 9.8, true),
    OUTDOOR_ROWING("Outdoor Rowing", 7.0, true),
    FUNCTIONAL_STRENGTH("Functional Strength", 6.0, false),
    SPRINT_TRAINING("Sprint Training", 11.5, true),
    CUSTOM("Custom Workout", 5.5, false);

    companion object {
        fun fromRaw(raw: String): WorkoutType =
            entries.firstOrNull { it.name == raw } ?: CUSTOM
    }
}

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    companion object {
        fun fromRaw(raw: String): MealType =
            entries.firstOrNull { it.name == raw } ?: SNACK
    }
}

enum class MissionTimeframe(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly");

    companion object {
        fun fromRaw(raw: String): MissionTimeframe =
            entries.firstOrNull { it.name == raw } ?: DAILY
    }
}

enum class MissionCategory(val label: String) {
    RUNNING("Running"),
    SPRINT("Sprint"),
    STRENGTH("Strength"),
    HYDRATION("Hydration"),
    CONSISTENCY("Consistency"),
    SPECIAL("Special"),
    CHALLENGE("Challenge");

    companion object {
        fun fromRaw(raw: String): MissionCategory =
            entries.firstOrNull { it.name == raw } ?: SPECIAL
    }
}

enum class MissionDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    ELITE("Elite");

    companion object {
        fun fromRaw(raw: String): MissionDifficulty =
            entries.firstOrNull { it.name == raw } ?: EASY
    }
}

enum class MissionStatus {
    ACTIVE,
    COMPLETED,
    SKIPPED;

    companion object {
        fun fromRaw(raw: String): MissionStatus =
            entries.firstOrNull { it.name == raw } ?: ACTIVE
    }
}

enum class MedalTier(val label: String) {
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold"),
    PLATINUM("Platinum"),
    SPECIAL("Special");

    companion object {
        fun fromRaw(raw: String): MedalTier =
            entries.firstOrNull { it.name == raw } ?: BRONZE
    }
}

enum class DisciplineExercise(
    val title: String,
    val defaultReps: Int,
    val defaultSeconds: Int,
    val safeInstruction: String
) {
    UTHAK_BAITHAK(
        "Uthak-Baithak (Squats)",
        12,
        45,
        "Stand shoulder-width apart, keep your chest lifted, lower gently to a comfortable depth, and stop immediately if you feel knee or back pain."
    ),
    SIT_UPS(
        "Sit-ups",
        12,
        45,
        "Keep knees bent and feet flat. Lift smoothly with your core without pulling your neck. Stop if you feel lower-back strain."
    ),
    WALL_PUSH_UPS(
        "Wall Push-ups",
        15,
        40,
        "Place palms flat on a sturdy wall at chest height, keep a straight posture, bend elbows smoothly, and avoid shoulder discomfort."
    ),
    KNEE_RAISES(
        "Knee Raises",
        16,
        40,
        "Stand tall and alternate lifting knees to waist comfort height at a controlled rhythm. Hold a chair or wall if balance support is needed."
    ),
    LIGHT_WALKING(
        "Light Walking",
        0,
        180,
        "Walk at an easy, conversational pace indoors or outdoors to restore circulation without overexertion."
    ),
    MOBILITY(
        "Mobility Exercises",
        0,
        120,
        "Perform gentle shoulder rolls, cat-cow stretches, ankle circles, and hip openers. Never force a stretch through pain."
    ),
    OPTIONAL_CHALLENGE(
        "Optional Fitness Challenge",
        10,
        60,
        "Voluntary bonus movement set for extra discipline points. Skip anytime during rest, recovery, illness, or fatigue."
    );

    companion object {
        fun fromRaw(raw: String): DisciplineExercise =
            entries.firstOrNull { it.name == raw } ?: UTHAK_BAITHAK
    }
}

enum class DisciplineStatus {
    COMPLETED,
    SKIPPED,
    RECOVERY_DAY;

    companion object {
        fun fromRaw(raw: String): DisciplineStatus =
            entries.firstOrNull { it.name == raw } ?: COMPLETED
    }
}

data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val age: Long = 25L,
    val fitnessGoal: String = "Build Endurance & Consistency",
    val preferredWorkout: String = WorkoutType.OUTDOOR_RUN.name,
    val dailyCalorieGoal: Long = 2200L,
    val dailyWaterGoalMl: Long = 2500L,
    val dailyProteinGoalG: Long = 110L,
    val vegetarianDefault: Boolean = true,
    val challengePoints: Long = 0L,
    val disciplinePoints: Long = 0L,
    val challengeLevel: Long = 1L,
    val punishmentLevel: Long = 1L,
    val currentStreak: Long = 0L,
    val longestStreak: Long = 0L,
    val lastActiveDate: String = "",
    val recoveryDates: List<String> = emptyList(),
    val activeDates: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val workoutReminderEnabled: Boolean = true,
    val workoutReminderHour: Long = 7L,
    val workoutReminderMinute: Long = 0L,
    val waterReminderEnabled: Boolean = true,
    val missionReminderEnabled: Boolean = true,
    val streakReminderEnabled: Boolean = true,
    val themeAccent: String = "NEON_LIME",
    val profilePrivate: Boolean = false,
    val hasSignature: Boolean = false,
    val signatureUpdatedAtMillis: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class WorkoutSession(
    val id: String = "",
    val userId: String = "",
    val workoutType: String = WorkoutType.OUTDOOR_RUN.name,
    val customTitle: String = "",
    val durationSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val averagePaceSecPerKm: Double = 0.0,
    val caloriesBurned: Long = 0L,
    val gpsTracked: Boolean = false,
    val gpsPointCount: Long = 0L,
    val dateKey: String = "",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class SprintRecord(
    val id: String = "",
    val userId: String = "",
    val distanceMeters: Long = 100L,
    val timeMillis: Long = 0L,
    val manualEntry: Boolean = false,
    val personalBest: Boolean = false,
    val dateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class RestLog(
    val id: String = "",
    val userId: String = "",
    val plannedSeconds: Long = 60L,
    val actualSeconds: Long = 60L,
    val status: String = "COMPLETED",
    val dateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class MealEntry(
    val id: String = "",
    val userId: String = "",
    val foodName: String = "",
    val mealType: String = MealType.LUNCH.name,
    val calories: Long = 0L,
    val proteinG: Long = 0L,
    val carbsG: Long = 0L,
    val fatsG: Long = 0L,
    val vegetarian: Boolean = true,
    val dateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class WaterLog(
    val id: String = "",
    val userId: String = "",
    val amountMl: Long = 0L,
    val dateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class MissionProgress(
    val id: String = "",
    val userId: String = "",
    val missionKey: String = "",
    val title: String = "",
    val description: String = "",
    val timeframe: String = MissionTimeframe.DAILY.name,
    val category: String = MissionCategory.RUNNING.name,
    val difficulty: String = MissionDifficulty.EASY.name,
    val targetValue: Long = 1L,
    val currentValue: Long = 0L,
    val rewardPoints: Long = 50L,
    val deadlineKey: String = "",
    val status: String = MissionStatus.ACTIVE.name,
    val completedDateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class MedalRecord(
    val id: String = "",
    val userId: String = "",
    val medalKey: String = "",
    val title: String = "",
    val tier: String = MedalTier.BRONZE.name,
    val description: String = "",
    val unlockedDateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class CertificateRecord(
    val id: String = "",
    val userId: String = "",
    val certificateCode: String = "",
    val certificateName: String = "",
    val recipientName: String = "",
    val achievementDescription: String = "",
    val medalEarned: String = "",
    val completionDateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class DisciplineLog(
    val id: String = "",
    val userId: String = "",
    val exerciseType: String = DisciplineExercise.UTHAK_BAITHAK.name,
    val level: Long = 1L,
    val repsCompleted: Long = 0L,
    val durationSeconds: Long = 0L,
    val status: String = DisciplineStatus.COMPLETED.name,
    val pointsAwarded: Long = 0L,
    val dateKey: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class VegetarianFoodPreset(
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatsG: Int
)

data class MedalDefinition(
    val key: String,
    val title: String,
    val tier: MedalTier,
    val description: String,
    val requirementLabel: String,
    val targetCount: Int
)

data class CertificateDefinition(
    val key: String,
    val name: String,
    val description: String,
    val medalTitle: String
)

data class MissionTemplate(
    val key: String,
    val title: String,
    val description: String,
    val timeframe: MissionTimeframe,
    val category: MissionCategory,
    val difficulty: MissionDifficulty,
    val targetValue: Long,
    val unitLabel: String,
    val rewardPoints: Long
)

object FitBusterCatalog {
    val disciplineLevelNames: List<String> = listOf(
        "Beginner Discipline",
        "Fitness Discipline",
        "Active Warrior",
        "Consistency Test",
        "Fitness Focus",
        "Discipline Master",
        "Challenge Champion",
        "Fitness Legend",
        "Elite Discipline",
        "Ultimate Discipline"
    )

    val challengeLevelThresholds: List<Long> = listOf(
        0L, 150L, 350L, 650L, 1050L, 1550L, 2200L, 3000L, 4000L, 5200L
    )

    val vegetarianFoods: List<VegetarianFoodPreset> = listOf(
        VegetarianFoodPreset("Oats, Chia & Almond Milk Bowl", MealType.BREAKFAST, 340, 14, 48, 10),
        VegetarianFoodPreset("Moong Dal Chilla with Mint Chutney", MealType.BREAKFAST, 290, 18, 36, 7),
        VegetarianFoodPreset("Paneer Tikka Quinoa Bowl", MealType.LUNCH, 520, 32, 46, 20),
        VegetarianFoodPreset("Dal Tadka, Brown Rice & Salad", MealType.LUNCH, 480, 20, 74, 9),
        VegetarianFoodPreset("Chickpea & Avocado Power Salad", MealType.LUNCH, 410, 17, 44, 18),
        VegetarianFoodPreset("Tofu Stir-Fry with Broccoli & Peppers", MealType.DINNER, 390, 26, 30, 16),
        VegetarianFoodPreset("Palak Paneer & 2 Multigrain Rotis", MealType.DINNER, 470, 24, 48, 18),
        VegetarianFoodPreset("Sprouted Moong & Pomegranate Chaat", MealType.SNACK, 210, 14, 32, 3),
        VegetarianFoodPreset("Greek Yogurt with Berries & Walnuts", MealType.SNACK, 240, 16, 18, 11),
        VegetarianFoodPreset("Roasted Makhana & Mixed Nuts", MealType.SNACK, 190, 6, 16, 11)
    )

    val medalDefinitions: List<MedalDefinition> = listOf(
        MedalDefinition(
            key = "bronze_fitness_beginner",
            title = "Bronze - Fitness Beginner",
            tier = MedalTier.BRONZE,
            description = "Complete 5 fitness missions to earn your Bronze foundation medal.",
            requirementLabel = "5 missions completed",
            targetCount = 5
        ),
        MedalDefinition(
            key = "silver_fitness_warrior",
            title = "Silver - Fitness Warrior",
            tier = MedalTier.SILVER,
            description = "Complete 15 fitness missions with relentless dedication.",
            requirementLabel = "15 missions completed",
            targetCount = 15
        ),
        MedalDefinition(
            key = "gold_fitness_champion",
            title = "Gold - Fitness Champion",
            tier = MedalTier.GOLD,
            description = "Complete 30 fitness missions to reach Gold standard.",
            requirementLabel = "30 missions completed",
            targetCount = 30
        ),
        MedalDefinition(
            key = "platinum_fitness_legend",
            title = "Platinum - Fitness Legend",
            tier = MedalTier.PLATINUM,
            description = "Complete 50 fitness missions across all categories.",
            requirementLabel = "50 missions completed",
            targetCount = 50
        ),
        MedalDefinition(
            key = "running_star_medal",
            title = "Running Star Medal",
            tier = MedalTier.SPECIAL,
            description = "Complete 10 outdoor runs or cover 25 km total running distance.",
            requirementLabel = "10 runs or 25 km",
            targetCount = 10
        ),
        MedalDefinition(
            key = "sprint_champion_medal",
            title = "Sprint Champion Medal",
            tier = MedalTier.SPECIAL,
            description = "Log 5 sprint sessions across 30m, 60m, or 100m distances.",
            requirementLabel = "5 sprint records",
            targetCount = 5
        ),
        MedalDefinition(
            key = "strength_master_medal",
            title = "Strength Master Medal",
            tier = MedalTier.SPECIAL,
            description = "Complete 10 functional strength workouts.",
            requirementLabel = "10 strength sessions",
            targetCount = 10
        ),
        MedalDefinition(
            key = "hydration_champion_medal",
            title = "Hydration Champion Medal",
            tier = MedalTier.SPECIAL,
            description = "Reach your daily water intake goal on 7 separate days.",
            requirementLabel = "7 hydrated days",
            targetCount = 7
        ),
        MedalDefinition(
            key = "fitness_consistency_medal",
            title = "Fitness Consistency Medal",
            tier = MedalTier.SPECIAL,
            description = "Maintain a 7-day active streak including planned recovery days.",
            requirementLabel = "7-day streak",
            targetCount = 7
        ),
        MedalDefinition(
            key = "discipline_medal",
            title = "Discipline Medal",
            tier = MedalTier.SPECIAL,
            description = "Complete 5 safe Discipline & Uthak-Baithak sessions.",
            requirementLabel = "5 discipline sessions",
            targetCount = 5
        ),
        MedalDefinition(
            key = "ultimate_fitness_champion_medal",
            title = "Ultimate Fitness Champion Medal",
            tier = MedalTier.PLATINUM,
            description = "Reach Challenge Level 10 or unlock 8 other FIT BUSTER medals.",
            requirementLabel = "Level 10 or 8 medals",
            targetCount = 8
        )
    )

    val certificateDefinitions: List<CertificateDefinition> = listOf(
        CertificateDefinition(
            key = "cert_fitness_beginner",
            name = "Fitness Beginner Certificate",
            description = "Awarded for completing 5 fitness missions and building foundational momentum.",
            medalTitle = "Bronze - Fitness Beginner"
        ),
        CertificateDefinition(
            key = "cert_fitness_warrior",
            name = "Fitness Warrior Certificate",
            description = "Awarded for completing 15 fitness missions with disciplined execution.",
            medalTitle = "Silver - Fitness Warrior"
        ),
        CertificateDefinition(
            key = "cert_fitness_champion",
            name = "Fitness Champion Certificate",
            description = "Awarded for completing 30 fitness missions at Gold performance level.",
            medalTitle = "Gold - Fitness Champion"
        ),
        CertificateDefinition(
            key = "cert_running_champion",
            name = "Running Champion Certificate",
            description = "Awarded for achieving the Running Star milestone in endurance training.",
            medalTitle = "Running Star Medal"
        ),
        CertificateDefinition(
            key = "cert_sprint_champion",
            name = "Sprint Champion Certificate",
            description = "Awarded for explosive speed mastery in 30m, 60m, and 100m sprint training.",
            medalTitle = "Sprint Champion Medal"
        ),
        CertificateDefinition(
            key = "cert_strength_warrior",
            name = "Strength Warrior Certificate",
            description = "Awarded for dedicated functional strength progression and power output.",
            medalTitle = "Strength Master Medal"
        ),
        CertificateDefinition(
            key = "cert_7_day_consistency",
            name = "7-Day Fitness Consistency Certificate",
            description = "Awarded for sustaining a verified 7-day fitness and recovery streak.",
            medalTitle = "Fitness Consistency Medal"
        ),
        CertificateDefinition(
            key = "cert_monthly_champion",
            name = "Monthly Fitness Champion Certificate",
            description = "Awarded for completing a 30-day consistency milestone or Monthly Mission.",
            medalTitle = "Gold - Fitness Champion"
        ),
        CertificateDefinition(
            key = "cert_discipline_achievement",
            name = "Discipline Achievement Certificate",
            description = "Awarded for safe, mindful completion of Discipline & Mobility challenges.",
            medalTitle = "Discipline Medal"
        ),
        CertificateDefinition(
            key = "cert_ultimate_fit_buster",
            name = "Ultimate FIT BUSTER Champion Certificate",
            description = "Highest FIT BUSTER honor for mastering missions, strength, speed, and consistency.",
            medalTitle = "Ultimate Fitness Champion Medal"
        )
    )

    val missionTemplates: List<MissionTemplate> = listOf(
        // Daily Missions
        MissionTemplate(
            key = "daily_workout_1",
            title = "Daily Active Session",
            description = "Complete at least 1 workout session today (min 5 min).",
            timeframe = MissionTimeframe.DAILY,
            category = MissionCategory.CONSISTENCY,
            difficulty = MissionDifficulty.EASY,
            targetValue = 1L,
            unitLabel = "workout",
            rewardPoints = 40L
        ),
        MissionTemplate(
            key = "daily_hydration_goal",
            title = "Hydration Target",
            description = "Drink at least 2000 ml of water today.",
            timeframe = MissionTimeframe.DAILY,
            category = MissionCategory.HYDRATION,
            difficulty = MissionDifficulty.EASY,
            targetValue = 2000L,
            unitLabel = "ml",
            rewardPoints = 35L
        ),
        MissionTemplate(
            key = "daily_run_distance",
            title = "Daily Track Miles",
            description = "Log 1,500 meters of outdoor walk, run, or sprint today.",
            timeframe = MissionTimeframe.DAILY,
            category = MissionCategory.RUNNING,
            difficulty = MissionDifficulty.MEDIUM,
            targetValue = 1500L,
            unitLabel = "m",
            rewardPoints = 60L
        ),
        MissionTemplate(
            key = "daily_sprint_drill",
            title = "Explosive Sprint Drill",
            description = "Complete 2 timed sprints (30m, 60m, or 100m) today.",
            timeframe = MissionTimeframe.DAILY,
            category = MissionCategory.SPRINT,
            difficulty = MissionDifficulty.MEDIUM,
            targetValue = 2L,
            unitLabel = "sprints",
            rewardPoints = 55L
        ),
        // Weekly Missions
        MissionTemplate(
            key = "weekly_workouts_4",
            title = "4-Workout Weekly Split",
            description = "Complete 4 workouts this week across any training discipline.",
            timeframe = MissionTimeframe.WEEKLY,
            category = MissionCategory.CONSISTENCY,
            difficulty = MissionDifficulty.MEDIUM,
            targetValue = 4L,
            unitLabel = "workouts",
            rewardPoints = 120L
        ),
        MissionTemplate(
            key = "weekly_strength_2",
            title = "Strength Foundation",
            description = "Finish 2 Functional Strength sessions this week.",
            timeframe = MissionTimeframe.WEEKLY,
            category = MissionCategory.STRENGTH,
            difficulty = MissionDifficulty.MEDIUM,
            targetValue = 2L,
            unitLabel = "sessions",
            rewardPoints = 110L
        ),
        MissionTemplate(
            key = "weekly_sprint_5",
            title = "Speed Demon Week",
            description = "Record 5 sprint bursts (30m/60m/100m) this week.",
            timeframe = MissionTimeframe.WEEKLY,
            category = MissionCategory.SPRINT,
            difficulty = MissionDifficulty.HARD,
            targetValue = 5L,
            unitLabel = "sprints",
            rewardPoints = 150L
        ),
        MissionTemplate(
            key = "weekly_hydration_5days",
            title = "5-Day Hydration Streak",
            description = "Log water intake on 5 days this week.",
            timeframe = MissionTimeframe.WEEKLY,
            category = MissionCategory.HYDRATION,
            difficulty = MissionDifficulty.MEDIUM,
            targetValue = 5L,
            unitLabel = "days",
            rewardPoints = 100L
        ),
        // Monthly & Special & Challenge Missions
        MissionTemplate(
            key = "monthly_minutes_300",
            title = "300-Minute Monthly Engine",
            description = "Accumulate 300 active workout minutes this month.",
            timeframe = MissionTimeframe.MONTHLY,
            category = MissionCategory.SPECIAL,
            difficulty = MissionDifficulty.HARD,
            targetValue = 300L,
            unitLabel = "min",
            rewardPoints = 300L
        ),
        MissionTemplate(
            key = "monthly_distance_25km",
            title = "25 KM Distance Mastery",
            description = "Cover 25,000 meters across runs, walks, and rowing this month.",
            timeframe = MissionTimeframe.MONTHLY,
            category = MissionCategory.RUNNING,
            difficulty = MissionDifficulty.ELITE,
            targetValue = 25000L,
            unitLabel = "m",
            rewardPoints = 350L
        ),
        MissionTemplate(
            key = "monthly_challenge_discipline",
            title = "Iron Discipline Challenge",
            description = "Complete 5 safe Discipline & Mobility challenges this month.",
            timeframe = MissionTimeframe.MONTHLY,
            category = MissionCategory.CHALLENGE,
            difficulty = MissionDifficulty.HARD,
            targetValue = 5L,
            unitLabel = "challenges",
            rewardPoints = 220L
        )
    )
}
