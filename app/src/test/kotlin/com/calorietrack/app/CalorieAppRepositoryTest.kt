package com.calorietrack.app

import com.calorietrack.app.data.CalorieAppRepository
import com.calorietrack.app.model.LogSource
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.model.PortionUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CalorieAppRepositoryTest {

    private lateinit var repo: CalorieAppRepository

    @Before
    fun setup() {
        repo = CalorieAppRepository.instance
    }

    @Test
    fun testSignUpAndSignInFlow() {
        val email = "athlete_${System.currentTimeMillis()}@test.com"
        val password = "securePassword123"
        val name = "Test Runner"

        val signupResult = repo.signUp(email, password, name)
        assertTrue("Signup should succeed", signupResult.isSuccess)

        val user = signupResult.getOrNull()
        assertNotNull(user)
        assertEquals(email, user?.email)
        assertFalse("Profile should be incomplete initially", user!!.isProfileComplete)

        // Sign in
        val signinResult = repo.signIn(email, password)
        assertTrue("Signin should succeed", signinResult.isSuccess)
    }

    @Test
    fun testLogMealAndDailySummary() {
        val initialCount = repo.loggedMeals.value.size
        val entry = LoggedMealEntry(
            id = UUID.randomUUID().toString(),
            foodName = "Test Rice",
            mealSlot = MealSlot.LUNCH,
            quantity = 1f,
            unit = PortionUnit.BOWLS,
            calories = 195,
            proteinGrams = 4,
            carbsGrams = 42,
            fatGrams = 1,
            source = LogSource.CAMERA_AI
        )

        repo.logMeal(entry)
        assertEquals(initialCount + 1, repo.loggedMeals.value.size)

        val summary = repo.getDailySummary()
        assertTrue("Total calories should include logged meal", summary.totalCalories >= 195)

        // Delete meal
        repo.deleteMeal(entry.id)
        assertEquals(initialCount, repo.loggedMeals.value.size)
    }
}

