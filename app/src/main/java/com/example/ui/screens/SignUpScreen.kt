package com.example.ui.screens

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.AuthManager
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ExpenseAccent
import com.example.ui.theme.LightBg
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCard

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    BackHandler { onBackToWelcome() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    // Password strength calculation
    val passwordStrength = remember(password) {
        when {
            password.isEmpty() -> 0f
            password.length < 6 -> 0.25f
            password.length in 6..7 -> 0.5f
            password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() } -> 1.0f
            else -> 0.75f
        }
    }

    val passwordStrengthLabel = remember(passwordStrength) {
        when {
            passwordStrength == 0f -> ""
            passwordStrength <= 0.25f -> "Too weak"
            passwordStrength <= 0.5f -> "Fair"
            passwordStrength <= 0.75f -> "Good"
            else -> "Strong"
        }
    }

    val passwordStrengthColor = remember(passwordStrength) {
        when {
            passwordStrength <= 0.25f -> ExpenseAccent
            passwordStrength <= 0.5f -> Color(0xFFF59E0B)
            passwordStrength <= 0.75f -> MintDark
            else -> MintGreen
        }
    }

    fun validate(): Boolean {
        var isValid = true
        generalError = null

        val trimmedName = fullName.trim()
        if (trimmedName.isEmpty()) {
            fullNameError = "Full name is required"
            isValid = false
        } else if (trimmedName.length < 2) {
            fullNameError = "Full name must be at least 2 characters"
            isValid = false
        } else {
            fullNameError = null
        }

        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            emailError = "Email address is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }

        if (password.isEmpty()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        if (!agreedToTerms) {
            termsError = "You must agree to the Terms of Service and Privacy Policy"
            isValid = false
        } else {
            termsError = null
        }

        return isValid
    }

    fun submitSignUp() {
        if (isLoading) return
        if (!validate()) return

        focusManager.clearFocus()
        isLoading = true
        generalError = null

        AuthManager.signUpWithEmail(
            fullName = fullName,
            email = email,
            password = password,
            scope = coroutineScope,
            onAuthSuccess = {
                isLoading = false
                onSignUpSuccess()
            },
            onAuthError = { err ->
                isLoading = false
                generalError = err
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .statusBarsPadding()
            .testTag("signup_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Top Nav
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToWelcome,
                    modifier = Modifier.testTag("signup_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepNavy
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.finora_app_logo),
                        contentDescription = "Finora Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Join Finora",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = DeepNavy
                    )
                    Text(
                        text = "Your money, under your complete control",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // General Error Banner
            if (generalError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ExpenseAccent.copy(alpha = 0.10f))
                        .border(1.dp, ExpenseAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("signup_error_banner")
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = ExpenseAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = generalError!!,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = ExpenseAccent,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Input: Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (fullNameError != null) fullNameError = null
                },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Alex Morgan") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = if (fullNameError != null) ExpenseAccent else TextSecondary)
                },
                isError = fullNameError != null,
                supportingText = fullNameError?.let { { Text(it, color = ExpenseAccent) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_fullname_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintDark,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = WhiteCard,
                    unfocusedContainerColor = WhiteCard
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input: Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = { Text("Email Address") },
                placeholder = { Text("name@example.com") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = if (emailError != null) ExpenseAccent else TextSecondary)
                },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = ExpenseAccent) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintDark,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = WhiteCard,
                    unfocusedContainerColor = WhiteCard
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input: Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = { Text("Password") },
                placeholder = { Text("At least 6 characters") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = if (passwordError != null) ExpenseAccent else TextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = ExpenseAccent) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintDark,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = WhiteCard,
                    unfocusedContainerColor = WhiteCard
                )
            )

            // Password strength bar
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { passwordStrength },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = passwordStrengthColor,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = passwordStrengthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = passwordStrengthColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input: Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError != null) confirmPasswordError = null
                },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = if (confirmPasswordError != null) ExpenseAccent else TextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it, color = ExpenseAccent) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submitSignUp() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintDark,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = WhiteCard,
                    unfocusedContainerColor = WhiteCard
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        agreedToTerms = !agreedToTerms
                        if (termsError != null) termsError = null
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = {
                        agreedToTerms = it
                        if (termsError != null) termsError = null
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MintDark,
                        uncheckedColor = if (termsError != null) ExpenseAccent else TextSecondary
                    ),
                    modifier = Modifier.testTag("signup_terms_checkbox")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I agree to Finora's Terms of Service and Privacy Policy. Personal data is isolated under zero-trust rules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (termsError != null) ExpenseAccent else TextSecondary,
                    lineHeight = 18.sp
                )
            }

            if (termsError != null) {
                Text(
                    text = termsError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpenseAccent,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = { submitSignUp() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_submit_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepNavy,
                    contentColor = WhiteCard,
                    disabledContainerColor = DeepNavy.copy(alpha = 0.6f),
                    disabledContentColor = WhiteCard.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MintGreen
                    )
                } else {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider: OR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCBD5E1))
                Text(
                    text = "  OR CONTINUE WITH  ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCBD5E1))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign Up Button
            OutlinedButton(
                onClick = {
                    if (isLoading) return@OutlinedButton
                    isLoading = true
                    generalError = null
                    AuthManager.signInWithGoogle(
                        context = context,
                        credentialManager = credentialManager,
                        scope = coroutineScope,
                        onAuthSuccess = {
                            isLoading = false
                            onSignUpSuccess()
                        },
                        onAuthError = { err ->
                            isLoading = false
                            generalError = err
                        },
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_google_btn"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = WhiteCard,
                    contentColor = DeepNavy
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.finora_app_logo),
                        contentDescription = "Google Sign-In",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign up with Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Switch to Login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.testTag("signup_to_login_btn")
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MintDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
