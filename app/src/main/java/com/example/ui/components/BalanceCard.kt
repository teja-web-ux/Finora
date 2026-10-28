package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MintGreen
import com.example.ui.theme.WhiteCard
import com.example.util.CurrencyFormatter

@Composable
fun BalanceCard(
    combinedBalancePaise: Long,
    monthlyIncomePaise: Long,
    monthlyExpensePaise: Long,
    hideBalance: Boolean,
    onToggleHideBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(DeepNavy, DarkNavy, Color(0xFF14244B))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .border(1.dp, Color(0x3355E6B0), RoundedCornerShape(24.dp))
            .padding(22.dp)
            .testTag("balance_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MintGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOTAL AVAILABLE BALANCE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color(0xFFA5B4CB)
                    )
                }

                IconButton(
                    onClick = onToggleHideBalance,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toggle_hide_balance_btn")
                ) {
                    Icon(
                        imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance Visibility",
                        tint = Color(0xFFA5B4CB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val balanceText = if (hideBalance) {
                "₹ • • • • • •"
            } else {
                CurrencyFormatter.formatPaise(combinedBalancePaise)
            }

            Text(
                text = balanceText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = WhiteCard
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1F32D583))
                        .border(1.dp, Color(0x3332D583), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x3332D583)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Income",
                            tint = IncomeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9EAFC8)
                        )
                        Text(
                            text = if (hideBalance) "••••" else CurrencyFormatter.formatPaise(monthlyIncomePaise),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = IncomeGreen
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1FF16B7A))
                        .border(1.dp, Color(0x33F16B7A), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x33F16B7A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Expenses",
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9EAFC8)
                        )
                        Text(
                            text = if (hideBalance) "••••" else CurrencyFormatter.formatPaise(monthlyExpensePaise),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExpenseRed
                        )
                    }
                }
            }
        }
    }
}
