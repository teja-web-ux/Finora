package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.model.Budget
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.CircularProgressChart
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
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.viewmodel.FinoraViewModel

@Composable
fun BudgetScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val activeMonthBudgets by viewModel.activeMonthBudgets.collectAsState()
    val activeMonth by viewModel.activeBudgetMonth.collectAsState()

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    val totalBudgetPaise = remember(activeMonthBudgets) {
        activeMonthBudgets.sumOf { it.limitPaise }
    }
    val totalSpentPaise = remember(activeMonthBudgets) {
        activeMonthBudgets.sumOf { it.spentPaise }
    }
    val remainingPaise = totalBudgetPaise - totalSpentPaise
    val overallPercentage = if (totalBudgetPaise > 0) {
        (totalSpentPaise.toFloat() / totalBudgetPaise.toFloat()).coerceIn(0f, 1.5f)
    } else 0f

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("budget_screen"),
        containerColor = LightBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBudgetDialog = true },
                containerColor = DeepNavy,
                contentColor = MintGreen,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_budget_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Header & Month Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Budget",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Monthly Spending Limits & Insights",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Month Selector Controls
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(WhiteCard)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.changeBudgetMonth(-1) },
                            modifier = Modifier.size(34.dp).testTag("prev_month_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = DeepNavy, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = DateUtils.formatMonthDisplay(activeMonth),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepNavy,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { viewModel.changeBudgetMonth(1) },
                            modifier = Modifier.size(34.dp).testTag("next_month_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = DeepNavy, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Circular Chart Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp)),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressChart(
                            percentage = overallPercentage,
                            size = 160.dp,
                            strokeWidth = 16.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            BudgetStatItem("Total Budget", CurrencyFormatter.formatPaise(totalBudgetPaise), TextPrimary)
                            BudgetStatItem("Spent", CurrencyFormatter.formatPaise(totalSpentPaise), if (overallPercentage >= 0.8f) ExpenseRed else MintDark)
                            BudgetStatItem(
                                "Remaining",
                                if (remainingPaise >= 0) CurrencyFormatter.formatPaise(remainingPaise) else "-${CurrencyFormatter.formatPaise(-remainingPaise)}",
                                if (remainingPaise >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORY BUDGETS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = "${activeMonthBudgets.size} Categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (activeMonthBudgets.isEmpty()) {
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
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No budget set for ${DateUtils.formatMonthDisplay(activeMonth)}", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Set monthly category limits to control your spending.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddBudgetDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                            ) {
                                Text("Create Category Budget")
                            }
                        }
                    }
                }
            } else {
                items(activeMonthBudgets, key = { it.id }) { budget ->
                    CategoryBudgetCard(
                        budget = budget,
                        onDelete = { budgetToDelete = budget }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp)) // padding for bottom nav
            }
        }
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

    if (budgetToDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete Budget?",
            message = "Remove budget for '${budgetToDelete?.category}'?",
            onDismiss = { budgetToDelete = null },
            onConfirm = {
                budgetToDelete?.id?.let { viewModel.deleteBudget(it) }
                budgetToDelete = null
            }
        )
    }
}

@Composable
private fun BudgetStatItem(label: String, amount: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = amount, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}

@Composable
fun CategoryBudgetCard(
    budget: Budget,
    onDelete: () -> Unit
) {
    val progress = if (budget.limitPaise > 0) {
        (budget.spentPaise.toFloat() / budget.limitPaise.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isWarning = progress >= 0.8f && progress < 1.0f
    val isExceeded = progress >= 1.0f

    val barColor = when {
        isExceeded -> ExpenseRed
        isWarning -> WarningOrange
        else -> MintGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isExceeded) ExpenseRed.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = WhiteCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = budget.category,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    if (isExceeded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ExpenseRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EXCEEDED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = ExpenseRed
                            )
                        }
                    } else if (isWarning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WarningOrange.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "80% REACHED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = WarningOrange
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = barColor,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyFormatter.formatPaise(budget.spentPaise)} of ${CurrencyFormatter.formatPaise(budget.limitPaise)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = barColor
                )
            }
        }
    }
}
