package com.calorietrack.app.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorietrack.app.data.CalorieAppRepository
import com.calorietrack.app.data.DailySummary
import com.calorietrack.app.model.LogSource
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.ui.upload.FoodUploadSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    repository: CalorieAppRepository = CalorieAppRepository.instance
) {
    val user by repository.currentUser.collectAsState()
    val loggedMeals by repository.loggedMeals.collectAsState()
    val waterMl by repository.waterDrankMl.collectAsState()
    var showUploadSheet by remember { mutableStateOf(false) }

    val summary = remember(loggedMeals, user, waterMl) {
        repository.getDailySummary()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hi, ${user?.name ?: "Athlete"} 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Today's Calorie & Macro Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Log Food") },
                text = { Text("Log Food Intake", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Main Calorie Ring & Remaining Overview
            item {
                CalorieGaugeCard(summary = summary, onOpenUpload = { showUploadSheet = true })
            }

            // Macronutrient Progress Bars (Protein, Carbs, Fats)
            item {
                MacroCard(summary = summary)
            }

            // Water Intake Tracker
            item {
                WaterTrackerCard(
                    drankMl = summary.waterDrankMl,
                    targetMl = summary.targetWaterMl,
                    onAddWater = { repository.addWater(250) },
                    onReset = { repository.resetWater() }
                )
            }

            // Meal Slot Sections
            items(MealSlot.values()) { slot ->
                val slotMeals = loggedMeals.filter { it.mealSlot == slot }
                MealSlotDiarySection(
                    slot = slot,
                    meals = slotMeals,
                    onDelete = { repository.deleteMeal(it.id) },
                    onAddClick = { showUploadSheet = true }
                )
            }
        }

        if (showUploadSheet) {
            FoodUploadSheet(
                onDismiss = { showUploadSheet = false },
                onLogMeal = { entry -> repository.logMeal(entry) },
                onLogMultipleMeals = { list -> list.forEach { repository.logMeal(it) } }
            )
        }
    }
}

@Composable
private fun CalorieGaugeCard(summary: DailySummary, onOpenUpload: () -> Unit) {
    val animatedProgress by animateFloatAsState(targetValue = summary.calorieProgress, label = "calProgress")

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Calorie Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Target: ${summary.targetCalories} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Center Circular Progress Ring
            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    strokeWidth = 14.dp
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.remainingCalories}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "kcal remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Consumed vs Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${summary.totalCalories} kcal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text("Consumed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${summary.targetCalories} kcal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text("Daily Goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MacroCard(summary: DailySummary) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Macronutrient Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            MacroProgressRow(
                label = "Protein",
                current = summary.totalProtein,
                target = summary.targetProtein,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            MacroProgressRow(
                label = "Carbohydrates",
                current = summary.totalCarbs,
                target = summary.targetCarbs,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            MacroProgressRow(
                label = "Fats",
                current = summary.totalFat,
                target = summary.targetFat,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun MacroProgressRow(label: String, current: Int, target: Int, color: Color) {
    val progress = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            Text(
                "$current / $target g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun WaterTrackerCard(drankMl: Int, targetMl: Int, onAddWater: () -> Unit, onReset: () -> Unit) {
    val glasses = (drankMl / 250)
    val targetGlasses = (targetMl / 250).coerceAtLeast(8)
    val progress = if (targetMl > 0) (drankMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Daily Water Hydration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "$drankMl / $targetMl ml ($glasses / $targetGlasses glasses)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(18.dp))
                }
                Button(
                    onClick = onAddWater,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+250ml", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun MealSlotDiarySection(
    slot: MealSlot,
    meals: List<LoggedMealEntry>,
    onDelete: (LoggedMealEntry) -> Unit,
    onAddClick: () -> Unit
) {
    val totalSlotCalories = meals.sumOf { it.calories }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        slot.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        slot.typicalTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$totalSlotCalories kcal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Log to ${slot.displayName}")
                    }
                }
            }

            if (meals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No foods logged yet. Tap + to log via Camera, Barcode, or Voice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    meals.forEach { meal ->
                        LoggedMealRow(meal = meal, onDelete = { onDelete(meal) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LoggedMealRow(meal: LoggedMealEntry, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Source Icon badge
                val sourceIcon = when (meal.source) {
                    LogSource.CAMERA_AI -> Icons.Default.CameraAlt
                    LogSource.BARCODE -> Icons.Default.QrCodeScanner
                    LogSource.VOICE -> Icons.Default.Mic
                    LogSource.MANUAL -> Icons.Default.Restaurant
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        sourceIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(meal.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${if (meal.quantity % 1f == 0f) meal.quantity.toInt().toString() else meal.quantity} ${meal.unit.displayName.split(" ").first()} • P: ${meal.proteinGrams}g C: ${meal.carbsGrams}g F: ${meal.fatGrams}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${meal.calories} kcal",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

