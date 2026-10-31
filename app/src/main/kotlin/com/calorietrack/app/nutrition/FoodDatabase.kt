package com.calorietrack.app.nutrition

import com.calorietrack.app.model.FoodItem
import com.calorietrack.app.model.PortionUnit

object FoodDatabase {

    val foods: List<FoodItem> = listOf(
        // Grains & Rice & Breads
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
            description = "Standard cooked white grain rice (1 medium bowl is ~150g = 195 kcal)"
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
            id = "chicken_biryani",
            name = "Chicken Biryani",
            category = "Mixed Meals",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 180f,
            proteinPer100g = 9.5f,
            carbsPer100g = 21.0f,
            fatPer100g = 6.8f,
            fiberPer100g = 1.2f,
            description = "Fragrant spiced basmati rice with chicken (1 plate/bowl ~250g = 450 kcal)"
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
            name = "Rolled Oats / Oatmeal",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 150f,
            proteinPer100g = 5.0f,
            carbsPer100g = 26.0f,
            fatPer100g = 2.5f,
            fiberPer100g = 4.0f,
            description = "Cooked oats with water or milk (1 bowl ~180g)"
        ),
        FoodItem(
            id = "cooked_pasta",
            name = "Cooked Pasta / Spaghetti",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 158f,
            proteinPer100g = 5.8f,
            carbsPer100g = 31.0f,
            fatPer100g = 0.9f,
            fiberPer100g = 1.8f,
            description = "Boiled pasta (1 plate/bowl ~180g = 284 kcal)"
        ),
        FoodItem(
            id = "noodles_ramen",
            name = "Noodles / Ramen",
            category = "Grains",
            defaultServingUnit = PortionUnit.BOWLS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 140f,
            proteinPer100g = 4.5f,
            carbsPer100g = 24.0f,
            fatPer100g = 3.0f,
            fiberPer100g = 1.2f,
            description = "1 bowl cooked noodles ~200g"
        ),

        // Proteins & Meat & Eggs
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
            description = "Lean grilled chicken fillet (~150g is 248 kcal, 46g protein)"
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
            id = "egg_omelette",
            name = "Egg Omelette / Scrambled",
            category = "Eggs",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 190f,
            proteinPer100g = 12.0f,
            carbsPer100g = 1.5f,
            fatPer100g = 15.0f,
            description = "2-egg omelette cooked with touch of oil (~100g = 190 kcal)"
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
            description = "Cooked toor or moong dal (1 bowl ~ 200g = 232 kcal)"
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

        // Fruits
        FoodItem(
            id = "banana",
            name = "Fresh Banana",
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
            id = "orange_citrus",
            name = "Fresh Orange",
            category = "Fruits",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 47f,
            proteinPer100g = 0.9f,
            carbsPer100g = 11.8f,
            fatPer100g = 0.1f,
            fiberPer100g = 2.4f,
            description = "1 medium orange ~130g (62 kcal)"
        ),
        FoodItem(
            id = "mango_fresh",
            name = "Fresh Mango Slices",
            category = "Fruits",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 60f,
            proteinPer100g = 0.8f,
            carbsPer100g = 15.0f,
            fatPer100g = 0.4f,
            fiberPer100g = 1.6f,
            description = "Sweet fresh mango (1 cup ~165g = 99 kcal)"
        ),

        // Vegetables & Salads
        FoodItem(
            id = "mixed_green_salad",
            name = "Fresh Green Salad",
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
        FoodItem(
            id = "boiled_potatoes",
            name = "Boiled / Mashed Potatoes",
            category = "Vegetables",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 87f,
            proteinPer100g = 1.9f,
            carbsPer100g = 20.1f,
            fatPer100g = 0.1f,
            fiberPer100g = 1.8f,
            description = "1 medium potato ~150g (130 kcal)"
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

        // Common Snacks & Fast Meals
        FoodItem(
            id = "pizza_slice",
            name = "Pizza Slice (Cheese / Veg)",
            category = "Fast Food",
            defaultServingUnit = PortionUnit.SLICES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 266f,
            proteinPer100g = 11.0f,
            carbsPer100g = 33.0f,
            fatPer100g = 10.0f,
            fiberPer100g = 2.3f,
            description = "1 regular slice is ~107g (285 kcal)"
        ),
        FoodItem(
            id = "burger_sandwich",
            name = "Burger / Veg Sandwich",
            category = "Fast Food",
            defaultServingUnit = PortionUnit.PIECES,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 250f,
            proteinPer100g = 12.0f,
            carbsPer100g = 30.0f,
            fatPer100g = 9.0f,
            fiberPer100g = 2.0f,
            description = "1 standard burger/sandwich ~160g = 400 kcal"
        ),

        // Dairy & Beverages
        FoodItem(
            id = "greek_yogurt_plain",
            name = "Plain Greek Yogurt / Curd",
            category = "Dairy",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 0.75f,
            caloriesPer100g = 59f,
            proteinPer100g = 10.0f,
            carbsPer100g = 3.6f,
            fatPer100g = 0.4f,
            description = "High protein yogurt / curd"
        ),
        FoodItem(
            id = "milk_whole",
            name = "Cow's Milk (Glass)",
            category = "Dairy",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 62f,
            proteinPer100g = 3.2f,
            carbsPer100g = 4.8f,
            fatPer100g = 3.3f,
            description = "1 glass/cup is ~240ml = 149 kcal"
        ),
        FoodItem(
            id = "milk_tea_chai",
            name = "Indian Milk Tea (Chai)",
            category = "Beverages",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 65f,
            proteinPer100g = 2.0f,
            carbsPer100g = 9.0f,
            fatPer100g = 2.4f,
            description = "1 standard teacup ~150ml (98 kcal)"
        ),
        FoodItem(
            id = "black_coffee",
            name = "Black Coffee",
            category = "Beverages",
            defaultServingUnit = PortionUnit.CUPS,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = 2f,
            proteinPer100g = 0.1f,
            carbsPer100g = 0.3f,
            fatPer100g = 0.0f,
            description = "Zero sugar black coffee"
        )
    )

    fun search(query: String): List<FoodItem> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return foods
        return foods.filter {
            it.name.lowercase().contains(clean) ||
            it.category.lowercase().contains(clean) ||
            it.description.lowercase().contains(clean)
        }
    }

