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
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.calorietrack.app.nutrition.BarcodeDatabase
import com.calorietrack.app.nutrition.BarcodeItem
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanDialog(
    onDismiss: () -> Unit,
    onConfirmLog: (LoggedMealEntry) -> Unit
) {
    val context = LocalContext.current

    var barcodeInput by remember { mutableStateOf("8901030383742") } // Quaker Oats default
    var foundItem by remember { mutableStateOf<BarcodeItem?>(BarcodeDatabase.lookup("8901030383742")) }
    var servingsMultiplier by remember { mutableFloatStateOf(1.0f) }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.BREAKFAST) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    fun handleDetectedBarcode(rawBarcode: String) {
        val clean = rawBarcode.trim()
        barcodeInput = clean
        val match = BarcodeDatabase.lookup(clean)
        if (match != null) {
            foundItem = match
            searchError = null
            Toast.makeText(context, "Scanned: ${match.brand} ${match.foodItem.name}", Toast.LENGTH_SHORT).show()
        } else {
            // Create fallback item for any valid scanned product barcode
            val genericFood = FoodItem(
                id = "barcode_$clean",
                name = "Packaged Food ($clean)",
                category = "Scanned Item",
                defaultServingUnit = PortionUnit.PACKET,
                defaultServingQuantity = 1.0f,
                caloriesPer100g = 250f,
                proteinPer100g = 8.0f,
                carbsPer100g = 30.0f,
                fatPer100g = 10.0f
            )
            foundItem = BarcodeItem(
                barcode = clean,
                brand = "Scanned Product",
                foodItem = genericFood,
                servingDescription = "1 package ($clean)"
            )
            searchError = null
            Toast.makeText(context, "Barcode $clean recognized!", Toast.LENGTH_SHORT).show()
        }
    }

    // Google Play Services Native Code Scanner (Zero permission required, live camera UI)
    fun startGoogleCodeScanner() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128
            )
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)
        isScanning = true

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                isScanning = false
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrBlank()) {
                    handleDetectedBarcode(rawValue)
                }
            }
            .addOnCanceledListener {
                isScanning = false
            }
            .addOnFailureListener { e ->
                isScanning = false
                Toast.makeText(context, "Scanner error: ${e.message}. You can also snap a photo of the barcode.", Toast.LENGTH_LONG).show()
            }
    }

    // ML Kit Image Barcode Analyzer for Photo / Gallery
    fun analyzeBarcodeFromBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val first = barcodes.first()
                    val raw = first.rawValue ?: first.displayValue
                    if (!raw.isNullOrBlank()) {
                        handleDetectedBarcode(raw)
                    } else {
                        searchError = "No barcode detected in this image. Try holding closer."
                    }
                } else {
                    searchError = "No barcode found in image. Try holding the camera closer with good lighting."
                }
            }
            .addOnFailureListener { e ->
                searchError = "Scan failed: ${e.message}"
            }
    }

    // Helper to create photo URI for barcode photo
    var barcodePhotoUri by remember { mutableStateOf<Uri?>(null) }
    fun createBarcodeTempUri(): Uri {
        val tempFile = File.createTempFile("barcode_snap_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    val takeBarcodePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && barcodePhotoUri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(barcodePhotoUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    analyzeBarcodeFromBitmap(bitmap)
                }
            } catch (e: Exception) {
                searchError = "Could not process photo"
            }
        }
    }

    val barcodePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createBarcodeTempUri()
            barcodePhotoUri = uri
            takeBarcodePhotoLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission needed to take barcode photos", Toast.LENGTH_SHORT).show()
        }
    }

    val pickBarcodeImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    analyzeBarcodeFromBitmap(bitmap)
                }
            } catch (e: Exception) {
                searchError = "Could not load image"
            }
        }
    }

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

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action: Open Native Camera Barcode Scanner
                Button(
                    onClick = { startGoogleCodeScanner() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Live Camera Scanner", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Photo Options: Snap Barcode or Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val uri = createBarcodeTempUri()
                                barcodePhotoUri = uri
                                takeBarcodePhotoLauncher.launch(uri)
                            } else {
                                barcodePermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Snap Photo", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { pickBarcodeImageLauncher.launch("image/*") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("From Gallery", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Barcode Presets
                Text("Or select popular test products:", style = MaterialTheme.typography.labelSmall)
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
                                handleDetectedBarcode(code)
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
                                handleDetectedBarcode(barcodeInput)
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

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

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
