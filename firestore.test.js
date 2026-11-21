const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msOffset = 1000) {
  return new Date(Date.now() - msOffset);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated access is denied across user profile and subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("workouts").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("User profile: owner create, partial update, cross-user denial, and ghost field rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Runner",
      email: "alice@example.com",
      age: 26,
      fitnessGoal: "Build Endurance",
      preferredWorkout: "OUTDOOR_RUN",
      dailyCalorieGoal: 2200,
      dailyWaterGoalMl: 2500,
      dailyProteinGoalG: 110,
      vegetarianDefault: true,
      challengePoints: 0,
      disciplinePoints: 0,
      challengeLevel: 1,
      punishmentLevel: 1,
      currentStreak: 0,
      longestStreak: 0,
      lastActiveDate: "",
      recoveryDates: [],
      activeDates: [],
      notificationsEnabled: true,
      workoutReminderEnabled: true,
      workoutReminderHour: 7,
      workoutReminderMinute: 0,
      waterReminderEnabled: true,
      missionReminderEnabled: true,
      streakReminderEnabled: true,
      themeAccent: "NEON_LIME",
      profilePrivate: false,
      hasSignature: false,
      signatureUpdatedAtMillis: 0,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      dailyWaterGoalMl: 3000,
      currentStreak: 1,
      updatedAt: pastDate(500),
    })
  );

  // Ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastDate(500),
    })
  );

  // Cross-user read & write rejected
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Bob", updatedAt: pastDate(500) }));

  // Listing /users root is denied
  await assertFails(aliceDb.collection("users").get());
});

test("Workouts, Sprints, RestLogs, Meals, WaterLogs: valid Kotlin payloads succeed & invalid/cross-user fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(5000);

  // Workout
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w_1").set({
      userId: ALICE_UID,
      workoutType: "OUTDOOR_RUN",
      customTitle: "Morning Tempo",
      durationSeconds: 1800,
      distanceMeters: 5200.5,
      averagePaceSecPerKm: 346.1,
      caloriesBurned: 420,
      gpsTracked: true,
      gpsPointCount: 120,
      dateKey: "2026-11-21",
      notes: "Felt strong",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("workouts").get());

  // Reject extra `id` field matching @DocumentId trap
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("workouts").doc("w_2").set({
      id: "w_2",
      userId: ALICE_UID,
      workoutType: "OUTDOOR_RUN",
      durationSeconds: 600,
      caloriesBurned: 90,
      dateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Sprint
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("sprints").doc("s_1").set({
      userId: ALICE_UID,
      distanceMeters: 100,
      timeMillis: 12450,
      manualEntry: false,
      personalBest: true,
      dateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // RestLog
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("rest_logs").doc("r_1").set({
      userId: ALICE_UID,
      plannedSeconds: 60,
      actualSeconds: 60,
      status: "COMPLETED",
      dateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Meal
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("meals").doc("m_1").set({
      userId: ALICE_UID,
      foodName: "Paneer Tikka Bowl",
      mealType: "LUNCH",
      calories: 520,
      proteinG: 32,
      carbsG: 48,
      fatsG: 18,
      vegetarian: true,
      dateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // WaterLog
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").doc("wl_1").set({
      userId: ALICE_UID,
      amountMl: 500,
      dateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("workouts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("sprints").doc("s_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("meals").doc("m_1").get());
});

test("Missions, Medals, Certificates, DisciplineLogs, PrivateAssets: rules & terminal locking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(5000);

  // Mission create & active -> completed transition, then terminal lock
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("missions").doc("mis_1").set({
      userId: ALICE_UID,
      missionKey: "daily_run_1",
      title: "Run 2 km",
      description: "Complete a 2 km outdoor run today",
      timeframe: "DAILY",
      category: "RUNNING",
      difficulty: "EASY",
      targetValue: 2000,
      currentValue: 0,
      rewardPoints: 50,
      deadlineKey: "2026-11-21",
      status: "ACTIVE",
      completedDateKey: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("missions").doc("mis_1").update({
      currentValue: 2000,
      status: "COMPLETED",
      completedDateKey: "2026-11-21",
      updatedAt: pastDate(500),
    })
  );

  // Terminal state locking: cannot modify after COMPLETED
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("missions").doc("mis_1").update({
      status: "ACTIVE",
      updatedAt: pastDate(200),
    })
  );

  // Medal create
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("medals").doc("bronze_beginner").set({
      userId: ALICE_UID,
      medalKey: "bronze_beginner",
      title: "Bronze - Fitness Beginner",
      tier: "BRONZE",
      description: "Complete 5 fitness missions.",
      unlockedDateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Certificate create
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("certificates").doc("cert_beginner").set({
      userId: ALICE_UID,
      certificateCode: "FB-CERT-2026-0001",
      certificateName: "Fitness Beginner Certificate",
      recipientName: "Alice Runner",
      achievementDescription: "Completed 5 fitness missions on FIT BUSTER.",
      medalEarned: "Bronze - Fitness Beginner",
      completionDateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Discipline log create
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("discipline_logs").doc("d_1").set({
      userId: ALICE_UID,
      exerciseType: "UTHAK_BAITHAK",
      level: 1,
      repsCompleted: 15,
      durationSeconds: 45,
      status: "COMPLETED",
      pointsAwarded: 25,
      dateKey: "2026-11-21",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Private signature asset
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("private").doc("signature").set({
      userId: ALICE_UID,
      assetType: "SIGNATURE",
      imageBase64: "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
      mimeType: "image/png",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob cannot read Alice's private signature or certificates
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("private").doc("signature").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("certificates").get());

  // Tampered future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").doc("wl_future").set({
      userId: ALICE_UID,
      amountMl: 250,
      dateKey: "2026-11-21",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});
