# Finora — Your Money, in Your Control

Finora is a native Android personal finance management application built with Kotlin, Jetpack Compose, Material 3, and Cloud Firestore. It replicates a luxury fintech product aesthetic featuring deep navy gradient surfaces, glowing mint-green accents, rounded card containers, and zero-trust security.

---

## 📱 APK File Location

The compiled Android application package is available directly in the project file explorer:
- **`Finora.apk`** (located at the root of the project)
- **`apk/Finora-debug.apk`** (located in the `apk/` folder)

You can download `Finora.apk` directly from the file tree or install it on any Android device (Android 7.0+ / API 24+).

---

## Key Features

1. **Welcome & Authentication (Screen 1)**
   - Finora branding with stylized logo monogram and "Your Money, in Your Control" tagline.
   - Luxury floating credit card 3D illustration.
   - Jetpack Credential Manager Google Sign-In (`GetSignInWithGoogleOption`) with seamless session persistence.
   - Account creation and login with input validation, loading states, and password reset workflows.

2. **Home Dashboard (Screen 2)**
   - Personalized greeting with user initials avatar and notification bell.
   - Total Available Balance card in Deep Navy gradient (`#101D3A` to `#0B1530`) with Mint Green (`#55E6B0`) accents.
   - Interactive show/hide balance privacy toggle.
   - Monthly Income and Monthly Expense breakdown pills.
   - Quick Actions: Add Income, Add Expense, Transfer, View Transactions, Create Budget, Savings Goals.
   - Monthly budget summary and savings goal progress glance cards.
   - Date-grouped recent transactions with category icons and formatted amounts.

3. **Transactions (Screen 3)**
   - Real-time search filtering across title, category, and notes.
   - Type filter chips: All, Income, Expense, Transfer.
   - Date-grouped transaction history (Today, Yesterday, Earlier).
   - Formatted INR (`₹`) amounts with green indicators for income and red indicators for expenses.
   - Full Add Transaction and Delete Transaction workflows with validation and confirmation dialogs.

4. **My Cards (Screen 4)**
   - Finora virtual card mockup with contactless icon, masked card number (`•••• •••• •••• 4892`), holder name, expiry date, and balance.
   - Interactive card controls: Lock Card toggle (freezes transactions), Manage Daily Spending Limits slider dialog.
   - Personal bookkeeping and virtual card management compliance disclaimer.

5. **Send Money & Transfers (Screen 5)**
   - Account-to-account internal transfers and contact payment bookkeeping.
   - Quick contacts carousel (recent payees).
   - Atomic Firestore transactions updating both source and destination balances simultaneously.
   - Transfer review sheet and success receipt confirmation.

6. **My Budget (Screen 6)**
   - Month-by-month budget selector with previous and next navigation.
   - Dynamic Circular Progress chart rendering overall monthly spending proportion.
   - Category budget limits with real-time calculated spending from actual saved expense transactions.
   - Dynamic progress bars with 80% warning alerts and 100% budget exceeded badges.
   - Add and delete budget workflows.

7. **Savings Goals (Screen 7)**
   - Customizable savings milestones (Emergency Fund, Vacation, New Laptop, Education, etc.).
   - Target amount, current saved amount, remaining amount, target date, and progress percentages.
   - "Add Funds" contribution workflow.
   - Status badges (Active, Completed, Paused).

8. **Profile & Settings (Screen 8)**
   - User profile identity card with name editor.
   - Tracked accounts manager (add bank accounts, digital cash wallets, cards).
   - Demonstration data controls: "Load Demo Data" and "Clear Demo Data".
   - Privacy preferences: Default hide balance toggle.
   - Zero-trust security documentation.
   - Account sign-out and account deletion workflow.

---

## Financial Calculation Principles

- **Integer Paise Precision**: All currency amounts are stored and calculated in integer paise (1 Rupee = 100 paise) to eliminate floating-point arithmetic errors.
- **Combined Balance**: Sum of all tracked accounts' `balancePaise`.
- **Internal Transfers**: Internal transfers move funds between tracked accounts atomically without altering the combined net worth or counting as new income or expenses.
- **Monthly Spending**: Automatically derived from saved `EXPENSE` transactions matching the selected month (`YYYY-MM`).
- **Savings Progress**: Calculated as `(savedAmountPaise / targetAmountPaise) * 100`.

---

## Architecture & Technology Stack

- **Runtime**: Native Android (minSdk 24, targetSdk 36)
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with unidirectional data flow and Kotlin Coroutines/Flow
- **Backend**: Firebase Cloud Firestore with zero-trust security rules and Jetpack Credential Manager Google Sign-In
- **Testing**: Node.js rules testing (`@firebase/rules-unit-testing`) and Robolectric JVM repository integration testing (`FirestoreEmulatorTestBase`)
