package com.example.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StandardKeypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onToggleSign: () -> Unit,
    onPercentage: () -> Unit,
    onEvaluate: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Memory Functions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val memHeight = 36.dp
            val memFontSize = 12.sp
            CyberKey(text = "MC", modifier = Modifier.weight(1f), height = memHeight, fontSize = memFontSize, variant = KeyVariant.FUNCTION, onClick = onMemoryClear)
            CyberKey(text = "MR", modifier = Modifier.weight(1f), height = memHeight, fontSize = memFontSize, variant = KeyVariant.FUNCTION, onClick = onMemoryRecall)
            CyberKey(text = "M+", modifier = Modifier.weight(1f), height = memHeight, fontSize = memFontSize, variant = KeyVariant.FUNCTION, onClick = onMemoryAdd)
            CyberKey(text = "M-", modifier = Modifier.weight(1f), height = memHeight, fontSize = memFontSize, variant = KeyVariant.FUNCTION, onClick = onMemorySubtract)
            CyberKey(text = "MS", modifier = Modifier.weight(1f), height = memHeight, fontSize = memFontSize, variant = KeyVariant.FUNCTION, onClick = onMemoryStore)
        }

        // Row 1: (, ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberKey(text = "(", modifier = Modifier.weight(1f), variant = KeyVariant.FUNCTION, onClick = { onDigit("(") })
            CyberKey(text = ")", modifier = Modifier.weight(1f), variant = KeyVariant.FUNCTION, onClick = { onDigit(")") })
            CyberKey(text = "%", modifier = Modifier.weight(1f), variant = KeyVariant.FUNCTION, onClick = onPercentage)
            CyberKey(text = "÷", modifier = Modifier.weight(1f), variant = KeyVariant.OPERATOR, fontSize = 22.sp, onClick = { onOperator("÷") })
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberKey(text = "7", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("7") })
            CyberKey(text = "8", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("8") })
            CyberKey(text = "9", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("9") })
            CyberKey(text = "×", modifier = Modifier.weight(1f), variant = KeyVariant.OPERATOR, fontSize = 22.sp, onClick = { onOperator("×") })
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberKey(text = "4", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("4") })
            CyberKey(text = "5", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("5") })
            CyberKey(text = "6", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("6") })
            CyberKey(text = "−", modifier = Modifier.weight(1f), variant = KeyVariant.OPERATOR, fontSize = 22.sp, onClick = { onOperator("−") })
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberKey(text = "1", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("1") })
            CyberKey(text = "2", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("2") })
            CyberKey(text = "3", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("3") })
            CyberKey(text = "+", modifier = Modifier.weight(1f), variant = KeyVariant.OPERATOR, fontSize = 22.sp, onClick = { onOperator("+") })
        }

        // Row 5: +/-, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberKey(text = "+/-", modifier = Modifier.weight(1f), variant = KeyVariant.FUNCTION, fontSize = 16.sp, onClick = onToggleSign)
            CyberKey(text = "0", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 20.sp, onClick = { onDigit("0") })
            CyberKey(text = ".", modifier = Modifier.weight(1f), variant = KeyVariant.DIGIT, fontSize = 22.sp, onClick = { onDigit(".") })
            CyberKey(text = "=", modifier = Modifier.weight(1f), variant = KeyVariant.ACTION_EQUALS, fontSize = 24.sp, onClick = onEvaluate)
        }
    }
}
