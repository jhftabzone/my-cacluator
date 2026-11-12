package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.ui.*
import com.example.calculator.viewmodel.CalculatorMode
import com.example.calculator.viewmodel.CalculatorViewModel
import com.example.ui.theme.CyberTheme
import com.example.ui.theme.MatrixRainBackground
import com.example.ui.theme.cyberBorder
import com.example.ui.theme.scanlines

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()

            CyberTheme(themeType = selectedTheme) {
                val isBootCompleted by viewModel.isBootCompleted.collectAsStateWithLifecycle()

                if (!isBootCompleted) {
                    CyberBootScreen(
                        onComplete = { viewModel.completeBoot() }
                    )
                } else {
                    CalculatorMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> { viewModel.onInput("0"); return true }
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> { viewModel.onInput("1"); return true }
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> { viewModel.onInput("2"); return true }
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> { viewModel.onInput("3"); return true }
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> { viewModel.onInput("4"); return true }
            KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> {
                if (event?.isShiftPressed == true) viewModel.onPercentage() else viewModel.onInput("5")
                return true
            }
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> {
                if (event?.isShiftPressed == true) viewModel.onOperator("^") else viewModel.onInput("6")
                return true
            }
            KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> { viewModel.onInput("7"); return true }
            KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> {
                if (event?.isShiftPressed == true) viewModel.onOperator("×") else viewModel.onInput("8")
                return true
            }
            KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> {
                if (event?.isShiftPressed == true) viewModel.onInput("(") else viewModel.onInput("9")
                return true
            }
            KeyEvent.KEYCODE_NUMPAD_ADD, KeyEvent.KEYCODE_PLUS -> { viewModel.onOperator("+"); return true }
            KeyEvent.KEYCODE_NUMPAD_SUBTRACT, KeyEvent.KEYCODE_MINUS -> { viewModel.onOperator("−"); return true }
            KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_STAR -> { viewModel.onOperator("×"); return true }
            KeyEvent.KEYCODE_NUMPAD_DIVIDE, KeyEvent.KEYCODE_SLASH -> { viewModel.onOperator("÷"); return true }
            KeyEvent.KEYCODE_NUMPAD_DOT, KeyEvent.KEYCODE_PERIOD -> { viewModel.onInput("."); return true }
            KeyEvent.KEYCODE_NUMPAD_EQUALS, KeyEvent.KEYCODE_EQUALS, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (event?.isShiftPressed == true) viewModel.onOperator("+") else viewModel.onEvaluate()
                return true
            }
            KeyEvent.KEYCODE_DEL -> { viewModel.onBackspace(); return true }
            KeyEvent.KEYCODE_ESCAPE -> { viewModel.onClear(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun CalculatorMainScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.calculatorMode.collectAsStateWithLifecycle()
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val displayResult by viewModel.displayResult.collectAsStateWithLifecycle()
    val previewResult by viewModel.previewResult.collectAsStateWithLifecycle()
    val angleMode by viewModel.angleMode.collectAsStateWithLifecycle()
    val hasMemory by viewModel.hasMemory.collectAsStateWithLifecycle()
    val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val matrixRainEnabled by viewModel.matrixRainEnabled.collectAsStateWithLifecycle()
    val scanlinesEnabled by viewModel.scanlinesEnabled.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val hudNotification by viewModel.hudNotification.collectAsStateWithLifecycle()
    val programmerState by viewModel.programmerState.collectAsStateWithLifecycle()
    val challengeState by viewModel.challengeState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = selectedTheme.backgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(selectedTheme.backgroundColor)
                .then(if (scanlinesEnabled) Modifier.scanlines() else Modifier)
        ) {
            // Optional Matrix Rain Background
            if (matrixRainEnabled) {
                MatrixRainBackground(
                    primaryColor = selectedTheme.primaryColor,
                    numColumns = 16,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Header HUD
                CyberHeader(
                    currentMode = currentMode,
                    onSelectMode = { viewModel.setMode(it) },
                    soundEnabled = soundEnabled,
                    onToggleSound = { viewModel.toggleSound() },
                    hapticEnabled = hapticEnabled,
                    onToggleHaptic = { viewModel.toggleHaptic() },
                    matrixRainEnabled = matrixRainEnabled,
                    onToggleMatrixRain = { viewModel.toggleMatrixRain() },
                    scanlinesEnabled = scanlinesEnabled,
                    onToggleScanlines = { viewModel.toggleScanlines() },
                    selectedTheme = selectedTheme,
                    onSelectTheme = { viewModel.setTheme(it) },
                    historyCount = history.size,
                    onOpenHistory = { showHistorySheet = true },
                    systemStatus = systemStatus
                )

                // Main Display (for Standard, Scientific, Hacker modes)
                if (currentMode != CalculatorMode.CHALLENGE) {
                    CalculatorDisplay(
                        expression = expression,
                        result = displayResult,
                        previewResult = previewResult,
                        angleMode = angleMode,
                        onToggleAngleMode = { viewModel.toggleAngleMode() },
                        hasMemory = hasMemory,
                        memoryValue = memoryValue,
                        onMemoryClear = { viewModel.memoryClear() },
                        onMemoryRecall = { viewModel.memoryRecall() },
                        onBackspace = { viewModel.onBackspace() },
                        onClear = { viewModel.onClear() },
                        onNotify = { viewModel.showNotification(it) },
                        calculatorMode = currentMode,
                        scanlinesEnabled = scanlinesEnabled
                    )
                }

                // Interactive Mode Body
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentMode,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "mode_transition"
                    ) { mode ->
                        when (mode) {
                            CalculatorMode.STANDARD -> {
                                StandardKeypad(
                                    onDigit = { viewModel.onInput(it) },
                                    onOperator = { viewModel.onOperator(it) },
                                    onToggleSign = { viewModel.onToggleSign() },
                                    onPercentage = { viewModel.onPercentage() },
                                    onEvaluate = { viewModel.onEvaluate() },
                                    onMemoryClear = { viewModel.memoryClear() },
                                    onMemoryRecall = { viewModel.memoryRecall() },
                                    onMemoryAdd = { viewModel.memoryAdd() },
                                    onMemorySubtract = { viewModel.memorySubtract() },
                                    onMemoryStore = { viewModel.memoryStore() }
                                )
                            }
                            CalculatorMode.SCIENTIFIC -> {
                                ScientificKeypad(
                                    onDigit = { viewModel.onInput(it) },
                                    onOperator = { viewModel.onOperator(it) },
                                    onFunction = { viewModel.onFunction(it) },
                                    onSquare = { viewModel.onSquare() },
                                    onCube = { viewModel.onCube() },
                                    onReciprocal = { viewModel.onReciprocal() },
                                    onFactorial = { viewModel.onFactorial() },
                                    onToggleSign = { viewModel.onToggleSign() },
                                    onEvaluate = { viewModel.onEvaluate() }
                                )
                            }
                            CalculatorMode.HACKER -> {
                                ProgrammerKeypad(
                                    programmerState = programmerState,
                                    onSelectWordSize = { viewModel.setProgrammerWordSize(it) },
                                    onSelectRadix = { viewModel.setProgrammerRadix(it) },
                                    onToggleBit = { viewModel.toggleProgrammerBit(it) },
                                    onBitwiseOp = { viewModel.applyBitwiseOp(it) },
                                    onCheckPrime = { viewModel.checkPrimeNode() },
                                    onGenerateRandom = { viewModel.generateRandomNode() },
                                    onDigit = { viewModel.onInput(it) },
                                    onOperator = { viewModel.onOperator(it) },
                                    onEvaluate = { viewModel.onEvaluate() }
                                )
                            }
                            CalculatorMode.CHALLENGE -> {
                                CyberChallengeView(
                                    state = challengeState,
                                    onSubmitAnswer = { viewModel.submitChallengeAnswer(it) },
                                    onNextQuestion = { viewModel.nextChallengeQuestion() },
                                    onReset = { viewModel.resetChallenge() }
                                )
                            }
                        }
                    }
                }
            }

            // Momentary Cyber HUD Notification Pill
            AnimatedVisibility(
                visible = hudNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            ) {
                hudNotification?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF071B11).copy(alpha = 0.96f))
                            .cyberBorder(strokeWidth = 1.dp, color = selectedTheme.primaryColor, cornerRadius = 8.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = selectedTheme.primaryColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Calculation History Bottom Sheet
    if (showHistorySheet) {
        HistorySheet(
            historyList = history,
            onReuseExpression = { viewModel.reuseExpression(it) },
            onReuseResult = { viewModel.reuseResult(it) },
            onDeleteItem = { viewModel.deleteHistoryItem(it) },
            onClearAll = { viewModel.clearAllHistory() },
            onDismiss = { showHistorySheet = false }
        )
    }
}
