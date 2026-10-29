package com.calorietrack.app.nutrition

import com.calorietrack.app.model.CustomizedDietChart
import com.calorietrack.app.model.DietaryPreference
import com.calorietrack.app.model.FitnessGoal
import com.calorietrack.app.model.Gender
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.model.MealSlotDietPlan
import com.calorietrack.app.model.NutritionTargets
import com.calorietrack.app.model.SuggestedMealItem
import com.calorietrack.app.model.UserProfile
import kotlin.math.roundToInt

object NutritionCalculator {

    /**
     * Calculates Basal Metabolic Rate (BMR) using Mifflin-St Jeor formula
     */
    fun calculateBmr(weightKg: Float, heightCm: Float, age: Int, gender: Gender): Int {
        val base = (10.0f * weightKg) + (6.25f * heightCm) - (5.0f * age)
        val bmr = when (gender) {
            Gender.MALE -> base + 5f
            Gender.FEMALE -> base - 161f
            Gender.OTHER -> base - 78f // intermediate average
        }
        return bmr.roundToInt().coerceAtLeast(1000)
    }

    /**
     * Computes complete NutritionTargets including TDEE, Calorie Goal, Macros, and Water
     */
    fun computeTargets(profile: UserProfile): NutritionTargets {
        val bmr = calculateBmr(profile.weightKg, profile.heightCm, profile.age, profile.gender)
        val tdee = (bmr * profile.activityLevel.multiplier).roundToInt()

        // Calorie target based on fitness goal
        var targetCalories = tdee + profile.goal.calorieAdjustment
        // Safety bounds
        val minCalories = if (profile.gender == Gender.FEMALE) 1200 else 1450
        targetCalories = targetCalories.coerceAtLeast(minCalories)

        // Protein calculation: grams per kg based on goal & preference
        val proteinPerKg = when (profile.goal) {
            FitnessGoal.BUILD_MUSCLE -> 2.0f
            FitnessGoal.LOSE_WEIGHT -> 1.8f
            FitnessGoal.MILD_WEIGHT_LOSS -> 1.6f
            FitnessGoal.MAINTAIN -> 1.4f
        }
        val proteinGrams = (profile.weightKg * proteinPerKg).roundToInt()
        val proteinCalories = proteinGrams * 4

        // Fat calculation: 25% - 30% of total calories
        val fatPercentage = if (profile.preference == DietaryPreference.LOW_CARB) 0.40f else 0.28f
        val fatCalories = targetCalories * fatPercentage
        val fatGrams = (fatCalories / 9.0f).roundToInt()

        // Carbs calculation: remainder calories
        val remainingCalories = (targetCalories - proteinCalories - fatCalories).coerceAtLeast(200.0)
        val carbsGrams = (remainingCalories / 4.0).roundToInt()

        // Water intake: 35 ml per kg body weight
        val waterIntakeMl = (profile.weightKg * 35f).roundToInt().coerceAtLeast(2000)

        return NutritionTargets(
            dailyCalories = targetCalories,
            bmr = bmr,
            tdee = tdee,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            waterIntakeMl = waterIntakeMl
        )
    }

    /**
     * Generates a fully personalized healthy diet chart structured across 4 meal slots
     */
    fun generateDietChart(profile: UserProfile, targets: NutritionTargets): CustomizedDietChart {
        val totalCal = targets.dailyCalories

        // Meal slot distribution: Breakfast 25%, Lunch 35%, Afternoon Snack 15%, Dinner 25%
        val breakfastCal = (totalCal * 0.25f).roundToInt()
        val lunchCal = (totalCal * 0.35f).roundToInt()
        val snackCal = (totalCal * 0.15f).roundToInt()
        val dinnerCal = (totalCal * 0.25f).roundToInt()

        val mealPlans = listOf(
            createBreakfastPlan(breakfastCal, profile.preference),
            createLunchPlan(lunchCal, profile.preference),
            createSnackPlan(snackCal, profile.preference),
            createDinnerPlan(dinnerCal, profile.preference)
        )

        val guidelines = listOf(
            "Hydration: Drink at least ${(targets.waterIntakeMl / 1000f)} liters of water distributed throughout the day.",
            "Meal Timing: Try not to skip breakfast, and maintain consistent meal hours.",
            "Portion Control: Use our Camera or Voice logger to measure servings accurately (e.g. 1 bowl of rice ~ 150g).",
            "Fiber & Vegetables: Include green vegetables or salad with lunch and dinner for gut health.",
            "Sleep: Prioritize 7-8 hours of quality sleep to optimize metabolism and hormone regulation."
        )

        val summary = "Customized for ${profile.age} yrs, ${profile.weightKg} kg, ${profile.heightCm} cm (${profile.goal.displayName} | ${profile.preference.displayName})"

        return CustomizedDietChart(
            userSummary = summary,
            totalDailyCalories = totalCal,
            proteinTargetGrams = targets.proteinGrams,
            carbsTargetGrams = targets.carbsGrams,
            fatTargetGrams = targets.fatGrams,
            waterTargetLiters = targets.waterIntakeMl / 1000f,
            mealPlans = mealPlans,
            generalGuidelines = guidelines
        )
    }

