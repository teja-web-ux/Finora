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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.Goal
import com.example.model.GoalStatus
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.ContributeGoalDialog
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightBg
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.viewmodel.FinoraViewModel

@Composable
fun GoalsScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalForContribution by remember { mutableStateOf<Goal?>(null) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    val totalTargetPaise = remember(goals) { goals.sumOf { it.targetAmountPaise } }
    val totalSavedPaise = remember(goals) { goals.sumOf { it.savedAmountPaise } }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("goals_screen"),
        containerColor = LightBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = DeepNavy,
                contentColor = MintGreen,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Savings Goals",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Plan and track future financial milestones",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x1F24B99A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = MintDark, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Total Savings Glance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "TOTAL SAVED ACROSS GOALS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = CurrencyFormatter.formatPaise(totalSavedPaise), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MintDark)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "TARGET TOTAL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = CurrencyFormatter.formatPaise(totalTargetPaise), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepNavy)
                        }
                    }
                }
            }

            if (goals.isEmpty()) {
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
                            Text("No savings goals active", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Set up dedicated targets like Emergency Fund, Vacation, or New Gadget.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                            ) {
                                Text("Create First Goal")
                            }
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    GoalCardItem(
                        goal = goal,
                        onContribute = { goalForContribution = goal },
                        onDelete = { goalToDelete = goal }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
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

    if (goalForContribution != null) {
        ContributeGoalDialog(
            goalName = goalForContribution?.name ?: "Goal",
            onDismiss = { goalForContribution = null },
            onContribute = { amount ->
                goalForContribution?.id?.let { viewModel.contributeToGoal(it, amount) }
                goalForContribution = null
            }
        )
    }

    if (goalToDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete Savings Goal?",
            message = "Are you sure you want to remove '${goalToDelete?.name}'?",
            onDismiss = { goalToDelete = null },
            onConfirm = {
                goalToDelete?.id?.let { viewModel.deleteGoal(it) }
                goalToDelete = null
            }
        )
    }
}

@Composable
fun GoalCardItem(
    goal: Goal,
    onContribute: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmountPaise > 0) {
        (goal.savedAmountPaise.toFloat() / goal.targetAmountPaise.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isCompleted = goal.savedAmountPaise >= goal.targetAmountPaise && goal.targetAmountPaise > 0
    val remainingPaise = (goal.targetAmountPaise - goal.savedAmountPaise).coerceAtLeast(0L)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = WhiteCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val statusColor = if (isCompleted) MintDark else Color(0xFF64748B)
                    val statusText = if (isCompleted) "COMPLETED" else "ACTIVE"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = statusColor
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isCompleted) MintDark else MintGreen,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${CurrencyFormatter.formatPaise(goal.savedAmountPaise)} saved",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Target: ${CurrencyFormatter.formatPaise(goal.targetAmountPaise)} • Remaining: ${CurrencyFormatter.formatPaise(remainingPaise)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MintDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Target Date: ${goal.targetDate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }

                if (!isCompleted) {
                    Button(
                        onClick = onContribute,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("contribute_goal_${goal.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MintGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Funds", color = WhiteCard, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
