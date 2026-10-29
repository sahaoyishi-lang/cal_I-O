package com.calorietrack.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorietrack.app.data.CalorieAppRepository
import com.calorietrack.app.model.ActivityLevel
import com.calorietrack.app.model.CustomizedDietChart
import com.calorietrack.app.model.DietaryPreference
import com.calorietrack.app.model.FitnessGoal
import com.calorietrack.app.model.Gender
import com.calorietrack.app.model.UserProfile
import com.calorietrack.app.nutrition.NutritionCalculator
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileSaved: (CustomizedDietChart) -> Unit,
    modifier: Modifier = Modifier,
    repository: CalorieAppRepository = CalorieAppRepository.instance
) {
    val existingProfile = repository.currentUser.collectAsState().value?.profile

    var ageText by remember { mutableStateOf(existingProfile?.age?.toString() ?: "26") }
    var weightText by remember { mutableStateOf(existingProfile?.weightKg?.roundToInt()?.toString() ?: "65") }
    var heightText by remember { mutableStateOf(existingProfile?.heightCm?.roundToInt()?.toString() ?: "170") }
    var selectedGender by remember { mutableStateOf(existingProfile?.gender ?: Gender.FEMALE) }
    var selectedActivity by remember { mutableStateOf(existingProfile?.activityLevel ?: ActivityLevel.MODERATE) }
    var selectedGoal by remember { mutableStateOf(existingProfile?.goal ?: FitnessGoal.LOSE_WEIGHT) }
    var selectedPreference by remember { mutableStateOf(existingProfile?.preference ?: DietaryPreference.BALANCED) }

    val age = ageText.toIntOrNull() ?: 26
    val weight = weightText.toFloatOrNull() ?: 65f
    val height = heightText.toFloatOrNull() ?: 170f

    // Live preview calculations
    val currentProfile = UserProfile(
        age = age,
        gender = selectedGender,
        weightKg = weight,
        heightCm = height,
        activityLevel = selectedActivity,
        goal = selectedGoal,
        preference = selectedPreference
    )
    val liveTargets = NutritionCalculator.computeTargets(currentProfile)

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Customize Your Diet Chart", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Preview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Live Calculated Target",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "BMI: ${String.format("%.1f", currentProfile.bmi)} (${currentProfile.bmiCategory})",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricPreviewItem("Daily Calories", "${liveTargets.dailyCalories}", "kcal/day")
                        MetricPreviewItem("BMR (Basal)", "${liveTargets.bmr}", "kcal")
                        MetricPreviewItem("TDEE", "${liveTargets.tdee}", "kcal")
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricPreviewItem("Protein", "${liveTargets.proteinGrams}g", "${(liveTargets.proteinGrams * 400 / liveTargets.dailyCalories)}%")
                        MetricPreviewItem("Carbs", "${liveTargets.carbsGrams}g", "${(liveTargets.carbsGrams * 400 / liveTargets.dailyCalories)}%")
                        MetricPreviewItem("Fats", "${liveTargets.fatGrams}g", "${(liveTargets.fatGrams * 900 / liveTargets.dailyCalories)}%")
                        MetricPreviewItem("Water", "${String.format("%.1f", liveTargets.waterIntakeMl / 1000f)}L", "Daily")
                    }
                }
            }

            // Section 1: Body Metrics
            Text("1. Personal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Gender Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gender.values().forEach { gender ->
                    val isSelected = selectedGender == gender
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGender = gender },
                        label = { Text(gender.name.lowercase().capitalize()) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Age, Weight, Height Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { if (it.length <= 3) ageText = it },
                    label = { Text("Age (yrs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { if (it.length <= 4) weightText = it },
                    label = { Text("Weight (kg)") },
                    leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { if (it.length <= 4) heightText = it },
                    label = { Text("Height (cm)") },
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Section 2: Activity Level
            Text("2. Activity Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.values().forEach { activity ->
                    val isSelected = selectedActivity == activity
                    Card(
                        onClick = { selectedActivity = activity },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedActivity = activity }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(activity.displayName, fontWeight = FontWeight.Bold)
                                Text(
                                    activity.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Goal & Dietary Preference
            Text("3. Goal & Diet Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Text("Primary Fitness Goal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FitnessGoal.values().take(2).forEach { goal ->
                    val isSelected = selectedGoal == goal
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGoal = goal },
                        label = { Text(goal.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FitnessGoal.values().drop(2).forEach { goal ->
                    val isSelected = selectedGoal == goal
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGoal = goal },
                        label = { Text(goal.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Dietary Preference", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DietaryPreference.values().forEach { pref ->
                    val isSelected = selectedPreference == pref
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPreference = pref },
                        label = { Text(pref.displayName) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save & Generate Diet Chart Button
            Button(
                onClick = {
                    val chart = repository.saveProfile(currentProfile)
                    onProfileSaved(chart)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Generate My Healthy Diet Chart",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricPreviewItem(label: String, value: String, sublabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

