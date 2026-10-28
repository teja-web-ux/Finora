package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LightBg
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.viewmodel.FinoraViewModel

@Composable
fun TransactionsScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val activeFilter by viewModel.transactionFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Group transactions by date
    val grouped = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("transactions_screen"),
        containerColor = LightBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTxDialog = true },
                containerColor = DeepNavy,
                contentColor = MintGreen,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search title, category or notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WhiteCard,
                    unfocusedContainerColor = WhiteCard,
                    focusedBorderColor = MintDark,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips: All, Income, Expense, Transfer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "All",
                    "INCOME" to "Income",
                    "EXPENSE" to "Expenses",
                    "TRANSFER" to "Transfers"
                ).forEach { (filterVal, label) ->
                    val isSelected = activeFilter == filterVal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) DeepNavy else WhiteCard)
                            .border(1.dp, if (isSelected) DeepNavy else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .clickable { viewModel.setTransactionFilter(filterVal) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_$filterVal"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) WhiteCard else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Transactions grouped by date
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button to record a new transaction",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (dateStr, txList) ->
                        item {
                            Text(
                                text = DateUtils.formatFriendlyDate(dateStr),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }

                        items(txList, key = { it.id }) { tx ->
                            val accountName = accounts.find { it.id == tx.accountId }?.name ?: "Account"
                            TransactionDetailItem(
                                transaction = tx,
                                accountName = accountName,
                                onDelete = { transactionToDelete = tx }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp)) // padding for bottom nav
                    }
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddTxDialog) {
        AddTransactionDialog(
            accounts = accounts,
            onDismiss = { showAddTxDialog = false },
            onSave = { type, title, amount, category, date, accId, destId, desc ->
                viewModel.addTransaction(type, title, amount, category, date, accId, destId, desc)
                showAddTxDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete Transaction?",
            message = "Are you sure you want to delete '${transactionToDelete?.title}'? This action cannot be undone.",
            onDismiss = { transactionToDelete = null },
            onConfirm = {
                transactionToDelete?.id?.let { viewModel.deleteTransaction(it) }
                transactionToDelete = null
            }
        )
    }
}

@Composable
fun TransactionDetailItem(
    transaction: Transaction,
    accountName: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = WhiteCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isIncome = transaction.type == TransactionType.INCOME.name
            val isTransfer = transaction.type == TransactionType.TRANSFER.name
            val iconBg = if (isIncome) Color(0x1F32D583) else if (isTransfer) Color(0x1F101D3A) else Color(0x1FF16B7A)
            val iconTint = if (isIncome) IncomeGreen else if (isTransfer) DeepNavy else ExpenseRed

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowUpward else if (isTransfer) Icons.Default.SwapHoriz else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "${transaction.category} • $accountName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (!transaction.description.isNullOrBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            val amountPrefix = if (isIncome) "+ " else if (isTransfer) "⇄ " else "- "
            val amountColor = if (isIncome) IncomeGreen else if (isTransfer) DeepNavy else ExpenseRed

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountPrefix + CurrencyFormatter.formatPaise(transaction.amountPaise),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
