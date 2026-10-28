package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard

data class NavigationDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

val NAV_DESTINATIONS = listOf(
    NavigationDestination("dashboard", "Home", Icons.Default.Home, "nav_home"),
    NavigationDestination("cards", "Cards", Icons.Default.CreditCard, "nav_cards"),
    NavigationDestination("transfers", "Transfer", Icons.Default.SwapHoriz, "nav_transfers"),
    NavigationDestination("budget", "Budget", Icons.Default.PieChart, "nav_budget"),
    NavigationDestination("profile", "Profile", Icons.Default.Person, "nav_profile")
)

@Composable
fun FinoraBottomNavBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        containerColor = WhiteCard,
        tonalElevation = 8.dp
    ) {
        NAV_DESTINATIONS.forEach { destination ->
            val selected = currentScreen == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkNavy,
                    selectedTextColor = DeepNavy,
                    indicatorColor = MintGreen,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag(destination.testTag)
            )
        }
    }
}
