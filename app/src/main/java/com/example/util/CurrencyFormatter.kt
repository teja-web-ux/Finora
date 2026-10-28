package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats integer paise into INR currency display (e.g. 50000 paise -> ₹500.00 or ₹500)
     */
    fun formatPaise(paise: Long, includeDecimals: Boolean = false): String {
        val rupees = paise / 100.0
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
        format.maximumFractionDigits = if (includeDecimals) 2 else 0
        format.minimumFractionDigits = if (includeDecimals) 2 else 0
        val formatted = format.format(rupees)
        // Clean currency symbol if needed to ensure ₹
        return if (!formatted.startsWith("₹")) {
            "₹" + formatted.replace(Regex("[^0-9,.-]"), "").trim()
        } else {
            formatted
        }
    }

    /**
     * Converts rupees string (e.g. "450" or "450.50") to integer paise.
     */
    fun rupeesToPaise(rupeesInput: String): Long {
        val cleaned = rupeesInput.replace(",", "").trim()
        val value = cleaned.toDoubleOrNull() ?: 0.0
        return (value * 100).toLong()
    }

    /**
     * Converts paise to clean rupees string for text fields.
     */
    fun paiseToRupeesString(paise: Long): String {
        val rupees = paise / 100.0
        return if (rupees % 1.0 == 0.0) {
            rupees.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", rupees)
        }
    }
}
