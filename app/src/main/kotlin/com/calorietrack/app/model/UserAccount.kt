package com.calorietrack.app.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val name: String,
    val isProfileComplete: Boolean = false,
    val profile: UserProfile? = null,
    val targets: NutritionTargets? = null
)

@Serializable
enum class Gender {
    MALE, FEMALE, OTHER
}

@Serializable
enum class ActivityLevel(val displayName: String, val multiplier: Float, val description: String) {
    SEDENTARY("Sedentary", 1.2f, "Little or no exercise, desk job"),
    LIGHT("Lightly Active", 1.375f, "Light exercise 1-3 days/week"),
    MODERATE("Moderately Active", 1.55f, "Moderate exercise 3-5 days/week"),
    VERY_ACTIVE("Very Active", 1.725f, "Hard exercise 6-7 days/week"),
    EXTREME("Extra Active", 1.9f, "Very hard exercise & physical job")
}

@Serializable
enum class FitnessGoal(val displayName: String, val calorieAdjustment: Int, val description: String) {
    LOSE_WEIGHT("Lose Weight", -500, "Healthy steady weight loss (~0.5 kg/week)"),
    MILD_WEIGHT_LOSS("Mild Weight Loss", -250, "Gradual gentle weight loss"),
    MAINTAIN("Maintain Weight", 0, "Keep current weight & improve fitness"),
    BUILD_MUSCLE("Build Muscle / Gain", 350, "Lean muscle mass with surplus protein")
}

@Serializable
enum class DietaryPreference(val displayName: String) {
    BALANCED("Balanced Diet"),
    HIGH_PROTEIN("High Protein & Fitness"),
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan / Plant-Based"),
    LOW_CARB("Low Carb / Keto Friendly")
}

@Serializable
data class UserProfile(
    val age: Int,
    val gender: Gender,
    val weightKg: Float,
    val heightCm: Float,
    val activityLevel: ActivityLevel,
    val goal: FitnessGoal,
    val preference: DietaryPreference
) {
    val bmi: Float
        get() {
            val heightM = heightCm / 100f
            return if (heightM > 0) weightKg / (heightM * heightM) else 0f
        }

    val bmiCategory: String
        get() = when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Normal weight"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }
}

@Serializable
data class NutritionTargets(
    val dailyCalories: Int,
    val bmr: Int,
    val tdee: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val waterIntakeMl: Int
)

