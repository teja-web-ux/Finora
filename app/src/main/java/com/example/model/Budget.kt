package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Budget(
    val id: String = "",
    val userId: String = "",
    val month: String = "", // e.g. "2026-10"
    val category: String = "",
    val limitPaise: Long = 0L,
    val spentPaise: Long = 0L,
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
