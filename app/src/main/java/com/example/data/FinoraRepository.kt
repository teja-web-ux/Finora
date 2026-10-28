package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Account
import com.example.model.AccountType
import com.example.model.Budget
import com.example.model.Goal
import com.example.model.GoalStatus
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.UserSettings
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.FirebaseApp
import java.util.UUID

class FinoraRepository(val db: FirebaseFirestore) {

    companion object {
        fun create(context: Context): FinoraRepository {
            val firestore = try {
                val dbId = try {
                    val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
                    if (resId != 0) context.getString(resId) else ""
                } catch (_: Exception) {
                    ""
                }
                if (dbId.isNotBlank() && dbId != "(default)") {
                    try {
                        FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
                    } catch (_: Exception) {
                        try {
                            FirebaseFirestore.getInstance(dbId)
                        } catch (_: Exception) {
                            FirebaseFirestore.getInstance()
                        }
                    }
                } else {
                    FirebaseFirestore.getInstance()
                }
            } catch (_: Exception) {
                FirebaseFirestore.getInstance()
            }
            return FinoraRepository(firestore)
        }
    }

    constructor(context: Context) : this(create(context).db)

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    // -------------------------------------------------------------
    // USER PROFILE
    // -------------------------------------------------------------

    fun observeProfile(userId: String? = null): Flow<UserProfile?> = flow {
        val uid = userId ?: auth.currentUser?.uid
        if (uid == null) {
            emit(null)
            return@flow
        }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot -> snapshot.toObject(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    emit(null)
                }
        )
    }

    suspend fun saveProfile(fullName: String, email: String, photoUrl: String? = null) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            val data = mutableMapOf<String, Any>(
                "userId" to uid,
                "fullName" to fullName,
                "email" to email,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            photoUrl?.let { data["photoUrl"] = it }

            if (!snapshot.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(data).await()
            } else {
                docRef.set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // ACCOUNTS
    // -------------------------------------------------------------

    fun observeAccounts(userId: String? = null): Flow<List<Account>> = flow {
        val uid = userId ?: requireUserId()
        val path = "users/$uid/accounts"
        emitAll(
            db.collection("users").document(uid).collection("accounts")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Account::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun saveAccount(account: Account): String {
        val uid = requireUserId()
        val accountId = if (account.id.isBlank()) "acc_${UUID.randomUUID().toString().take(8)}" else account.id
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        val payload = mutableMapOf<String, Any>(
            "id" to accountId,
            "userId" to uid,
            "name" to account.name,
            "accountType" to account.accountType,
            "balancePaise" to account.balancePaise,
            "isDefault" to account.isDefault,
            "cardNumberMasked" to account.cardNumberMasked,
            "colorHex" to account.colorHex,
            "isDemo" to account.isDemo,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            val exists = docRef.get().await().exists()
            if (!exists) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
            return accountId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAccount(accountId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // TRANSACTIONS
    // -------------------------------------------------------------

    fun observeTransactions(userId: String? = null): Flow<List<Transaction>> = flow {
        val uid = userId ?: auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Transaction::class.java)
                        .sortedByDescending { it.date }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("transactions").document(transaction.id)
        val updates = mutableMapOf<String, Any>(
            "title" to transaction.title,
            "category" to transaction.category,
            "date" to transaction.date,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        transaction.description?.let { updates["description"] = it }
        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun createTransaction(transaction: Transaction): String {
        val uid = requireUserId()
        val txId = if (transaction.id.isBlank()) "tx_${UUID.randomUUID().toString().take(8)}" else transaction.id
        val docRef = db.collection("users").document(uid).collection("transactions").document(txId)
        val payload = mutableMapOf<String, Any>(
            "id" to txId,
            "userId" to uid,
            "type" to transaction.type,
            "title" to transaction.title,
            "amountPaise" to transaction.amountPaise,
            "category" to transaction.category,
            "date" to transaction.date,
            "accountId" to transaction.accountId,
            "isDemo" to transaction.isDemo,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        transaction.destinationAccountId?.let { payload["destinationAccountId"] = it }
        transaction.description?.let { payload["description"] = it }

        try {
            // Also adjust source account balance atomically
            val accountRef = db.collection("users").document(uid).collection("accounts").document(transaction.accountId)
            db.runTransaction { firestoreTx ->
                val accountSnapshot = firestoreTx.get(accountRef)
                if (accountSnapshot.exists()) {
                    val currentBalance = accountSnapshot.getLong("balancePaise") ?: 0L
                    val newBalance = when (transaction.type) {
                        TransactionType.INCOME.name -> currentBalance + transaction.amountPaise
                        TransactionType.EXPENSE.name -> currentBalance - transaction.amountPaise
                        TransactionType.TRANSFER.name -> currentBalance - transaction.amountPaise
                        else -> currentBalance
                    }
                    firestoreTx.update(accountRef, "balancePaise", newBalance, "updatedAt", FieldValue.serverTimestamp())
                }
                firestoreTx.set(docRef, payload)
            }.await()
            return txId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(transactionId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("transactions").document(transactionId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun transferBetweenAccounts(
        sourceAccountId: String,
        destinationAccountId: String,
        amountPaise: Long,
        title: String,
        date: String,
        notes: String?
    ): String {
        val uid = requireUserId()
        val txId = "tx_${UUID.randomUUID().toString().take(8)}"
        val txDocRef = db.collection("users").document(uid).collection("transactions").document(txId)
        val sourceRef = db.collection("users").document(uid).collection("accounts").document(sourceAccountId)
        val destRef = db.collection("users").document(uid).collection("accounts").document(destinationAccountId)

        val txPayload = mutableMapOf<String, Any>(
            "id" to txId,
            "userId" to uid,
            "type" to TransactionType.TRANSFER.name,
            "title" to title,
            "amountPaise" to amountPaise,
            "category" to "Transfer",
            "date" to date,
            "accountId" to sourceAccountId,
            "destinationAccountId" to destinationAccountId,
            "isDemo" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        notes?.let { txPayload["description"] = it }

        try {
            db.runTransaction { firestoreTx ->
                val sourceSnap = firestoreTx.get(sourceRef)
                val destSnap = firestoreTx.get(destRef)

                if (sourceSnap.exists() && destSnap.exists()) {
                    val srcBal = sourceSnap.getLong("balancePaise") ?: 0L
                    val destBal = destSnap.getLong("balancePaise") ?: 0L
                    firestoreTx.update(sourceRef, "balancePaise", srcBal - amountPaise, "updatedAt", FieldValue.serverTimestamp())
                    firestoreTx.update(destRef, "balancePaise", destBal + amountPaise, "updatedAt", FieldValue.serverTimestamp())
                }
                firestoreTx.set(txDocRef, txPayload)
            }.await()
            return txId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txDocRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // BUDGETS
    // -------------------------------------------------------------

    fun observeBudgets(userId: String? = null): Flow<List<Budget>> = flow {
        val uid = userId ?: auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/budgets"
        emitAll(
            db.collection("users").document(uid).collection("budgets")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Budget::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun saveBudget(budget: Budget): String {
        val uid = requireUserId()
        val budgetId = if (budget.id.isBlank()) "bg_${UUID.randomUUID().toString().take(8)}" else budget.id
        val docRef = db.collection("users").document(uid).collection("budgets").document(budgetId)
        val payload = mutableMapOf<String, Any>(
            "id" to budgetId,
            "userId" to uid,
            "month" to budget.month,
            "category" to budget.category,
            "limitPaise" to budget.limitPaise,
            "spentPaise" to budget.spentPaise,
            "isDemo" to budget.isDemo,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            val exists = docRef.get().await().exists()
            if (!exists) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
            return budgetId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBudget(budgetId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("budgets").document(budgetId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // GOALS
    // -------------------------------------------------------------

    fun observeGoals(userId: String? = null): Flow<List<Goal>> = flow {
        val uid = userId ?: auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/goals"
        emitAll(
            db.collection("users").document(uid).collection("goals")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Goal::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun saveGoal(goal: Goal): String {
        val uid = requireUserId()
        val goalId = if (goal.id.isBlank()) "goal_${UUID.randomUUID().toString().take(8)}" else goal.id
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
        val payload = mutableMapOf<String, Any>(
            "id" to goalId,
            "userId" to uid,
            "name" to goal.name,
            "targetAmountPaise" to goal.targetAmountPaise,
            "savedAmountPaise" to goal.savedAmountPaise,
            "targetDate" to goal.targetDate,
            "category" to goal.category,
            "status" to goal.status,
            "isDemo" to goal.isDemo,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            val exists = docRef.get().await().exists()
            if (!exists) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.set(payload, SetOptions.merge()).await()
            }
            return goalId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun addGoalContribution(goalId: String, additionalPaise: Long) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val currentSaved = snapshot.getLong("savedAmountPaise") ?: 0L
                val target = snapshot.getLong("targetAmountPaise") ?: 0L
                val newSaved = currentSaved + additionalPaise
                val newStatus = if (newSaved >= target && target > 0) GoalStatus.COMPLETED.name else GoalStatus.ACTIVE.name
                docRef.update(
                    "savedAmountPaise", newSaved,
                    "status", newStatus,
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGoal(goalId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // SETTINGS & PREFERENCES
    // -------------------------------------------------------------

    fun observeSettings(userId: String? = null): Flow<UserSettings?> = flow {
        val uid = userId ?: auth.currentUser?.uid
        if (uid == null) {
            emit(null)
            return@flow
        }
        val path = "users/$uid/settings/preferences"
        emitAll(
            db.collection("users").document(uid).collection("settings").document("preferences")
                .snapshots()
                .map { snapshot -> snapshot.toObject(UserSettings::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    emit(null)
                }
        )
    }

    suspend fun initializeUserDataIfEmpty(userId: String): Boolean {
        try {
            val accountsSnapshot = db.collection("users").document(userId).collection("accounts").limit(1).get().await()
            if (accountsSnapshot.isEmpty) {
                // Initialize default bank account
                val defaultAcc = Account(
                    id = "acc_primary",
                    userId = userId,
                    name = "Primary Bank Account",
                    accountType = AccountType.BANK.name,
                    balancePaise = 2500000L, // ₹25,000.00
                    isDefault = true,
                    cardNumberMasked = "•••• •••• •••• 4281",
                    colorHex = "#101D3A",
                    isDemo = false
                )
                saveAccount(defaultAcc)

                // Initialize starting balance transaction in transactions collection
                val welcomeTx = Transaction(
                    id = "tx_welcome_income",
                    userId = userId,
                    type = TransactionType.INCOME.name,
                    title = "Initial Account Opening",
                    amountPaise = 2500000L,
                    category = "Salary",
                    date = DateUtils.currentDateIso(),
                    accountId = "acc_primary",
                    description = "Opening balance transfer",
                    isDemo = false
                )
                createTransaction(welcomeTx)

                // Initialize default budget
                val welcomeBudget = Budget(
                    id = "bg_initial_food",
                    userId = userId,
                    month = DateUtils.currentMonthString(),
                    category = "Food & Dining",
                    limitPaise = 1000000L, // ₹10,000
                    spentPaise = 0L,
                    isDemo = false
                )
                saveBudget(welcomeBudget)

                return true
            }
        } catch (_: Exception) {
            // Non-blocking initialization
        }
        return false
    }

    suspend fun updateSettings(settings: UserSettings) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("preferences")
        val payload = mapOf(
            "id" to "preferences",
            "userId" to uid,
            "currency" to settings.currency,
            "hideBalance" to settings.hideBalance,
            "hasDemoData" to settings.hasDemoData,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // -------------------------------------------------------------
    // DEMO DATA SEEDING & REMOVAL
    // -------------------------------------------------------------

    suspend fun loadDemoData() {
        val uid = requireUserId()
        val today = DateUtils.currentDateIso()
        val currentMonth = DateUtils.currentMonthString()

        // 1. Demo Accounts
        val mainAcc = Account(
            id = "demo_acc_main",
            userId = uid,
            name = "Main Salary Account",
            accountType = AccountType.BANK.name,
            balancePaise = 7450000L, // ₹74,500.00
            isDefault = true,
            cardNumberMasked = "•••• •••• •••• 4892",
            colorHex = "#101D3A",
            isDemo = true
        )
        val savingsAcc = Account(
            id = "demo_acc_savings",
            userId = uid,
            name = "Emergency Savings",
            accountType = AccountType.SAVINGS.name,
            balancePaise = 15000000L, // ₹1,50,000.00
            isDefault = false,
            cardNumberMasked = "•••• •••• •••• 1204",
            colorHex = "#24B99A",
            isDemo = true
        )
        val walletAcc = Account(
            id = "demo_acc_wallet",
            userId = uid,
            name = "Digital Cash Wallet",
            accountType = AccountType.WALLET.name,
            balancePaise = 425000L, // ₹4,250.00
            isDefault = false,
            cardNumberMasked = "•••• •••• •••• 9318",
            colorHex = "#55E6B0",
            isDemo = true
        )

        saveAccount(mainAcc)
        saveAccount(savingsAcc)
        saveAccount(walletAcc)

        // 2. Demo Transactions
        val demoTxs = listOf(
            Transaction(
                id = "demo_tx_1",
                userId = uid,
                type = TransactionType.INCOME.name,
                title = "Monthly Salary Deposit",
                amountPaise = 10000000L, // ₹1,00,000.00
                category = "Salary",
                date = today,
                accountId = "demo_acc_main",
                description = "Tech Solutions Corp Payroll",
                isDemo = true
            ),
            Transaction(
                id = "demo_tx_2",
                userId = uid,
                type = TransactionType.EXPENSE.name,
                title = "Organic Supermarket",
                amountPaise = 345000L, // ₹3,450.00
                category = "Food & Dining",
                date = today,
                accountId = "demo_acc_main",
                description = "Weekly family groceries",
                isDemo = true
            ),
            Transaction(
                id = "demo_tx_3",
                userId = uid,
                type = TransactionType.EXPENSE.name,
                title = "Fuel & Metro Pass",
                amountPaise = 185000L, // ₹1,850.00
                category = "Transport",
                date = today,
                accountId = "demo_acc_main",
                description = "Monthly commuting pass",
                isDemo = true
            ),
            Transaction(
                id = "demo_tx_4",
                userId = uid,
                type = TransactionType.EXPENSE.name,
                title = "Electronics & Gadgets",
                amountPaise = 249900L, // ₹2,499.00
                category = "Shopping",
                date = today,
                accountId = "demo_acc_main",
                description = "Wireless noise-cancelling headphones",
                isDemo = true
            ),
            Transaction(
                id = "demo_tx_5",
                userId = uid,
                type = TransactionType.TRANSFER.name,
                title = "Transfer to Savings",
                amountPaise = 2000000L, // ₹20,000.00
                category = "Transfer",
                date = today,
                accountId = "demo_acc_main",
                destinationAccountId = "demo_acc_savings",
                description = "Monthly savings allocation",
                isDemo = true
            )
        )

        val txColl = db.collection("users").document(uid).collection("transactions")
        for (tx in demoTxs) {
            val payload = mutableMapOf<String, Any>(
                "id" to tx.id,
                "userId" to uid,
                "type" to tx.type,
                "title" to tx.title,
                "amountPaise" to tx.amountPaise,
                "category" to tx.category,
                "date" to tx.date,
                "accountId" to tx.accountId,
                "isDemo" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            tx.destinationAccountId?.let { payload["destinationAccountId"] = it }
            tx.description?.let { payload["description"] = it }
            txColl.document(tx.id).set(payload).await()
        }

        // 3. Demo Budgets
        val demoBudgets = listOf(
            Budget(
                id = "demo_bg_food",
                userId = uid,
                month = currentMonth,
                category = "Food & Dining",
                limitPaise = 1500000L, // ₹15,000.00
                spentPaise = 345000L,
                isDemo = true
            ),
            Budget(
                id = "demo_bg_transport",
                userId = uid,
                month = currentMonth,
                category = "Transport",
                limitPaise = 500000L, // ₹5,000.00
                spentPaise = 185000L,
                isDemo = true
            ),
            Budget(
                id = "demo_bg_shopping",
                userId = uid,
                month = currentMonth,
                category = "Shopping",
                limitPaise = 1000000L, // ₹10,000.00
                spentPaise = 850000L, // 85% - will trigger warning!
                isDemo = true
            ),
            Budget(
                id = "demo_bg_bills",
                userId = uid,
                month = currentMonth,
                category = "Bills & Utilities",
                limitPaise = 800000L, // ₹8,000.00
                spentPaise = 250000L,
                isDemo = true
            )
        )
        for (b in demoBudgets) {
            saveBudget(b)
        }

        // 4. Demo Goals
        val demoGoals = listOf(
            Goal(
                id = "demo_goal_emergency",
                userId = uid,
                name = "Emergency Fund",
                targetAmountPaise = 20000000L, // ₹2,00,000
                savedAmountPaise = 14500000L, // ₹1,45,000 (72.5%)
                targetDate = "2027-06-30",
                category = "Safety",
                status = GoalStatus.ACTIVE.name,
                isDemo = true
            ),
            Goal(
                id = "demo_goal_laptop",
                userId = uid,
                name = "New MacBook Pro",
                targetAmountPaise = 15000000L, // ₹1,50,000
                savedAmountPaise = 12000000L, // ₹1,20,000 (80%)
                targetDate = "2026-12-15",
                category = "Technology",
                status = GoalStatus.ACTIVE.name,
                isDemo = true
            ),
            Goal(
                id = "demo_goal_vacation",
                userId = uid,
                name = "Bali Vacation",
                targetAmountPaise = 10000000L, // ₹1,00,000
                savedAmountPaise = 4500000L, // ₹45,000 (45%)
                targetDate = "2027-04-10",
                category = "Travel",
                status = GoalStatus.ACTIVE.name,
                isDemo = true
            )
        )
        for (g in demoGoals) {
            saveGoal(g)
        }

        updateSettings(UserSettings(userId = uid, hasDemoData = true, hideBalance = false))
    }

    suspend fun clearDemoData() {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)

        // Clean demo accounts
        val accounts = userDoc.collection("accounts").whereEqualTo("isDemo", true).get().await()
        for (doc in accounts.documents) doc.reference.delete().await()

        // Clean demo transactions
        val txs = userDoc.collection("transactions").whereEqualTo("isDemo", true).get().await()
        for (doc in txs.documents) doc.reference.delete().await()

        // Clean demo budgets
        val budgets = userDoc.collection("budgets").whereEqualTo("isDemo", true).get().await()
        for (doc in budgets.documents) doc.reference.delete().await()

        // Clean demo goals
        val goals = userDoc.collection("goals").whereEqualTo("isDemo", true).get().await()
        for (doc in goals.documents) doc.reference.delete().await()

        updateSettings(UserSettings(userId = uid, hasDemoData = false))
    }

    suspend fun deleteUserAccountAndData() {
        val uid = requireUserId()
        val userDoc = db.collection("users").document(uid)

        // Clear subcollections
        for (sub in listOf("accounts", "transactions", "budgets", "goals", "settings")) {
            val docs = userDoc.collection(sub).get().await()
            for (doc in docs.documents) doc.reference.delete().await()
        }
        userDoc.delete().await()
        auth.currentUser?.delete()?.await()
    }
}
