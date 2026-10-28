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

test("Unauthenticated user: cannot read Alice's profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read Alice's transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Bob cannot read Alice's transactions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      id: "tx_1",
      userId: ALICE_UID,
      type: "EXPENSE",
      title: "Groceries",
      amountPaise: 45000,
      category: "Food & Dining",
      date: "2026-10-27",
      accountId: "acc_main",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").get());
});

test("Alice can create a valid transaction", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_alice_1").set({
      id: "tx_alice_1",
      userId: ALICE_UID,
      type: "INCOME",
      title: "Salary",
      amountPaise: 5000000,
      category: "Salary",
      date: "2026-10-27",
      accountId: "acc_main",
      createdAt: new Date(),
    })
  );
});

test("Alice cannot create transaction with non-positive amount", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_bad").set({
      id: "tx_bad",
      userId: ALICE_UID,
      type: "EXPENSE",
      title: "Negative",
      amountPaise: -500,
      category: "Food & Dining",
      date: "2026-10-27",
      accountId: "acc_main",
      createdAt: new Date(),
    })
  );
});

test("Alice can create valid savings goal", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("goals").doc("goal_1").set({
      id: "goal_1",
      userId: ALICE_UID,
      name: "Emergency Fund",
      targetAmountPaise: 10000000,
      savedAmountPaise: 2500000,
      targetDate: "2027-12-31",
      status: "ACTIVE",
      createdAt: new Date(),
    })
  );
});

test("Bob cannot update Alice's savings goal", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("goals").doc("goal_1").set({
      id: "goal_1",
      userId: ALICE_UID,
      name: "Emergency Fund",
      targetAmountPaise: 10000000,
      savedAmountPaise: 2500000,
      targetDate: "2027-12-31",
      status: "ACTIVE",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("goals").doc("goal_1").update({
      savedAmountPaise: 0,
    })
  );
});
