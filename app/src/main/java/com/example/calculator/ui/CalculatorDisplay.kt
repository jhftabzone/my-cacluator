package com.example.calculator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.engine.AngleMode
import com.example.calculator.viewmodel.CalculatorMode
import com.example.ui.theme.LocalCyberTheme
import com.example.ui.theme.cyberBorder
import com.example.ui.theme.scanlines

@Composable
fun CalculatorDisplay(
    expression: String,
    result: String,
    previewResult: String?,
    angleMode: AngleMode,
    onToggleAngleMode: () -> Unit,
    hasMemory: Boolean,
    memoryValue: Double,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onNotify: (String) -> Unit,
    calculatorMode: CalculatorMode,
    scanlinesEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = LocalCyberTheme.current
    val context = LocalContext.current
    val exprScrollState = rememberScrollState()

    // Auto-scroll to end of expression on changes
    LaunchedEffect(expression) {
        exprScrollState.scrollTo(exprScrollState.maxValue)
    }

    // Blinking cursor
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )

    // Dynamic result text size
    val resultFontSize = when {
        result.length > 18 -> 22.sp
        result.length > 13 -> 28.sp
        result.length > 9 -> 34.sp
        else -> 42.sp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surfaceColor.copy(alpha = 0.95f))
            .then(if (scanlinesEnabled) Modifier.scanlines(theme.backgroundColor.copy(alpha = 0.35f)) else Modifier)
            .cyberBorder(strokeWidth = 1.2.dp, color = theme.primaryColor, cornerRadius = 12.dp)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // HUD Top Indicators Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // DEG / RAD Toggle Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.cardColor)
                            .border(1.dp, theme.secondaryColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable { onToggleAngleMode() }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("badge_deg_rad")
                    ) {
                        Text(
                            text = if (angleMode == AngleMode.DEG) "DEG" else "RAD",
                            color = theme.secondaryColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Memory Indicator
                    if (hasMemory) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(theme.primaryColor.copy(alpha = 0.2f))
                                .border(1.dp, theme.primaryColor, RoundedCornerShape(4.dp))
                                .clickable { onMemoryRecall() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("badge_memory")
                        ) {
                            Text(
                                text = "MEM: ON",
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mode Tag
                    Text(
                        text = "[${calculatorMode.shortCode}]",
                        color = theme.primaryColor.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Action icons: Copy, Backspace, Clear
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Copy Result Button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Akram Calc Result", result)
                            clipboard.setPrimaryClip(clip)
                            onNotify("COPIED TO CLIPBOARD // $result")
                        },
                        modifier = Modifier.size(32.dp).testTag("btn_copy_result")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = theme.secondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Backspace Button
                    IconButton(
                        onClick = onBackspace,
                        modifier = Modifier.size(32.dp).testTag("btn_backspace")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Clear AC Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF330909))
                            .border(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .clickable { onClear() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("btn_clear_ac")
                    ) {
                        Text(
                            text = "AC",
                            color = Color(0xFFFF6666),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expression input display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    color = if (expression.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.85f),
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                if (expression.isNotEmpty()) {
                    Text(
                        text = "_",
                        color = theme.primaryColor.copy(alpha = cursorAlpha),
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Live Preview Result (e.g. "= 42" in cyan while typing)
            if (previewResult != null && previewResult != result) {
                Text(
                    text = "= $previewResult",
                    color = theme.secondaryColor.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Primary Display Result
            Text(
                text = result,
                color = theme.primaryColor,
                fontSize = resultFontSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("display_result"),
                maxLines = 1
            )
        }
    }
}
