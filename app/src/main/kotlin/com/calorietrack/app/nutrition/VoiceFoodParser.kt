package com.calorietrack.app.nutrition

import com.calorietrack.app.model.FoodItem
import com.calorietrack.app.model.NutritionValues
import com.calorietrack.app.model.PortionUnit

data class ParsedVoiceItem(
    val foodItem: FoodItem,
    val quantity: Float,
    val unit: PortionUnit,
    val nutrition: NutritionValues,
    val rawText: String
)

object VoiceFoodParser {

    private val numberWords = mapOf(
        "a" to 1.0f,
        "an" to 1.0f,
        "one" to 1.0f,
        "two" to 2.0f,
        "three" to 3.0f,
        "four" to 4.0f,
        "five" to 5.0f,
        "six" to 6.0f,
        "half" to 0.5f,
        "quarter" to 0.25f
    )

    fun parse(spokenText: String): List<ParsedVoiceItem> {
        val results = mutableListOf<ParsedVoiceItem>()
        if (spokenText.isBlank()) return results

        // Split by conjunctions
        val parts = spokenText.lowercase()
            .replace("i had", "")
            .replace("i ate", "")
            .replace("ate", "")
            .replace("consumed", "")
            .replace("having", "")
            .split(Regex("(?i)\\b(and|with|plus|along with|,|\\+)\\b"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (part in parts) {
            val parsed = parseSingleSegment(part)
            if (parsed != null) {
                results.add(parsed)
            }
        }

        // If no match by split, try whole string
        if (results.isEmpty()) {
            val single = parseSingleSegment(spokenText.lowercase())
            if (single != null) results.add(single)
        }

        return results
    }

    private fun parseSingleSegment(segment: String): ParsedVoiceItem? {
        val words = segment.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return null

        var detectedQuantity: Float = 1.0f
        var detectedUnit: PortionUnit? = null

        // Detect quantity
        val firstWord = words.first()
        val numFromWord = numberWords[firstWord]
        val numFromDigit = firstWord.toFloatOrNull()

        if (numFromDigit != null) {
            detectedQuantity = numFromDigit
        } else if (numFromWord != null) {
            detectedQuantity = numFromWord
        }

        // Detect unit
        when {
            segment.contains("bowl") || segment.contains("bowls") -> detectedUnit = PortionUnit.BOWLS
            segment.contains("cup") || segment.contains("cups") || segment.contains("glass") -> detectedUnit = PortionUnit.CUPS
            segment.contains("slice") || segment.contains("slices") -> detectedUnit = PortionUnit.SLICES
            segment.contains("piece") || segment.contains("pieces") -> detectedUnit = PortionUnit.PIECES
            segment.contains("gram") || segment.contains("grams") || segment.contains(" g ") -> {
                detectedUnit = PortionUnit.GRAMS
                // If quantity like "150 grams", extract that
                val gramMatch = Regex("(\\d+)\\s*(grams?|g)").find(segment)
                if (gramMatch != null) {
                    detectedQuantity = gramMatch.groupValues[1].toFloatOrNull() ?: detectedQuantity
                }
            }
            segment.contains("spoon") || segment.contains("tbsp") || segment.contains("tablespoon") -> detectedUnit = PortionUnit.TABLESPOON
        }

        // Food matching
        var matchedFood: FoodItem? = null
        for (food in FoodDatabase.foods) {
            val cleanName = food.name.lowercase()
            val cleanId = food.id.lowercase().replace("_", " ")

            // Check if key words match
            val keywords = cleanName.split(" ").filter { it.length > 2 }
            val idWords = cleanId.split(" ").filter { it.length > 2 }

            val match = keywords.any { segment.contains(it) } || idWords.any { segment.contains(it) }
            if (match) {
                matchedFood = food
                break
            }
        }

        // Fallback common aliases
        if (matchedFood == null) {
            matchedFood = when {
                segment.contains("rice") -> FoodDatabase.findById("rice_cooked_white")
                segment.contains("roti") || segment.contains("chapati") -> FoodDatabase.findById("roti_chapati")
                segment.contains("egg") -> FoodDatabase.findById("boiled_egg")
                segment.contains("chicken") -> FoodDatabase.findById("chicken_breast_grilled")
                segment.contains("bread") || segment.contains("toast") -> FoodDatabase.findById("bread_whole_wheat")
                segment.contains("oat") -> FoodDatabase.findById("rolled_oats")
                segment.contains("dal") || segment.contains("lentil") -> FoodDatabase.findById("dal_lentils_yellow")
                segment.contains("salad") -> FoodDatabase.findById("mixed_green_salad")
                segment.contains("yogurt") || segment.contains("curd") -> FoodDatabase.findById("greek_yogurt_plain")
                segment.contains("milk") -> FoodDatabase.findById("milk_whole")
                segment.contains("salmon") || segment.contains("fish") -> FoodDatabase.findById("salmon_fillet")
                segment.contains("paneer") -> FoodDatabase.findById("paneer_cottage_cheese")
                segment.contains("tofu") -> FoodDatabase.findById("tofu_firm")
                segment.contains("apple") -> FoodDatabase.findById("apple")
                segment.contains("banana") -> FoodDatabase.findById("banana")
                segment.contains("pasta") -> FoodDatabase.findById("cooked_pasta")
                segment.contains("almond") || segment.contains("nuts") -> FoodDatabase.findById("almonds_raw")
                else -> null
            }
        }

        if (matchedFood == null) return null

        val finalUnit = detectedUnit ?: matchedFood.defaultServingUnit
        val nutrition = matchedFood.calculateNutrition(detectedQuantity, finalUnit)

        return ParsedVoiceItem(
            foodItem = matchedFood,
            quantity = detectedQuantity,
            unit = finalUnit,
            nutrition = nutrition,
            rawText = segment
        )
    }
}

