package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CosmicAITheme
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SecondaryNebula
import com.example.ui.viewmodel.AstrologyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AstrologyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure seamless status bar integration matching design metrics
        enableEdgeToEdge()

        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            CosmicAITheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser == null) {
                        IntroScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {}
                        )
                    } else {
                        MainNavigationWorkspace(
                            viewModel = viewModel,
                            currentUser = currentUser!!
                        )
                    }
                }
            }
        }
    }
}

sealed class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    object Oracles : NavigationTab("oracles", "Oracles", Icons.Default.AutoAwesome)
    object GuruChat : NavigationTab("guru_chat", "Guru Chat", Icons.Default.Forum)
    object Chronicle : NavigationTab("chronicle", "Chronicle", Icons.Default.History)
    object Control : NavigationTab("control", "Control", Icons.Default.Shield)
    object Settings : NavigationTab("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationWorkspace(
    viewModel: AstrologyViewModel,
    currentUser: com.example.data.model.UserEntity
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDark by viewModel.isDarkMode.collectAsState()

    val tabs = listOf(
        NavigationTab.Oracles,
        NavigationTab.GuruChat,
        NavigationTab.Chronicle,
        NavigationTab.Control,
        NavigationTab.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_navigation_bar"),
                containerColor = if (isDark) BackgroundDark else MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = "${tab.title} icon",
                                tint = if (isSelected) PrimaryGold else (if (isDark) Color.LightGray else Color.Gray)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryGold else (if (isDark) Color.LightGray else Color.Gray)
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize().testTag("app_main_navigation_scaffold")
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationTab.Oracles.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Oracles Home Screen
            composable(NavigationTab.Oracles.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToReadingDetail = {
                        navController.navigate("reading_detail")
                    }
                )
            }

            // Real-time spiritual AI chat screen
            composable(NavigationTab.GuruChat.route) {
                ChatScreen(viewModel = viewModel)
            }

            // Star chronicle history screen
            composable(NavigationTab.Chronicle.route) {
                SavedReadingsScreen(
                    viewModel = viewModel,
                    onNavigateToReadingDetail = {
                        navController.navigate("reading_detail")
                    }
                )
            }

            // Admin Control Panel console screen
            composable(NavigationTab.Control.route) {
                AdminScreen(viewModel = viewModel)
            }

            // Account settings subscreen
            composable(NavigationTab.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onLogoutPressed = {
                        // Resets navigation back to home start securely
                        navController.navigate(NavigationTab.Oracles.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            // Reports reading detail viewer screen
            composable("reading_detail") {
                ReadingDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