    private fun createBreakfastPlan(calories: Int, pref: DietaryPreference): MealSlotDietPlan {
        val options = when (pref) {
            DietaryPreference.VEGAN, DietaryPreference.VEGETARIAN -> listOf(
                SuggestedMealItem("Rolled Oats with Almond Milk & Berries", "1 bowl (60g oats + 200ml milk)", (calories * 0.65f).toInt(), 14, 52, 9, listOf("Add 1 tbsp chia seeds", "Topped with sliced banana")),
                SuggestedMealItem("Sprouted Moong Salad or Paneer Bhurji", "1 bowl (150g)", (calories * 0.35f).toInt(), 16, 12, 10, listOf("Tofu scramble as vegan swap"))
            )
            DietaryPreference.HIGH_PROTEIN -> listOf(
                SuggestedMealItem("3 Scrambled Whole Eggs + Spinach", "3 eggs + 1 cup spinach", (calories * 0.55f).toInt(), 22, 4, 16, listOf("Egg whites swap")),
                SuggestedMealItem("Whole Grain Sourdough Toast & Avocado", "1 slice + 1/2 avocado", (calories * 0.45f).toInt(), 8, 26, 14, listOf("Low-carb protein bread"))
            )
            DietaryPreference.LOW_CARB -> listOf(
                SuggestedMealItem("Avocado Omelette with Olive Oil", "2 eggs + 1/2 avocado", (calories * 0.7f).toInt(), 18, 6, 28, listOf("Feta cheese crumbles")),
                SuggestedMealItem("Handful of Walnuts & Chia Pudding", "30g nuts + 150ml almond milk", (calories * 0.3f).toInt(), 8, 5, 20)
            )
            else -> listOf(
                SuggestedMealItem("Oatmeal with Greek Yogurt & Honey", "1 bowl (50g oats + 100g yogurt)", (calories * 0.6f).toInt(), 18, 48, 8, listOf("Fresh berries topping")),
                SuggestedMealItem("2 Boiled Eggs & Whole Wheat Toast", "2 eggs + 1 slice toast", (calories * 0.4f).toInt(), 16, 18, 11)
            )
        }
        return MealSlotDietPlan(
            slot = MealSlot.BREAKFAST,
            targetCalories = calories,
            timing = "07:30 AM - 09:00 AM",
            primaryOptions = options,
            tips = "Kickstart your metabolism with high fiber and clean protein. Hydrate with 1 full glass of warm water before breakfast."
        )
    }

    private fun createLunchPlan(calories: Int, pref: DietaryPreference): MealSlotDietPlan {
        val options = when (pref) {
            DietaryPreference.VEGAN, DietaryPreference.VEGETARIAN -> listOf(
                SuggestedMealItem("Steamed Brown Rice or Quinoa", "1 bowl (150g)", (calories * 0.35f).toInt(), 6, 45, 3, listOf("Whole wheat roti (2 pcs)")),
                SuggestedMealItem("Yellow Lentil Dal / Tofu Curry", "1 bowl (200g)", (calories * 0.45f).toInt(), 18, 30, 8, listOf("Chickpea chana masala")),
                SuggestedMealItem("Cucumber, Tomato & Carrot Salad", "1 plate (150g)", (calories * 0.20f).toInt(), 3, 10, 1, listOf("Lemon-mint dressing"))
            )
            DietaryPreference.HIGH_PROTEIN -> listOf(
                SuggestedMealItem("Grilled Chicken Breast or Salmon", "1 fillet (180g)", (calories * 0.50f).toInt(), 42, 0, 8, listOf("Lean beef or grilled fish")),
                SuggestedMealItem("Brown Rice or Sweet Potato", "1 bowl (140g)", (calories * 0.35f).toInt(), 5, 38, 2, listOf("Quinoa")),
                SuggestedMealItem("Steamed Broccoli & Green Beans", "1 cup (150g)", (calories * 0.15f).toInt(), 4, 12, 1)
            )
            DietaryPreference.LOW_CARB -> listOf(
                SuggestedMealItem("Grilled Herb Chicken or Paneer Steak", "200g", (calories * 0.65f).toInt(), 40, 2, 22),
                SuggestedMealItem("Cauliflower Mash with Olive Oil", "1 bowl (150g)", (calories * 0.20f).toInt(), 4, 8, 12),
                SuggestedMealItem("Mixed Green Salad with Seeds", "1 bowl (100g)", (calories * 0.15f).toInt(), 3, 4, 10)
            )
            else -> listOf(
                SuggestedMealItem("Cooked Brown Rice or Roti", "1 bowl rice (150g) / 2 rotis", (calories * 0.35f).toInt(), 7, 45, 3),
                SuggestedMealItem("Grilled Chicken or Paneer/Dal", "1 portion (150g)", (calories * 0.45f).toInt(), 28, 12, 11),
                SuggestedMealItem("Fresh Garden Salad & Curd", "1 small bowl (100g)", (calories * 0.20f).toInt(), 6, 8, 4)
            )
        }
        return MealSlotDietPlan(
            slot = MealSlot.LUNCH,
            targetCalories = calories,
            timing = "12:30 PM - 02:00 PM",
            primaryOptions = options,
            tips = "Lunch is your primary energy fuel. Ensure a 1:1 plate ratio of greens/vegetables to your grains."
        )
    }

