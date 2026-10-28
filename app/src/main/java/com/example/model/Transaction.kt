package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

@IgnoreExtraProperties
data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: String = TransactionType.EXPENSE.name,
    val title: String = "",
    val amountPaise: Long = 0L,
    val category: String = "Other",
    val date: String = "",
    val accountId: String = "",
    val destinationAccountId: String? = null,
    val description: String? = null,
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
