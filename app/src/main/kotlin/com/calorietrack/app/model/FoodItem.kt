package com.calorietrack.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class MealSlot(val displayName: String, val typicalTime: String) {
    BREAKFAST("Breakfast", "07:30 AM - 09:00 AM"),
    LUNCH("Lunch", "12:30 PM - 02:00 PM"),
    SNACK("Afternoon Snack", "04:30 PM - 05:30 PM"),
    DINNER("Dinner", "07:30 PM - 09:00 PM")
}

@Serializable
enum class PortionUnit(val displayName: String, val gramsEquivalent: Float) {
    GRAMS("Grams (g)", 1.0f),
    BOWLS("Bowl (approx 150g)", 150.0f),
    CUPS("Cup (approx 200g)", 200.0f),
    SLICES("Slice (approx 35g)", 35.0f),
    PIECES("Piece / Unit", 60.0f),
    TABLESPOON("Tablespoon (approx 15g)", 15.0f),
    PACKET("Single Packet", 100.0f)
}

@Serializable
enum class LogSource {
    CAMERA_AI,
    BARCODE,
    VOICE,
    MANUAL
}

@Serializable
data class FoodItem(
    val id: String,
    val name: String,
    val category: String,
    val defaultServingUnit: PortionUnit,
    val defaultServingQuantity: Float,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float = 0f,
    val description: String = ""
) {
    fun calculateNutrition(quantity: Float, unit: PortionUnit): NutritionValues {
        val totalGrams = quantity * unit.gramsEquivalent
        val ratio = totalGrams / 100.0f
        return NutritionValues(
            calories = (caloriesPer100g * ratio).toInt(),
            proteinGrams = (proteinPer100g * ratio).toInt(),
            carbsGrams = (carbsPer100g * ratio).toInt(),
            fatGrams = (fatPer100g * ratio).toInt(),
            fiberGrams = (fiberPer100g * ratio).toInt(),
            weightGrams = totalGrams.toInt()
        )
    }
}

@Serializable
data class NutritionValues(
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val fiberGrams: Int,
    val weightGrams: Int
)

@Serializable
data class LoggedMealEntry(
    val id: String,
    val foodName: String,
    val mealSlot: MealSlot,
    val quantity: Float,
    val unit: PortionUnit,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val source: LogSource,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val notes: String = ""
)

