package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BalanceCard
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LightBg
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.viewmodel.FinoraViewModel

@Composable
fun DashboardScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val activeMonthBudgets by viewModel.activeMonthBudgets.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val hideBalance by viewModel.hideBalance.collectAsState()
    val activeMonth by viewModel.activeBudgetMonth.collectAsState()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var selectedTxType by remember { mutableStateOf(TransactionType.EXPENSE.name) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    val recentTransactions = remember(transactions) {
        transactions.take(5)
    }

    val userName = profile?.fullName?.ifBlank { "Alex" } ?: "Alex"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("dashboard_screen")
    ) {
        // 1. Personalized Greeting & Notifications Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DeepNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MintGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Good day,",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MintGreen,
                                contentColor = DarkNavy
                            ) { Text("2") }
                        }
                    ) {
                        IconButton(
                            onClick = { /* Notifications panel */ },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(WhiteCard)
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                .size(42.dp)
                                .testTag("notifications_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = DeepNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Main Balance Card
        item {
            BalanceCard(
                combinedBalancePaise = summary.combinedBalancePaise,
                monthlyIncomePaise = summary.monthlyIncomePaise,
                monthlyExpensePaise = summary.monthlyExpensePaise,
                hideBalance = hideBalance,
                onToggleHideBalance = { viewModel.toggleHideBalance() }
            )
            Spacer(modifier = Modifier.height(22.dp))
        }

        // 3. Quick Actions Grid
        item {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Income",
                    bgColor = Color(0x1F32D583),
                    iconColor = IncomeGreen,
                    testTag = "action_add_income",
                    onClick = {
                        selectedTxType = TransactionType.INCOME.name
                        showAddTxDialog = true
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.Remove,
                    label = "Expense",
                    bgColor = Color(0x1FF16B7A),
                    iconColor = ExpenseRed,
                    testTag = "action_add_expense",
                    onClick = {
                        selectedTxType = TransactionType.EXPENSE.name
                        showAddTxDialog = true
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    bgColor = Color(0x1F101D3A),
                    iconColor = DeepNavy,
                    testTag = "action_transfer",
                    onClick = { viewModel.navigateTo("transfers") }
                )
                QuickActionButton(
                    icon = Icons.Default.Receipt,
                    label = "History",
                    bgColor = Color(0x1F24B99A),
                    iconColor = MintDark,
                    testTag = "action_history",
                    onClick = { viewModel.navigateTo("transactions") }
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
        }

        // 4. Empty State / Demo Data Helper Banner
        if (accounts.isEmpty() && transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0x3355E6B0), RoundedCornerShape(18.dp))
                        .testTag("demo_data_banner"),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome to Finora!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Get started immediately by exploring pre-populated demonstration accounts, budgets, and transactions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadDemoData() },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("load_demo_data_dashboard_btn")
                        ) {
                            Text("Load Demo Data", color = WhiteCard)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        // 5. Monthly Budget Overview Glance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MONTHLY BUDGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )
                TextButton(
                    onClick = { viewModel.navigateTo("budget") },
                    modifier = Modifier.testTag("view_all_budgets_btn")
                ) {
                    Text("Details", color = MintDark, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MintDark, modifier = Modifier.size(14.dp))
                }
            }

            val totalBudgetPaise = activeMonthBudgets.sumOf { it.limitPaise }
            val totalSpentPaise = activeMonthBudgets.sumOf { it.spentPaise }
            val progress = if (totalBudgetPaise > 0) (totalSpentPaise.toFloat() / totalBudgetPaise.toFloat()).coerceIn(0f, 1f) else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { viewModel.navigateTo("budget") }
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = DateUtils.formatMonthDisplay(activeMonth),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% spent",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (progress >= 0.8f) ExpenseRed else MintDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (progress >= 1.0f) ExpenseRed else if (progress >= 0.8f) WarningOrange else MintGreen,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: ${CurrencyFormatter.formatPaise(totalSpentPaise)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Limit: ${CurrencyFormatter.formatPaise(totalBudgetPaise)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
        }

        // 6. Savings Goals Glance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVINGS GOALS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )
                TextButton(
                    onClick = { viewModel.navigateTo("goals") },
                    modifier = Modifier.testTag("view_all_goals_btn")
                ) {
                    Text("View All", color = MintDark, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MintDark, modifier = Modifier.size(14.dp))
                }
            }

            if (goals.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No active savings goals", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { showAddGoalDialog = true }) {
                            Text("Create a Goal")
                        }
                    }
                }
            } else {
                val primaryGoal = goals.first()
                val goalProgress = if (primaryGoal.targetAmountPaise > 0)
                    (primaryGoal.savedAmountPaise.toFloat() / primaryGoal.targetAmountPaise.toFloat()).coerceIn(0f, 1f)
                else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { viewModel.navigateTo("goals") }
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x1F24B99A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = MintDark, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = primaryGoal.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${CurrencyFormatter.formatPaise(primaryGoal.savedAmountPaise)} of ${CurrencyFormatter.formatPaise(primaryGoal.targetAmountPaise)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { goalProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MintGreen,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${(goalProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MintDark
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
        }

        // 7. Recent Transactions Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )
                TextButton(
                    onClick = { viewModel.navigateTo("transactions") },
                    modifier = Modifier.testTag("view_all_transactions_btn")
                ) {
                    Text("See All", color = MintDark, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MintDark, modifier = Modifier.size(14.dp))
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No transactions logged yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showAddTxDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                        ) {
                            Text("Add First Transaction")
                        }
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                DashboardTransactionItem(
                    transaction = tx,
                    hideBalance = hideBalance,
                    onClick = { viewModel.navigateTo("transactions") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
        }
    }

    // Dialogs
    if (showAddTxDialog) {
        AddTransactionDialog(
            accounts = accounts,
            initialType = selectedTxType,
            onDismiss = { showAddTxDialog = false },
            onSave = { type, title, amount, category, date, accId, destId, desc ->
                viewModel.addTransaction(type, title, amount, category, date, accId, destId, desc)
                showAddTxDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            month = activeMonth,
            onDismiss = { showAddBudgetDialog = false },
            onSave = { category, limit ->
                viewModel.saveBudget(category, limit)
                showAddBudgetDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { name, target, date, cat ->
                viewModel.saveGoal(name, target, date, cat)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
    }
}

@Composable
fun DashboardTransactionItem(
    transaction: Transaction,
    hideBalance: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
                    .size(42.dp)
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
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${transaction.category} • ${DateUtils.formatFriendlyDate(transaction.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            val amountDisplay = if (hideBalance) {
                "••••"
            } else {
                val prefix = if (isIncome) "+ " else if (isTransfer) "⇄ " else "- "
                prefix + CurrencyFormatter.formatPaise(transaction.amountPaise)
            }

            val amountColor = if (isIncome) IncomeGreen else if (isTransfer) DeepNavy else ExpenseRed

            Text(
                text = amountDisplay,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }
    }
}
