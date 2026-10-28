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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Account
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightBg
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.viewmodel.FinoraViewModel

data class QuickContact(val name: String, val initials: String, val color: Color)

val QUICK_CONTACTS = listOf(
    QuickContact("Rahul Sharma", "RS", Color(0xFF6366F1)),
    QuickContact("Priya Patel", "PP", Color(0xFFEC4899)),
    QuickContact("Alex Morgan", "AM", Color(0xFF10B981)),
    QuickContact("Mom", "M", Color(0xFFF59E0B)),
    QuickContact("Landlord", "L", Color(0xFF8B5CF6))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()

    var transferMode by remember { mutableStateOf("INTERNAL") } // "INTERNAL" or "CONTACT"
    var selectedFromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedToAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }
    var recipientName by remember { mutableStateOf("") }
    var amountRupees by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSuccessReceipt by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("transfer_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transfers & Payments",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Record atomic account-to-account transfers and outgoing payment logs.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (transferMode == "INTERNAL") DeepNavy else Color.Transparent)
                        .clickable { transferMode = "INTERNAL" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_internal_transfer"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Between Accounts",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (transferMode == "INTERNAL") WhiteCard else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (transferMode == "CONTACT") DeepNavy else Color.Transparent)
                        .clickable { transferMode = "CONTACT" }
                        .padding(vertical = 10.dp)
                        .testTag("tab_contact_payment"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Record Payment",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (transferMode == "CONTACT") WhiteCard else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Transfer Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Source Account Dropdown
                    Text(
                        text = "FROM ACCOUNT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val fromAcc = accounts.find { it.id == selectedFromAccountId }
                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = fromAcc?.let { "${it.name} (${CurrencyFormatter.formatPaise(it.balancePaise)})" } ?: "Select Source Account",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${CurrencyFormatter.formatPaise(acc.balancePaise)})") },
                                    onClick = {
                                        selectedFromAccountId = acc.id
                                        fromExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (transferMode == "INTERNAL") {
                        // Destination Account Dropdown
                        Text(
                            text = "TO ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val toAcc = accounts.find { it.id == selectedToAccountId }
                        ExposedDropdownMenuBox(
                            expanded = toExpanded,
                            onExpandedChange = { toExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = toAcc?.let { "${it.name} (${CurrencyFormatter.formatPaise(it.balancePaise)})" } ?: "Select Destination Account",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = toExpanded,
                                onDismissRequest = { toExpanded = false }
                            ) {
                                accounts.filter { it.id != selectedFromAccountId }.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${CurrencyFormatter.formatPaise(acc.balancePaise)})") },
                                        onClick = {
                                            selectedToAccountId = acc.id
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Quick Contacts Selector
                        Text(
                            text = "QUICK CONTACTS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(QUICK_CONTACTS) { contact ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { recipientName = contact.name }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(contact.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.initials,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = WhiteCard
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = contact.name.substringBefore(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient / Payee Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transfer_recipient_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Input
                    Text(
                        text = "TRANSFER AMOUNT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = amountRupees,
                        onValueChange = { amountRupees = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes Input
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Purpose (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Review & Confirmation Button
                    Button(
                        onClick = {
                            val paise = CurrencyFormatter.rupeesToPaise(amountRupees)
                            if (paise > 0) {
                                showConfirmationDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("review_transfer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (transferMode == "INTERNAL") "Review Account Transfer" else "Record Payment",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = WhiteCard
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Account-to-account transfers update balances atomically using Firestore transactions without affecting your combined net cash flow.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Confirmation Sheet Dialog
    if (showConfirmationDialog) {
        val paise = CurrencyFormatter.rupeesToPaise(amountRupees)
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = if (transferMode == "INTERNAL") "Confirm Account Transfer" else "Confirm Payment Record",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Amount: ${CurrencyFormatter.formatPaise(paise)}", fontWeight = FontWeight.Bold, color = DeepNavy)
                    if (transferMode == "INTERNAL") {
                        val fromName = accounts.find { it.id == selectedFromAccountId }?.name ?: "Source"
                        val toName = accounts.find { it.id == selectedToAccountId }?.name ?: "Destination"
                        Text("From: $fromName", style = MaterialTheme.typography.bodyMedium)
                        Text("To: $toName", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("Payee: $recipientName", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (notes.isNotBlank()) {
                        Text("Note: $notes", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val date = DateUtils.currentDateIso()
                        if (transferMode == "INTERNAL") {
                            viewModel.addTransaction(
                                type = "TRANSFER",
                                title = "Transfer to ${accounts.find { it.id == selectedToAccountId }?.name ?: "Account"}",
                                amountPaise = paise,
                                category = "Transfer",
                                date = date,
                                accountId = selectedFromAccountId,
                                destinationAccountId = selectedToAccountId,
                                description = notes.ifBlank { null }
                            )
                        } else {
                            viewModel.addTransaction(
                                type = "EXPENSE",
                                title = recipientName.ifBlank { "Payment" },
                                amountPaise = paise,
                                category = "Other",
                                date = date,
                                accountId = selectedFromAccountId,
                                description = notes.ifBlank { null }
                            )
                        }
                        showConfirmationDialog = false
                        showSuccessReceipt = true
                        amountRupees = ""
                        notes = ""
                        recipientName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintDark),
                    modifier = Modifier.testTag("confirm_transfer_btn")
                ) {
                    Text("Confirm & Record", color = WhiteCard)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Receipt Dialog
    if (showSuccessReceipt) {
        AlertDialog(
            onDismissRequest = { showSuccessReceipt = false },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintDark, modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Transaction Recorded", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Text(
                    "Your transaction has been securely saved to Cloud Firestore and account balances have been synchronized.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessReceipt = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Done")
                }
            }
        )
    }
}
