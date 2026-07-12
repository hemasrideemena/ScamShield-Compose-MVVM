package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScamReportDao {
    @Query("SELECT * FROM scam_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ScamReport>>

    @Query("SELECT * FROM scam_reports WHERE sender LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchReports(query: String): Flow<List<ScamReport>>

    @Query("SELECT * FROM scam_reports WHERE sender = :sender LIMIT 1")
    suspend fun getReportBySender(sender: String): ScamReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScamReport): Long

    @Update
    suspend fun updateReport(report: ScamReport)

    @Query("UPDATE scam_reports SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun incrementUpvotes(id: Int)

    @Query("DELETE FROM scam_reports WHERE id = :id")
    suspend fun deleteReport(id: Int)

    @Query("DELETE FROM scam_reports")
    suspend fun clearAllReports()

    // --- Verified Scam / Recently Checked Operations ---
    @Query("SELECT * FROM verified_scams ORDER BY timestamp DESC")
    fun getAllVerifiedScams(): Flow<List<VerifiedScam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerifiedScam(scam: VerifiedScam): Long

    @Query("DELETE FROM verified_scams WHERE id = :id")
    suspend fun deleteVerifiedScam(id: Int)

    @Query("DELETE FROM verified_scams")
    suspend fun clearAllVerifiedScams()
}

