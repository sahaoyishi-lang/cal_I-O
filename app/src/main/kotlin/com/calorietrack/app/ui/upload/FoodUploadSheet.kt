package com.calorietrack.app.ui.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorietrack.app.model.LoggedMealEntry

enum class UploadMode {
    NONE,
    CAMERA_CLARIFY,
    BARCODE,
    VOICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodUploadSheet(
    onDismiss: () -> Unit,
    onLogMeal: (LoggedMealEntry) -> Unit,
    onLogMultipleMeals: (List<LoggedMealEntry>) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMode by remember { mutableStateOf(UploadMode.NONE) }

    when (activeMode) {
        UploadMode.CAMERA_CLARIFY -> {
            CameraClarifyDialog(
                onDismiss = { activeMode = UploadMode.NONE },
                onConfirmLog = { entry ->
                    onLogMeal(entry)
                    activeMode = UploadMode.NONE
                    onDismiss()
                }
            )
        }
        UploadMode.BARCODE -> {
            BarcodeScanDialog(
                onDismiss = { activeMode = UploadMode.NONE },
                onConfirmLog = { entry ->
                    onLogMeal(entry)
                    activeMode = UploadMode.NONE
                    onDismiss()
                }
            )
        }
        UploadMode.VOICE -> {
            VoiceLogDialog(
                onDismiss = { activeMode = UploadMode.NONE },
                onConfirmLog = { entries ->
                    onLogMultipleMeals(entries)
                    activeMode = UploadMode.NONE
                    onDismiss()
                }
            )
        }
        UploadMode.NONE -> {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        "Upload Food Intake",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Choose one of the 3 smart logging options",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option 1: Camera with Portion Clarifier
                    UploadOptionTile(
                        icon = Icons.Default.CameraAlt,
                        title = "1. Camera Photo (with Portion Clarifier)",
                        subtitle = "Snap a pic of your food (e.g. rice) & adjust the exact amount/bowls",
                        badge = "Recommended",
                        onClick = { activeMode = UploadMode.CAMERA_CLARIFY }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Barcode Scanner
                    UploadOptionTile(
                        icon = Icons.Default.QrCodeScanner,
                        title = "2. Barcode Scanner",
                        subtitle = "Scan packaged foods, snacks, milk, oats & protein shakes",
                        badge = "Instant",
                        onClick = { activeMode = UploadMode.BARCODE }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 3: Voice / Speech-to-Text
                    UploadOptionTile(
                        icon = Icons.Default.Mic,
                        title = "3. Voice Food Diary",
                        subtitle = "Speak naturally (e.g. \"two boiled eggs and a glass of milk\")",
                        badge = "Hands-free",
                        onClick = { activeMode = UploadMode.VOICE }
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadOptionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

