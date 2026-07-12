package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- API Request & Response Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val thinkingConfig: ThinkingConfig? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class ScamAnalysisResult(
    val riskScore: Int,
    val scamPattern: String,
    val recommendedAction: String,
    val isScam: Boolean,
    val keyIndicators: List<String>
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiServiceClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    fun getMoshi(): Moshi = moshi
}

class GeminiService {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val apiService = GeminiServiceClient.apiService
    private val moshi = GeminiServiceClient.getMoshi()

    /**
     * Deeply analyzes a suspicious message, phone number, website link, or UPI ID
     * using the advanced reasoning model (gemini-3.1-pro-preview) with HIGH thinking level.
     */
    suspend fun analyzeScam(
        sender: String,
        content: String,
        category: String
    ): ScamAnalysisResult {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback safe simulation response when API key is missing
            return runSimulationAnalysis(sender, content, category)
        }

        val prompt = """
            Analyze this potential fraud/scam report:
            - Source / Sender / Website: "$sender"
            - Category: "$category"
            - Message Content / Scam details: "$content"

            Evaluate carefully and respond ONLY in strict JSON format matching the following schema.
            Do not wrap in markdown ```json blocks. Simply return the JSON raw text.
            Schema:
            {
              "riskScore": Int (from 0 to 100),
              "scamPattern": "Detailed explanation of the social engineering and scam tactics identified",
              "recommendedAction": "Actionable safety advice for the user (e.g., block number immediately, do not click the URL)",
              "isScam": Boolean (true if suspicious activity is detected, false otherwise),
              "keyIndicators": ["Indicator 1", "Indicator 2", "Indicator 3"]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                thinkingConfig = ThinkingConfig(thinkingLevel = "high"),
                responseMimeType = "application/json"
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a cyber security analyst specializing in financial scams, phishing, vishing, UPI fraud, fake job offers, and social engineering. Analyze queries objectively and provide output in valid JSON matching the exact schema requested."))
            )
        )

        return try {
            val response = apiService.generateContent("gemini-3.1-pro-preview", apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No response received from Gemini")
            
            // Handle optional markdown json formatting wrappers if model didn't follow instruction
            val cleanedJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = moshi.adapter(ScamAnalysisResult::class.java)
            adapter.fromJson(cleanedJson) ?: throw Exception("Failed to parse analysis result")
        } catch (e: Exception) {
            e.printStackTrace()
            // In case of error (e.g. rate limit, thinking not supported by current API tier),
            // fallback gracefully to a smart prompt on gemini-3.5-flash
            try {
                fallbackAnalyzeWithFlash(sender, content, category)
            } catch (fallbackEx: Exception) {
                runSimulationAnalysis(sender, content, category)
            }
        }
    }

    private suspend fun fallbackAnalyzeWithFlash(
        sender: String,
        content: String,
        category: String
    ): ScamAnalysisResult {
        val prompt = """
            Analyze this scam report:
            Sender: "$sender"
            Category: "$category"
            Content: "$content"
            
