package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthManager {

    private const val TAG = "AuthManager"

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (Firebase.auth.currentUser != null) {
            onAuthSuccess()
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (_: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val activity = context as? Activity ?: run {
                    onAuthError("Context is not an Activity")
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onAuthError("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Sign-in failed. Please try again.")
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit
    ) {
        scope.launch {
            try {
                Firebase.auth.signInWithEmailAndPassword(email.trim(), password).await()
                onAuthSuccess()
            } catch (e: FirebaseAuthInvalidUserException) {
                onAuthError("No account found with this email. Please check your email or sign up.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onAuthError("Invalid credentials. Please verify your email and password.")
            } catch (e: FirebaseAuthException) {
                val msg = when {
                    e.errorCode.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                    e.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ||
                    e.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ->
                        "Email/Password sign-in is not enabled in Firebase Console. Please enable Email/Password provider under Authentication > Sign-in method, or sign in with Google."
                    e.errorCode.contains("USER_DISABLED", ignoreCase = true) ->
                        "This account has been disabled. Please contact support."
                    else -> e.localizedMessage ?: "Authentication failed: ${e.message}"
                }
                onAuthError(msg)
            } catch (e: Exception) {
                Log.e(TAG, "Email sign-in failed", e)
                onAuthError(e.localizedMessage ?: "Sign-in failed. Please check network connection.")
            }
        }
    }

    fun signUpWithEmail(
        fullName: String,
        email: String,
        password: String,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val authResult = Firebase.auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val user = authResult.user
                if (user != null && fullName.isNotBlank()) {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName.trim())
                        .build()
                    try {
                        user.updateProfile(profileUpdate).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to set display name on new user profile: ${e.message}")
                    }
                }
                onAuthSuccess()
            } catch (e: FirebaseAuthUserCollisionException) {
                onAuthError("An account with this email already exists. Please log in instead.")
            } catch (e: FirebaseAuthWeakPasswordException) {
                onAuthError("Password is too weak. Please use at least 6 characters with mixed letters and numbers.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onAuthError("Please provide a valid email address.")
            } catch (e: FirebaseAuthException) {
                val msg = when {
                    e.errorCode.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                    e.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ||
                    e.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ->
                        "Email/Password provider is not enabled in Firebase Console. Please enable Email/Password under Authentication > Sign-in method, or sign up with Google."
                    else -> e.localizedMessage ?: "Registration failed: ${e.message}"
                }
                onAuthError(msg)
            } catch (e: Exception) {
                Log.e(TAG, "Email sign-up failed", e)
                onAuthError(e.localizedMessage ?: "Account creation failed. Please check your network and try again.")
            }
        }
    }

    fun sendPasswordReset(
        email: String,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                Firebase.auth.sendPasswordResetEmail(email.trim()).await()
                onSuccess()
            } catch (e: FirebaseAuthInvalidUserException) {
                onError("No registered user found with this email address.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onError("Please provide a valid email address.")
            } catch (e: FirebaseAuthException) {
                val msg = when {
                    e.errorCode.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                    e.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ||
                    e.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ->
                        "Password reset is not available until Email/Password provider is enabled in Firebase Console."
                    else -> e.localizedMessage ?: "Could not send reset email: ${e.message}"
                }
                onError(msg)
            } catch (e: Exception) {
                Log.e(TAG, "Password reset failed", e)
                onError(e.localizedMessage ?: "Failed to send reset link. Please check network connection.")
            }
        }
    }

    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSignOutComplete: () -> Unit
    ) {
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
