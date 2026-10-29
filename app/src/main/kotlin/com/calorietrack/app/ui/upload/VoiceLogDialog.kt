package com.calorietrack.app.ui.upload

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.calorietrack.app.model.LogSource
import com.calorietrack.app.model.LoggedMealEntry
import com.calorietrack.app.model.MealSlot
import com.calorietrack.app.nutrition.ParsedVoiceItem
import com.calorietrack.app.nutrition.VoiceFoodParser
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLogDialog(
    onDismiss: () -> Unit,
    onConfirmLog: (List<LoggedMealEntry>) -> Unit
) {
    var spokenText by remember { mutableStateOf("I had two boiled eggs and a glass of milk") }
    var parsedItems by remember { mutableStateOf<List<ParsedVoiceItem>>(VoiceFoodParser.parse(spokenText)) }
    var isListening by remember { mutableStateOf(false) }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.BREAKFAST) }

    // Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                spokenText = spoken
                parsedItems = VoiceFoodParser.parse(spoken)
            }
        }
    }

    // Pulse animation while listening
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
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
                            "Voice Food Diary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Speak your meal naturally",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone Circle Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            if (isListening) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .border(
                            2.dp,
                            if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            isListening = true
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me what you ate...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                isListening = false
                            }
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Speak",
                            tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (isListening) "Listening... Speak now" else "Tap microphone to speak",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Spoken Text Input & Edit
                OutlinedTextField(
                    value = spokenText,
                    onValueChange = {
                        spokenText = it
                        parsedItems = VoiceFoodParser.parse(it)
                    },
                    label = { Text("What you ate") },
                    placeholder = { Text("e.g. 1 bowl of rice and chicken breast") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Preset Speech Examples
                Text(
                    "Try sample spoken meals:",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 8.dp, bottom = 4.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val samplePhrases = listOf(
                        "I had two boiled eggs and a glass of milk",
                        "One bowl of white rice with chicken breast",
                        "Three rotis and a bowl of dal"
                    )
                    samplePhrases.forEach { phrase ->
                        SuggestionChip(
                            onClick = {
                                spokenText = phrase
                                parsedItems = VoiceFoodParser.parse(phrase)
                            },
                            label = { Text(phrase, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Parsed Foods Breakdown Card
                if (parsedItems.isNotEmpty()) {
                    Text(
                        "Detected Foods & Quantities (${parsedItems.size}):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        parsedItems.forEach { parsed ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(parsed.foodItem.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Amount: ${if (parsed.quantity % 1f == 0f) parsed.quantity.toInt().toString() else parsed.quantity} ${parsed.unit.displayName.split(" ").first()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${parsed.nutrition.calories} kcal",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "P: ${parsed.nutrition.proteinGrams}g | C: ${parsed.nutrition.carbsGrams}g",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Meal Slot
                    Text("Log to Meal:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.Start))
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
                                val entries = parsedItems.map { parsed ->
                                    LoggedMealEntry(
                                        id = UUID.randomUUID().toString(),
                                        foodName = parsed.foodItem.name,
                                        mealSlot = selectedMealSlot,
                                        quantity = parsed.quantity,
                                        unit = parsed.unit,
                                        calories = parsed.nutrition.calories,
                                        proteinGrams = parsed.nutrition.proteinGrams,
                                        carbsGrams = parsed.nutrition.carbsGrams,
                                        fatGrams = parsed.nutrition.fatGrams,
                                        source = LogSource.VOICE,
                                        notes = "Spoken: \"$spokenText\""
                                    )
                                }
                                onConfirmLog(entries)
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log ${parsedItems.size} Foods", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

