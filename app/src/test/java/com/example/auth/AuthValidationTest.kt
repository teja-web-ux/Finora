package com.example.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun validateEmail_withValidAddresses_returnsValid() {
        val validEmails = listOf(
            "user@finora.com",
            "first.last@domain.co.uk",
            "test_123@example.org"
        )
        for (email in validEmails) {
            val result = AuthValidation.validateEmail(email)
            assertTrue("Expected valid for $email", result.isValid)
            assertNull(result.errorOrNull)
        }
    }

    @Test
    fun validateEmail_withInvalidAddresses_returnsInvalid() {
        val invalidEmails = listOf(
            "",
            "   ",
            "notanemail",
            "@missinguser.com",
            "user@.com",
            "user@domain"
        )
        for (email in invalidEmails) {
            val result = AuthValidation.validateEmail(email)
            assertFalse("Expected invalid for $email", result.isValid)
            assertTrue(result.errorOrNull?.isNotEmpty() == true)
        }
    }

    @Test
    fun validatePassword_lengthChecks() {
        assertFalse(AuthValidation.validatePassword("").isValid)
        assertFalse(AuthValidation.validatePassword("12345").isValid)
        assertTrue(AuthValidation.validatePassword("123456").isValid)
        assertTrue(AuthValidation.validatePassword("FinoraPassword2026!").isValid)
    }

    @Test
    fun validatePasswordConfirmation_matchesCorrectly() {
        assertFalse(AuthValidation.validatePasswordConfirmation("pass123", "").isValid)
        assertFalse(AuthValidation.validatePasswordConfirmation("pass123", "pass456").isValid)
        assertTrue(AuthValidation.validatePasswordConfirmation("pass123", "pass123").isValid)
    }

    @Test
    fun validateFullName_enforcesMinimumLength() {
        assertFalse(AuthValidation.validateFullName("").isValid)
        assertFalse(AuthValidation.validateFullName("a").isValid)
        assertTrue(AuthValidation.validateFullName("Al").isValid)
        assertTrue(AuthValidation.validateFullName("John Doe").isValid)
    }

    @Test
    fun validateTerms_requiresConsent() {
        assertFalse(AuthValidation.validateTerms(false).isValid)
        assertTrue(AuthValidation.validateTerms(true).isValid)
    }

    @Test
    fun calculatePasswordStrength_ranksAppropriately() {
        assertEquals(PasswordStrengthLevel.EMPTY, AuthValidation.calculatePasswordStrength(""))
        assertEquals(PasswordStrengthLevel.WEAK, AuthValidation.calculatePasswordStrength("12345"))
        assertEquals(PasswordStrengthLevel.STRONG, AuthValidation.calculatePasswordStrength("FinoraSecure123!"))
    }
}
