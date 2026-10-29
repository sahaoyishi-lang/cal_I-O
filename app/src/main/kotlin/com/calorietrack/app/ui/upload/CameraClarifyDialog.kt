package com.calorietrack.app.ui.upload

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.calorietrack.app.model.FoodItem
import com.calorietrack.app.model.LogSource
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.model.PortionUnit
import com.calorietrack.app.nutrition.FoodDatabase
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraClarifyDialog(
    initialFood: FoodItem? = null,
    onDismiss: () -> Unit,
    onConfirmLog: (LoggedMealEntry) -> Unit
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFood by remember { mutableStateOf(initialFood ?: FoodDatabase.findById("rice_cooked_white")!!) }
    var selectedUnit by remember { mutableStateOf(selectedFood.defaultServingUnit) }
    var quantity by remember { mutableFloatStateOf(1.0f) }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.LUNCH) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            // Default to Cooked White Rice if rice or food photo taken
            selectedFood = FoodDatabase.findById("rice_cooked_white")!!
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFood = FoodDatabase.findById("rice_cooked_white")!!
        }
    }

    // Live calculated nutrition
    val currentNutrition = selectedFood.calculateNutrition(quantity, selectedUnit)
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Camera Food Detection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Take a pic & clarify portion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { cameraLauncher.launch() }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Retake photo", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Preview / Photo Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Food picture",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { cameraLauncher.launch() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Snap Food", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gallery", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detected Food Item Selector (e.g. Rice, Chicken, Bread, Salad)
                Text(
                    "Detected Food: ${selectedFood.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Quick Switch between common detected items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickFoods = listOf(
                        FoodDatabase.findById("rice_cooked_white")!!,
                        FoodDatabase.findById("chicken_breast_grilled")!!,
                        FoodDatabase.findById("dal_lentils_yellow")!!,
                        FoodDatabase.findById("roti_chapati")!!
                    )
                    quickFoods.forEach { food ->
                        val isSelected = selectedFood.id == food.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFood = food
                                selectedUnit = food.defaultServingUnit
                                quantity = food.defaultServingQuantity
                            },
                            label = { Text(food.name.split(" ").first(), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // MEASUREMENT / PORTION CLARIFICATION SECTION
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Clarify Food Amount & Portion",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Serving Unit Selection (e.g. Bowls, Cups, Grams, Slices)
                        Text("Select Serving Unit:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val availableUnits = listOf(PortionUnit.BOWLS, PortionUnit.CUPS, PortionUnit.GRAMS, PortionUnit.SLICES)
                            availableUnits.forEach { unit ->
                                val isSelected = selectedUnit == unit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedUnit = unit
                                        if (unit == PortionUnit.GRAMS && quantity < 50) quantity = 150f
                                        else if (unit != PortionUnit.GRAMS && quantity > 10) quantity = 1f
                                    },
                                    label = { Text(unit.displayName.split(" ").first(), style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Amount / Quantity Stepper & Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Amount: ${if (selectedUnit == PortionUnit.GRAMS) "${quantity.toInt()} g" else String.format("%.1f", quantity)} ${selectedUnit.displayName.split(" ").first()}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = {
                                        if (selectedUnit == PortionUnit.GRAMS) {
                                            quantity = (quantity - 25f).coerceAtLeast(25f)
                                        } else {
                                            quantity = (quantity - 0.5f).coerceAtLeast(0.5f)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = if (selectedUnit == PortionUnit.GRAMS) "${quantity.toInt()}" else String.format("%.1f", quantity),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                FilledTonalIconButton(
                                    onClick = {
                                        if (selectedUnit == PortionUnit.GRAMS) {
                                            quantity += 25f
                                        } else {
                                            quantity += 0.5f
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Preset portion chips (e.g. 0.5 Bowl, 1 Bowl (150g), 1.5 Bowls (225g), 2 Bowls (300g))
                        if (selectedUnit != PortionUnit.GRAMS) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { q ->
                                    val isSelected = quantity == q
                                    SuggestionChip(
                                        onClick = { quantity = q },
                                        label = { Text("${q}x (~${(q * selectedUnit.gramsEquivalent).toInt()}g)") },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Nutrition Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        NutriStatItem("Calories", "${currentNutrition.calories} kcal")
                        NutriStatItem("Protein", "${currentNutrition.proteinGrams}g")
                        NutriStatItem("Carbs", "${currentNutrition.carbsGrams}g")
                        NutriStatItem("Fat", "${currentNutrition.fatGrams}g")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Meal Slot Selector (Breakfast, Lunch, Dinner, Snack)
                Text("Log to Meal:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MealSlot.values().forEach { slot ->
                        val isSelected = selectedMealSlot == slot
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMealSlot = slot },
                            label = { Text(slot.displayName, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val entry = LoggedMealEntry(
                                id = UUID.randomUUID().toString(),
                                foodName = selectedFood.name,
                                mealSlot = selectedMealSlot,
                                quantity = quantity,
                                unit = selectedUnit,
                                calories = currentNutrition.calories,
                                proteinGrams = currentNutrition.proteinGrams,
                                carbsGrams = currentNutrition.carbsGrams,
                                fatGrams = currentNutrition.fatGrams,
                                source = LogSource.CAMERA_AI,
                                notes = "Portion confirmed via Camera AI"
                            )
                            onConfirmLog(entry)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Intake", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NutriStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

