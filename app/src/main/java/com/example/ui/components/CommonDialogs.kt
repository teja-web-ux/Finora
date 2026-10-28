package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Account
import com.example.model.TransactionType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

val STANDARD_CATEGORIES = listOf(
    "Food & Dining",
    "Transport",
    "Shopping",
    "Bills & Utilities",
    "Education",
    "Health",
    "Entertainment",
    "Travel",
    "Salary",
    "Investment",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<Account>,
    initialType: String = TransactionType.EXPENSE.name,
    onDismiss: () -> Unit,
    onSave: (type: String, title: String, amountPaise: Long, category: String, date: String, accountId: String, destAccountId: String?, desc: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var amountRupees by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(STANDARD_CATEGORIES.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var accountExpanded by remember { mutableStateOf(false) }
    var selectedDestAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }
    var destAccountExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    val date = remember { DateUtils.currentDateIso() }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(WhiteCard)
                .padding(20.dp)
                .testTag("add_transaction_dialog")
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle: Income, Expense, Transfer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF2F6))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        TransactionType.EXPENSE.name to "Expense",
                        TransactionType.INCOME.name to "Income",
                        TransactionType.TRANSFER.name to "Transfer"
                    ).forEach { (typeVal, label) ->
                        val isSelected = selectedType == typeVal
                        val activeBg = when (typeVal) {
                            TransactionType.INCOME.name -> IncomeGreen
                            TransactionType.EXPENSE.name -> ExpenseRed
                            else -> DeepNavy
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeBg else Color.Transparent)
                                .clickable { selectedType = typeVal }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) WhiteCard else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount
                OutlinedTextField(
                    value = amountRupees,
                    onValueChange = { amountRupees = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Payee") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown
                if (selectedType != TransactionType.TRANSFER.name) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            STANDARD_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Account Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    val accountName = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account"
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (selectedType == TransactionType.TRANSFER.name) "From Account" else "Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${CurrencyFormatter.formatPaise(acc.balancePaise)})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                // Destination Account Dropdown for Transfer
                if (selectedType == TransactionType.TRANSFER.name) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = destAccountExpanded,
                        onExpandedChange = { destAccountExpanded = it }
                    ) {
                        val destName = accounts.find { it.id == selectedDestAccountId }?.name ?: "Select Destination"
                        OutlinedTextField(
                            value = destName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destAccountExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = destAccountExpanded,
                            onDismissRequest = { destAccountExpanded = false }
                        ) {
                            accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${CurrencyFormatter.formatPaise(acc.balancePaise)})") },
                                    onClick = {
                                        selectedDestAccountId = acc.id
                                        destAccountExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val paise = CurrencyFormatter.rupeesToPaise(amountRupees)
                            val finalCategory = if (selectedType == TransactionType.TRANSFER.name) "Transfer" else selectedCategory
                            val destId = if (selectedType == TransactionType.TRANSFER.name) selectedDestAccountId else null
                            onSave(
                                selectedType,
                                title.ifBlank { if (selectedType == TransactionType.TRANSFER.name) "Transfer" else "Transaction" },
                                paise,
                                finalCategory,
                                date,
                                selectedAccountId,
                                destId,
                                description.ifBlank { null }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_tx_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = WhiteCard)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    month: String,
    onDismiss: () -> Unit,
    onSave: (category: String, limitPaise: Long) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(STANDARD_CATEGORIES.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var limitRupees by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Budget for ${DateUtils.formatMonthDisplay(month)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        STANDARD_CATEGORIES.filter { it != "Salary" && it != "Transfer" }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitRupees,
                    onValueChange = { limitRupees = it },
                    label = { Text("Monthly Limit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paise = CurrencyFormatter.rupeesToPaise(limitRupees)
                    onSave(selectedCategory, paise)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("save_budget_btn")
            ) {
                Text("Create Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, targetAmountPaise: Long, targetDate: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetRupees by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("2027-12-31") }
    var category by remember { mutableStateOf("Savings") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Savings Goal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Vacation, Laptop)") },
                    modifier = Modifier.fillMaxWidth().testTag("goal_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetRupees,
                    onValueChange = { targetRupees = it },
                    label = { Text("Target Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paise = CurrencyFormatter.rupeesToPaise(targetRupees)
                    onSave(name, paise, targetDate, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                modifier = Modifier.testTag("save_goal_btn")
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ContributeGoalDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onContribute: (additionalPaise: Long) -> Unit
) {
    var amountRupees by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add to $goalName", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            OutlinedTextField(
                value = amountRupees,
                onValueChange = { amountRupees = it },
                label = { Text("Contribution Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("contribute_amount_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val paise = CurrencyFormatter.rupeesToPaise(amountRupees)
                    onContribute(paise)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MintDark),
                modifier = Modifier.testTag("confirm_contribute_btn")
            ) {
                Text("Add Contribution", color = WhiteCard)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Text(message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                modifier = Modifier.testTag("confirm_delete_btn")
            ) {
                Text("Delete", color = WhiteCard)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
