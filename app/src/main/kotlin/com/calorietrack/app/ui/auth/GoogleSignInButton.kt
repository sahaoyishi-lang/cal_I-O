package com.calorietrack.app.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorietrack.app.model.User

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continue with Google",
    isLoading: Boolean = false
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                GoogleIcon(modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

/**
 * Draws the authentic Google 4-color 'G' logo
 */
@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w.coerceAtMost(h) / 2f) * 0.85f
        val stroke = r * 0.38f

        val arcRect = Rect(cx - r, cy - r, cx + r, cy + r)

        // 1. Red Top Arc (190° to 300°)
        val redPath = Path().apply {
            arcTo(arcRect, 185f, 130f, false)
        }
        drawPath(redPath, Color(0xFFEA4335), style = Stroke(width = stroke, cap = StrokeCap.Butt))

        // 2. Yellow Left Arc (120° to 190°)
        val yellowPath = Path().apply {
            arcTo(arcRect, 120f, 75f, false)
        }
        drawPath(yellowPath, Color(0xFFFBBC05), style = Stroke(width = stroke, cap = StrokeCap.Butt))

        // 3. Green Bottom Arc (35° to 125°)
        val greenPath = Path().apply {
            arcTo(arcRect, 35f, 95f, false)
        }
        drawPath(greenPath, Color(0xFF34A853), style = Stroke(width = stroke, cap = StrokeCap.Butt))

        // 4. Blue Horizontal Bar & Right Arc
        val bluePath = Path().apply {
            arcTo(arcRect, 320f, 80f, false)
        }
        drawPath(bluePath, Color(0xFF4285F4), style = Stroke(width = stroke, cap = StrokeCap.Butt))

        // Center Blue crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx, cy),
            end = Offset(cx + r, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )
    }
}

/**
 * Modern interactive Google Account prompt allowing user to select or customize their account.
 */
@Composable
fun GoogleAccountSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoogleIcon(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sign in with Google", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Choose an account to continue to NutriTrack:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isCustomMode) {
                    // Default quick user choice
                    Card(
                        onClick = { onConfirm("Google User", "user@gmail.com") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "G",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Continue as Google User", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("user@gmail.com", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }

                    // Option to enter another email
                    TextButton(
                        onClick = { isCustomMode = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Use another Google account")
                    }
                } else {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("e.g. Sarah") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Google Email") },
                        placeholder = { Text("name@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (isCustomMode) {
                Button(
                    onClick = {
                        val name = if (customName.isNotBlank()) customName.trim() else "Google User"
                        val email = if (customEmail.isNotBlank()) customEmail.trim() else "user@gmail.com"
                        onConfirm(name, email)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Continue")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

