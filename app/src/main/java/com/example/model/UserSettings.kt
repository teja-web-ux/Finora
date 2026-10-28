package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserSettings(
    val id: String = "preferences",
    val userId: String = "",
    val currency: String = "INR",
    val hideBalance: Boolean = false,
    val hasDemoData: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
