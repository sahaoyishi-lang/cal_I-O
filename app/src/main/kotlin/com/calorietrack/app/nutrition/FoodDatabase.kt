package com.calorietrack.app.nutrition

import com.calorietrack.app.model.FoodItem
import com.calorietrack.app.model.PortionUnit

object FoodDatabase {

    val foods: List<FoodItem> = listOf(
        // Grains & Carbs
        FoodItem(
            id = "rice_cooked_white",
            name = "Cooked White Rice",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 130f,
            proteinPer100g = 2.7f,
            carbsPer100g = 28.2f,
            fatPer100g = 0.3f,
            fiberPer100g = 0.4f,
            description = "Standard cooked white grain rice (1 medium bowl is ~150g)"
        ),
        FoodItem(
            id = "rice_cooked_brown",
            name = "Cooked Brown Rice",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 123f,
            proteinPer100g = 2.7f,
            carbsPer100g = 25.6f,
            fatPer100g = 1.0f,
            fiberPer100g = 1.6f,
            description = "Whole grain fiber-rich cooked brown rice"
        ),
        FoodItem(
            id = "roti_chapati",
            name = "Wheat Roti / Chapati",
            category = "Grains",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 2.0f,
            caloriesPer100g = 260f,
            proteinPer100g = 9.0f,
            carbsPer100g = 51.0f,
            fatPer100g = 3.5f,
            fiberPer100g = 4.0f,
            description = "1 medium homemade whole wheat roti is ~40g (104 kcal)"
        ),
        FoodItem(
            id = "bread_whole_wheat",
            name = "Whole Wheat Bread",
            category = "Grains",
            defaultServingUnit = PortionUnit.SLICES,
            defaultServingQuantity = 2.0f,
            caloriesPer100g = 247f,
            proteinPer100g = 13.0f,
            carbsPer100g = 41.0f,
            fatPer100g = 3.4f,
            fiberPer100g = 6.0f,
            description = "1 standard slice is ~35g (86 kcal)"
        ),
        FoodItem(
            id = "rolled_oats",
            name = "Rolled Oats (Raw)",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 0.5f,
            caloriesPer100g = 389f,
            proteinPer100g = 16.9f,
            carbsPer100g = 66.3f,
            fatPer100g = 6.9f,
            fiberPer100g = 10.6f,
            description = "High fiber complex carbohydrate"
        ),
        FoodItem(
            id = "cooked_pasta",
            name = "Cooked Pasta",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 158f,
            proteinPer100g = 5.8f,
            carbsPer100g = 31.0f,
            fatPer100g = 0.9f,
            fiberPer100g = 1.8f
        ),

        // Proteins
        FoodItem(
            id = "chicken_breast_grilled",
            name = "Grilled Chicken Breast",
            category = "Poultry",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 165f,
            proteinPer100g = 31.0f,
            carbsPer100g = 0.0f,
            fatPer100g = 3.6f,
            fiberPer100g = 0f,
            description = "Skinless, lean grilled chicken breast (~150g fillet is 248 kcal, 46g protein)"
        ),
        FoodItem(
            id = "boiled_egg",
            name = "Boiled Whole Egg",
            category = "Eggs",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 2.0f,
            caloriesPer100g = 155f,
            proteinPer100g = 13.0f,
            carbsPer100g = 1.1f,
            fatPer100g = 11.0f,
            description = "1 large egg is ~50g (78 kcal, 6.5g protein)"
        ),
        FoodItem(
            id = "egg_whites",
            name = "Egg White",
            category = "Eggs",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 3.0f,
            caloriesPer100g = 52f,
            proteinPer100g = 11.0f,
            carbsPer100g = 0.7f,
            fatPer100g = 0.2f,
            description = "1 egg white is ~33g (17 kcal, 3.6g protein)"
        ),
        FoodItem(
            id = "salmon_fillet",
            name = "Baked / Grilled Salmon",
            category = "Fish",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 208f,
            proteinPer100g = 20.4f,
            carbsPer100g = 0.0f,
            fatPer100g = 13.0f,
            description = "Rich in omega-3 healthy fats (~150g fillet)"
        ),
        FoodItem(
            id = "paneer_cottage_cheese",
            name = "Fresh Paneer (Cottage Cheese)",
            category = "Dairy",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 0.7f,
            caloriesPer100g = 265f,
            proteinPer100g = 18.3f,
            carbsPer100g = 3.4f,
            fatPer100g = 20.8f,
            description = "Indian cottage cheese"
        ),
        FoodItem(
            id = "tofu_firm",
            name = "Firm Tofu",
            category = "Plant Protein",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 0.7f,
            caloriesPer100g = 144f,
            proteinPer100g = 17.3f,
            carbsPer100g = 2.8f,
            fatPer100g = 8.7f,
            description = "Plant-based soy protein"
        ),
        FoodItem(
            id = "dal_lentils_yellow",
            name = "Yellow Lentil Dal (Cooked)",
            category = "Legumes",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 116f,
            proteinPer100g = 9.0f,
            carbsPer100g = 20.0f,
            fatPer100g = 1.8f,
            fiberPer100g = 4.2f,
            description = "Cooked toor or moong dal (1 bowl ~ 200g)"
        ),
        FoodItem(
            id = "chickpeas_chana",
            name = "Cooked Chickpeas (Chana)",
            category = "Legumes",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 164f,
            proteinPer100g = 8.9f,
            carbsPer100g = 27.4f,
            fatPer100g = 2.6f,
            fiberPer100g = 7.6f
        ),

        // Dairy
        FoodItem(
            id = "greek_yogurt_plain",
            name = "Plain Greek Yogurt",
            category = "Dairy",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 0.75f,
            caloriesPer100g = 59f,
            proteinPer100g = 10.0f,
            carbsPer100g = 3.6f,
            fatPer100g = 0.4f,
            description = "High protein fat-free or low-fat greek yogurt"
        ),
        FoodItem(
            id = "milk_whole",
            name = "Whole Milk",
            category = "Dairy",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 62f,
            proteinPer100g = 3.2f,
            carbsPer100g = 4.8f,
            fatPer100g = 3.3f,
            description = "1 glass/cup is ~240ml"
        ),

        // Fruits & Veggies
        FoodItem(
            id = "banana",
            name = "Banana",
            category = "Fruits",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 89f,
            proteinPer100g = 1.1f,
            carbsPer100g = 22.8f,
            fatPer100g = 0.3f,
            fiberPer100g = 2.6f,
            description = "1 medium banana is ~118g (105 kcal)"
        ),
        FoodItem(
            id = "apple",
            name = "Fresh Apple",
            category = "Fruits",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 52f,
            proteinPer100g = 0.3f,
            carbsPer100g = 13.8f,
            fatPer100g = 0.2f,
            fiberPer100g = 2.4f,
            description = "1 medium apple is ~180g (95 kcal)"
        ),
        FoodItem(
            id = "broccoli_steamed",
            name = "Steamed Broccoli",
            category = "Vegetables",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 35f,
            proteinPer100g = 2.4f,
            carbsPer100g = 7.2f,
            fatPer100g = 0.4f,
            fiberPer100g = 3.3f
        ),
        FoodItem(
            id = "mixed_green_salad",
            name = "Fresh Garden Salad",
            category = "Vegetables",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 20f,
            proteinPer100g = 1.2f,
            carbsPer100g = 3.6f,
            fatPer100g = 0.2f,
            fiberPer100g = 1.8f,
            description = "Cucumber, lettuce, tomatoes, carrots"
        ),

        // Healthy Fats & Extras
        FoodItem(
            id = "almonds_raw",
            name = "Raw Almonds",
            category = "Nuts & Seeds",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 15.0f,
            caloriesPer100g = 579f,
            proteinPer100g = 21.2f,
            carbsPer100g = 21.6f,
            fatPer100g = 49.9f,
            description = "15 almonds is ~18g (104 kcal)"
        ),
        FoodItem(
            id = "peanut_butter",
            name = "Peanut Butter",
            category = "Spreads",
            defaultServingUnit = PortionUnit.TABLESPOON,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 588f,
            proteinPer100g = 25.1f,
            carbsPer100g = 20.0f,
            fatPer100g = 50.4f,
            description = "1 tbsp is ~16g (94 kcal)"
        ),
        FoodItem(
            id = "olive_oil",
            name = "Extra Virgin Olive Oil",
            category = "Oils",
            defaultServingUnit = PortionUnit.TABLESPOON,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 884f,
            proteinPer100g = 0f,
            carbsPer100g = 0f,
            fatPer100g = 100f,
            description = "1 tbsp is ~14g (120 kcal)"
        )
    )

    fun search(query: String): List<FoodItem> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return foods.take(15)
        return foods.filter {
            it.name.lowercase().contains(clean) ||
            it.category.lowercase().contains(clean) ||
            it.description.lowercase().contains(clean)
        }
    }

    fun findById(id: String): FoodItem? {
        return foods.find { it.id == id }
    }

    fun findByNameFuzzy(name: String): FoodItem? {
        val clean = name.trim().lowercase()
        return foods.find { it.name.lowercase().contains(clean) || clean.contains(it.name.lowercase()) }
            ?: foods.firstOrNull { it.id.contains(clean) }
    }
}

