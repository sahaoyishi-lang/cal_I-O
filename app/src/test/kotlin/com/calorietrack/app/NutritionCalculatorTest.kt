package com.calorietrack.app

import com.calorietrack.app.model.ActivityLevel
import com.calorietrack.app.model.DietaryPreference
import com.calorietrack.app.model.FitnessGoal
import com.calorietrack.app.model.Gender
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.model.UserProfile
import com.calorietrack.app.nutrition.NutritionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionCalculatorTest {

    @Test
    fun testBmrCalculation_Female() {
        // 60kg, 165cm, 25 years old, female
        // 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25 -> 1345
        val bmr = NutritionCalculator.calculateBmr(60f, 165f, 25, Gender.FEMALE)
        assertEquals(1345, bmr)
    }

    @Test
    fun testBmrCalculation_Male() {
        // 75kg, 178cm, 30 years old, male
        // 10*75 + 6.25*178 - 5*30 + 5 = 750 + 1112.5 - 150 + 5 = 1717.5 -> 1718
        val bmr = NutritionCalculator.calculateBmr(75f, 178f, 30, Gender.MALE)
        assertEquals(1718, bmr)
    }

    @Test
    fun testTargetsCalculation_WeightLoss() {
        val profile = UserProfile(
            age = 28,
            gender = Gender.FEMALE,
            weightKg = 65f,
            heightCm = 168f,
            activityLevel = ActivityLevel.MODERATE, // 1.55
            goal = FitnessGoal.LOSE_WEIGHT, // -500 kcal
            preference = DietaryPreference.BALANCED
        )

        val targets = NutritionCalculator.computeTargets(profile)

        assertTrue("TDEE should be higher than BMR", targets.tdee > targets.bmr)
        assertTrue("Weight loss calories should be less than TDEE", targets.dailyCalories < targets.tdee)
        assertTrue("Protein target should be positive", targets.proteinGrams > 80)
        assertTrue("Water target should be at least 2 liters", targets.waterIntakeMl >= 2000)
    }

    @Test
    fun testGenerateDietChart_ContainsAllMealSlots() {
        val profile = UserProfile(
            age = 25,
            gender = Gender.MALE,
            weightKg = 72f,
            heightCm = 175f,
            activityLevel = ActivityLevel.MODERATE,
            goal = FitnessGoal.BUILD_MUSCLE,
            preference = DietaryPreference.HIGH_PROTEIN
        )

        val targets = NutritionCalculator.computeTargets(profile)
        val chart = NutritionCalculator.generateDietChart(profile, targets)

        assertNotNull(chart)
        assertEquals(4, chart.mealPlans.size)

        val slots = chart.mealPlans.map { it.slot }
        assertTrue(slots.contains(MealSlot.BREAKFAST))
        assertTrue(slots.contains(MealSlot.LUNCH))
        assertTrue(slots.contains(MealSlot.SNACK))
        assertTrue(slots.contains(MealSlot.DINNER))

        for (plan in chart.mealPlans) {
            assertTrue("Each slot should have suggested meal options", plan.primaryOptions.isNotEmpty())
            assertTrue("Each slot should have target calories", plan.targetCalories > 0)
        }
    }
}

