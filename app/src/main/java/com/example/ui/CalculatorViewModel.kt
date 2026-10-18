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

    fun onDigit(digit: String) {
        if (exactResult != null) {
            exactResult = null
            _expression.value = ""
        }
        if (_expression.value.length >= 45) return
        _errorMessage.value = null
        _lastEvaluation.value = null
        val current = _expression.value
        val actualDigit = if (digit == ".") "," else digit

        if (actualDigit == ",") {
            if (endsWithDecimalInCurrentNumber(current)) {
                return
            }
            val updated = if (current.isEmpty() || isOperatorChar(current.last()) || current.last() == '(') {
                current + "0,"
            } else {
                current + ","
            }
            _expression.value = updated
            repository.settingsManager.saveLastExpression(updated)
            updatePreview(updated)
            return
        }

        val updated = if (current == "0") {
            actualDigit
        } else {
            current + actualDigit
        }

        _expression.value = updated
        repository.settingsManager.saveLastExpression(updated)
        updatePreview(updated)
    }

    fun onOperator(op: String) {
        if (op == "%") {
            val last = _expression.value.lastOrNull() ?: return
            if (isOperatorChar(last) || last == '(') return
            onInsert(op)
            return
        }
        _errorMessage.value = null
        _lastEvaluation.value = null
        val current = exactResult?.let { "(${it})" } ?: _expression.value
        exactResult = null

        if (current.isEmpty()) {
            if (op == "−" || op == "-") {
                _expression.value = "−"
                repository.settingsManager.saveLastExpression("−")
                return
            }
            return
        }

        // If length limit exceeded and we are not replacing an operator, block it
        val lastChar = current.last()
        if (current.length >= 45 && !isOperatorChar(lastChar)) {
            return
        }

        // If last character is already an operator, replace it
        val updated = if ((op == "−" || op == "-") && isOperatorChar(lastChar)) {
            // A minus following an operator starts a signed operand (2×−3).
            if (current.length > 1 && lastChar in "−-" && isOperatorChar(current[current.lastIndex - 1])) current
            else current + op
        } else if (isOperatorChar(lastChar)) {
            val withoutSign = if (current.length > 1 && lastChar in "−-" && isOperatorChar(current[current.lastIndex - 1])) current.dropLast(1) else current
            withoutSign.dropLast(1) + op
        } else {
            current + op
        }

        _expression.value = updated
        repository.settingsManager.saveLastExpression(updated)
        updatePreview(updated)
    }

    fun onParentheses() {
        if (exactResult != null) {
            exactResult = null
            _expression.value = ""
        }
        if (_expression.value.length >= 45) return
        _errorMessage.value = null
        _lastEvaluation.value = null
        val current = _expression.value
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }

        val updated = if (current.isEmpty()) {
            "("
        } else {
            val lastChar = current.last()
            if (openCount > closeCount && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!')) {
                current + ")"
            } else if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e') {
                current + "×("
            } else {
                current + "("
            }
        }

        _expression.value = updated
        repository.settingsManager.saveLastExpression(updated)
        updatePreview(updated)
    }

    fun onInsert(text: String) {
        if (text == "^") {
            onOperator(text)
            return
        }
        exactResult?.let { result ->
            _expression.value = if (text == "%" || text == "!") "($result)" else ""
            exactResult = null
        }
        if (_expression.value.length >= 45) return
        _errorMessage.value = null
        _lastEvaluation.value = null
        val current = _expression.value
        val updated = current + text
        _expression.value = updated
        repository.settingsManager.saveLastExpression(updated)
        updatePreview(updated)
    }

    fun onBackspace() {
        exactResult = null
        _errorMessage.value = null
        _lastEvaluation.value = null
        val current = _expression.value
        if (current.isNotEmpty()) {
            // Handle multi-character tokens like "sin(", "asin(", "log(", "sqrt("
            val multiChars = listOf("asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "ln(", "sqrt(", "cbrt(")
            var deleted = false
            for (fn in multiChars) {
                if (current.endsWith(fn)) {
                    val updated = current.substring(0, current.length - fn.length)
                    _expression.value = updated
                    repository.settingsManager.saveLastExpression(updated)
                    updatePreview(updated)
                    deleted = true
                    break
                }
            }
            if (!deleted) {
                val updated = current.dropLast(1)
                _expression.value = updated
                repository.settingsManager.saveLastExpression(updated)
                updatePreview(updated)
            }
        }
    }

    fun onClear() {
        exactResult = null
        _lastEvaluation.value = null
        _expression.value = ""
        _resultPreview.value = null
        _errorMessage.value = null
        repository.settingsManager.saveLastExpression("")
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
                _expression.value = resultStr
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
        _lastEvaluation.value = null
        if (useResultOnly) {
            _expression.value = item.result
        } else {
            _expression.value = item.expression
        }
        _errorMessage.value = null
        repository.settingsManager.saveLastExpression(_expression.value)
        updatePreview(_expression.value)
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

    private fun endsWithDecimalInCurrentNumber(expr: String): Boolean {
        var i = expr.length - 1
        while (i >= 0) {
            val c = expr[i]
            if (c == '.' || c == ',') return true
            if (!c.isDigit()) break
            i--
        }
        return false
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
