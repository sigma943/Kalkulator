package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.engine.CalculationResult
import com.example.engine.CalculatorEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class HistoryViewMode {
    HIDDEN,
    HALF,
    FULL
}

sealed class Screen {
    object Calculator : Screen()
    object UnitConverter : Screen()
    object History : Screen()
    object Settings : Screen()
    object Themes : Screen()
}

data class EvaluationEvent(
    val previousExpression: String,
    val result: String,
    val timestamp: Long
)

class CalculatorViewModel(
    private val repository: CalculatorRepository
) : ViewModel() {

    val settingsManager: SettingsManager = repository.settingsManager
    val settings: StateFlow<AppSettings> = repository.settingsManager.settings

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Calculator)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _expression = MutableStateFlow(
        if (settings.value.keepLastExpression) settings.value.lastExpression else ""
    )
    val expression: StateFlow<String> = _expression.asStateFlow()
    private val _cursor = MutableStateFlow(_expression.value.length)
    val cursor: StateFlow<Int> = _cursor.asStateFlow()

    private val _resultPreview = MutableStateFlow<String?>(null)
    val resultPreview: StateFlow<String?> = _resultPreview.asStateFlow()

    private val _lastEvaluation = MutableStateFlow<EvaluationEvent?>(null)
    val lastEvaluation: StateFlow<EvaluationEvent?> = _lastEvaluation.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _angleUnit = MutableStateFlow(settings.value.defaultAngleUnit)
    val angleUnit: StateFlow<String> = _angleUnit.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(settings.value.showScientificByDefault)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _historyViewMode = MutableStateFlow(HistoryViewMode.HIDDEN)
    val historyViewMode: StateFlow<HistoryViewMode> = _historyViewMode.asStateFlow()

    private val _showHogRiderEasterEgg = MutableStateFlow(false)
    val showHogRiderEasterEgg: StateFlow<Boolean> = _showHogRiderEasterEgg.asStateFlow()

    private var exactResult: Double? = null

    val isHistoryExpanded: StateFlow<Boolean> = _historyViewMode
        .map { it != HistoryViewMode.HIDDEN }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        updatePreview(_expression.value)
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun toggleHistoryExpanded() {
        _historyViewMode.value = when (_historyViewMode.value) {
            HistoryViewMode.HIDDEN -> HistoryViewMode.HALF
            HistoryViewMode.HALF, HistoryViewMode.FULL -> HistoryViewMode.HIDDEN
        }
    }

    fun toggleHistoryFull() {
        _historyViewMode.value = when (_historyViewMode.value) {
            HistoryViewMode.HALF -> HistoryViewMode.FULL
            HistoryViewMode.FULL -> HistoryViewMode.HALF
            HistoryViewMode.HIDDEN -> HistoryViewMode.HALF
        }
    }

    fun setHistoryViewMode(mode: HistoryViewMode) {
        _historyViewMode.value = mode
    }

    fun setHistoryExpanded(expanded: Boolean) {
        _historyViewMode.value = if (expanded) HistoryViewMode.HALF else HistoryViewMode.HIDDEN
    }

    fun navigateBack(): Boolean {
        if (_historyViewMode.value != HistoryViewMode.HIDDEN) {
            _historyViewMode.value = HistoryViewMode.HIDDEN
            return true
        }
        return if (_currentScreen.value != Screen.Calculator) {
            _currentScreen.value = Screen.Calculator
            true
        } else {
            false
        }
    }

    fun toggleScientific() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    fun toggleAngleUnit() {
        val next = if (_angleUnit.value == "DEG") "RAD" else "DEG"
        _angleUnit.value = next
        updatePreview(_expression.value)
    }

    fun moveCursor(position: Int) {
        val cursor = position.coerceIn(0, _expression.value.length)
        if (cursor != _expression.value.length) {
            exactResult = null
            _lastEvaluation.value = null
        }
        _cursor.value = cursor
    }

    fun onDigit(digit: String) {
        startFreshAfterResult()
        val current = _expression.value
        val position = _cursor.value
        val before = current.take(position)
        val after = current.drop(position)
        val digitText = if (digit == ".") "," else digit

        if (digitText == ",") {
            val numberCharacters: (Char) -> Boolean = { it.isDigit() || it in "., " }
            val number = before.takeLastWhile(numberCharacters) + after.takeWhile(numberCharacters)
            if (number.any { it == '.' || it == ',' }) return
            val separator = if (number.isEmpty() && (before.isEmpty() ||
                    isOperatorChar(before.last()) || before.last() == '(')) "0," else ","
            editAtCursor(separator)
        } else if (current == "0" && position == 1) {
            updateExpression(digitText)
        } else {
            editAtCursor(digitText)
        }
    }

    fun onOperator(op: String) {
        if (op == "%") {
            val previous = _expression.value.getOrNull(_cursor.value - 1) ?: return
            if (isOperatorChar(previous) || previous == '(') return
            onInsert(op)
            return
        }
        continueAfterResult()
        val current = _expression.value
        val before = current.take(_cursor.value)
        val after = current.drop(_cursor.value)
        val isMinus = op == "−" || op == "-"
        val last = before.lastOrNull()
        if (last == null) {
            if (isMinus) editAtCursor("−")
            return
        }

        val hasSignedOperand = before.length > 1 && last in "−-" &&
            isOperatorChar(before[before.lastIndex - 1])
        val updatedBefore = when {
            isMinus && isOperatorChar(last) -> if (hasSignedOperand) before else before + op
            isOperatorChar(last) -> before.dropLast(if (hasSignedOperand) 2 else 1) + op
            else -> before + op
        }
        val updated = updatedBefore + after
        if (updated.length > 45 && updated.length > current.length) return
        updateExpression(updated, updatedBefore.length)
    }

    fun onParentheses() {
        startFreshAfterResult()
        val before = _expression.value.take(_cursor.value)
        val last = before.lastOrNull()
        val hasOpen = before.count { it == '(' } > before.count { it == ')' }
        val afterOperand = last != null && (last.isDigit() || last in ")πe")
        editAtCursor(when {
            hasOpen && (afterOperand || last == '!') -> ")"
            afterOperand -> "×("
            else -> "("
        })
    }

    fun onInsert(text: String) {
        if (text == "^") {
            onOperator(text)
            return
        }
        if (text == "%" || text == "!") continueAfterResult() else startFreshAfterResult()
        editAtCursor(text)
    }

    fun onBackspace() {
        exactResult = null
        _errorMessage.value = null
        _lastEvaluation.value = null
        val position = _cursor.value
        if (position == 0) return
        val current = _expression.value
        val before = current.take(position)
        val functions = listOf("asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "ln(", "sqrt(", "cbrt(")
        val deleteCount = functions.firstOrNull { before.endsWith(it) }?.length ?: 1
        val start = position - deleteCount
        updateExpression(current.removeRange(start, position), start)
    }

    fun onClear() {
        exactResult = null
        updateExpression("")
    }

    private fun startFreshAfterResult() {
        if (exactResult == null) return
        exactResult = null
        setExpression("")
    }

    private fun continueAfterResult() {
        val result = exactResult ?: return
        exactResult = null
        setExpression("($result)")
    }

    private fun editAtCursor(text: String) {
        val current = _expression.value
        if (current.length + text.length > 45) return
        val cursor = _cursor.value
        updateExpression(current.take(cursor) + text + current.drop(cursor), cursor + text.length)
    }

    private fun setExpression(text: String, cursor: Int = text.length) {
        _expression.value = text
        _cursor.value = cursor
    }

    private fun updateExpression(text: String, cursor: Int = text.length) {
        _errorMessage.value = null
        _lastEvaluation.value = null
        setExpression(text, cursor)
        repository.settingsManager.saveLastExpression(text)
        updatePreview(text)
    }

    fun onClearAll() {
        onClear()
    }

    fun onEquals() {
        if (exactResult != null) return
        val enteredExpression = _expression.value
        // Complete open parentheses just as the live preview does.
        val missing = enteredExpression.count { it == '(' } - enteredExpression.count { it == ')' }
        val expr = enteredExpression + ")".repeat(missing.coerceAtLeast(0))
        if (expr.isBlank()) return

        val engineAngle = if (_angleUnit.value == "DEG") CalculatorEngine.AngleUnit.DEG else CalculatorEngine.AngleUnit.RAD
        val cfg = settings.value

        when (val res = CalculatorEngine.evaluate(
            expression = expr,
            angleUnit = engineAngle,
            decimalPlaces = cfg.decimalPlaces,
            formatGrouping = cfg.formatLargeNumbers
        )) {
            is CalculationResult.Success -> {
                exactResult = res.value
                val resultStr = res.formatted
                viewModelScope.launch {
                    repository.saveHistory(expr, resultStr)
                }
                _lastEvaluation.value = EvaluationEvent(
                    previousExpression = expr,
                    result = resultStr,
                    timestamp = System.currentTimeMillis()
                )
                setExpression(resultStr)
                _resultPreview.value = null
                _errorMessage.value = null
                repository.settingsManager.saveLastExpression(res.value.toString())
            }
            is CalculationResult.Error -> {
                _lastEvaluation.value = null
                _errorMessage.value = res.message.ifEmpty { "Błąd" }
            }
        }
    }

    fun dismissHogRiderEasterEgg() {
        _showHogRiderEasterEgg.value = false
    }

    fun triggerHogRiderEasterEgg() {
        _showHogRiderEasterEgg.value = true
    }

    fun onSelectHistory(item: HistoryEntity, useResultOnly: Boolean) {
        exactResult = null
        updateExpression(if (useResultOnly) item.result else item.expression)
        _historyViewMode.value = HistoryViewMode.HIDDEN
        _currentScreen.value = Screen.Calculator
    }

    fun onDeleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    private fun updatePreview(expr: String) {
        if (expr.isBlank()) {
            _resultPreview.value = null
            return
        }
        val engineAngle = if (_angleUnit.value == "DEG") CalculatorEngine.AngleUnit.DEG else CalculatorEngine.AngleUnit.RAD
        val cfg = settings.value

        val preview = CalculatorEngine.evaluatePreview(
            expression = expr,
            angleUnit = engineAngle,
            decimalPlaces = cfg.decimalPlaces,
            formatGrouping = cfg.formatLargeNumbers
        )
        _resultPreview.value = preview
    }

    private fun isOperatorChar(c: Char): Boolean {
        return c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^'
    }
}

class CalculatorViewModelFactory(
    private val repository: CalculatorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
