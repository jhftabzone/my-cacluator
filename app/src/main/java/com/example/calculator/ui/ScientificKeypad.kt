package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCyberTheme

private enum class SciCategory(val title: String) {
    TRIG("TRIG / HYP"),
    POWERS("POWERS / ROOTS"),
    FUNCS("LOG / ADVANCED")
}

@Composable
fun ScientificKeypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onSquare: () -> Unit,
    onCube: () -> Unit,
    onReciprocal: () -> Unit,
    onFactorial: () -> Unit,
    onToggleSign: () -> Unit,
    onEvaluate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalCyberTheme.current
    var selectedCategory by remember { mutableStateOf(SciCategory.TRIG) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Category Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SciCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) theme.secondaryColor.copy(alpha = 0.25f) else theme.surfaceColor)
                        .border(
                            1.dp,
                            if (isSelected) theme.secondaryColor else theme.secondaryColor.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.title,
                        color = if (isSelected) Color.White else theme.secondaryColor.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Scientific functions based on active category
        val funcHeight = 44.dp
        val funcFontSize = 13.sp

        when (selectedCategory) {
            SciCategory.TRIG -> {
                // Trigonometry and Inverse / Hyperbolic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberKey(text = "sin", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("sin") })
                    CyberKey(text = "cos", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("cos") })
                    CyberKey(text = "tan", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("tan") })
                    CyberKey(text = "asin", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("asin") })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberKey(text = "acos", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("acos") })
                    CyberKey(text = "atan", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("atan") })
                    CyberKey(text = "sinh", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("sinh") })
                    CyberKey(text = "cosh", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("cosh") })
                }
            }
            SciCategory.POWERS -> {
                // Powers, Roots, Factorial, Reciprocal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberKey(text = "x²", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = onSquare)
                    CyberKey(text = "x³", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = onCube)
                    CyberKey(text = "xʸ", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onOperator("^") })
                    CyberKey(text = "√x", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("sqrt") })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberKey(text = "³√x", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("cbrt") })
                    CyberKey(text = "1/x", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = onReciprocal)
                    CyberKey(text = "n!", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = onFactorial)
                    CyberKey(text = "|x|", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("abs") })
                }
            }
            SciCategory.FUNCS -> {
                // Log, ln, constants pi, e, 10^x, e^x, nPr, nCr
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberKey(text = "log", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("log") })
                    CyberKey(text = "ln", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("ln") })
                    CyberKey(text = "10ˣ", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onDigit("10^") })
                    CyberKey(text = "eˣ", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("exp") })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberKey(text = "π", modifier = Modifier.weight(1f), height = funcHeight, fontSize = 16.sp, variant = KeyVariant.ACCENT, onClick = { onDigit("π") })
                    CyberKey(text = "e", modifier = Modifier.weight(1f), height = funcHeight, fontSize = 16.sp, variant = KeyVariant.ACCENT, onClick = { onDigit("e") })
                    CyberKey(text = "nPr", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("npr") })
                    CyberKey(text = "nCr", modifier = Modifier.weight(1f), height = funcHeight, fontSize = funcFontSize, variant = KeyVariant.FUNCTION, onClick = { onFunction("ncr") })
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Main Numerical & Operator Pad
        val numKeyHeight = 48.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "(", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.FUNCTION, onClick = { onDigit("(") })
            CyberKey(text = ")", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.FUNCTION, onClick = { onDigit(")") })
            CyberKey(text = ",", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.FUNCTION, onClick = { onDigit(",") })
            CyberKey(text = "÷", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("÷") })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "7", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("7") })
            CyberKey(text = "8", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("8") })
            CyberKey(text = "9", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("9") })
            CyberKey(text = "×", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("×") })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "4", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("4") })
            CyberKey(text = "5", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("5") })
            CyberKey(text = "6", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("6") })
            CyberKey(text = "−", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("−") })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "1", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("1") })
            CyberKey(text = "2", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("2") })
            CyberKey(text = "3", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("3") })
            CyberKey(text = "+", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("+") })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "+/-", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.FUNCTION, fontSize = 14.sp, onClick = onToggleSign)
            CyberKey(text = "0", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, onClick = { onDigit("0") })
            CyberKey(text = ".", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit(".") })
            CyberKey(text = "=", modifier = Modifier.weight(1f), height = numKeyHeight, variant = KeyVariant.ACTION_EQUALS, fontSize = 22.sp, onClick = onEvaluate)
        }
    }
}
