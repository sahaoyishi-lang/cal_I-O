package com.calorietrack.app.nutrition

import com.calorietrack.app.model.FoodItem
import com.calorietrack.app.model.PortionUnit

data class BarcodeItem(
    val barcode: String,
    val brand: String,
    val foodItem: FoodItem,
    val servingDescription: String
)

object BarcodeDatabase {

    val items: List<BarcodeItem> = listOf(
        BarcodeItem(
            barcode = "8901030383742",
            brand = "Quaker",
            foodItem = FoodItem(
                id = "quaker_oats_original",
                name = "Quaker Rolled White Oats",
                category = "Packaged Grains",
                defaultServingUnit = PortionUnit.BOWLS,
                defaultServingQuantity = 0.5f,
                caloriesPer100g = 374f,
                proteinPer100g = 13.6f,
                carbsPer100g = 60.0f,
                fatPer100g = 8.0f,
                fiberPer100g = 10.0f
            ),
            servingDescription = "1 serving (40g) = 150 kcal"
        ),
        BarcodeItem(
            barcode = "0749826002012",
            brand = "Optimum Nutrition",
            foodItem = FoodItem(
                id = "on_gold_standard_whey",
                name = "Gold Standard 100% Whey (Double Rich Chocolate)",
                category = "Supplements",
                defaultServingUnit = PortionUnit.PIECES,
                defaultServingQuantity = 1.0f,
                caloriesPer100g = 387f,
                proteinPer100g = 77.4f,
                carbsPer100g = 9.7f,
                fatPer100g = 4.8f
            ),
            servingDescription = "1 scoop (31g) = 120 kcal, 24g Protein"
        ),
        BarcodeItem(
            barcode = "8901262010053",
            brand = "Amul",
            foodItem = FoodItem(
                id = "amul_taaza_milk",
                name = "Amul Taaza Homogenised Toned Milk",
                category = "Packaged Dairy",
                defaultServingUnit = PortionUnit.CUPS,
                defaultServingQuantity = 1.0f,
                caloriesPer100g = 58f,
                proteinPer100g = 3.0f,
                carbsPer100g = 4.7f,
                fatPer100g = 3.0f
            ),
            servingDescription = "1 glass (200ml) = 116 kcal"
        ),
        BarcodeItem(
            barcode = "0041196910759",
            brand = "Chobani",
            foodItem = FoodItem(
                id = "chobani_greek_yogurt_0",
                name = "Chobani Non-Fat Greek Yogurt",
                category = "Packaged Dairy",
                defaultServingUnit = PortionUnit.CUPS,
                defaultServingQuantity = 0.75f,
                caloriesPer100g = 53f,
                proteinPer100g = 10.7f,
                carbsPer100g = 3.3f,
                fatPer100g = 0.0f
            ),
            servingDescription = "1 cup (150g) = 80 kcal, 16g Protein"
        ),
        BarcodeItem(
            barcode = "0852697001428",
            brand = "Kind Snacks",
            foodItem = FoodItem(
                id = "kind_dark_chocolate_almond",
                name = "Kind Dark Chocolate Nuts & Sea Salt Bar",
                category = "Snacks",
                defaultServingUnit = PortionUnit.PIECES,
                defaultServingQuantity = 1.0f,
                caloriesPer100g = 500f,
                proteinPer100g = 15.0f,
                carbsPer100g = 40.0f,
                fatPer100g = 37.5f,
                fiberPer100g = 17.5f
            ),
            servingDescription = "1 bar (40g) = 200 kcal, 6g Protein"
        ),
        BarcodeItem(
            barcode = "8904256708302",
            brand = "Pintola",
            foodItem = FoodItem(
                id = "pintola_all_natural_peanut_butter",
                name = "Pintola All Natural Peanut Butter Crunchy",
                category = "Spreads",
                defaultServingUnit = PortionUnit.TABLESPOON,
                defaultServingQuantity = 1.0f,
                caloriesPer100g = 625f,
                proteinPer100g = 30.0f,
                carbsPer100g = 18.0f,
                fatPer100g = 48.0f
            ),
            servingDescription = "1 tbsp (16g) = 100 kcal, 4.8g Protein"
        )
    )

    fun lookup(barcode: String): BarcodeItem? {
        val clean = barcode.trim()
        return items.find { it.barcode == clean }
    }
}

