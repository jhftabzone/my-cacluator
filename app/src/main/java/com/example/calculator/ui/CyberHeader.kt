package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.calculator.viewmodel.CalculatorMode
import com.example.ui.theme.CyberThemeType
import com.example.ui.theme.LocalCyberTheme
import com.example.ui.theme.cyberBorder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CyberHeader(
    currentMode: CalculatorMode,
    onSelectMode: (CalculatorMode) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    hapticEnabled: Boolean,
    onToggleHaptic: () -> Unit,
    matrixRainEnabled: Boolean,
    onToggleMatrixRain: () -> Unit,
    scanlinesEnabled: Boolean,
    onToggleScanlines: () -> Unit,
    selectedTheme: CyberThemeType,
    onSelectTheme: (CyberThemeType) -> Unit,
    historyCount: Int,
    onOpenHistory: () -> Unit,
    systemStatus: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalCyberTheme.current
    var currentTime by remember { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
        while (true) {
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Top status HUD line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live clock & security badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYS_CLOCK: $currentTime",
                    color = theme.primaryColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status message
            Text(
                text = systemStatus,
                color = theme.secondaryColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Brand Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(theme.surfaceColor.copy(alpha = 0.9f))
                .cyberBorder(color = theme.primaryColor.copy(alpha = 0.5f), cornerRadius = 10.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // AC Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Custom AC Cyber Logo
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.cardColor)
                        .border(1.dp, theme.primaryColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AC",
                        color = theme.primaryColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stringResource(id = R.string.tagline),
                        color = theme.primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Quick Tool Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sound toggle
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier.size(34.dp).testTag("btn_toggle_sound")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Audio",
                        tint = if (soundEnabled) theme.primaryColor else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Theme selector
                IconButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.size(34.dp).testTag("btn_palette")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Color Theme",
                        tint = theme.secondaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Matrix Rain toggle
                IconButton(
                    onClick = onToggleMatrixRain,
                    modifier = Modifier.size(34.dp).testTag("btn_matrix_rain")
                ) {
                    Icon(
                        imageVector = Icons.Default.Grain,
                        contentDescription = "Toggle Matrix Rain",
                        tint = if (matrixRainEnabled) theme.primaryColor else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // History Drawer button with badge
                BadgedBox(
                    badge = {
                        if (historyCount > 0) {
                            Badge(
                                containerColor = theme.primaryColor,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "$historyCount",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.size(34.dp).testTag("btn_open_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation History",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mode Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) theme.primaryColor else theme.surfaceColor)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) theme.primaryColor else theme.primaryColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("mode_${mode.name.lowercase(Locale.ROOT)}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        color = if (isSelected) Color.Black else theme.primaryColor.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Theme selector dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "SELECT CYBER PALETTE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CyberThemeType.entries.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(t.surfaceColor)
                                .border(1.dp, t.primaryColor, RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectTheme(t)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = t.title,
                                color = t.primaryColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(t.primaryColor, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(t.secondaryColor, CircleShape)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("CLOSE", fontFamily = FontFamily.Monospace, color = theme.primaryColor)
                }
            },
            containerColor = theme.backgroundColor,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
