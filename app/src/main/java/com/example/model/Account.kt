package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AccountType {
    BANK, SAVINGS, WALLET, CARD
}

@IgnoreExtraProperties
data class Account(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val accountType: String = AccountType.BANK.name,
    val balancePaise: Long = 0L,
    val isDefault: Boolean = false,
    val cardNumberMasked: String = "•••• •••• •••• 4892",
    val colorHex: String = "#101D3A",
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
