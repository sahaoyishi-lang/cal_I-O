package com.calorietrack.app.ui.upload

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import com.calorietrack.app.model.LogSource
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.nutrition.BarcodeDatabase
import com.calorietrack.app.nutrition.BarcodeItem
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanDialog(
    onDismiss: () -> Unit,
    onConfirmLog: (LoggedMealEntry) -> Unit
) {
    var barcodeInput by remember { mutableStateOf("8901030383742") } // Quaker Oats default
    var foundItem by remember { mutableStateOf<BarcodeItem?>(BarcodeDatabase.lookup("8901030383742")) }
    var servingsMultiplier by remember { mutableFloatStateOf(1.0f) }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.BREAKFAST) }
    var searchError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

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
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Barcode Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Scan package UPC / EAN code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Scanner Viewfinder with Animated Red Laser
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Laser Beam
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .offset(y = (laserOffsetY - 50).dp)
                            .background(Color.Red)
                    )

                    // Target Frame
                    Text(
                        "[ Align barcode within frame ]",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Barcode Presets
                Text("Tap to scan test barcode:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val presets = listOf(
                        "Oats" to "8901030383742",
                        "Whey" to "0749826002012",
                        "Milk" to "8901262010053",
                        "Greek Yog" to "0041196910759"
                    )
                    presets.forEach { (label, code) ->
                        val isSelected = barcodeInput == code
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                barcodeInput = code
                                foundItem = BarcodeDatabase.lookup(code)
                                searchError = null
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Manual Barcode Input
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = {
                        barcodeInput = it
                        val match = BarcodeDatabase.lookup(it)
                        if (match != null) {
                            foundItem = match
                            searchError = null
                        }
                    },
                    label = { Text("Barcode Number (EAN-13 / UPC)") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val match = BarcodeDatabase.lookup(barcodeInput)
                            if (match != null) {
                                foundItem = match
                                searchError = null
                            } else {
                                foundItem = null
                                searchError = "No nutrition data found for this barcode"
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Lookup")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (searchError != null) {
                    Text(
                        searchError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Found Item Details Card
                if (foundItem != null) {
                    val item = foundItem!!
                    val unit = item.foodItem.defaultServingUnit
                    val totalNutrition = item.foodItem.calculateNutrition(
                        item.foodItem.defaultServingQuantity * servingsMultiplier,
                        unit
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(item.brand, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(item.foodItem.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(item.servingDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                            // Serving Multiplier Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Servings: ${String.format("%.1f", servingsMultiplier)}x",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledTonalIconButton(
                                        onClick = { servingsMultiplier = (servingsMultiplier - 0.5f).coerceAtLeast(0.5f) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        "${String.format("%.1f", servingsMultiplier)}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )
                                    FilledTonalIconButton(
                                        onClick = { servingsMultiplier += 0.5f },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Nutrition Breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalNutrition.calories}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Calories", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalNutrition.proteinGrams}g", fontWeight = FontWeight.Bold)
                                    Text("Protein", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalNutrition.carbsGrams}g", fontWeight = FontWeight.Bold)
                                    Text("Carbs", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${totalNutrition.fatGrams}g", fontWeight = FontWeight.Bold)
                                    Text("Fat", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Meal Slot Selector
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons
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
                                    foodName = "${item.brand} ${item.foodItem.name}",
                                    mealSlot = selectedMealSlot,
                                    quantity = item.foodItem.defaultServingQuantity * servingsMultiplier,
                                    unit = unit,
                                    calories = totalNutrition.calories,
                                    proteinGrams = totalNutrition.proteinGrams,
                                    carbsGrams = totalNutrition.carbsGrams,
                                    fatGrams = totalNutrition.fatGrams,
                                    source = LogSource.BARCODE,
                                    notes = "Scanned Barcode: ${item.barcode}"
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
}

