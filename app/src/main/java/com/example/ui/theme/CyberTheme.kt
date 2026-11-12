package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

enum class CyberThemeType(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardColor: Color
) {
    MATRIX_GREEN(
        title = "MATRIX GREEN",
        primaryColor = Color(0xFF00FF66),
        secondaryColor = Color(0xFF00E5FF),
        backgroundColor = Color(0xFF040806),
        surfaceColor = Color(0xFF07120C),
        cardColor = Color(0xFF0B1F15)
    ),
    CYBER_CYAN(
        title = "CYBER CYAN",
        primaryColor = Color(0xFF00F0FF),
        secondaryColor = Color(0xFF9D00FF),
        backgroundColor = Color(0xFF030712),
        surfaceColor = Color(0xFF071120),
        cardColor = Color(0xFF0D1D36)
    ),
    HACKER_AMBER(
        title = "HACKER AMBER",
        primaryColor = Color(0xFFFFB000),
        secondaryColor = Color(0xFFFF4D00),
        backgroundColor = Color(0xFF0A0700),
        surfaceColor = Color(0xFF160E00),
        cardColor = Color(0xFF241700)
    ),
    BLOOD_PROTOCOL(
        title = "BLOOD PROTOCOL",
        primaryColor = Color(0xFFFF0055),
        secondaryColor = Color(0xFFFF6600),
        backgroundColor = Color(0xFF0D0005),
        surfaceColor = Color(0xFF19030C),
        cardColor = Color(0xFF290614)
    )
}

val LocalCyberTheme = compositionLocalOf { CyberThemeType.MATRIX_GREEN }

fun getCyberColorScheme(theme: CyberThemeType): ColorScheme {
    return darkColorScheme(
        primary = theme.primaryColor,
        onPrimary = Color.Black,
        primaryContainer = theme.cardColor,
        onPrimaryContainer = theme.primaryColor,
        secondary = theme.secondaryColor,
        onSecondary = Color.Black,
        background = theme.backgroundColor,
        onBackground = Color(0xFFE0E0E0),
        surface = theme.surfaceColor,
        onSurface = Color(0xFFE5FFE5),
        surfaceVariant = theme.cardColor,
        onSurfaceVariant = theme.primaryColor.copy(alpha = 0.8f),
        outline = theme.primaryColor.copy(alpha = 0.4f)
    )
}

@Composable
fun CyberTheme(
    themeType: CyberThemeType = CyberThemeType.MATRIX_GREEN,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalCyberTheme provides themeType) {
        MaterialTheme(
            colorScheme = getCyberColorScheme(themeType),
            typography = Typography,
            content = content
        )
    }
}

/**
 * Custom cyber border with corner accents
 */
fun Modifier.cyberBorder(
    strokeWidth: Dp = 1.dp,
    color: Color,
    cornerRadius: Dp = 6.dp
): Modifier = this
    .border(
        width = strokeWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = 0.7f),
                color.copy(alpha = 0.2f),
                color.copy(alpha = 0.6f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

/**
 * CRT scanline effect
 */
fun Modifier.scanlines(lineColor: Color = Color.Black.copy(alpha = 0.25f), step: Float = 6f): Modifier = this.drawWithContent {
    drawContent()
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.5f
        )
        y += step
    }
}

/**
 * Animated Matrix Rain background
 */
@Composable
fun MatrixRainBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00FF66),
    numColumns: Int = 18
) {
    val characters = remember {
        "0123456789ABCDEFΣΠ√∫≈≠±µΩ∆§λ¥$#@%&*<>/="
    }

    val drops = remember {
        List(numColumns) {
            mutableFloatStateOf(Random.nextFloat() * 1000f)
        }
    }
    val speeds = remember {
        List(numColumns) {
            Random.nextFloat() * 3f + 2f
        }
    }

    // Animation ticker
    val transition = rememberInfiniteTransition(label = "matrix_rain")
    val ticker by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_ticker"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val _tick = ticker // subscribe canvas to frame updates
        val colWidth = size.width / numColumns
        val charHeight = 22f

        for (col in 0 until numColumns) {
            // Update drop position
            var yPos = drops[col].floatValue + speeds[col]
            if (yPos > size.height + 200f) {
                yPos = -Random.nextFloat() * 150f
            }
            drops[col].floatValue = yPos

            val x = col * colWidth + colWidth / 2f
            val trailLength = 8

            for (trail in 0 until trailLength) {
                val cy = yPos - trail * charHeight
                if (cy in 0f..size.height) {
                    val alpha = if (trail == 0) 0.8f else (1f - (trail.toFloat() / trailLength)) * 0.35f
                    val headColor = if (trail == 0) Color.White else primaryColor.copy(alpha = alpha)

                    drawCircle(
                        color = headColor,
                        radius = if (trail == 0) 2.2f else 1.4f,
                        center = Offset(x, cy)
                    )
                }
            }
        }
    }
}
