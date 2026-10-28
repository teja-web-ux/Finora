package com.example.auth

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
    val errorOrNull: String? get() = (this as? Invalid)?.errorMessage
}

enum class PasswordStrengthLevel(val score: Float, val label: String) {
    EMPTY(0f, ""),
    WEAK(0.25f, "Too weak"),
    FAIR(0.5f, "Fair"),
    GOOD(0.75f, "Good"),
    STRONG(1.0f, "Strong")
}

object AuthValidation {

    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun validateFullName(name: String): ValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Full name is required")
            trimmed.length < 2 -> ValidationResult.Invalid("Full name must be at least 2 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Invalid("Email address is required")
            !EMAIL_REGEX.matches(trimmed) -> ValidationResult.Invalid("Please enter a valid email address")
            else -> ValidationResult.Valid
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Invalid("Password is required")
            password.length < 6 -> ValidationResult.Invalid("Password must be at least 6 characters")
            else -> ValidationResult.Valid
        }
    }

    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult.Invalid("Please confirm your password")
            password != confirmPassword -> ValidationResult.Invalid("Passwords do not match")
            else -> ValidationResult.Valid
        }
    }

    fun validateTerms(agreed: Boolean): ValidationResult {
        return if (agreed) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("You must accept the Terms of Service & Privacy Policy to continue")
        }
    }

    fun calculatePasswordStrength(password: String): PasswordStrengthLevel {
        if (password.isEmpty()) return PasswordStrengthLevel.EMPTY
        if (password.length < 6) return PasswordStrengthLevel.WEAK

        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 4 -> PasswordStrengthLevel.STRONG
            score == 3 -> PasswordStrengthLevel.GOOD
            score == 2 -> PasswordStrengthLevel.FAIR
            else -> PasswordStrengthLevel.WEAK
        }
    }
}
