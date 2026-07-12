package com.example.data

import kotlinx.coroutines.flow.Flow

class ScamReportRepository(
    private val scamReportDao: ScamReportDao,
    private val geminiService: GeminiService
) {
    val allReports: Flow<List<ScamReport>> = scamReportDao.getAllReports()
    val allVerifiedScams: Flow<List<VerifiedScam>> = scamReportDao.getAllVerifiedScams()

    fun searchReports(query: String): Flow<List<ScamReport>> = scamReportDao.searchReports(query)

    suspend fun getReportBySender(sender: String): ScamReport? = scamReportDao.getReportBySender(sender)

    suspend fun insertReport(report: ScamReport): Long = scamReportDao.insertReport(report)

    suspend fun updateReport(report: ScamReport) = scamReportDao.updateReport(report)

    suspend fun incrementUpvotes(id: Int) = scamReportDao.incrementUpvotes(id)

    suspend fun deleteReport(id: Int) = scamReportDao.deleteReport(id)

    suspend fun clearAllReports() = scamReportDao.clearAllReports()

    suspend fun insertVerifiedScam(scam: VerifiedScam): Long = scamReportDao.insertVerifiedScam(scam)

    suspend fun deleteVerifiedScam(id: Int) = scamReportDao.deleteVerifiedScam(id)

    suspend fun clearAllVerifiedScams() = scamReportDao.clearAllVerifiedScams()

    // Gemini API integrations
    suspend fun analyzeScam(sender: String, content: String, category: String): ScamAnalysisResult {
        return geminiService.analyzeScam(sender, content, category)
    }

    suspend fun chatWithScamExpert(history: List<Content>): String {
        return geminiService.chatWithScamExpert(history)
    }
}
