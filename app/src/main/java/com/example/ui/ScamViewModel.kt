package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface VerifyUiState {
    object Idle : VerifyUiState
    object Loading : VerifyUiState
    data class Success(val result: ScamAnalysisResult) : VerifyUiState
    data class Error(val message: String) : VerifyUiState
}

sealed interface ReportUiState {
    object Idle : ReportUiState
    object Loading : ReportUiState
    data class Success(val savedReport: ScamReport) : ReportUiState
    data class Error(val message: String) : ReportUiState
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ScamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScamReportRepository
    val allReports: StateFlow<List<ScamReport>>
    val verifiedScams: StateFlow<List<VerifiedScam>>
    
    // Search query and filtered results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredReports: StateFlow<List<ScamReport>>

    // User authentication state
    private val _currentUser = MutableStateFlow<String?>(null) // Holds user email
    val currentUser = _currentUser.asStateFlow()

    // Chatbot state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Hello! I am your ScamShield Assistant. You can ask me about any suspicious messages, job offers, or vishing calls you've received. How can I help secure your digital space today?", false)
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    private val geminiChatHistory = mutableListOf<Content>()

    // Scam Verification state
    private val _verifyState = MutableStateFlow<VerifyUiState>(VerifyUiState.Idle)
    val verifyState = _verifyState.asStateFlow()

    // Report Scam state
    private val _reportState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val reportState = _reportState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        val geminiService = GeminiService()
        repository = ScamReportRepository(database.scamReportDao(), geminiService)
        
