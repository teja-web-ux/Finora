package com.example.ui.screens

import android.util.Patterns
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun PasswordResetScreen(
    initialEmail: String = "",
    onNavigateBackToLogin: () -> Unit
) {
    BackHandler { onNavigateBackToLogin() }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf(initialEmail) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            emailError = "Email address is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            emailError = "Please enter a valid email address"
            return false
        }
        emailError = null
        return true
    }

    fun submitReset() {
        if (isLoading) return
        if (!validate()) return

        focusManager.clearFocus()
        isLoading = true
        generalError = null

        AuthManager.sendPasswordReset(
            email = email,
            scope = coroutineScope,
            onSuccess = {
                isLoading = false
                isSuccess = true
            },
            onError = { err ->
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
            .testTag("password_reset_screen")
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
                    onClick = onNavigateBackToLogin,
                    modifier = Modifier.testTag("reset_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepNavy
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (!isSuccess) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Reset",
                        tint = MintGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Forgot Your Password?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = DeepNavy
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter the email associated with your Finora account and we will send you a secure link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Error Banner
                if (generalError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ExpenseAccent.copy(alpha = 0.10f))
                            .border(1.dp, ExpenseAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                            .testTag("reset_error_banner")
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

                // Input: Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Account Email") },
                    placeholder = { Text("name@example.com") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = if (emailError != null) ExpenseAccent else TextSecondary
                        )
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = ExpenseAccent) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitReset() }
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintDark,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = WhiteCard,
                        unfocusedContainerColor = WhiteCard
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = { submitReset() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("reset_submit_btn"),
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
                            text = "Send Reset Link",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onNavigateBackToLogin,
                        modifier = Modifier.testTag("reset_to_login_btn")
                    ) {
                        Text(
                            text = "Remember your password? Sign In",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MintDark
                        )
                    }
                }
            } else {
                // Success View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(WhiteCard)
                        .border(1.5.dp, MintDark.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(24.dp)
                        .testTag("reset_success_card"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MintGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = MintDark,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Reset Link Dispatched!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = DeepNavy,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "We have sent password reset instructions to:\n$email\n\nPlease check your inbox (and spam folder) and follow the link to establish your new password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onNavigateBackToLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reset_success_back_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepNavy,
                                contentColor = WhiteCard
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Return to Sign In",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        TextButton(
                            onClick = {
                                isSuccess = false
                                submitReset()
                            },
                            modifier = Modifier.testTag("reset_resend_btn")
                        ) {
                            Text(
                                text = "Didn't receive email? Try again",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MintDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
