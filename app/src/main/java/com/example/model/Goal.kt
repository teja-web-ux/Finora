package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class GoalStatus {
    ACTIVE, COMPLETED, PAUSED
}

@IgnoreExtraProperties
data class Goal(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val targetAmountPaise: Long = 0L,
    val savedAmountPaise: Long = 0L,
    val targetDate: String = "",
    val category: String = "Savings",
    val status: String = GoalStatus.ACTIVE.name,
    val isDemo: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