    fun findById(id: String): FoodItem? {
        return foods.find { it.id == id }
    }

    /**
     * Intelligent mapping from ML Kit vision labels to FoodDatabase items
     */
    fun findBestMatchForLabels(labels: List<String>): FoodItem? {
        if (labels.isEmpty()) return null

        val labelString = labels.joinToString(" ").lowercase()

        // 1. Direct keyword rules based on common ML Kit labels
        if (labelString.contains("banana")) return findById("banana")
        if (labelString.contains("apple")) return findById("apple")
        if (labelString.contains("orange") || labelString.contains("citrus")) return findById("orange_citrus")
        if (labelString.contains("mango")) return findById("mango_fresh")
        if (labelString.contains("pizza")) return findById("pizza_slice")
        if (labelString.contains("burger") || labelString.contains("hamburger") || labelString.contains("sandwich")) return findById("burger_sandwich")
        if (labelString.contains("rice") || labelString.contains("biryani") || labelString.contains("pilaf")) {
            return if (labelString.contains("biryani")) findById("chicken_biryani") else findById("rice_cooked_white")
        }
        if (labelString.contains("bread") || labelString.contains("toast") || labelString.contains("bagel")) return findById("bread_whole_wheat")
        if (labelString.contains("roti") || labelString.contains("chapati") || labelString.contains("flatbread") || labelString.contains("tortilla")) return findById("roti_chapati")
        if (labelString.contains("egg") || labelString.contains("boiled egg")) return findById("boiled_egg")
        if (labelString.contains("omelet") || labelString.contains("omelette")) return findById("egg_omelette")
        if (labelString.contains("chicken") || labelString.contains("poultry") || labelString.contains("roast chicken")) return findById("chicken_breast_grilled")
        if (labelString.contains("salmon") || labelString.contains("fish") || labelString.contains("seafood")) return findById("salmon_fillet")
        if (labelString.contains("pasta") || labelString.contains("spaghetti") || labelString.contains("macaroni")) return findById("cooked_pasta")
        if (labelString.contains("noodle") || labelString.contains("ramen")) return findById("noodles_ramen")
        if (labelString.contains("salad") || labelString.contains("leaf vegetable") || labelString.contains("lettuce")) return findById("mixed_green_salad")
        if (labelString.contains("potato") || labelString.contains("french fries")) return findById("boiled_potatoes")
        if (labelString.contains("broccoli")) return findById("broccoli_steamed")
        if (labelString.contains("soup") || labelString.contains("broth") || labelString.contains("stew") || labelString.contains("curry")) return findById("dal_lentils_yellow")
        if (labelString.contains("oat") || labelString.contains("porridge") || labelString.contains("cereal")) return findById("rolled_oats")
        if (labelString.contains("coffee") || labelString.contains("espresso")) return findById("black_coffee")
        if (labelString.contains("tea") || labelString.contains("chai")) return findById("milk_tea_chai")
        if (labelString.contains("milk") || labelString.contains("dairy")) return findById("milk_whole")
        if (labelString.contains("yogurt") || labelString.contains("curd")) return findById("greek_yogurt_plain")

        // 2. Fuzzy search matching any label word
        for (label in labels) {
            val match = foods.firstOrNull { it.name.lowercase().contains(label.lowercase()) || it.category.lowercase().contains(label.lowercase()) }
            if (match != null) return match
        }

        return null
    }

