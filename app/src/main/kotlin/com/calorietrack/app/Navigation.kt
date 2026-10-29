package com.calorietrack.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.calorietrack.app.data.CalorieAppRepository
import com.calorietrack.app.ui.auth.AuthScreen
import com.calorietrack.app.ui.dashboard.DashboardScreen
import com.calorietrack.app.ui.dietchart.DietChartScreen
import com.calorietrack.app.ui.onboarding.ProfileSetupScreen
import com.calorietrack.app.ui.profile.ProfileScreen

@Composable
fun MainNavigation(
    repository: CalorieAppRepository = CalorieAppRepository.instance
) {
    val currentUser by repository.currentUser.collectAsState()

    // Determine starting key based on session
    val initialKey = when {
        currentUser == null -> AuthKey
        !currentUser!!.isProfileComplete -> OnboardingProfileKey
        else -> DashboardKey
    }

    val backStack = rememberNavBackStack(initialKey)

    // Sync backStack when user logs in or logs out
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            backStack.clear()
            backStack.add(AuthKey)
        } else if (!currentUser!!.isProfileComplete) {
            if (backStack.lastOrNull() !is OnboardingProfileKey) {
                backStack.clear()
                backStack.add(OnboardingProfileKey)
            }
        }
    }

    val currentScreen = backStack.lastOrNull()
    val showBottomBar = currentScreen is DashboardKey || currentScreen is DietChartKey || currentScreen is ProfileKey

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen is DashboardKey,
                        onClick = {
                            if (currentScreen !is DashboardKey) {
                                backStack.clear()
                                backStack.add(DashboardKey)
                            }
                        },
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Diary") },
                        label = { Text("Diary") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is DietChartKey,
                        onClick = {
                            if (currentScreen !is DietChartKey) {
                                backStack.clear()
                                backStack.add(DietChartKey)
                            }
                        },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Diet Chart") },
                        label = { Text("Diet Chart") }
                    )
                    NavigationBarItem(
                        selected = currentScreen is ProfileKey,
                        onClick = {
                            if (currentScreen !is ProfileKey) {
                                backStack.clear()
                                backStack.add(ProfileKey)
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                },
                entryProvider = entryProvider {
                    entry<AuthKey> {
                        AuthScreen(
                            onAuthSuccess = { user ->
                                backStack.clear()
                                if (user.isProfileComplete) {
                                    backStack.add(DashboardKey)
                                } else {
                                    backStack.add(OnboardingProfileKey)
                                }
                            }
                        )
                    }

                    entry<OnboardingProfileKey> {
                        ProfileSetupScreen(
                            onProfileSaved = { chart ->
                                backStack.clear()
                                backStack.add(DietChartKey)
                            }
                        )
                    }

                    entry<DashboardKey> {
                        DashboardScreen(
                            onProfileClick = {
                                backStack.add(ProfileKey)
                            }
                        )
                    }

                    entry<DietChartKey> {
                        DietChartScreen(
                            onEditProfileClick = {
                                backStack.add(OnboardingProfileKey)
                            }
                        )
                    }

                    entry<ProfileKey> {
                        ProfileScreen(
                            onBack = {
                                backStack.removeLastOrNull()
                            },
                            onEditProfile = {
                                backStack.add(OnboardingProfileKey)
                            },
                            onSignOut = {
                                backStack.clear()
                                backStack.add(AuthKey)
                            }
                        )
                    }
                }
            )
        }
    }
}
