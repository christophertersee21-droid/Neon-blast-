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

test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
});

test("Authenticated user: can create own profile with valid data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "AliceNeon",
      email: "alice@example.com",
      referralCode: "ALICE123",
      bestScore: 1200,
      gameCoins: 100,
    })
  );
});

test("Authenticated user: cannot create another user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "BobFake",
      email: "bob@example.com",
    })
  );
});

test("Authenticated user: can record valid game session for self", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("game_sessions").doc("session_1").set({
      sessionId: "session_1",
      userId: ALICE_UID,
      score: 550,
      linesCleared: 8,
      combos: 3,
      durationSeconds: 120,
    })
  );
});

test("Authenticated user: cannot record game session for another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("game_sessions").doc("session_2").set({
      sessionId: "session_2",
      userId: BOB_UID,
      score: 99999,
      linesCleared: 50,
      durationSeconds: 10,
    })
  );
});

test("Authenticated user: can submit valid withdrawal request for self", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("withdrawals").doc("w_1").set({
      userId: ALICE_UID,
      userEmail: "alice@example.com",
      amount: 25.0,
      currency: "USD",
      payoutMethod: "PayPal",
      payoutDetails: "alice@paypal.com",
      status: "PENDING",
      reference: "NB-W-1001",
    })
  );
});
