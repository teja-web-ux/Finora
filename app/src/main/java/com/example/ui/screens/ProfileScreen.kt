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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Account
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LightBg
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter
import com.example.viewmodel.FinoraViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(
    viewModel: FinoraViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val hideBalance by viewModel.hideBalance.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val user = Firebase.auth.currentUser
    val displayName = profile?.fullName?.ifBlank { user?.displayName ?: "Finora Member" } ?: "Finora Member"
    val userEmail = profile?.email?.ifBlank { user?.email ?: "user@finora.internal" } ?: "user@finora.internal"
    val initials = displayName.take(1).uppercase()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("profile_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Profile & Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // User Identity Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(DeepNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MintGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MintDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tracked Financial Accounts Card
            Text(
                text = "TRACKED ACCOUNTS (${accounts.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (accounts.isEmpty()) {
                        Text(
                            text = "No accounts registered. Add a bank, wallet, or savings account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        accounts.forEachIndexed { index, acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x1F101D3A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = acc.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                        Text(text = acc.accountType, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }

                                Text(
                                    text = CurrencyFormatter.formatPaise(acc.balancePaise),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MintDark
                                )
                            }
                            if (index < accounts.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showAddAccountDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Another Account")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Data Management Card
            Text(
                text = "DEMONSTRATION DATA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Populate your database with sample accounts, budgets, and transactions for demonstration and evaluation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.loadDemoData() },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("load_demo_data_btn")
                        ) {
                            Text("Load Demo Data", color = WhiteCard, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearDemoData() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("clear_demo_data_btn")
                        ) {
                            Text("Clear Demo Data", color = ExpenseRed, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences Card
            Text(
                text = "PREFERENCES & SECURITY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Hide Balance Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Hide Balances by Default", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                Text(text = "Mask numbers for visual privacy", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = hideBalance,
                            onCheckedChange = { viewModel.toggleHideBalance() },
                            colors = SwitchDefaults.colors(checkedThumbColor = WhiteCard, checkedTrackColor = MintDark)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

                    // Zero-Trust Security info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MintDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Zero-Trust Firestore Security", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text(text = "All personal financial records are protected by UID ownership rules", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

                    // About Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "About Finora", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout & Account Actions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Sign out
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showLogoutConfirmDialog = true }
                            .padding(vertical = 8.dp)
                            .testTag("logout_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = DeepNavy)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Sign Out", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepNavy)
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    // Delete Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDeleteAccountDialog = true }
                            .padding(vertical = 8.dp)
                            .testTag("delete_account_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Delete Account & Clean Data", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(displayName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfileName(editName)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        var accName by remember { mutableStateOf("") }
        var accBalance by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Add Tracked Account", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = accBalance,
                        onValueChange = { accBalance = it },
                        label = { Text("Initial Balance (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paise = CurrencyFormatter.rupeesToPaise(accBalance)
                        viewModel.addOrUpdateAccount(accName, "BANK", paise, "#101D3A")
                        showAddAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Add Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Sign Out of Finora?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("You will be returned to the Welcome screen. Your Cloud Firestore data remains securely stored.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    modifier = Modifier.testTag("confirm_signout_btn")
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Permanently Delete Account?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ExpenseRed) },
            text = { Text("This will permanently remove all your tracked accounts, transactions, budgets, and savings goals from Cloud Firestore. This cannot be undone.", color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccountAndData(onDeleted = onSignOut)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_delete_account_btn")
                ) {
                    Text("Delete Everything", color = WhiteCard)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Finora Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Finora — Your Money, in Your Control", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 1.0 (Production)", fontWeight = FontWeight.SemiBold, color = DeepNavy)
                    Text("A native Android fintech management application built with Kotlin, Jetpack Compose, Material 3, and Firebase Firestore.", style = MaterialTheme.typography.bodySmall)
                    Text("Features integer paise precision arithmetic, atomic internal transfers, monthly budget progress alarms, and zero-trust security rules.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)) {
                    Text("Close")
                }
            }
        )
    }
}
