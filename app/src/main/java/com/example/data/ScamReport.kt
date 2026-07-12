package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "scam_reports")
data class ScamReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val sender: String, // phone number, link, upi id, etc.
    val category: String, // Fake Call, Phishing, Fake Job, UPI Scam, E-commerce, Others
    val description: String,
    val screenshotUri: String? = null,
    val riskScore: Int,
    val scamPattern: String,
    val recommendedAction: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val upvotes: Int = 0,
    val reportedBy: String = "Anonymous",
    val keyIndicators: List<String> = emptyList()
)

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
