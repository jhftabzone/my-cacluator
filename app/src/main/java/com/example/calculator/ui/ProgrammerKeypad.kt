package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.calculator.engine.ProgrammerState
import com.example.calculator.engine.RadixType
import com.example.calculator.engine.WordSize
import com.example.ui.theme.LocalCyberTheme
import com.example.ui.theme.cyberBorder

@Composable
fun ProgrammerKeypad(
    programmerState: ProgrammerState,
    onSelectWordSize: (WordSize) -> Unit,
    onSelectRadix: (RadixType) -> Unit,
    onToggleBit: (Int) -> Unit,
    onBitwiseOp: (String) -> Unit,
    onCheckPrime: () -> Unit,
    onGenerateRandom: () -> Unit,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEvaluate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalCyberTheme.current
    var showBitboard by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Radix Registers Display HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(theme.surfaceColor)
                .cyberBorder(color = theme.primaryColor.copy(alpha = 0.4f), cornerRadius = 8.dp)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RadixRow(
                radix = RadixType.HEX,
                value = programmerState.hexString,
                isActive = programmerState.activeRadix == RadixType.HEX,
                onClick = { onSelectRadix(RadixType.HEX) }
            )
            RadixRow(
                radix = RadixType.DEC,
                value = programmerState.decString,
                isActive = programmerState.activeRadix == RadixType.DEC,
                onClick = { onSelectRadix(RadixType.DEC) }
            )
            RadixRow(
                radix = RadixType.OCT,
                value = programmerState.octString,
                isActive = programmerState.activeRadix == RadixType.OCT,
                onClick = { onSelectRadix(RadixType.OCT) }
            )
            RadixRow(
                radix = RadixType.BIN,
                value = programmerState.binString.chunked(4).joinToString(" "),
                isActive = programmerState.activeRadix == RadixType.BIN,
                onClick = { onSelectRadix(RadixType.BIN) }
            )
        }

        // Word Size Chips & Bitboard Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WordSize.entries.forEach { ws ->
                val isSelected = programmerState.wordSize == ws
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) theme.primaryColor else theme.cardColor)
                        .border(
                            1.dp,
                            if (isSelected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectWordSize(ws) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ws.name,
                        color = if (isSelected) Color.Black else theme.primaryColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bitboard expand button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (showBitboard) theme.secondaryColor else theme.cardColor)
                    .border(1.dp, theme.secondaryColor, RoundedCornerShape(4.dp))
                    .clickable { showBitboard = !showBitboard }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showBitboard) "BITS ▲" else "BITS ▼",
                    color = if (showBitboard) Color.Black else theme.secondaryColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Interactive Bit Matrix (tappable bits!)
        if (showBitboard) {
            val totalBits = programmerState.wordSize.bits
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.cardColor.copy(alpha = 0.8f))
                    .border(1.dp, theme.secondaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "INTERACTIVE BIT REGISTERS [TAP TO TOGGLE] // ACTIVE: $totalBits BITS",
                    color = theme.secondaryColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Render bits in 8-bit byte groups
                val byteCount = totalBits / 8
                for (b in (byteCount - 1) downTo 0) {
                    val startBit = (b + 1) * 8 - 1
                    val endBit = b * 8
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "B$b:",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(22.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            for (bitIdx in startBit downTo endBit) {
                                val isSet = ((programmerState.currentValue ushr bitIdx) and 1L) == 1L
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isSet) theme.primaryColor else Color.Black)
                                        .border(
                                            0.5.dp,
                                            if (isSet) theme.primaryColor else Color.DarkGray,
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable { onToggleBit(bitIdx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSet) "1" else "0",
                                        color = if (isSet) Color.Black else theme.primaryColor.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bitwise Logic Operator Row 1: AND, OR, XOR, NOT
        val bitBtnHeight = 40.dp
        val bitBtnFontSize = 12.sp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "AND", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("AND") })
            CyberKey(text = "OR", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("OR") })
            CyberKey(text = "XOR", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("XOR") })
            CyberKey(text = "NOT", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("NOT") })
        }

        // Bitwise Logic Operator Row 2: <<, >>, NAND, NOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "<<", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("SHL") })
            CyberKey(text = ">>", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("SHR") })
            CyberKey(text = "NAND", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("NAND") })
            CyberKey(text = "NOR", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onBitwiseOp("NOR") })
        }

        // Developer Utilities: PRIME?, RND, MOD, GCD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "PRIME?", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = 11.sp, variant = KeyVariant.ACCENT, onClick = onCheckPrime)
            CyberKey(text = "RANDOM", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = 11.sp, variant = KeyVariant.ACCENT, onClick = onGenerateRandom)
            CyberKey(text = "MOD", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onOperator("%") })
            CyberKey(text = "GCD", modifier = Modifier.weight(1f), height = bitBtnHeight, fontSize = bitBtnFontSize, variant = KeyVariant.FUNCTION, onClick = { onDigit("gcd(") })
        }

        // Hex & Digit Input Cluster
        val hexEnabled = programmerState.activeRadix == RadixType.HEX
        val octEnabled = programmerState.activeRadix != RadixType.BIN
        val binOnly = programmerState.activeRadix == RadixType.BIN

        // Hex A..F
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "A", modifier = Modifier.weight(1f), height = 44.dp, variant = if (hexEnabled) KeyVariant.FUNCTION else KeyVariant.DIGIT, onClick = { if (hexEnabled) onDigit("A") })
            CyberKey(text = "B", modifier = Modifier.weight(1f), height = 44.dp, variant = if (hexEnabled) KeyVariant.FUNCTION else KeyVariant.DIGIT, onClick = { if (hexEnabled) onDigit("B") })
            CyberKey(text = "C", modifier = Modifier.weight(1f), height = 44.dp, variant = if (hexEnabled) KeyVariant.FUNCTION else KeyVariant.DIGIT, onClick = { if (hexEnabled) onDigit("C") })
            CyberKey(text = "D", modifier = Modifier.weight(1f), height = 44.dp, variant = if (hexEnabled) KeyVariant.FUNCTION else KeyVariant.DIGIT, onClick = { if (hexEnabled) onDigit("D") })
            CyberKey(text = "E", modifier = Modifier.weight(1f), height = 44.dp, variant = if (hexEnabled) KeyVariant.FUNCTION else KeyVariant.DIGIT, onClick = { if (hexEnabled) onDigit("E") })
            CyberKey(text = "F", modifier = Modifier.weight(1f), height = 44.dp, variant = if (hexEnabled) KeyVariant.FUNCTION else KeyVariant.DIGIT, onClick = { if (hexEnabled) onDigit("F") })
        }

        // Numbers 7, 8, 9, ÷
        val keyH = 46.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "7", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (!binOnly) onDigit("7") })
            CyberKey(text = "8", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (programmerState.activeRadix in listOf(RadixType.DEC, RadixType.HEX)) onDigit("8") })
            CyberKey(text = "9", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (programmerState.activeRadix in listOf(RadixType.DEC, RadixType.HEX)) onDigit("9") })
            CyberKey(text = "÷", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("÷") })
        }

        // Numbers 4, 5, 6, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "4", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (!binOnly) onDigit("4") })
            CyberKey(text = "5", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (!binOnly) onDigit("5") })
            CyberKey(text = "6", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (!binOnly) onDigit("6") })
            CyberKey(text = "×", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("×") })
        }

        // Numbers 1, 2, 3, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "1", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { onDigit("1") })
            CyberKey(text = "2", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (!binOnly) onDigit("2") })
            CyberKey(text = "3", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.DIGIT, onClick = { if (!binOnly) onDigit("3") })
            CyberKey(text = "−", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("−") })
        }

        // Numbers 0, +, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CyberKey(text = "0", modifier = Modifier.weight(2f), height = keyH, variant = KeyVariant.DIGIT, onClick = { onDigit("0") })
            CyberKey(text = "+", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.OPERATOR, fontSize = 20.sp, onClick = { onOperator("+") })
            CyberKey(text = "=", modifier = Modifier.weight(1f), height = keyH, variant = KeyVariant.ACTION_EQUALS, fontSize = 22.sp, onClick = onEvaluate)
        }
    }
}

@Composable
private fun RadixRow(
    radix: RadixType,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalCyberTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) theme.primaryColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (isActive) theme.primaryColor else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${radix.label}:",
            color = if (isActive) theme.primaryColor else Color.Gray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp)
        )
        Text(
            text = value,
            color = if (isActive) Color.White else Color(0xFFC0C0C0),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
