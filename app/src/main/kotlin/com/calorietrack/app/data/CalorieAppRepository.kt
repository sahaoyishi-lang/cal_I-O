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

private data class UserSessionData(
    val passwordHash: String,
    var user: User,
    var dietChart: CustomizedDietChart? = null,
    val meals: MutableList<LoggedMealEntry> = mutableListOf(),
    var waterMl: Int = 0
)

class CalorieAppRepository private constructor() {

    // When the app is opened, start with no active user session (pure fresh state)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _dietChart = MutableStateFlow<CustomizedDietChart?>(null)
    val dietChart: StateFlow<CustomizedDietChart?> = _dietChart.asStateFlow()

    private val _loggedMeals = MutableStateFlow<List<LoggedMealEntry>>(emptyList())
    val loggedMeals: StateFlow<List<LoggedMealEntry>> = _loggedMeals.asStateFlow()

    private val _waterDrankMl = MutableStateFlow(0)
    val waterDrankMl: StateFlow<Int> = _waterDrankMl.asStateFlow()

    // Isolated per-user storage (key = normalized email)
    private val userDatabase = mutableMapOf<String, UserSessionData>()

    /**
     * Create a brand new user account with Email & Password.
     * Starts with a fresh slate (0 calories, 0 water, clean profile).
     */
    fun signUp(email: String, pass: String, name: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your name"))
        }
        if (userDatabase.containsKey(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("An account with this email already exists. Please Sign In."))
        }

        val newUser = User(
            email = trimmedEmail,
            name = name.trim(),
            isProfileComplete = false
        )

        val session = UserSessionData(
            passwordHash = pass,
            user = newUser,
            dietChart = null,
            meals = mutableListOf(),
            waterMl = 0
        )
        userDatabase[trimmedEmail] = session

        applyUserSession(session)
        return Result.success(newUser)
    }

    /**
     * Sign in to an existing account with Email & Password.
     */
    fun signIn(email: String, pass: String): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        val session = userDatabase[trimmedEmail]
            ?: return Result.failure(IllegalArgumentException("No account found for $trimmedEmail. Please Sign Up first."))

        if (session.passwordHash != pass && session.passwordHash != "google_oauth") {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        applyUserSession(session)
        return Result.success(session.user)
    }

    /**
     * Sign In or Sign Up with Google.
     * If user exists, log them in. If new, create a clean account for them.
     */
    fun signInWithGoogle(googleEmail: String, googleName: String): Result<User> {
        val trimmedEmail = googleEmail.trim().lowercase()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.failure(IllegalArgumentException("Invalid Google email address"))
        }

        val existingSession = userDatabase[trimmedEmail]
        if (existingSession != null) {
            applyUserSession(existingSession)
            return Result.success(existingSession.user)
        }

        // New Google account
        val displayName = if (googleName.isNotBlank()) googleName.trim() else trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val newUser = User(
            email = trimmedEmail,
            name = displayName,
            isProfileComplete = false
        )

        val newSession = UserSessionData(
            passwordHash = "google_oauth",
            user = newUser,
            dietChart = null,
            meals = mutableListOf(),
            waterMl = 0
        )
        userDatabase[trimmedEmail] = newSession

        applyUserSession(newSession)
        return Result.success(newUser)
    }

    /**
     * Sign out the active user and reset memory state.
     */
    fun signOut() {
        _currentUser.value = null
        _dietChart.value = null
        _loggedMeals.value = emptyList()
        _waterDrankMl.value = 0
    }

    /**
     * Save profile details (age, weight, height, goal, preference)
     * and compute personalized calorie targets & diet chart.
     */
    fun saveProfile(profile: UserProfile): CustomizedDietChart {
        val targets = NutritionCalculator.computeTargets(profile)
        val chart = NutritionCalculator.generateDietChart(profile, targets)

        val currentUserObj = _currentUser.value ?: User(email = "athlete@nutritrack.app", name = "Athlete")
        val updatedUser = currentUserObj.copy(
            isProfileComplete = true,
            profile = profile,
            targets = targets
        )

        _currentUser.value = updatedUser
        _dietChart.value = chart

        // Persist to user session
        val session = userDatabase[updatedUser.email.lowercase()]
        if (session != null) {
            session.user = updatedUser
            session.dietChart = chart
        } else {
            userDatabase[updatedUser.email.lowercase()] = UserSessionData(
                passwordHash = "local",
                user = updatedUser,
                dietChart = chart,
                meals = _loggedMeals.value.toMutableList(),
                waterMl = _waterDrankMl.value
            )
        }

        return chart
    }

    fun logMeal(entry: LoggedMealEntry) {
        val current = _loggedMeals.value.toMutableList()
        current.add(0, entry)
        _loggedMeals.value = current

        val email = _currentUser.value?.email?.lowercase()
        if (email != null) {
            userDatabase[email]?.meals?.clear()
            userDatabase[email]?.meals?.addAll(current)
        }
    }

    fun deleteMeal(id: String) {
        val filtered = _loggedMeals.value.filter { it.id != id }
        _loggedMeals.value = filtered

        val email = _currentUser.value?.email?.lowercase()
        if (email != null) {
            userDatabase[email]?.meals?.clear()
            userDatabase[email]?.meals?.addAll(filtered)
        }
    }

    fun addWater(ml: Int = 250) {
        _waterDrankMl.value += ml
        val email = _currentUser.value?.email?.lowercase()
        if (email != null) {
            userDatabase[email]?.waterMl = _waterDrankMl.value
        }
    }

    fun resetWater() {
        _waterDrankMl.value = 0
        val email = _currentUser.value?.email?.lowercase()
        if (email != null) {
            userDatabase[email]?.waterMl = 0
        }
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

    private fun applyUserSession(session: UserSessionData) {
        _currentUser.value = session.user
        _dietChart.value = session.dietChart
        _loggedMeals.value = session.meals.toList()
        _waterDrankMl.value = session.waterMl
    }

    companion object {
        val instance by lazy { CalorieAppRepository() }
    }
}