        allReports = repository.allReports
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        verifiedScams = repository.allVerifiedScams
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        filteredReports = combine(allReports, _searchQuery) { reports, query ->
            if (query.isBlank()) {
                reports
            } else {
                reports.filter {
                    it.sender.contains(query, ignoreCase = true) ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Add system instruction context to gemini history
        geminiChatHistory.add(
            Content(parts = listOf(Part("Hello, I need some cybersecurity scam guidance.")), role = "user")
        )
        geminiChatHistory.add(
            Content(parts = listOf(Part("Hello! I am your ScamShield Assistant. Ask me anything.")), role = "model")
        )
    }

    // --- Authentication Actions ---
    fun signIn(email: String) {
        viewModelScope.launch {
            _currentUser.value = email
        }
    }

    fun signOut() {
        _currentUser.value = null
    }

    // --- Search Action ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Community Interactivity ---
    fun upvoteReport(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementUpvotes(id)
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReport(id)
        }
    }

    // --- Scam Verification (High Thinking Mode) ---
    fun verifyThreat(sender: String, content: String, category: String) {
        viewModelScope.launch {
            _verifyState.value = VerifyUiState.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.analyzeScam(sender, content, category)
                }
                _verifyState.value = VerifyUiState.Success(result)
                
                // Save analyzed results locally to state-managed database (Recently Checked history)
                withContext(Dispatchers.IO) {
                    repository.insertVerifiedScam(
                        VerifiedScam(
                            sender = sender,
                            content = content,
                            category = category,
                            riskScore = result.riskScore,
                            scamPattern = result.scamPattern,
                            recommendedAction = result.recommendedAction,
                            isScam = result.isScam,
                            keyIndicators = result.keyIndicators
                        )
                    )
                }
            } catch (e: Exception) {
                _verifyState.value = VerifyUiState.Error(e.message ?: "An unknown error occurred during threat analysis.")
            }
        }
    }

    fun deleteVerifiedScam(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVerifiedScam(id)
        }
    }

    fun clearAllVerifiedScams() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllVerifiedScams()
        }
    }

    fun resetVerification() {
        _verifyState.value = VerifyUiState.Idle
    }

    // --- Submit New Report ---
    fun reportScam(
        title: String,
        sender: String,
        category: String,
        description: String,
        screenshotUri: String? = null
    ) {
        viewModelScope.launch {
            _reportState.value = ReportUiState.Loading
            try {
                // First use AI to predict risk score and generate key indicators in real time
                val analysis = withContext(Dispatchers.IO) {
                    repository.analyzeScam(sender, description, category)
                }

                val newReport = ScamReport(
                    title = title,
                    sender = sender,
                    category = category,
                    description = description,
                    screenshotUri = screenshotUri,
                    riskScore = analysis.riskScore,
                    scamPattern = analysis.scamPattern,
                    recommendedAction = analysis.recommendedAction,
                    reportedBy = _currentUser.value ?: "Anonymous",
                    keyIndicators = analysis.keyIndicators,
                    isVerified = analysis.riskScore >= 75
                )

                withContext(Dispatchers.IO) {
                    repository.insertReport(newReport)
                }

                _reportState.value = ReportUiState.Success(newReport)
            } catch (e: Exception) {
                e.printStackTrace()
                // In case of any network or processing failure, save with simulated safety calculation
                val mockAnalysis = runLocalSimulation(sender, description, category)
                val fallbackReport = ScamReport(
                    title = title,
                    sender = sender,
                    category = category,
                    description = description,
                    screenshotUri = screenshotUri,
                    riskScore = mockAnalysis.riskScore,
                    scamPattern = mockAnalysis.scamPattern,
                    recommendedAction = mockAnalysis.recommendedAction,
                    reportedBy = _currentUser.value ?: "Anonymous",
                    keyIndicators = mockAnalysis.keyIndicators,
                    isVerified = mockAnalysis.riskScore >= 75
                )

                withContext(Dispatchers.IO) {
                    repository.insertReport(fallbackReport)
                }
                _reportState.value = ReportUiState.Success(fallbackReport)
            }
        }
    }

    fun resetReportState() {
        _reportState.value = ReportUiState.Idle
    }

    // --- Chatbot Actions ---
    fun sendMessageToChat(text: String) {
        if (text.isBlank()) return

        // Append user message to UI state
        val userMsg = ChatMessage(text, true)
        _chatMessages.value = _chatMessages.value + userMsg

        // Prepare Gemini history
        geminiChatHistory.add(Content(parts = listOf(Part(text)), role = "user"))

        viewModelScope.launch {
            // Add placeholder message for "typing" effect
            val typingMsg = ChatMessage("Scanning databases...", false)
            _chatMessages.value = _chatMessages.value + typingMsg

            val responseText = withContext(Dispatchers.IO) {
                repository.chatWithScamExpert(geminiChatHistory)
            }

            // Remove typing message and add real model response
            _chatMessages.value = _chatMessages.value.filter { it != typingMsg } + ChatMessage(responseText, false)
            geminiChatHistory.add(Content(parts = listOf(Part(responseText)), role = "model"))
        }
    }

    private fun runLocalSimulation(sender: String, content: String, category: String): ScamAnalysisResult {
        val lowerContent = content.lowercase()
        val indicators = mutableListOf<String>()
        var score = 30
        var pattern = "This transaction/message appears to show low immediate scam markers, but exercise routine caution."
        var action = "Verify the recipient and do not click suspicious external attachments."

        if (lowerContent.contains("free") || lowerContent.contains("win") || lowerContent.contains("lottery")) {
            score = 88
            indicators.add("Unsolicited giveaway claim")
            pattern = "Prize/award lure: Coerces victims to deposit processing or commission fees to unlock high-value prizes."
            action = "Do not transact or send any advance payments."
        } else if (lowerContent.contains("otp") || lowerContent.contains("block") || lowerContent.contains("kyc")) {
            score = 97
            indicators.add("Credential request (OTP/PIN)")
            indicators.add("High urgency intimidation")
            pattern = "Utility / Banking Fraud: Leverages fear of financial lockouts to extract crucial multi-factor authentication tokens."
            action = "Block number immediately. Official services will never request UPI PINs or OTPs over call or chat."
        }

        return ScamAnalysisResult(
            riskScore = score,
            scamPattern = pattern,
            recommendedAction = action,
            isScam = score >= 50,
            keyIndicators = indicators
        )
    }
}
