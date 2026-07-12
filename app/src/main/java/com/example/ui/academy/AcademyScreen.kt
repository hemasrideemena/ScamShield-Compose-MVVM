package com.example.ui.academy

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyScreen(
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Security Audit, 1: Spot the Scam Game
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Academy Header
        Surface(
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Cyber Defense Academy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Gamified training modules to build community immunity against digital frauds",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Sub-tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(
                        label = "Security Audit",
                        icon = Icons.Default.Shield,
                        isActive = activeTab == 0,
                        onClick = { activeTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        label = "Spot-the-Scam",
                        icon = Icons.Default.Quiz,
                        isActive = activeTab == 1,
                        onClick = { activeTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeTab == 0) {
                SecurityAuditView()
            } else {
                SpotTheScamView()
            }
        }
    }
}

@Composable
fun TabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

// --- SUB-VIEW 1: SECURITY AUDIT ---

@Composable
fun SecurityAuditView() {
    val auditQuestions = listOf(
        AuditQuestion(
            id = 1,
            title = "Two-Factor Authentication (2FA)",
            description = "Do you have 2FA enabled on major messengers (WhatsApp/Telegram) and banking apps?",
            points = 25
        ),
        AuditQuestion(
            id = 2,
            title = "National Do Not Call Registry",
            description = "Is your mobile number registered under TRAI's DND registry to block commercial cold calls?",
            points = 20
        ),
        AuditQuestion(
            id = 3,
            title = "Biometrics & App Locks",
            description = "Do you secure GPay, PhonePe, and other financial apps with app-level biometric screen-locks?",
            points = 20
        ),
        AuditQuestion(
            id = 4,
            title = "Unique Passwords & Managers",
            description = "Do you avoid recycling the same password for banking, e-commerce, and social media sites?",
            points = 20
        ),
        AuditQuestion(
            id = 5,
            title = "Suspicious Link Caution",
            description = "Do you proactively avoid opening SMS rewards, lottery cards, or scratch-card link forwards?",
            points = 15
        )
    )

    // Keep track of answered yes
    val selectedAnswers = remember { mutableStateMapOf<Int, Boolean>() }

    val currentScore = auditQuestions.sumOf { q ->
        if (selectedAnswers[q.id] == true) q.points else 0
    }

    val scoreColor = when {
        currentScore >= 80 -> SafeGreen
        currentScore >= 50 -> WarningAmber
        else -> AlertRed
    }

    val ratingLabel = when {
        currentScore >= 80 -> "EXCELLENT PROTECTION"
        currentScore >= 50 -> "MODERATE RISK"
        else -> "CRITICALLY VULNERABLE"
    }

    val ratingDesc = when {
        currentScore >= 80 -> "Congratulations! Your accounts follow cyber-security best practices. You are well shielded."
        currentScore >= 50 -> "Decent, but your accounts are exposed to sophisticated social engineering. Review unchecked settings."
        else -> "High danger of online financial theft. Please immediately configure 2FA, biometric app locks, and practice extreme link caution."
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Account Vulnerability Assessment",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Generate your custom Cyber Security Posture Index",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Dynamic Progress Indicator Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                CircularProgressIndicator(
                    progress = { currentScore.toFloat() / 100f },
                    color = scoreColor,
                    strokeWidth = 10.dp,
                    trackColor = scoreColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(120.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentScore",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = scoreColor
                    )
                    Text(
                        text = "Score Index",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = ratingLabel,
                color = scoreColor,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )

            Text(
                text = ratingDesc,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }
    }

    Text(
        text = "SECURITY CHECKLIST ITEMS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )

    auditQuestions.forEach { q ->
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (selectedAnswers[q.id] == true) scoreColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedAnswers[q.id] == true,
                    onCheckedChange = { isChecked ->
                        selectedAnswers[q.id] = isChecked
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = scoreColor,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("audit_checkbox_${q.id}")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = q.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .background(scoreColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${q.points} Pts",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                        }
                    }
                    Text(
                        text = q.description,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

data class AuditQuestion(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int
)

// --- SUB-VIEW 2: SPOT THE SCAM GAME ---

@Composable
fun SpotTheScamView() {
    val challenges = listOf(
        ScamChallenge(
            id = 1,
            intro = "An SMS offering instant daily passive income.",
            sender = "+91 91238 29013",
            messageBody = "Congratulations! You have been selected for a part-time job from home. Work only 1 hour a day rating movies and earn up to Rs 15,000 daily. Commission paid immediately. Zero experience needed. Tap link to start immediately: https://telegram-hr-task.cc/join",
            redFlags = listOf(
                RedFlag(1, "Selected for a part-time job", "Legitimate companies never recruit candidates blindly through spam cold texts."),
                RedFlag(2, "earn up to Rs 15,000 daily", "Highly exaggerated salaries for low-skill tasks indicate classic bait schemes."),
                RedFlag(3, "https://telegram-hr-task.cc", "Relocating chats to unverified telegram handle URLs is the hallmark of task refund scams.")
            ),
            academicExplanation = "This is a Task Scam. They gain trust by paying small rewards (Rs 100-300) first, then demand large security deposits to release your accumulated 'balance' which is completely fake."
        ),
        ScamChallenge(
            id = 2,
            intro = "A notification claiming utility service cancellation.",
            sender = "AD-POWR",
            messageBody = "URGENT NOTICE: Dear customer, your electricity connection will be disconnected tonight at 9:30 PM due to pending update of previous month's bill. To avoid suspension immediately contact our regional electricity helpline officer at 8291039481.",
            redFlags = listOf(
                RedFlag(1, "disconnected tonight", "Artificial panic and high urgency is manufactured to override critical thinking."),
                RedFlag(2, "electricity helpline officer", "Official boards never appoint individual personal mobile numbers for utility bill collection."),
                RedFlag(3, "8291039481", "Dialing custom numbers exposes you to vishing or remote screen-sharing APK downloads.")
            ),
            academicExplanation = "Power Suspension Fraud. Scammers call back pretending to be support agents, instructing victims to install helper apps like AnyDesk to pay a 'Rs 10' charge, which subsequently records their credentials and steals thousands."
        )
    )

    var currentChallengeIndex by remember { mutableStateOf(0) }
    val challenge = challenges[currentChallengeIndex]

    // Track which flags have been discovered
    val discoveredFlags = remember { mutableStateMapOf<Int, Boolean>() }
    var showExplanation by remember { mutableStateOf(false) }

    // Reset game state on challenge change
    LaunchedEffect(currentChallengeIndex) {
        discoveredFlags.clear()
        showExplanation = false
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spot the Red Flags Game",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Challenge ${currentChallengeIndex + 1}/${challenges.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Tap on the highlighted areas to discover active security red flags.",
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Description intro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Scenario: ${challenge.intro}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated SMS Phone Device UI Frame
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFEF), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                // SMS Sender header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = challenge.sender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive text body
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topEnd = 16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    // Render the message highlighting red flags
                    HighlightableMessage(
                        fullText = challenge.messageBody,
                        flags = challenge.redFlags,
                        discoveredFlags = discoveredFlags,
                        onFlagClicked = { id -> discoveredFlags[id] = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Counter of discovered red flags
            val allDiscovered = challenge.redFlags.all { discoveredFlags[it.id] == true }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Red Flags Discovered: ${discoveredFlags.size}/${challenge.redFlags.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (allDiscovered) SafeGreen else MaterialTheme.colorScheme.onSurface
                )

                if (allDiscovered && !showExplanation) {
                    Button(
                        onClick = { showExplanation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Show Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discovered red flags details
            challenge.redFlags.forEach { flag ->
                if (discoveredFlags[flag.id] == true) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Red Flag",
                                tint = AlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "RED FLAG: \"${flag.textSnippet}\"",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                                Text(
                                    text = flag.explanation,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Explanation box
            AnimatedVisibility(
                visible = showExplanation,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Expert Cybersecurity Triage",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                        }
                        Text(
                            text = challenge.academicExplanation,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Next button
                        Button(
                            onClick = {
                                currentChallengeIndex = (currentChallengeIndex + 1) % challenges.size
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Case Scenario", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightableMessage(
    fullText: String,
    flags: List<RedFlag>,
    discoveredFlags: Map<Int, Boolean>,
    onFlagClicked: (Int) -> Unit
) {
    // Simple segmented text parser to support clicking specific substrings
    // Since Jetpack Compose Rich Text highlighting is powerful, let's render standard interactive rows
    // that contain the text with clickable flag segments or simple buttons for flags
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Full message body received:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = fullText,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select suspicious statements below:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        // Show flags as interactive highlighted clickable pills
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            flags.forEach { flag ->
                val isDiscovered = discoveredFlags[flag.id] == true
                Surface(
                    onClick = { onFlagClicked(flag.id) },
                    color = if (isDiscovered) AlertRed.copy(alpha = 0.15f) else Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isDiscovered) AlertRed else Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDiscovered) Icons.Default.Block else Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = if (isDiscovered) AlertRed else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "\"${flag.textSnippet}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDiscovered) AlertRed else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

data class ScamChallenge(
    val id: Int,
    val intro: String,
    val sender: String,
    val messageBody: String,
    val redFlags: List<RedFlag>,
    val academicExplanation: String
)

data class RedFlag(
    val id: Int,
    val textSnippet: String,
    val explanation: String
)
