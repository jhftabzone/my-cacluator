package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCyberTheme
import com.example.ui.theme.cyberBorder

enum class KeyVariant {
    DIGIT,
    OPERATOR,
    FUNCTION,
    ACTION_CLEAR,
    ACTION_EQUALS,
    ACCENT
}

@Composable
fun CyberKey(
    text: String,
    modifier: Modifier = Modifier,
    subText: String? = null,
    variant: KeyVariant = KeyVariant.DIGIT,
    fontSize: TextUnit = 18.sp,
    height: Dp = 56.dp,
    testTag: String = "key_$text",
    onClick: () -> Unit
) {
    val theme = LocalCyberTheme.current

    val (bgColor, textColor, borderColor) = when (variant) {
        KeyVariant.DIGIT -> Triple(
            theme.surfaceColor,
            Color.White,
            theme.primaryColor.copy(alpha = 0.25f)
        )
        KeyVariant.OPERATOR -> Triple(
            theme.cardColor,
            theme.primaryColor,
            theme.primaryColor.copy(alpha = 0.6f)
        )
        KeyVariant.FUNCTION -> Triple(
            theme.surfaceColor,
            theme.secondaryColor,
            theme.secondaryColor.copy(alpha = 0.35f)
        )
        KeyVariant.ACTION_CLEAR -> Triple(
            Color(0xFF2E0909),
            Color(0xFFFF4D4D),
            Color(0xFFFF3333).copy(alpha = 0.6f)
        )
        KeyVariant.ACTION_EQUALS -> Triple(
            theme.primaryColor.copy(alpha = 0.22f),
            theme.primaryColor,
            theme.primaryColor
        )
        KeyVariant.ACCENT -> Triple(
            theme.secondaryColor.copy(alpha = 0.2f),
            theme.secondaryColor,
            theme.secondaryColor.copy(alpha = 0.7f)
        )
    }

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .height(height)
            .clip(shape)
            .background(bgColor)
            .cyberBorder(
                strokeWidth = if (variant == KeyVariant.ACTION_EQUALS) 1.5.dp else 1.dp,
                color = borderColor,
                cornerRadius = 8.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = textColor),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = if (variant == KeyVariant.ACTION_EQUALS || variant == KeyVariant.OPERATOR) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (subText != null) {
                Text(
                    text = subText,
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}
