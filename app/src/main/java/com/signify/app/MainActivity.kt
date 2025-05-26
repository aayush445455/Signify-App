// File: app/src/main/java/com/signify/app/MainActivity.kt
package com.signify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.signify.app.auth.ui.LoginScreen
import com.signify.app.auth.ui.RegisterScreen
import com.signify.app.contact.ContactUsScreen
import com.signify.app.history.ui.HistoryScreen
import com.signify.app.home.HomeScreen
import com.signify.app.learn.ui.LessonsScreen
import com.signify.app.navigation.Screen
import com.signify.app.profile.ProfileScreen
import com.signify.app.settings.SettingsScreen
import com.signify.app.translator.ui.TranslatorScreen
import com.signify.app.ui.components.SlidingPillNavigationBar
import com.signify.app.ui.theme.SignifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SignifyTheme {
                // 1) Grab your DI container
                val container = (application as SignifyApplication).container

                // 2) Setup navigation
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val route = backStack?.destination?.route ?: Screen.Login.route

                val tabs = listOf(
                    Screen.Lessons,
                    Screen.History,
                    Screen.Translator,
                    Screen.Home,
                    Screen.Settings,
                    Screen.ContactUs,
                    Screen.Profile
                )
                val selectedIndex = tabs.indexOfFirst { it.route == route }
                    .takeIf { it >= 0 } ?: 3

                Scaffold(
                    bottomBar = {
                        if (route in tabs.map { it.route }) {
                            SlidingPillNavigationBar(
                                icons = listOf(
                                    "Learn"     to Icons.Default.PlayArrow,
                                    "History"   to Icons.Default.History,
                                    "Translate" to Icons.Default.GTranslate,
                                    "Home"      to Icons.Default.Home,
                                    "Settings"  to Icons.Default.Settings,
                                    "Contact"   to Icons.Default.ContactMail,
                                    "Profile"   to Icons.Default.Person
                                ),
                                selectedIndex = selectedIndex,
                                onSelected = { idx ->
                                    navController.navigate(tabs[idx].route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                barColor    = MaterialTheme.colorScheme.surface,
                                pillColor   = MaterialTheme.colorScheme.primary,
                                iconTintOn  = MaterialTheme.colorScheme.onPrimary,
                                iconTintOff = MaterialTheme.colorScheme.onSurfaceVariant,
                                barHeight   = 56.dp,
                                pillHeight  = 4.dp,
                                pillWidth   = 36.dp,
                                iconSize    = 24.dp
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController    = navController,
                        startDestination = Screen.Login.route,
                        modifier         = Modifier.padding(innerPadding)
                    ) {
                        // Auth
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(Screen.Register.route)
                                }
                            )
                        }
                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Register.route) { inclusive = true }
                                    }
                                },
                                onBackToLogin = { navController.popBackStack() }
                            )
                        }

                        // Core feature tabs – now passing container
                        composable(Screen.Lessons.route) {
                            LessonsScreen(container)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(container)
                        }
                        composable(Screen.Translator.route) {
                            TranslatorScreen(container)
                        }

                        // Other tabs
                        composable(Screen.Home.route) {
                            HomeScreen(
                                currentRoute = route,
                                onNavigate   = { screen ->
                                    navController.navigate(screen.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen()
                        }
                        composable(Screen.ContactUs.route) {
                            ContactUsScreen()
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onNavigate = { dest ->
                                    navController.navigate(dest) { launchSingleTop = true }
                                },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
