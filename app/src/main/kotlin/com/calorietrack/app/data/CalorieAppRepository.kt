package com.calorietrack.app.data

import com.calorietrack.app.model.CustomizedDietChart
import com.calorietrack.app.model.DietaryPreference
import com.calorietrack.app.model.FitnessGoal
import com.calorietrack.app.model.Gender
import com.calorietrack.app.model.ActivityLevel
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.model.NutritionTargets
import com.calorietrack.app.model.PortionUnit
import com.calorietrack.app.model.User
import com.calorietrack.app.model.UserProfile
import com.calorietrack.app.nutrition.NutritionCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class DailySummary(
    val totalCalories: Int,
    val targetCalories: Int,
    val remainingCalories: Int,
    val totalProtein: Int,
    val targetProtein: Int,
    val totalCarbs: Int,
    val targetCarbs: Int,
    val totalFat: Int,
    val targetFat: Int,
    val waterDrankMl: Int,
    val targetWaterMl: Int
) {
    val calorieProgress: Float
        get() = if (targetCalories > 0) (totalCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f) else 0f

    val proteinProgress: Float
        get() = if (targetProtein > 0) (totalProtein.toFloat() / targetProtein.toFloat()).coerceIn(0f, 1f) else 0f

    val carbsProgress: Float
        get() = if (targetCarbs > 0) (totalCarbs.toFloat() / targetCarbs.toFloat()).coerceIn(0f, 1f) else 0f

    val fatProgress: Float
        get() = if (targetFat > 0) (totalFat.toFloat() / targetFat.toFloat()).coerceIn(0f, 1f) else 0f
}

class CalorieAppRepository private constructor() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _dietChart = MutableStateFlow<CustomizedDietChart?>(null)
    val dietChart: StateFlow<CustomizedDietChart?> = _dietChart.asStateFlow()

    private val _loggedMeals = MutableStateFlow<List<LoggedMealEntry>>(emptyList())
    val loggedMeals: StateFlow<List<LoggedMealEntry>> = _loggedMeals.asStateFlow()

    private val _waterDrankMl = MutableStateFlow(1250) // initial water sample
    val waterDrankMl: StateFlow<Int> = _waterDrankMl.asStateFlow()

    // Mock registered users database for local persistence
    private val registeredUsers = mutableMapOf<String, Pair<String, User>>()

    init {
        // Pre-populate demo user for smooth testing
        val demoEmail = "demo@healthy.com"
        val demoProfile = UserProfile(
            age = 26,
            gender = Gender.FEMALE,
            weightKg = 62.0f,
            heightCm = 165.0f,
            activityLevel = ActivityLevel.MODERATE,
            goal = FitnessGoal.LOSE_WEIGHT,
            preference = DietaryPreference.BALANCED
        )
        val demoTargets = NutritionCalculator.computeTargets(demoProfile)
        val demoUser = User(
            email = demoEmail,
            name = "Alex Morgan",
            isProfileComplete = true,
            profile = demoProfile,
            targets = demoTargets
        )
        registeredUsers[demoEmail] = Pair("password123", demoUser)
        _currentUser.value = demoUser
        _dietChart.value = NutritionCalculator.generateDietChart(demoProfile, demoTargets)

        // Seed some initial logged meals for realistic experience
        _loggedMeals.value = listOf(
            LoggedMealEntry(
                id = UUID.randomUUID().toString(),
                foodName = "Rolled Oats with Berries",
                mealSlot = MealSlot.BREAKFAST,
                quantity = 1f,
                unit = PortionUnit.BOWLS,
                calories = 320,
                proteinGrams = 12,
                carbsGrams = 54,
                fatGrams = 6,
                source = com.calorietrack.app.model.LogSource.MANUAL,
                timestamp = System.currentTimeMillis() - 14400000
            ),
            LoggedMealEntry(
                id = UUID.randomUUID().toString(),
                foodName = "Boiled Whole Egg",
                mealSlot = MealSlot.BREAKFAST,
                quantity = 2f,
                unit = PortionUnit.PIECES,
                calories = 156,
                proteinGrams = 13,
                carbsGrams = 2,
                fatGrams = 11,
                source = com.calorietrack.app.model.LogSource.VOICE,
                timestamp = System.currentTimeMillis() - 14000000
            )
        )
    }

    fun signUp(email: String, pass: String, name: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }

        val newUser = User(
            email = trimmedEmail,
            name = name.trim(),
            isProfileComplete = false
        )
        registeredUsers[trimmedEmail] = Pair(pass, newUser)
        _currentUser.value = newUser
        _dietChart.value = null
        _loggedMeals.value = emptyList()
        return Result.success(newUser)
    }

    fun signIn(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val entry = registeredUsers[trimmedEmail]
            ?: return Result.failure(IllegalArgumentException("No account found with this email"))

        if (entry.first != pass) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }

        val user = entry.second
        _currentUser.value = user
        if (user.profile != null && user.targets != null) {
            _dietChart.value = NutritionCalculator.generateDietChart(user.profile, user.targets)
        }
        return Result.success(user)
    }

    fun signOut() {
        _currentUser.value = null
        _dietChart.value = null
        _loggedMeals.value = emptyList()
    }

    fun saveProfile(profile: UserProfile): CustomizedDietChart {
        val targets = NutritionCalculator.computeTargets(profile)
        val chart = NutritionCalculator.generateDietChart(profile, targets)

        val user = _currentUser.value ?: User(email = "user@calorie.app", name = "User")
        val updatedUser = user.copy(
            isProfileComplete = true,
            profile = profile,
            targets = targets
        )
        _currentUser.value = updatedUser
        registeredUsers[updatedUser.email] = Pair(registeredUsers[updatedUser.email]?.first ?: "pass", updatedUser)
        _dietChart.value = chart
        return chart
    }

    fun logMeal(entry: LoggedMealEntry) {
        val current = _loggedMeals.value.toMutableList()
        current.add(0, entry)
        _loggedMeals.value = current
    }

    fun deleteMeal(id: String) {
        _loggedMeals.value = _loggedMeals.value.filter { it.id != id }
    }

    fun addWater(ml: Int = 250) {
        _waterDrankMl.value += ml
    }

    fun resetWater() {
        _waterDrankMl.value = 0
    }

    fun getDailySummary(): DailySummary {
        val user = _currentUser.value
        val targetCal = user?.targets?.dailyCalories ?: 2000
        val targetProt = user?.targets?.proteinGrams ?: 110
        val targetCarb = user?.targets?.carbsGrams ?: 220
        val targetFat = user?.targets?.fatGrams ?: 65
        val targetWater = user?.targets?.waterIntakeMl ?: 2500

        var totalCal = 0
        var totalProt = 0
        var totalCarb = 0
        var totalFat = 0

        for (meal in _loggedMeals.value) {
            totalCal += meal.calories
            totalProt += meal.proteinGrams
            totalCarb += meal.carbsGrams
            totalFat += meal.fatGrams
        }

        return DailySummary(
            totalCalories = totalCal,
            targetCalories = targetCal,
            remainingCalories = (targetCal - totalCal).coerceAtLeast(0),
            totalProtein = totalProt,
            targetProtein = targetProt,
            totalCarbs = totalCarb,
            targetCarbs = targetCarb,
            totalFat = totalFat,
            targetFat = targetFat,
            waterDrankMl = _waterDrankMl.value,
            targetWaterMl = targetWater
        )
    }

    companion object {
        val instance by lazy { CalorieAppRepository() }
    }
}