    /**
     * Finds related/similar foods of the same type/category (e.g. for rice: brown rice, biryani, dal, oats)
     */
    fun getRelatedFoods(food: FoodItem): List<FoodItem> {
        val sameCategory = foods.filter { it.category == food.category && it.id != food.id }
        val commonPairs = when (food.id) {
            "rice_cooked_white" -> listOf("rice_cooked_brown", "chicken_biryani", "dal_lentils_yellow", "rolled_oats", "cooked_pasta")
            "rice_cooked_brown" -> listOf("rice_cooked_white", "chicken_biryani", "rolled_oats", "dal_lentils_yellow")
            "chicken_biryani" -> listOf("rice_cooked_white", "dal_lentils_yellow", "chicken_breast_grilled", "roti_chapati")
            "roti_chapati" -> listOf("bread_whole_wheat", "dal_lentils_yellow", "paneer_cottage_cheese", "chicken_breast_grilled")
            "bread_whole_wheat" -> listOf("roti_chapati", "burger_sandwich", "rolled_oats", "boiled_egg")
            "dal_lentils_yellow" -> listOf("chickpeas_chana", "rice_cooked_white", "roti_chapati", "chicken_biryani")
            "chicken_breast_grilled" -> listOf("chicken_biryani", "salmon_fillet", "boiled_egg", "egg_omelette")
            "boiled_egg" -> listOf("egg_omelette", "chicken_breast_grilled", "bread_whole_wheat", "milk_whole")
            "egg_omelette" -> listOf("boiled_egg", "bread_whole_wheat", "roti_chapati", "milk_whole")
            "banana" -> listOf("apple", "orange_citrus", "mango_fresh", "rolled_oats")
            "apple" -> listOf("banana", "orange_citrus", "mango_fresh", "mixed_green_salad")
            "pizza_slice" -> listOf("burger_sandwich", "cooked_pasta", "noodles_ramen")
            "burger_sandwich" -> listOf("pizza_slice", "bread_whole_wheat", "cooked_pasta")
            "mixed_green_salad" -> listOf("broccoli_steamed", "apple", "boiled_potatoes")
            "milk_tea_chai" -> listOf("black_coffee", "milk_whole", "bread_whole_wheat")
            "black_coffee" -> listOf("milk_tea_chai", "milk_whole")
            else -> emptyList()
        }.mapNotNull { findById(it) }

        val combined = (commonPairs + sameCategory).distinctBy { it.id }.filter { it.id != food.id }
        return if (combined.isNotEmpty()) combined.take(6) else foods.filter { it.id != food.id }.take(6)
    }

    /**
     * Looks up or constructs a custom food item with realistic nutrition if not in database
     */
    fun findOrCreateFood(name: String): FoodItem {
        val trimmed = name.trim()
        val exactOrFuzzy = foods.firstOrNull { 
            it.name.equals(trimmed, ignoreCase = true) || 
            it.name.contains(trimmed, ignoreCase = true) || 
            trimmed.contains(it.name, ignoreCase = true) 
        }
        if (exactOrFuzzy != null) return exactOrFuzzy

        val lower = trimmed.lowercase()
        val isGrainOrRice = lower.contains("rice") || lower.contains("chawal") || lower.contains("khichdi") || lower.contains("poha") || lower.contains("upma") || lower.contains("dosa") || lower.contains("idli") || lower.contains("grain")
        val isBread = lower.contains("roti") || lower.contains("paratha") || lower.contains("naan") || lower.contains("puri") || lower.contains("bread") || lower.contains("chapati")
        val isCurryOrDal = lower.contains("dal") || lower.contains("curry") || lower.contains("gravy") || lower.contains("sabzi") || lower.contains("soup") || lower.contains("rajma") || lower.contains("chole")
        val isSweetOrFruit = lower.contains("apple") || lower.contains("sweet") || lower.contains("kheer") || lower.contains("dessert") || lower.contains("fruit") || lower.contains("cake")

        val (cals, prot, carbs, fat, unit) = when {
            isGrainOrRice -> Quintuple(150f, 3.5f, 30f, 1.5f, PortionUnit.BOWLS)
            isBread -> Quintuple(270f, 8f, 48f, 5f, PortionUnit.PIECES)
            isCurryOrDal -> Quintuple(130f, 6.5f, 16f, 4.5f, PortionUnit.BOWLS)
            isSweetOrFruit -> Quintuple(120f, 1.5f, 26f, 2f, PortionUnit.CUPS)
            else -> Quintuple(160f, 7f, 22f, 4.5f, PortionUnit.BOWLS)
        }

        return FoodItem(
            id = "custom_${trimmed.lowercase().replace(" ", "_")}",
            name = trimmed.replaceFirstChar { it.uppercase() },
            category = "Custom Meal",
            defaultServingUnit = unit,
            defaultServingQuantity = 1.0f,
            caloriesPer100g = cals,
            proteinPer100g = prot,
            carbsPer100g = carbs,
            fatPer100g = fat,
            description = "Custom meal: $trimmed"
        )
    }

    private data class Quintuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
