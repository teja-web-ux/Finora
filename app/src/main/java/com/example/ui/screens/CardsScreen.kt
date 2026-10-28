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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Account
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

@Composable
fun CardsScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val hideBalance by viewModel.hideBalance.collectAsState()

    var isCardLocked by remember { mutableStateOf(false) }
    var showManageLimitsDialog by remember { mutableStateOf(false) }
    var dailyLimitRupees by remember { mutableFloatStateOf(50000f) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    val activeAccount = accounts.firstOrNull()
    val cardholderName = profile?.fullName?.ifBlank { "ALEXANDER CHEN" }?.uppercase() ?: "ALEXANDER CHEN"
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("cards_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Cards",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Button(
                    onClick = { showAddAccountDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_card_account_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MintGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Card", color = WhiteCard, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Navy Gradient Virtual Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isCardLocked) listOf(Color(0xFF334155), Color(0xFF1E293B))
                            else listOf(DeepNavy, DarkNavy, Color(0xFF172952))
                        )
                    )
                    .border(1.5.dp, if (isCardLocked) Color(0xFF64748B) else MintGreen.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(22.dp)
                    .testTag("virtual_card_view")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: Brand & Contactless
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FINORA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp
                                ),
                                color = WhiteCard
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MintGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isCardLocked) "LOCKED" else "VIRTUAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isCardLocked) Color(0xFFF16B7A) else MintGreen
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Contactless",
                            tint = Color(0xFFA5B4CB),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Card Number
                    Column {
                        Text(
                            text = activeAccount?.cardNumberMasked ?: "•••• •••• •••• 4892",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            ),
                            color = WhiteCard
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hideBalance) "Balance: ••••" else "Balance: ${CurrencyFormatter.formatPaise(activeAccount?.balancePaise ?: 7450000L)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MintGreen
                        )
                    }

                    // Bottom Row: Holder Name & Expiry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "CARD HOLDER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = cardholderName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = WhiteCard
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXPIRES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "10/29",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = WhiteCard
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Management Controls
            Text(
                text = "CARD CONTROLS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = WhiteCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Lock Card Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isCardLocked) Color(0x1FF16B7A) else Color(0x1F24B99A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCardLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (isCardLocked) ExpenseRed else MintDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isCardLocked) "Card is Locked" else "Lock Virtual Card",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isCardLocked) "Transactions are paused" else "Temporarily disable spending",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isCardLocked,
                            onCheckedChange = { isCardLocked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WhiteCard,
                                checkedTrackColor = ExpenseRed,
                                uncheckedThumbColor = WhiteCard,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.testTag("lock_card_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Manage Limits Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showManageLimitsDialog = true }
                            .padding(vertical = 8.dp)
                            .testTag("manage_limits_btn"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1F101D3A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Manage Spending Limits",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Daily limit: ₹${dailyLimitRupees.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Disclaimer Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice",
                        tint = DeepNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Finora is a personal finance bookkeeping and budgeting application, not a card issuer. Displayed card numbers and balances are synthetic demonstration records for personal tracking only.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Manage Limits Dialog
    if (showManageLimitsDialog) {
        AlertDialog(
            onDismissRequest = { showManageLimitsDialog = false },
            title = {
                Text("Spending Limit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column {
                    Text(
                        text = "Set daily online spending limit: ₹${dailyLimitRupees.toInt()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = dailyLimitRupees,
                        onValueChange = { dailyLimitRupees = it },
                        valueRange = 5000f..200000f,
                        steps = 38,
                        colors = SliderDefaults.colors(
                            thumbColor = MintDark,
                            activeTrackColor = MintDark,
                            inactiveTrackColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showManageLimitsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManageLimitsDialog = false }) {
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
                        label = { Text("Account Name (e.g. HDFC Salary)") },
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
}
