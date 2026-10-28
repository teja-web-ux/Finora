package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.auth.AuthManager
import com.example.ui.navigation.FinoraBottomNavBar
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PasswordResetScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.FinoraTheme
import com.example.ui.theme.LightBg
import com.example.viewmodel.FinoraViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: FinoraViewModel by viewModels {
        FinoraViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FinoraTheme {
                FinoraApp(viewModel)
            }
        }
    }
}

@Composable
fun FinoraApp(viewModel: FinoraViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var currentAuthSubScreen by remember { mutableStateOf("welcome") } // "welcome", "login", "signup", "reset"

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                viewModel.syncCurrentUserProfile()
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Auto sign-in attempt on launch
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    viewModel.syncCurrentUserProfile()
                },
                onUnauthenticated = { /* stay on welcome */ },
                scope = coroutineScope
            )
        }
    }

    // Show snackbar feedback
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    if (currentUser == null) {
        when (currentAuthSubScreen) {
            "welcome" -> {
                WelcomeScreen(
                    onGetStartedClicked = { currentAuthSubScreen = "signup" },
                    onLoginClicked = { currentAuthSubScreen = "login" }
                )
            }
            "login" -> {
                LoginScreen(
                    onLoginSuccess = {
                        currentUser = Firebase.auth.currentUser
                        viewModel.syncCurrentUserProfile()
                    },
                    onNavigateToSignUp = { currentAuthSubScreen = "signup" },
                    onNavigateToForgotPassword = { currentAuthSubScreen = "reset" },
                    onBackToWelcome = { currentAuthSubScreen = "welcome" }
                )
            }
            "signup" -> {
                SignUpScreen(
                    onSignUpSuccess = {
                        currentUser = Firebase.auth.currentUser
                        viewModel.syncCurrentUserProfile()
                    },
                    onNavigateToLogin = { currentAuthSubScreen = "login" },
                    onBackToWelcome = { currentAuthSubScreen = "welcome" }
                )
            }
            "reset" -> {
                PasswordResetScreen(
                    onNavigateBackToLogin = { currentAuthSubScreen = "login" }
                )
            }
            else -> {
                WelcomeScreen(
                    onGetStartedClicked = { currentAuthSubScreen = "signup" },
                    onLoginClicked = { currentAuthSubScreen = "login" }
                )
            }
        }
    } else {
        val currentScreen by viewModel.currentScreen.collectAsState()

        // Handle system back navigation to return to Dashboard from sub-screens
        if (currentScreen != "dashboard") {
            BackHandler {
                viewModel.navigateTo("dashboard")
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                FinoraBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { destination ->
                        viewModel.navigateTo(destination)
                    }
                )
            },
            containerColor = LightBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBg)
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "cards" -> CardsScreen(viewModel = viewModel)
                    "transfers" -> TransferScreen(viewModel = viewModel)
                    "budget" -> BudgetScreen(viewModel = viewModel)
                    "profile" -> ProfileScreen(
                        viewModel = viewModel,
                        onSignOut = {
                            AuthManager.signOut(
                                context = context,
                                credentialManager = credentialManager,
                                scope = coroutineScope,
                                onSignOutComplete = {
                                    currentUser = null
                                    currentAuthSubScreen = "welcome"
                                    viewModel.navigateTo("dashboard")
                                }
                            )
                        }
                    )
                    "transactions" -> TransactionsScreen(viewModel = viewModel)
                    "goals" -> GoalsScreen(viewModel = viewModel)
                    else -> DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
