package com.calorietrack.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AuthKey : NavKey

@Serializable
data object OnboardingProfileKey : NavKey

@Serializable
data object DashboardKey : NavKey

@Serializable
data object DietChartKey : NavKey

@Serializable
data object ProfileKey : NavKey
