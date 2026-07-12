package com.example.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScamReport
import com.example.ui.ScamViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ScamViewModel,
    onNavigateToVerify: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAcademy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.allReports.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    // Calculate metrics
    val totalReports = reports.size
    val verifiedCount = reports.count { it.isVerified }
    val avgRisk = if (reports.isNotEmpty()) reports.map { it.riskScore }.average().toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Upper Control Board Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ScamShield Controller",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (currentUser != null) "Hema Sri" else "Guest Agent",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Logout Button
                    IconButton(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .size(40.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Dashboard Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Reports",
                        value = totalReports.toString(),
                        icon = Icons.Default.Folder,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Verified",
                        value = verifiedCount.toString(),
                        icon = Icons.Default.VerifiedUser,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Avg Threat",
                        value = "$avgRisk%",
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Animated Live Threat Banner Ticker
        LiveWarningTicker()

        // Quick Navigation Grid
        Text(
            text = "PREVENTIVE TOOLS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickToolButton(
                title = "Verify Threat",
                subtitle = "Deep AI scan",
                icon = Icons.Default.Search,
                badgeText = "Thinking",
                color = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToVerify,
                modifier = Modifier.weight(1f)
            )

            QuickToolButton(
                title = "Report Fraud",
                subtitle = "Secure post",
                icon = Icons.Default.AddCircle,
                badgeText = "Submit",
                color = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToReport,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = onNavigateToChat,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("chat_banner")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "Chatbot Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = "Talk with Threat Counsel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Consult real-time on calls, texts, or job schemes",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = onNavigateToAcademy,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("academy_banner")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Academy Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1.0f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Cyber Defense Academy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PLAY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = "Take audits & play 'Spot-the-Scam' red flag simulator",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        // Custom Visual Category Chart Card
        Text(
            text = "AI SCAM INSIGHTS & ANALYTICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 24.dp, top = 28.dp, bottom = 12.dp)
        )

        CategoryChartCard(reports = reports)

        // Recent Public Scam Reports Board
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT SCAN ENTRIES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (reports.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Protected Icon",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Threat Entries Found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Everything is quiet. Tap 'Report Fraud' to post local threats.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                reports.take(3).forEach { report ->
                    CompactScamItem(report = report)
                }
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LiveWarningTicker() {
    val alerts = listOf(
        "🚨 ACTIVE SCAM ALERT: Part-time job scams offering quick salary on WhatsApp/Telegram are active now. Never send processing money!",
        "🔒 SECURITY ADVICE: Banks never call asking for your UPI PIN, OTP or card verification passwords. Block them!",
        "⚠️ PHISHING ALERT: Power Bill Suspension SMS messages pointing to unknown mobile numbers are fraudulent. Stay safe!",
        "📱 VISHING WARNING: Cybercriminals are spoofing family contacts claiming custody. Verify offline before transacting!"
    )

    var currentAlertIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(6000)
            currentAlertIndex = (currentAlertIndex + 1) % alerts.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AlertRed.copy(alpha = 0.1f))
            .padding(vertical = 10.dp, horizontal = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Alert Symbol",
                tint = AlertRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AnimatedContent(
                targetState = alerts[currentAlertIndex],
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                },
                label = "TickerAnimation"
            ) { alert ->
                Text(
                    text = alert,
                    fontSize = 11.5.sp,
                    color = AlertRed,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickToolButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun CategoryChartCard(reports: List<ScamReport>) {
    // Tally categories
    val categories = listOf("Fake Job", "Phishing", "UPI Scam", "Fake Call", "Others")
    val defaultCounts = mapOf(
        "Fake Job" to 2,
        "Phishing" to 1,
        "UPI Scam" to 1,
        "Fake Call" to 1,
        "Others" to 0
    )

    val categoryCounts = remember(reports) {
        if (reports.isEmpty()) {
            defaultCounts
        } else {
            val counts = reports.groupBy { it.category }
                .mapValues { it.value.size }
            
            categories.associateWith { counts[it] ?: 0 }
        }
    }

    val maxCount = categoryCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .testTag("analytics_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Scam Categories Distribution",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Breakdown of registered cyber scam patterns",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful Custom Canvas Bar Chart Drawing
            val barPrimary = MaterialTheme.colorScheme.primary
            val barSecondary = MaterialTheme.colorScheme.secondary
            val barTer = MaterialTheme.colorScheme.tertiary
            val textColor = MaterialTheme.colorScheme.onSurface

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = categories.size
                val spaceBetween = 24.dp.toPx()
                val totalSpacing = spaceBetween * (barCount - 1)
                val barWidth = (canvasWidth - totalSpacing) / barCount

                categories.forEachIndexed { index, cat ->
                    val count = categoryCounts[cat] ?: 0
                    val barHeight = (count.toFloat() / maxCount) * (canvasHeight - 20.dp.toPx())
                    
                    val x = index * (barWidth + spaceBetween)
                    val y = canvasHeight - barHeight

                    // Dynamic bar colors
                    val color = when (index) {
                        0 -> barPrimary
                        1 -> barSecondary
                        2 -> barTer
                        3 -> AlertRed
                        else -> WarningAmber
                    }

                    // Draw rounded bar
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw count label on top of bar
                    if (count > 0) {
                        // Drawing text in standard Canvas is simpler with native Canvas or we can just draw indicator circles or rely on bottom labels.
                        // To keep it 100% robust and compile-safe on Jetpack Compose, let's keep canvas draw calls simple and standard.
                        drawCircle(
                            color = color.copy(alpha = 0.8f),
                            radius = 3.dp.toPx(),
                            center = Offset(x + barWidth / 2, y - 8.dp.toPx())
                        )
                    }
                }

                // Draw base line
                drawLine(
                    color = textColor.copy(alpha = 0.15f),
                    start = Offset(0f, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 1.dp.toPx()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Labels / Legends
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEachIndexed { index, cat ->
                    val count = categoryCounts[cat] ?: 0
                    val dotColor = when (index) {
                        0 -> barPrimary
                        1 -> barSecondary
                        2 -> barTer
                        3 -> AlertRed
                        else -> WarningAmber
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(dotColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$cat ($count)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactScamItem(report: ScamReport) {
    val riskColor = when {
        report.riskScore >= 75 -> AlertRed
        report.riskScore >= 40 -> WarningAmber
        else -> SafeGreen
    }

    val icon = when (report.category) {
        "Fake Job" -> Icons.Default.WorkOutline
        "Phishing" -> Icons.Default.Language
        "UPI Scam" -> Icons.Default.AccountBalanceWallet
        "Fake Call" -> Icons.Default.PhoneInTalk
        else -> Icons.Default.Security
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(riskColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = riskColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = report.sender,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Threat Badge
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(riskColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${report.riskScore}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = riskColor
                    )
                }
                Text(
                    text = "Risk Score",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
