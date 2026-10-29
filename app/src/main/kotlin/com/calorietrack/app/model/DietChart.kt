package com.calorietrack.app.model

import kotlinx.serialization.Serializable

@Serializable
data class SuggestedMealItem(
    val name: String,
    val portion: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val alternatives: List<String> = emptyList()
)

@Serializable
data class MealSlotDietPlan(
    val slot: MealSlot,
    val targetCalories: Int,
    val timing: String,
    val primaryOptions: List<SuggestedMealItem>,
    val tips: String
)

@Serializable
data class CustomizedDietChart(
    val userSummary: String,
    val totalDailyCalories: Int,
    val proteinTargetGrams: Int,
    val carbsTargetGrams: Int,
    val fatTargetGrams: Int,
    val waterTargetLiters: Float,
    val mealPlans: List<MealSlotDietPlan>,
    val generalGuidelines: List<String>
)

