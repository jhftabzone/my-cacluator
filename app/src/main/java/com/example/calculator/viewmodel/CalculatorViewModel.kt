package com.example.calculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.audio.CyberAudioEngine
import com.example.calculator.data.CalculationDao
import com.example.calculator.data.CalculatorDatabase
import com.example.calculator.data.CalculationEntity
import com.example.calculator.engine.*
import com.example.ui.theme.CyberThemeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class CalculatorMode(val title: String, val shortCode: String) {
    STANDARD("STANDARD", "STD"),
    SCIENTIFIC("SCIENTIFIC", "SCI"),
    HACKER("HACKER / DEV", "HEX"),
    CHALLENGE("CYBER HACK", "GAME")
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CalculatorDatabase.getInstance(application)
    private val dao: CalculationDao = database.calculationDao()
    val audioEngine = CyberAudioEngine(application)

    private val _calculatorMode = MutableStateFlow(CalculatorMode.STANDARD)
    val calculatorMode: StateFlow<CalculatorMode> = _calculatorMode.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _displayResult = MutableStateFlow("0")
    val displayResult: StateFlow<String> = _displayResult.asStateFlow()

    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    private val _angleMode = MutableStateFlow(AngleMode.DEG)
    val angleMode: StateFlow<AngleMode> = _angleMode.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    val hasMemory: StateFlow<Boolean> = _memoryValue.map { abs(it) > 1e-12 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _matrixRainEnabled = MutableStateFlow(true)
    val matrixRainEnabled: StateFlow<Boolean> = _matrixRainEnabled.asStateFlow()

    private val _scanlinesEnabled = MutableStateFlow(true)
    val scanlinesEnabled: StateFlow<Boolean> = _scanlinesEnabled.asStateFlow()

    private val _selectedTheme = MutableStateFlow(CyberThemeType.MATRIX_GREEN)
    val selectedTheme: StateFlow<CyberThemeType> = _selectedTheme.asStateFlow()

    private val _programmerState = MutableStateFlow(ProgrammerState())
    val programmerState: StateFlow<ProgrammerState> = _programmerState.asStateFlow()

    private val _systemStatus = MutableStateFlow("ONLINE // READY")
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    private val _isBootCompleted = MutableStateFlow(false)
    val isBootCompleted: StateFlow<Boolean> = _isBootCompleted.asStateFlow()

    private val _hudNotification = MutableStateFlow<String?>(null)
    val hudNotification: StateFlow<String?> = _hudNotification.asStateFlow()

    private val _challengeState = MutableStateFlow(CyberChallengeState())
    val challengeState: StateFlow<CyberChallengeState> = _challengeState.asStateFlow()

    val history: StateFlow<List<CalculationEntity>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var justCalculated = false

    init {
        // Prepare initial challenge question
        _challengeState.update { it.copy(activeQuestion = CyberChallengeEngine.generateQuestion(1)) }
    }

    fun completeBoot() {
        _isBootCompleted.value = true
        audioEngine.playBoot(_soundEnabled.value)
    }

    fun setMode(mode: CalculatorMode) {
        _calculatorMode.value = mode
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        _systemStatus.value = "MODE // ${mode.title}"
        if (mode == CalculatorMode.HACKER) {
            syncProgrammerWithExpression()
        }
    }

    fun toggleAngleMode() {
        _angleMode.value = if (_angleMode.value == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        audioEngine.playKeyClick(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        updateLivePreview(_expression.value)
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
        showNotification(if (_soundEnabled.value) "AUDIO PROTOCOL: ACTIVE" else "AUDIO PROTOCOL: MUTED")
    }

    fun toggleHaptic() {
        _hapticEnabled.value = !_hapticEnabled.value
        showNotification(if (_hapticEnabled.value) "HAPTIC FEEDBACK: ON" else "HAPTIC FEEDBACK: OFF")
    }

    fun toggleMatrixRain() {
        _matrixRainEnabled.value = !_matrixRainEnabled.value
        showNotification(if (_matrixRainEnabled.value) "MATRIX RAIN: VISIBLE" else "MATRIX RAIN: HIDDEN")
    }

    fun toggleScanlines() {
        _scanlinesEnabled.value = !_scanlinesEnabled.value
        showNotification(if (_scanlinesEnabled.value) "CRT SCANLINES: ON" else "CRT SCANLINES: OFF")
    }

    fun setTheme(theme: CyberThemeType) {
        _selectedTheme.value = theme
        audioEngine.playOperator(_soundEnabled.value)
        showNotification("PALETTE: ${theme.title}")
    }

    fun showNotification(msg: String) {
        _hudNotification.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(2200)
            if (_hudNotification.value == msg) {
                _hudNotification.value = null
            }
        }
    }

    // Input handlers
    fun onInput(token: String) {
        audioEngine.playKeyClick(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)

        val current = _expression.value
        val newExpr = if (justCalculated && isNumberOrParen(token)) {
            justCalculated = false
            token
        } else {
            justCalculated = false
            current + token
        }

        _expression.value = newExpr
        updateLivePreview(newExpr)
    }

    fun onOperator(op: String) {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)

        justCalculated = false
        val current = _expression.value
        if (current.isEmpty()) {
            if (_displayResult.value != "0" && !_displayResult.value.startsWith("ERR")) {
                _expression.value = _displayResult.value + " " + op + " "
            } else if (op == "−" || op == "-") {
                _expression.value = "−"
            }
            return
        }

        // Avoid repeated operators
        val trimmed = current.trimEnd()
        val lastChar = trimmed.lastOrNull()
        if (lastChar == '+' || lastChar == '−' || lastChar == '-' || lastChar == '×' || lastChar == '*' || lastChar == '÷' || lastChar == '/') {
            _expression.value = trimmed.dropLast(1).trimEnd() + " " + op + " "
        } else {
            _expression.value = "$current $op "
        }
        updateLivePreview(_expression.value)
    }

    fun onFunction(fn: String) {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        justCalculated = false
        val current = _expression.value
        _expression.value = "$current$fn("
        updateLivePreview(_expression.value)
    }

    fun onBackspace() {
        audioEngine.playKeyClick(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        justCalculated = false
        val current = _expression.value
        if (current.isNotEmpty()) {
            val updated = if (current.endsWith(" ")) {
                current.trimEnd().dropLast(1).trimEnd()
            } else {
                current.dropLast(1)
            }
            _expression.value = updated
            updateLivePreview(updated)
        }
    }

    fun onClear() {
        audioEngine.playClear(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        _expression.value = ""
        _displayResult.value = "0"
        _previewResult.value = null
        _systemStatus.value = "TERMINAL CLEARED"
        justCalculated = false
    }

    fun onToggleSign() {
        audioEngine.playKeyClick(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "−"
            return
        }
        if (current.startsWith("−")) {
            _expression.value = current.drop(1)
        } else if (current.startsWith("-")) {
            _expression.value = current.drop(1)
        } else {
            _expression.value = "−($current)"
        }
        updateLivePreview(_expression.value)
    }

    fun onPercentage() {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current%"
            updateLivePreview(_expression.value)
        }
    }

    fun onReciprocal() {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val current = _expression.value
        if (current.isEmpty() && _displayResult.value != "0") {
            _expression.value = "1/(${_displayResult.value})"
        } else if (current.isNotEmpty()) {
            _expression.value = "1/($current)"
        }
        updateLivePreview(_expression.value)
    }

    fun onSquare() {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^2"
            updateLivePreview(_expression.value)
        }
    }

    fun onCube() {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current^3"
            updateLivePreview(_expression.value)
        }
    }

    fun onFactorial() {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = "$current!"
            updateLivePreview(_expression.value)
        }
    }

    fun onEvaluate() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        val parser = MathParser(_angleMode.value)
        val result = parser.evaluate(expr)

        when (result) {
            is EvalResult.Success -> {
                audioEngine.playEquals(_soundEnabled.value)
                audioEngine.triggerHaptic(_hapticEnabled.value, strong = true)
                _displayResult.value = result.formatted
                _previewResult.value = null
                _systemStatus.value = "COMPUTED // 100% INTEGRITY"
                justCalculated = true

                // Save to history
                viewModelScope.launch(Dispatchers.IO) {
                    dao.insert(
                        CalculationEntity(
                            expression = expr,
                            result = result.formatted,
                            mode = _calculatorMode.value.name
                        )
                    )
                }

                // If in hacker mode, sync programmer state
                syncProgrammerFromNumber(result.value.toLong())
            }
            is EvalResult.Error -> {
                audioEngine.playError(_soundEnabled.value)
                audioEngine.triggerHaptic(_hapticEnabled.value, strong = true)
                _displayResult.value = result.message
                _previewResult.value = null
                _systemStatus.value = "SYNTAX REJECTED"
            }
        }
    }

    private fun updateLivePreview(expr: String) {
        if (expr.isBlank()) {
            _previewResult.value = null
            return
        }
        try {
            val parser = MathParser(_angleMode.value)
            val res = parser.evaluate(expr)
            if (res is EvalResult.Success) {
                _previewResult.value = res.formatted
            } else {
                _previewResult.value = null
            }
        } catch (_: Exception) {
            _previewResult.value = null
        }
    }

    // Memory functions
    fun memoryClear() {
        _memoryValue.value = 0.0
        audioEngine.playClear(_soundEnabled.value)
        showNotification("MEMORY CLEARED [MC]")
    }

    fun memoryRecall() {
        val mem = _memoryValue.value
        val formatted = MathParser().formatResult(mem)
        onInput(formatted)
        showNotification("MEMORY RECALLED [MR]: $formatted")
    }

    fun memoryAdd() {
        val currentVal = getCurrentValForMemory()
        _memoryValue.value += currentVal
        audioEngine.playOperator(_soundEnabled.value)
        showNotification("MEMORY [M+]: +$currentVal")
    }

    fun memorySubtract() {
        val currentVal = getCurrentValForMemory()
        _memoryValue.value -= currentVal
        audioEngine.playOperator(_soundEnabled.value)
        showNotification("MEMORY [M-]: -$currentVal")
    }

    fun memoryStore() {
        val currentVal = getCurrentValForMemory()
        _memoryValue.value = currentVal
        audioEngine.playOperator(_soundEnabled.value)
        showNotification("MEMORY STORED [MS]: $currentVal")
    }

    private fun getCurrentValForMemory(): Double {
        val parser = MathParser(_angleMode.value)
        val res = parser.evaluate(_expression.value.ifEmpty { _displayResult.value })
        return if (res is EvalResult.Success) res.value else 0.0
    }

    // History interaction
    fun reuseExpression(expr: String) {
        _expression.value = expr
        updateLivePreview(expr)
        audioEngine.playKeyClick(_soundEnabled.value)
        showNotification("EXPRESSION LOADED")
    }

    fun reuseResult(res: String) {
        onInput(res)
        showNotification("RESULT INSERTED")
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(id)
        }
        audioEngine.playClear(_soundEnabled.value)
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAll()
        }
        audioEngine.playClear(_soundEnabled.value)
        showNotification("ALL HISTORY PURGED")
    }

    // Programmer Mode logic
    fun setProgrammerWordSize(ws: WordSize) {
        _programmerState.update { it.copy(wordSize = ws) }
        audioEngine.playOperator(_soundEnabled.value)
        showNotification("WORD SIZE: ${ws.label}")
    }

    fun setProgrammerRadix(radix: RadixType) {
        _programmerState.update { it.copy(activeRadix = radix) }
        audioEngine.playKeyClick(_soundEnabled.value)
    }

    fun toggleProgrammerBit(bitIndex: Int) {
        audioEngine.playKeyClick(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val currentVal = _programmerState.value.currentValue
        val ws = _programmerState.value.wordSize
        val updatedVal = ProgrammerEngine.toggleBit(currentVal, bitIndex, ws)
        _programmerState.update { it.copy(currentValue = updatedVal) }
        _displayResult.value = updatedVal.toString()
        _expression.value = updatedVal.toString()
    }

    fun applyBitwiseOp(op: String, operand: Long? = null) {
        audioEngine.playOperator(_soundEnabled.value)
        audioEngine.triggerHaptic(_hapticEnabled.value)
        val currentVal = _programmerState.value.currentValue
        val ws = _programmerState.value.wordSize
        val b = operand ?: getSecondOperandFromExpr()
        val result = ProgrammerEngine.applyBitwise(currentVal, b, op, ws)
        _programmerState.update { it.copy(currentValue = result) }
        _displayResult.value = result.toString()
        _expression.value = "$currentVal $op $b"
        showNotification("$op EXECUTION // RESULT: $result")
    }

    fun checkPrimeNode() {
        audioEngine.playOperator(_soundEnabled.value)
        val currentVal = _programmerState.value.currentValue
        val (isPrime, msg) = ProgrammerEngine.checkPrime(currentVal)
        showNotification("PRIME TEST: $msg")
        _systemStatus.value = if (isPrime) "NODE IS PRIME // SECURE" else "NODE IS COMPOSITE"
    }

    fun generateRandomNode() {
        audioEngine.playOperator(_soundEnabled.value)
        val rnd = ProgrammerEngine.generateRandom(0, 65535)
        syncProgrammerFromNumber(rnd)
        _displayResult.value = rnd.toString()
        _expression.value = rnd.toString()
        showNotification("RANDOM VALUE GENERATED: $rnd")
    }

    private fun syncProgrammerWithExpression() {
        val parser = MathParser()
        val res = parser.evaluate(_expression.value.ifEmpty { _displayResult.value })
        if (res is EvalResult.Success) {
            syncProgrammerFromNumber(res.value.toLong())
        }
    }

    private fun syncProgrammerFromNumber(num: Long) {
        _programmerState.update { it.copy(currentValue = num) }
    }

    private fun getSecondOperandFromExpr(): Long {
        val parser = MathParser()
        val res = parser.evaluate(_expression.value)
        return if (res is EvalResult.Success) res.value.toLong() else 2L
    }

    // Cyber Challenge methods
    fun submitChallengeAnswer(selectedIndex: Int) {
        val current = _challengeState.value
        val q = current.activeQuestion ?: return
        val isCorrect = selectedIndex == q.correctIndex

        if (isCorrect) {
            audioEngine.playEquals(_soundEnabled.value)
            audioEngine.triggerHaptic(_hapticEnabled.value, strong = true)
            val newScore = current.score + q.rewardScore + (current.streak * 25)
            val newStreak = current.streak + 1
            val bestStreak = maxOf(current.bestStreak, newStreak)
            val newLevel = 1 + (newScore / 400)
            _challengeState.value = current.copy(
                score = newScore,
                streak = newStreak,
                bestStreak = bestStreak,
                level = newLevel,
                answeredCount = current.answeredCount + 1,
                feedbackMessage = "CIPHER OVERRIDDEN! +${q.rewardScore + (current.streak * 25)} PTS",
                isCorrect = true,
                activeQuestion = CyberChallengeEngine.generateQuestion(newLevel)
            )
        } else {
            audioEngine.playError(_soundEnabled.value)
            audioEngine.triggerHaptic(_hapticEnabled.value, strong = true)
            _challengeState.value = current.copy(
                streak = 0,
                feedbackMessage = "ACCESS DENIED! ${q.explanation}",
                isCorrect = false,
                activeQuestion = CyberChallengeEngine.generateQuestion(current.level)
            )
        }
    }

    fun nextChallengeQuestion() {
        _challengeState.update {
            it.copy(
                activeQuestion = CyberChallengeEngine.generateQuestion(it.level),
                feedbackMessage = null,
                isCorrect = null
            )
        }
    }

    fun resetChallenge() {
        _challengeState.value = CyberChallengeState(
            activeQuestion = CyberChallengeEngine.generateQuestion(1)
        )
        audioEngine.playClear(_soundEnabled.value)
    }

    private fun isNumberOrParen(token: String): Boolean {
        return token.all { it.isDigit() || it == '.' || it == '(' || it == 'π' || it == 'e' }
    }
}
