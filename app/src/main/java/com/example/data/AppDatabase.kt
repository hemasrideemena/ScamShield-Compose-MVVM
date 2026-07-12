package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScamReport::class, VerifiedScam::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scamReportDao(): ScamReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scamshield_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.scamReportDao())
                }
            }
        }

        suspend fun populateInitialData(dao: ScamReportDao) {
            val initialReports = listOf(
                ScamReport(
                    title = "Work from Home YouTube Video Like Scam",
                    sender = "+91 93456 12390",
                    category = "Fake Job",
                    description = "They contact on WhatsApp offering Rs 150 per YouTube video liked. At first, they send Rs 300 to GPay as confidence-builder. Then they add you to a Telegram channel and ask to invest money to unlock premium tasks. Lost Rs 45,000.",
                    riskScore = 98,
                    scamPattern = "Classic Task Scam: Uses micro-payments to build trust, followed by structured psychological pressure to make premium deposits to unlock more earnings.",
                    recommendedAction = "Immediately report and block the sender on WhatsApp/Telegram. Never make deposits to receive salary or commissions.",
                    timestamp = System.currentTimeMillis() - 4 * 3600 * 1000,
                    isVerified = true,
                    upvotes = 24,
                    reportedBy = "h****i@gmail.com",
                    keyIndicators = listOf("WhatsApp outreach", "Telegram relocation", "Deposit requested for tasks", "Micro-payments trust trap")
                ),
                ScamReport(
                    title = "Fake Electricity Office Bill Suspension SMS",
                    sender = "VM-MSEDCL",
                    category = "Phishing",
                    description = "SMS text: 'Dear customer, your electricity connection will be suspended tonight at 9:30 PM because your previous month's bill is updated. Immediately contact our officer at 8291039485.'",
                    riskScore = 95,
                    scamPattern = "Fear & Urgency Phishing: Instills immediate panic regarding suspension of core utilities, driving victims to dial simulated support numbers where they are forced to install remote desktop APKs like TeamViewer/AnyDesk.",
                    recommendedAction = "Never dial mobile numbers sent in utility SMS messages. Pay bills only via the official power company portal.",
                    timestamp = System.currentTimeMillis() - 10 * 3600 * 1000,
                    isVerified = true,
                    upvotes = 42,
                    reportedBy = "anonymous_scam_hunter",
                    keyIndicators = listOf("Urgent threat of disconnect", "Personal mobile number provided", "Sent from unofficial numeric header")
                ),
                ScamReport(
                    title = "UPI Google Pay Scratch Card Prize Scam",
                    sender = "http://scratch-gpay-rewards.cc",
                    category = "UPI Scam",
                    description = "Received a WhatsApp link claiming I won a GPay cashback. The link opened a web page displaying a scratch card revealing Rs 4,999. Clicking 'Send to Bank' launched Google Pay with a payment request. It asked for my UPI PIN to 'receive' the reward.",
                    riskScore = 92,
                    scamPattern = "UPI Request Fraud: Exploits the visual confusion of digital transactions by initiating a PAYMENT REQUEST disguised as a RECEIVE transaction.",
                    recommendedAction = "Never enter your UPI PIN to receive money. UPI PIN is strictly used only to send or debit funds.",
                    timestamp = System.currentTimeMillis() - 24 * 3600 * 1000,
                    isVerified = true,
                    upvotes = 35,
                    reportedBy = "scammed_student_99",
                    keyIndicators = listOf("Entering PIN to receive funds", "Fake promotional URL", "Urgent claim expiry")
                ),
                ScamReport(
                    title = "Amazon Part-Time Affiliate Partner Offer",
                    sender = "+1 (206) 555-0144",
                    category = "Fake Job",
                    description = "Got an SMS: 'Hello, I'm an Amazon recruitment officer. We are hiring part-time partners. Work 30 minutes a day and earn 10,000 INR. Tap link to talk to support.' The link leads to WhatsApp chat.",
                    riskScore = 88,
                    scamPattern = "Social Engineering / Brand Impersonation: Exploits well-known e-commerce brand trust (Amazon) to lure people looking for passive income.",
                    recommendedAction = "Amazon does not recruit random users via SMS. Avoid and do not provide details.",
                    timestamp = System.currentTimeMillis() - 36 * 3600 * 1000,
                    isVerified = false,
                    upvotes = 12,
                    reportedBy = "alert_citizen",
                    keyIndicators = listOf("Amazon impersonation", "High daily salary claim", "WhatsApp contact")
                ),
                ScamReport(
                    title = "Urgent Call claiming family member in police custody",
                    sender = "+91 74012 39021",
                    category = "Fake Call",
                    description = "Got a call from someone pretending to be a senior police officer. Said my nephew was arrested in a drug case and demanded Rs 50,000 bribe via UPI immediately to release him without registering a FIR. Played fake crying voice in background.",
                    riskScore = 99,
                    scamPattern = "Vishing (Voice Phishing) / Extreme Panic Trap: Leverages family safety panic and bribes to extort instant online payments.",
                    recommendedAction = "Cut the call immediately. Call the family member directly to verify their safety. Do not pay any money.",
                    timestamp = System.currentTimeMillis() - 48 * 3600 * 1000,
                    isVerified = true,
                    upvotes = 68,
                    reportedBy = "charuu_dev",
                    keyIndicators = listOf("Arrest extortion threat", "UPI payment demand", "Fake crying audio", "Simulated urgency")
                )
            )

            initialReports.forEach { dao.insertReport(it) }
        }
    }
}
