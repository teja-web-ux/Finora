package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FinoraRepository
import com.example.model.Account
import com.example.model.AccountType
import com.example.model.Budget
import com.example.model.Goal
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.UserSettings
import com.example.util.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardSummary(
    val combinedBalancePaise: Long = 0L,
    val monthlyIncomePaise: Long = 0L,
    val monthlyExpensePaise: Long = 0L,
    val netCashFlowPaise: Long = 0L,
    val totalSavingsPaise: Long = 0L
)

class FinoraViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: FinoraRepository = FinoraRepository.create(application)
) : AndroidViewModel(application) {

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FinoraViewModel(application, FinoraRepository.create(application)) as T
                }
            }
    }

    private val auth = Firebase.auth
    private val currentUserId = MutableStateFlow<String?>(auth.currentUser?.uid)

    // UI Feedback & Dialog State
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Active Screen & Navigation
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Transaction Filters
    private val _transactionFilter = MutableStateFlow("ALL")
    val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active Budget Month (defaults to current month, e.g. "2026-10")
    private val _activeBudgetMonth = MutableStateFlow(DateUtils.currentMonthString())
    val activeBudgetMonth: StateFlow<String> = _activeBudgetMonth.asStateFlow()

    // Hide balance toggle
    private val _hideBalance = MutableStateFlow(false)
    val hideBalance: StateFlow<Boolean> = _hideBalance.asStateFlow()

    // Data streams
    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: StateFlow<UserProfile?> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.observeProfile(uid) else flowOf(null)
    }.catch { emit(null) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<Account>> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.observeAccounts(uid) else flowOf(emptyList())
    }.catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.observeTransactions(uid) else flowOf(emptyList())
    }.catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val budgets: StateFlow<List<Budget>> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.observeBudgets(uid) else flowOf(emptyList())
    }.catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<Goal>> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.observeGoals(uid) else flowOf(emptyList())
    }.catch { emit(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val settings: StateFlow<UserSettings?> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.observeSettings(uid) else flowOf(null)
    }.catch { emit(null) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        _transactionFilter,
        _searchQuery
    ) { txList, filter, query ->
        txList.filter { tx ->
            val matchesFilter = when (filter) {
                "INCOME" -> tx.type == TransactionType.INCOME.name
                "EXPENSE" -> tx.type == TransactionType.EXPENSE.name
                "TRANSFER" -> tx.type == TransactionType.TRANSFER.name
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    (tx.description?.contains(query, ignoreCase = true) == true)
            matchesFilter && matchesQuery
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Budgets for active month
    val activeMonthBudgets: StateFlow<List<Budget>> = combine(
        budgets,
        transactions,
        _activeBudgetMonth
    ) { bList, txList, month ->
        // Calculate dynamic spending from saved expense transactions for this month
        val monthExpensesByCategory = txList.filter {
            it.type == TransactionType.EXPENSE.name && it.date.startsWith(month)
        }.groupBy { it.category }

        bList.filter { it.month == month }.map { b ->
            val actualSpent = monthExpensesByCategory[b.category]?.sumOf { it.amountPaise } ?: 0L
            b.copy(spentPaise = actualSpent)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Financial Summary Calculations
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        accounts,
        transactions
    ) { accList, txList ->
        val currentMonth = DateUtils.currentMonthString()
        val combinedBalance = accList.sumOf { it.balancePaise }
        val savingsBalance = accList.filter { it.accountType == AccountType.SAVINGS.name }.sumOf { it.balancePaise }

        val monthlyIncome = txList.filter {
            it.type == TransactionType.INCOME.name && it.date.startsWith(currentMonth)
        }.sumOf { it.amountPaise }

        val monthlyExpense = txList.filter {
            it.type == TransactionType.EXPENSE.name && it.date.startsWith(currentMonth)
        }.sumOf { it.amountPaise }

        val netCashFlow = monthlyIncome - monthlyExpense

        DashboardSummary(
            combinedBalancePaise = combinedBalance,
            monthlyIncomePaise = monthlyIncome,
            monthlyExpensePaise = monthlyExpense,
            netCashFlowPaise = netCashFlow,
            totalSavingsPaise = savingsBalance
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    init {
        // Sync profile on login if available
        syncCurrentUserProfile()
    }

    fun syncCurrentUserProfile() {
        val user = auth.currentUser
        currentUserId.value = user?.uid
        if (user != null) {
            viewModelScope.launch {
                try {
                    val name = user.displayName ?: user.email?.substringBefore("@") ?: "Finora User"
                    val email = user.email ?: ""
                    val photo = user.photoUrl?.toString()
                    repository.saveProfile(name, email, photo)
                    repository.initializeUserDataIfEmpty(user.uid)
                } catch (e: Exception) {
                    // Non-blocking sync error
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleHideBalance() {
        val newVal = !_hideBalance.value
        _hideBalance.value = newVal
        viewModelScope.launch {
            repository.updateSettings(UserSettings(userId = auth.currentUser?.uid ?: "", hideBalance = newVal))
        }
    }

    fun changeBudgetMonth(delta: Int) {
        _activeBudgetMonth.value = DateUtils.adjustMonth(_activeBudgetMonth.value, delta)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // -------------------------------------------------------------
    // MUTATIONS
    // -------------------------------------------------------------

    fun addTransaction(
        type: String,
        title: String,
        amountPaise: Long,
        category: String,
        date: String,
        accountId: String,
        destinationAccountId: String? = null,
        description: String? = null
    ) {
        if (title.isBlank()) {
            _userMessage.value = "Please enter a valid title"
            return
        }
        if (amountPaise <= 0) {
            _userMessage.value = "Amount must be greater than zero"
            return
        }
        if (accountId.isBlank()) {
            _userMessage.value = "Please select an account"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (type == TransactionType.TRANSFER.name && !destinationAccountId.isNullOrBlank()) {
                    repository.transferBetweenAccounts(
                        sourceAccountId = accountId,
                        destinationAccountId = destinationAccountId,
                        amountPaise = amountPaise,
                        title = title,
                        date = date,
                        notes = description
                    )
                    _userMessage.value = "Transfer recorded successfully"
                } else {
                    repository.createTransaction(
                        Transaction(
                            userId = auth.currentUser?.uid ?: "",
                            type = type,
                            title = title,
                            amountPaise = amountPaise,
                            category = category,
                            date = date,
                            accountId = accountId,
                            destinationAccountId = destinationAccountId,
                            description = description
                        )
                    )
                    _userMessage.value = "Transaction saved successfully"
                }
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to save transaction"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transactionId)
                _userMessage.value = "Transaction removed"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to delete transaction"}"
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                _userMessage.value = "Transaction updated"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to update transaction"}"
            }
        }
    }

    fun addOrUpdateAccount(
        name: String,
        accountType: String,
        initialBalancePaise: Long,
        colorHex: String,
        accountId: String? = null
    ) {
        if (name.isBlank()) {
            _userMessage.value = "Please enter an account name"
            return
        }
        viewModelScope.launch {
            try {
                repository.saveAccount(
                    Account(
                        id = accountId ?: "",
                        userId = auth.currentUser?.uid ?: "",
                        name = name,
                        accountType = accountType,
                        balancePaise = initialBalancePaise,
                        colorHex = colorHex
                    )
                )
                _userMessage.value = "Account saved successfully"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to save account"}"
            }
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAccount(accountId)
                _userMessage.value = "Account removed"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to delete account"}"
            }
        }
    }

    fun saveBudget(category: String, limitPaise: Long, budgetId: String? = null) {
        if (category.isBlank()) {
            _userMessage.value = "Please select a category"
            return
        }
        if (limitPaise <= 0) {
            _userMessage.value = "Budget limit must be greater than zero"
            return
        }
        viewModelScope.launch {
            try {
                repository.saveBudget(
                    Budget(
                        id = budgetId ?: "",
                        userId = auth.currentUser?.uid ?: "",
                        month = _activeBudgetMonth.value,
                        category = category,
                        limitPaise = limitPaise
                    )
                )
                _userMessage.value = "Budget saved for ${_activeBudgetMonth.value}"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to save budget"}"
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBudget(budgetId)
                _userMessage.value = "Budget removed"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to delete budget"}"
            }
        }
    }

    fun saveGoal(name: String, targetAmountPaise: Long, targetDate: String, category: String, goalId: String? = null) {
        if (name.isBlank()) {
            _userMessage.value = "Please enter a goal name"
            return
        }
        if (targetAmountPaise <= 0) {
            _userMessage.value = "Target amount must be greater than zero"
            return
        }
        viewModelScope.launch {
            try {
                repository.saveGoal(
                    Goal(
                        id = goalId ?: "",
                        userId = auth.currentUser?.uid ?: "",
                        name = name,
                        targetAmountPaise = targetAmountPaise,
                        targetDate = targetDate,
                        category = category
                    )
                )
                _userMessage.value = "Savings goal saved"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to save goal"}"
            }
        }
    }

    fun contributeToGoal(goalId: String, additionalPaise: Long) {
        if (additionalPaise <= 0) {
            _userMessage.value = "Contribution must be greater than zero"
            return
        }
        viewModelScope.launch {
            try {
                repository.addGoalContribution(goalId, additionalPaise)
                _userMessage.value = "Goal contribution added"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to contribute to goal"}"
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goalId)
                _userMessage.value = "Goal removed"
            } catch (e: Exception) {
                _userMessage.value = "Error: ${e.localizedMessage ?: "Failed to delete goal"}"
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.loadDemoData()
                _userMessage.value = "Demo financial data loaded successfully"
            } catch (e: Exception) {
                _userMessage.value = "Failed to load demo data: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearDemoData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.clearDemoData()
                _userMessage.value = "Demo data cleared"
            } catch (e: Exception) {
                _userMessage.value = "Failed to clear demo data: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfileName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            try {
                repository.saveProfile(newName, auth.currentUser?.email ?: "")
                _userMessage.value = "Profile updated"
            } catch (e: Exception) {
                _userMessage.value = "Failed to update profile: ${e.localizedMessage}"
            }
        }
    }

    fun deleteAccountAndData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteUserAccountAndData()
                onDeleted()
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete account: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
