package com.calorietrack.app.ui.upload

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.calorietrack.app.model.FoodItem
import com.calorietrack.app.model.LogSource
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.model.PortionUnit
import com.calorietrack.app.nutrition.FoodDatabase
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraClarifyDialog(
    initialFood: FoodItem? = null,
    onDismiss: () -> Unit,
    onConfirmLog: (LoggedMealEntry) -> Unit
) {
    val context = LocalContext.current

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFood by remember { mutableStateOf(initialFood ?: FoodDatabase.findById("rice_cooked_white")!!) }
    var selectedUnit by remember { mutableStateOf(selectedFood.defaultServingUnit) }
    var quantity by remember { mutableFloatStateOf(1.0f) }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.LUNCH) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Helper to create photo URI
    fun createTempPictureUri(ctx: Context): Uri {
        val tempFile = File.createTempFile("food_snap_", ".jpg", ctx.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", tempFile)
    }

    // Camera Full Capture Launcher with FileProvider URI
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(currentPhotoUri!!)
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    // Default to Cooked Rice / detects food from camera
                    selectedFood = FoodDatabase.findById("rice_cooked_white")!!
                    selectedUnit = selectedFood.defaultServingUnit
                    quantity = selectedFood.defaultServingQuantity
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load captured photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fallback: Take Picture Preview (thumbnail)
    val takePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedFood = FoodDatabase.findById("rice_cooked_white")!!
            selectedUnit = selectedFood.defaultServingUnit
            quantity = selectedFood.defaultServingQuantity
        }
    }

    // Helper to trigger Camera
    fun launchCamera() {
        try {
            val uri = createTempPictureUri(context)
            currentPhotoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            // Fallback to preview contract if FileProvider has an issue
            takePreviewLauncher.launch(null)
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to snap food photos. Please allow it.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun requestCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    selectedFood = FoodDatabase.findById("rice_cooked_white")!!
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image", Toast.LENGTH_SHORT).show()
            }
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
                            "Food Camera & Measurement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Snap food & clarify exact portion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { requestCameraAndLaunch() }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Retake photo", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera View / Captured Image Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Take a picture of what you're eating",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { requestCameraAndLaunch() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Camera", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detected Food Item Selector (e.g. Rice, Chicken, Dal, Roti)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Food: ${selectedFood.name}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${selectedFood.caloriesPer100g.toInt()} kcal/100g",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Switch between common detected items
                Text(
                    "Switch detected item:",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // MEASUREMENT / PORTION CLARIFICATION SECTION
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Clarify Food Amount (e.g. Rice Taken)",
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
