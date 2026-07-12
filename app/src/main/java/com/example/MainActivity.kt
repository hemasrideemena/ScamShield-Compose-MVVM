package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.ScamViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.verify.VerifyScreen
import com.example.ui.community.CommunityScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.report.ReportScamScreen
import com.example.ui.academy.AcademyScreen
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    Dashboard, Verify, Community, Chat, Academy, Report
}

class MainActivity : ComponentActivity() {
    private val viewModel: ScamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

                if (currentUser == null) {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { currentScreen = Screen.Dashboard }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (currentScreen != Screen.Report) {
                                NavigationBar(
                                    modifier = Modifier.testTag("bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Dashboard,
                                        onClick = { currentScreen = Screen.Dashboard },
                                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard") },
                                        modifier = Modifier.testTag("nav_tab_dashboard")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Verify,
                                        onClick = { currentScreen = Screen.Verify },
                                        icon = { Icon(Icons.Default.Search, contentDescription = "Scanner") },
                                        label = { Text("Verify") },
                                        modifier = Modifier.testTag("nav_tab_verify")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Community,
                                        onClick = { currentScreen = Screen.Community },
                                        icon = { Icon(Icons.Default.People, contentDescription = "Community") },
                                        label = { Text("Board") },
                                        modifier = Modifier.testTag("nav_tab_community")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Chat,
                                        onClick = { currentScreen = Screen.Chat },
                                        icon = { Icon(Icons.Default.SupportAgent, contentDescription = "AI Expert") },
                                        label = { Text("AI Advisor") },
                                        modifier = Modifier.testTag("nav_tab_chat")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Academy,
                                        onClick = { currentScreen = Screen.Academy },
                                        icon = { Icon(Icons.Default.School, contentDescription = "Cyber Academy") },
                                        label = { Text("Academy") },
                                        modifier = Modifier.testTag("nav_tab_academy")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                Screen.Dashboard -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToVerify = { currentScreen = Screen.Verify },
                                    onNavigateToReport = { currentScreen = Screen.Report },
                                    onNavigateToChat = { currentScreen = Screen.Chat },
                                    onNavigateToAcademy = { currentScreen = Screen.Academy }
                                )
                                Screen.Verify -> VerifyScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = Screen.Dashboard }
                                )
                                Screen.Community -> CommunityScreen(
                                    viewModel = viewModel
                                )
                                Screen.Chat -> ChatScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = Screen.Dashboard }
                                )
                                Screen.Academy -> AcademyScreen()
                                Screen.Report -> ReportScamScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = Screen.Dashboard }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