            Respond only in raw JSON format matching:
            {"riskScore": 85, "scamPattern": "explanation", "recommendedAction": "action", "isScam": true, "keyIndicators": ["ind1", "ind2"]}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                responseMimeType = "application/json"
            )
        )
        val response = apiService.generateContent("gemini-3.5-flash", apiKey, request)
        val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response")
        val cleaned = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        return moshi.adapter(ScamAnalysisResult::class.java).fromJson(cleaned)
            ?: throw Exception("Failed to parse JSON")
    }

    /**
     * Maintains a multi-turn chat interaction using gemini-3.5-flash
     */
    suspend fun chatWithScamExpert(history: List<Content>): String {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Safe simulated conversation when API key is missing
            val lastUserMsg = history.lastOrNull { it.role == "user" }?.parts?.firstOrNull()?.text ?: ""
            return getSimulatedChatResponse(lastUserMsg)
        }

        val request = GenerateContentRequest(
            contents = history,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are ScamShield AI, an empathetic, highly knowledgeable cyber defense expert. Your primary goal is to help users identify potential financial frauds, online scams, phishing attempts, and fake work-from-home offers. Give clear, direct safety recommendations, warnings, and guidelines on how to file complaints (e.g., cybercrime.gov.in in India). Be precise, supportive, and highly focused on safety. Do not generate code or generic chat unless relevant to scam mitigation."))
            )
        )

        return try {
            val response = apiService.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, but I could not analyze that message right now. Please stay safe and avoid sharing any credentials!"
        } catch (e: Exception) {
            e.printStackTrace()
            "I'm having trouble connecting to my threat database. As a general precaution: NEVER share OTPs, click unknown links, or pay fees to receive job offers."
        }
    }

    private fun runSimulationAnalysis(sender: String, content: String, category: String): ScamAnalysisResult {
        val lowerContent = content.lowercase()
        val lowerSender = sender.lowercase()

        var score = 15
        val indicators = mutableListOf<String>()
        var pattern = "This input looks relatively low risk, but stay vigilant. Ensure you do not share any sensitive personal or financial details."
        var recommended = "Verify through official customer support channels before taking any action."

        if (lowerContent.contains("free") || lowerContent.contains("win") || lowerContent.contains("lottery") || lowerContent.contains("selected")) {
            score = 85
            indicators.add("Unsolicited reward or prize offer")
            indicators.add("Creates a false sense of fortune")
            pattern = "Classic prize/lottery fraud designed to bait victims into paying 'processing fees' or tax clearances."
            recommended = "Do not engage, click any link, or provide payment. Block and report this number immediately."
        } else if (lowerContent.contains("part-time") || lowerContent.contains("salary") || lowerContent.contains("work from home") || lowerContent.contains("telegram task") || lowerContent.contains("youtube like")) {
            score = 95
            indicators.add("High-pay work-from-home offers with zero experience required")
            indicators.add("Uses messaging apps like Telegram or WhatsApp for onboarding")
            pattern = "Prevalent 'Task Scam' where users are initially paid small amounts for simple liking/subscribing tasks, then pressured to deposit large sums to unlock premium task tiers."
            recommended = "Never deposit money to receive payment. Block contacts claiming to be HR managers on WhatsApp/Telegram."
        } else if (lowerContent.contains("otp") || lowerContent.contains("upi pin") || lowerContent.contains("password") || lowerContent.contains("bank account blocked") || lowerContent.contains("kyc update")) {
            score = 98
            indicators.add("Urgent bank account suspension threat")
            indicators.add("Requests sensitive PIN, OTP, or password credentials")
            pattern = "Vishing/Phishing attempt attempting to induce panic by claiming bank or digital wallet suspensions, forcing quick credential entry."
            recommended = "Banks NEVER request OTPs or PINs over phone or SMS. Do not share any OTP. Visit your local branch to verify."
        } else if (lowerContent.contains("click") || lowerContent.contains("link") || lowerContent.contains("http") || lowerContent.contains(".apk") || lowerSender.contains("http") || lowerSender.contains("bit.ly") || lowerSender.contains("tinyurl")) {
            score = 75
            indicators.add("Suspicious short URL or unknown link")
            indicators.add("Urges clicking to avoid penalty or claiming delivery")
            pattern = "Phishing links aimed at deploying malware/spyware (.apk) or harvesting login credentials via lookalike pages."
            recommended = "Do not click. Hover over links to check real destinations. Never install apps from untrusted source links."
        }

        return ScamReport(
            sender = sender,
            title = "Analyzed Scam Report",
            category = category,
            description = content,
            riskScore = score,
            scamPattern = pattern,
            recommendedAction = recommended,
            keyIndicators = indicators
        ).let {
            ScamAnalysisResult(
                riskScore = score,
                scamPattern = pattern,
                recommendedAction = recommended,
                isScam = score >= 50,
                keyIndicators = indicators
            )
        }
    }

    private fun getSimulatedChatResponse(userMsg: String): String {
        val msg = userMsg.lowercase()
        return when {
            msg.contains("hello") || msg.contains("hi") -> {
                "Hello! I am your ScamShield Assistant. You can ask me about any suspicious messages, job offers, or vishing calls you've received. How can I help secure your digital space today?"
            }
            msg.contains("job") || msg.contains("part-time") || msg.contains("task") -> {
                "⚠️ Task-based job scams are extremely common right now! Scam recruiters text you on WhatsApp/Telegram offering easy money for liking YouTube videos or rating hotels. They will pay you small commissions first, but then ask for 'deposits' to complete higher-level tasks. Please remember: REAL JOBS NEVER ASK YOU TO PAY TO WORK. Never send money to these handlers."
            }
            msg.contains("link") || msg.contains("sms") || msg.contains("phishing") -> {
                "🔗 Phishing links are malicious websites made to look like bank portals, Amazon, or government sites. If you receive an unsolicited SMS claiming your electricity bill is unpaid, or your FedEx package is on hold, check the URL carefully. It will usually be a suspicious domain (e.g., '.cc', '.xyz', or shortened links). Never enter OTPs or passwords there!"
            }
            msg.contains("upi") || msg.contains("gpay") || msg.contains("phonepe") -> {
                "💸 UPI Fraud alert: Scammers use the 'Request Money' feature on GPay/PhonePe to trick you. They will send you a payment request, claiming you are *receiving* a refund or prize money. Remember: YOU ONLY NEED TO ENTER YOUR UPI PIN TO SEND/PAY MONEY, NEVER TO RECEIVE IT. If someone tells you to enter your PIN to claim cash, it is a 100% scam!"
            }
            msg.contains("cybercrime") || msg.contains("report") || msg.contains("police") -> {
                "📞 If you have lost money to a digital scam, act quickly! \n1. Immediately report it to the National Cyber Crime portal: www.cybercrime.gov.in (or call 1930 in India).\n2. Contact your bank to freeze the beneficiary account or block your card. Do this within 2 hours (the golden hour) for high chances of fund recovery!"
            }
            else -> {
                "I understand you are asking about safety guidelines. To evaluate a threat: check if they are demanding money urgently, requesting OTPs/PINs, or calling from a normal mobile number instead of an official brand ID. What are the specific details of the message you received?"
            }
        }
    }
}
