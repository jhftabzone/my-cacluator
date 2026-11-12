package com.example.calculator.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCyberTheme
import com.example.ui.theme.MatrixRainBackground
import com.example.ui.theme.scanlines
import kotlinx.coroutines.delay

@Composable
fun CyberBootScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalCyberTheme.current

    val bootLines = listOf(
        ">> INITIALIZING AKRAM NEURAL CORE v4.2...",
        ">> CALIBRATING HIGH-PRECISION QUANTUM ALU... OK",
        ">> PARSING TRIG & SCIENTIFIC REGISTERS... OK",
        ">> SECURITY AUDIT: SECURE // 100% AD-FREE VERIFIED",
        ">> CALCULATE BEYOND LIMITS.",
        ">> SYSTEM READY // ENGAGING CYBER MATRIX."
    )

    var visibleLineCount by remember { mutableIntStateOf(1) }
    var progress by remember { mutableFloatStateOf(0.1f) }

    LaunchedEffect(Unit) {
        for (i in 1..bootLines.size) {
            visibleLineCount = i
            progress = i.toFloat() / bootLines.size
            delay(280)
        }
        delay(400)
        onComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .scanlines()
    ) {
        // Subtle Matrix rain in background
        MatrixRainBackground(
            primaryColor = theme.primaryColor,
            numColumns = 14,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Center Branding Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF040A07).copy(alpha = 0.92f))
                    .border(1.dp, theme.primaryColor, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                // AC Cyber Emblem
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.cardColor)
                        .border(1.5.dp, theme.primaryColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AC",
                        color = theme.primaryColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "AKRAM CALCULATOR",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Calculate Beyond Limits.",
                    color = theme.primaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Terminal log lines
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until visibleLineCount) {
                        Text(
                            text = bootLines[i],
                            color = if (i == visibleLineCount - 1) theme.secondaryColor else theme.primaryColor.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (i == visibleLineCount - 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = theme.primaryColor,
                    trackColor = theme.cardColor
                )
            }

            // Skip / Engage Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.primaryColor, RoundedCornerShape(8.dp))
                    .clickable { onComplete() }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .testTag("btn_skip_boot")
            ) {
                Text(
                    text = "ENGAGE TERMINAL [SKIP] >>",
                    color = theme.primaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
