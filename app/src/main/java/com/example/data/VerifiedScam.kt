package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verified_scams")
data class VerifiedScam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val content: String,
    val category: String,
    val riskScore: Int,
    val scamPattern: String,
    val recommendedAction: String,
    val isScam: Boolean,
    val keyIndicators: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
