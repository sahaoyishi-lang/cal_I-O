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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

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
    var quantity by remember { mutableFloatStateOf(selectedFood.defaultServingQuantity) }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.LUNCH) }
    
    // AI detection states
    var isAnalyzing by remember { mutableStateOf(false) }
    var detectedLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var detectionConfidence by remember { mutableFloatStateOf(0f) }
    var detectionMessage by remember { mutableStateOf<String?>(null) }

    // Food Search picker dialog state
    var showFoodSearchDialog by remember { mutableStateOf(false) }

    fun runFoodRecognition(bitmap: Bitmap) {
        isAnalyzing = true
        detectionMessage = "AI analyzing food..."
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    isAnalyzing = false
                    val labelTexts = labels.map { it.text }
                    detectedLabels = labelTexts

                    val bestMatch = FoodDatabase.findBestMatchForLabels(labelTexts)
                    if (bestMatch != null) {
                        selectedFood = bestMatch
                        selectedUnit = bestMatch.defaultServingUnit
                        quantity = bestMatch.defaultServingQuantity
                        val topConfidence = labels.firstOrNull()?.confidence ?: 0.85f
                        detectionConfidence = topConfidence
                        detectionMessage = "Identified: ${bestMatch.name} (${(topConfidence * 100).toInt()}% confidence)"
                    } else {
                        val firstLabel = labelTexts.firstOrNull()
                        detectionMessage = if (firstLabel != null) {
                            "Detected '$firstLabel'. Please confirm or pick the exact food below."
                        } else {
                            "Food not clearly recognized. Please select or search your meal below."
                        }
                    }
                }
                .addOnFailureListener { e ->
                    isAnalyzing = false
                    detectionMessage = "Recognition error: ${e.message}"
                }
        } catch (e: Exception) {
            isAnalyzing = false
            detectionMessage = "Could not analyze image: ${e.message}"
        }
    }

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
                    runFoodRecognition(bitmap)
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
            runFoodRecognition(bitmap)
        }
    }

    // Helper to trigger Camera
    fun launchCamera() {
        try {
            val uri = createTempPictureUri(context)
            currentPhotoUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
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
            Toast.makeText(context, "Camera permission is required to snap food photos.", Toast.LENGTH_LONG).show()
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
                    runFoodRecognition(bitmap)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Live calculated nutrition
    val currentNutrition = selectedFood.calculateNutrition(quantity, selectedUnit)
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
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
                            "Smart Food Camera",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Snap pic & clarify exact portion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { requestCameraAndLaunch() }) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Retake photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera View / Captured Image Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Food picture",
                            modifier = Modifier.fillMaxSize()
                        )

                        // Retake / Change Photo overlay button
                        Surface(
                            onClick = { requestCameraAndLaunch() },
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retake", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Take a picture of what you're eating",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { requestCameraAndLaunch() },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Camera", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery")
                                }
                            }
                        }
                    }

                    // Analyzing overlay
                    if (isAnalyzing) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI identifying food...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // AI Detection Message Banner
                if (detectionMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = detectionMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Food Header & "Change / Search" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedFood.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${selectedFood.category} • ${selectedFood.caloriesPer100g.toInt()} kcal/100g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showFoodSearchDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Search/Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =========================================================
                // MEASUREMENT / PORTION CLARIFICATION SECTION
                // =========================================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Clarify Exact Amount Taken",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Serving Unit Selection
                        Text("Portion Measurement Unit:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val availableUnits = listOf(PortionUnit.BOWLS, PortionUnit.GRAMS, PortionUnit.CUPS, PortionUnit.PIECES, PortionUnit.SLICES)
                            availableUnits.forEach { unit ->
                                val isSelected = selectedUnit == unit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedUnit = unit
                                        if (unit == PortionUnit.GRAMS) {
                                            quantity = 150f
                                        } else if (quantity > 10f) {
                                            quantity = 1.0f
                                        }
                                    },
                                    label = { Text(unit.displayName, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Quantity Stepper & Clarifier
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Quantity Taken", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (selectedUnit == PortionUnit.GRAMS) "${quantity.roundToInt()} grams" else "$quantity ${selectedUnit.displayName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        val step = if (selectedUnit == PortionUnit.GRAMS) 25f else 0.5f
                                        quantity = (quantity - step).coerceAtLeast(step)
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }

                                FilledTonalIconButton(
                                    onClick = {
                                        val step = if (selectedUnit == PortionUnit.GRAMS) 25f else 0.5f
                                        quantity += step
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }

                        // Quick portion preset chips (e.g. for Rice: 0.5 Bowl, 1 Bowl, 1.5 Bowls, 2 Bowls)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presets = if (selectedUnit == PortionUnit.GRAMS) {
                                listOf(100f to "100g", 150f to "150g", 200f to "200g", 250f to "250g")
                            } else {
                                listOf(0.5f to "½", 1.0f to "1", 1.5f to "1½", 2.0f to "2")
                            }
                            presets.forEach { (presetVal, presetLabel) ->
                                OutlinedButton(
                                    onClick = { quantity = presetVal },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(presetLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Nutrition Facts Summary Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Intake Nutrition",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${currentNutrition.calories} kcal",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            NutrientBadge("Protein", "${currentNutrition.proteinGrams}g")
                            NutrientBadge("Carbs", "${currentNutrition.carbsGrams}g")
                            NutrientBadge("Fats", "${currentNutrition.fatGrams}g")
                            NutrientBadge("Weight", "${currentNutrition.weightGrams}g")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meal Slot Selector (Breakfast, Lunch, Snacks, Dinner)
                Text("Select Meal Time:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealSlot.values().forEach { slot ->
                        val isSelected = selectedMealSlot == slot
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMealSlot = slot },
                            label = { Text(slot.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Log Meal Button
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
                            source = LogSource.CAMERA_AI
                        )
                        onConfirmLog(entry)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Confirm & Log ${currentNutrition.calories} kcal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Food Search & Selection Dialog
    if (showFoodSearchDialog) {
        FoodSearchAndSelectionDialog(
            onDismiss = { showFoodSearchDialog = false },
            onFoodSelected = { food ->
                selectedFood = food
                selectedUnit = food.defaultServingUnit
                quantity = food.defaultServingQuantity
                showFoodSearchDialog = false
            }
        )
    }
}

@Composable
private fun NutrientBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

/**
 * Real-time Food Search Dialog allowing user to pick any food
 */
@Composable
private fun FoodSearchAndSelectionDialog(
    onDismiss: () -> Unit,
    onFoodSelected: (FoodItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) {
        FoodDatabase.search(searchQuery)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select / Search Food", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search e.g. Rice, Apple, Chicken...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "${searchResults.size} foods available:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { food ->
                        Card(
                            onClick = { onFoodSelected(food) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(food.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${food.category} • ${food.caloriesPer100g.toInt()} kcal/100g",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "${food.caloriesPer100g.toInt()} kcal",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
