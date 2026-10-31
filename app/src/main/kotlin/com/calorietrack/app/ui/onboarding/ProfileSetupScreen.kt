package com.calorietrack.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
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
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileSaved: (CustomizedDietChart) -> Unit,
    modifier: Modifier = Modifier,
    repository: CalorieAppRepository = CalorieAppRepository.instance
) {
    val existingProfile = repository.currentUser.collectAsState().value?.profile

    // Do NOT assume or pre-fill default values for new users — let them enter their own details!
    var ageText by remember { mutableStateOf(existingProfile?.age?.toString() ?: "") }
    var weightText by remember { mutableStateOf(existingProfile?.weightKg?.roundToInt()?.toString() ?: "") }
    var heightText by remember { mutableStateOf(existingProfile?.heightCm?.roundToInt()?.toString() ?: "") }
    var selectedGender by remember { mutableStateOf(existingProfile?.gender ?: Gender.FEMALE) }
    var selectedActivity by remember { mutableStateOf(existingProfile?.activityLevel ?: ActivityLevel.MODERATE) }
    var selectedGoal by remember { mutableStateOf(existingProfile?.goal ?: FitnessGoal.LOSE_WEIGHT) }
    var selectedPreference by remember { mutableStateOf(existingProfile?.preference ?: DietaryPreference.BALANCED) }

    val age = ageText.toIntOrNull()
    val weight = weightText.toFloatOrNull()
    val height = heightText.toFloatOrNull()

    // Validation: Require the user to supply their real details
    val isDetailsValid = age != null && age in 10..120 &&
            weight != null && weight in 20f..350f &&
            height != null && height in 60f..260f

    // Only compute live preview if the user has provided their measurements
    val currentProfile = if (isDetailsValid) {
        UserProfile(
            age = age!!,
            gender = selectedGender,
            weightKg = weight!!,
            heightCm = height!!,
            activityLevel = selectedActivity,
            goal = selectedGoal,
            preference = selectedPreference
        )
    } else null

    val liveTargets = currentProfile?.let { NutritionCalculator.computeTargets(it) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Personalize Your Diet Plan", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Instruction
            Text(
                text = "Enter your body measurements so NutriTrack can accurately compute your BMR, daily calorie budget, and custom meal chart.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dynamic Target Preview Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDetailsValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isDetailsValid && liveTargets != null && currentProfile != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Your Calculated Targets",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "BMI: ${String.format(Locale.US, "%.1f", currentProfile.bmi)} (${currentProfile.bmiCategory})",
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
                            MetricPreviewItem("TDEE (Burn)", "${liveTargets.tdee}", "kcal")
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                "Protein: ${liveTargets.proteinGrams}g",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Carbs: ${liveTargets.carbsGrams}g",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Fat: ${liveTargets.fatGrams}g",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Please input your age, current weight, and height below to calculate your tailored diet chart.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section 1: User Input Measurements
            Text("1. Enter Your Body Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Gender Selection
            Text("Gender", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gender.values().forEach { gender ->
                    val isSelected = selectedGender == gender
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGender = gender },
                        label = { Text(gender.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Age Input
            OutlinedTextField(
                value = ageText,
                onValueChange = {
                    if (it.length <= 3 && it.all { ch -> ch.isDigit() }) ageText = it
                },
                label = { Text("Your Age (years)*") },
                placeholder = { Text("e.g. 24") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Weight Input
            OutlinedTextField(
                value = weightText,
                onValueChange = {
                    if (it.length <= 5 && it.all { ch -> ch.isDigit() || ch == '.' }) weightText = it
                },
                label = { Text("Current Weight (kg)*") },
                placeholder = { Text("e.g. 62") },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Height Input
            OutlinedTextField(
                value = heightText,
                onValueChange = {
                    if (it.length <= 5 && it.all { ch -> ch.isDigit() || ch == '.' }) heightText = it
                },
                label = { Text("Height (cm)*") },
                placeholder = { Text("e.g. 165") },
                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Section 2: Activity Level
            Text("2. Activity Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.values().forEach { activity ->
                    val isSelected = selectedActivity == activity
                    Card(
                        onClick = { selectedActivity = activity },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedActivity = activity }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    activity.displayName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
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
                    if (isDetailsValid && currentProfile != null) {
                        val chart = repository.saveProfile(currentProfile)
                        onProfileSaved(chart)
                    }
                },
                enabled = isDetailsValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDetailsValid) "Generate My Healthy Diet Chart ✨" else "Enter Age, Weight & Height to Continue",
                    fontSize = 15.sp,
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