    private fun createSnackPlan(calories: Int, pref: DietaryPreference): MealSlotDietPlan {
        val options = when (pref) {
            DietaryPreference.VEGAN -> listOf(
                SuggestedMealItem("Roasted Makhana (Foxnuts) & Roasted Chana", "1 cup (40g)", (calories * 0.6f).toInt(), 8, 24, 4),
                SuggestedMealItem("Green Tea with Chia Seeds & Sliced Apple", "1 cup + 1 small apple", (calories * 0.4f).toInt(), 2, 22, 1)
            )
            DietaryPreference.HIGH_PROTEIN -> listOf(
                SuggestedMealItem("Greek Yogurt with Handful of Almonds", "150g yogurt + 15g almonds", (calories * 0.7f).toInt(), 19, 10, 11),
                SuggestedMealItem("1 Whey Protein Shake or Sprouted Grams", "1 scoop in water / 1 bowl", (calories * 0.3f).toInt(), 24, 4, 2)
            )
            else -> listOf(
                SuggestedMealItem("Greek Yogurt or Cottage Cheese with Berries", "1 bowl (120g)", (calories * 0.6f).toInt(), 12, 15, 4),
                SuggestedMealItem("Roasted Almonds & Green Tea", "15-20 almonds", (calories * 0.4f).toInt(), 6, 5, 14)
            )
        }
        return MealSlotDietPlan(
            slot = MealSlot.SNACK,
            targetCalories = calories,
            timing = "04:30 PM - 05:30 PM",
            primaryOptions = options,
            tips = "A healthy snack prevents overeating at dinner. Avoid sugary snacks or processed fried foods."
        )
    }

    private fun createDinnerPlan(calories: Int, pref: DietaryPreference): MealSlotDietPlan {
        val options = when (pref) {
            DietaryPreference.VEGAN, DietaryPreference.VEGETARIAN -> listOf(
                SuggestedMealItem("Tofu or Paneer Stir-fry with Veggies", "1 bowl (200g)", (calories * 0.6f).toInt(), 22, 14, 16),
                SuggestedMealItem("Vegetable Soup or Quinoa Bowl", "1 large bowl (250ml)", (calories * 0.4f).toInt(), 8, 28, 4)
            )
            DietaryPreference.HIGH_PROTEIN -> listOf(
                SuggestedMealItem("Baked Salmon / White Fish or Chicken Breast", "1 fillet (160g)", (calories * 0.55f).toInt(), 36, 0, 10),
                SuggestedMealItem("Roasted Asparagus & Bell Peppers", "1 plate (150g)", (calories * 0.20f).toInt(), 4, 10, 5),
                SuggestedMealItem("Small Baked Sweet Potato", "100g", (calories * 0.25f).toInt(), 2, 21, 0)
            )
            DietaryPreference.LOW_CARB -> listOf(
                SuggestedMealItem("Pan-seared Salmon or Tofu with Spinach", "180g", (calories * 0.75f).toInt(), 34, 3, 20),
                SuggestedMealItem("Creamy Zucchini & Mushroom Sauté", "1 cup", (calories * 0.25f).toInt(), 4, 6, 12)
            )
            else -> listOf(
                SuggestedMealItem("Grilled Chicken / Paneer Tikka with Lemon", "1 plate (160g)", (calories * 0.55f).toInt(), 30, 8, 12),
                SuggestedMealItem("Clear Vegetable Lentil Soup", "1 big bowl (250ml)", (calories * 0.25f).toInt(), 6, 18, 2),
                SuggestedMealItem("1 Multigrain Roti or Half Bowl Brown Rice", "1 roti (45g)", (calories * 0.20f).toInt(), 4, 22, 2)
            )
        }
        return MealSlotDietPlan(
            slot = MealSlot.DINNER,
            targetCalories = calories,
            timing = "07:30 PM - 09:00 PM",
            primaryOptions = options,
            tips = "Keep dinner lighter than lunch and finish at least 2 to 3 hours before going to bed for optimal sleep & digestion."
        )
    }
}

