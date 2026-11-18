package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.CalculatorStorage
import com.example.domain.AngleUnit
import com.example.domain.CalcError
import com.example.domain.CalculatorEngine
import com.example.domain.EvaluationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val livePreview: String = "",
    val isResultDisplayed: Boolean = false,
    val error: CalcError? = null,
    val lastExpression: String? = null,
    val lastResult: String? = null,
    val lastRawResult: String? = null,
    val history: List<CalculationRecord> = emptyList(),
    val isDarkTheme: Boolean = true,
    val isScientificMode: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val isHistorySheetOpen: Boolean = false,
)

class CalculatorViewModel(
    private val storage: CalculatorStorage,
    initialDarkTheme: Boolean = true,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState(isDarkTheme = initialDarkTheme))
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            storage.isDarkThemeFlow.collect { savedDark ->
                if (savedDark != null) {
                    _uiState.update { it.copy(isDarkTheme = savedDark) }
                }
            }
        }
        viewModelScope.launch {
            storage.isScientificModeFlow.collect { scientific ->
                _uiState.update { it.copy(isScientificMode = scientific) }
            }
        }
        viewModelScope.launch {
            storage.angleUnitFlow.collect { unit ->
                _uiState.update { state ->
                    state.copy(
                        angleUnit = unit,
                        livePreview = computeLivePreview(state.expression, unit),
                    )
                }
            }
        }
        viewModelScope.launch {
            storage.historyFlow.collect { historyList ->
                _uiState.update { state ->
                    val latest = historyList.firstOrNull()
                    state.copy(
                        history = historyList,
                        lastExpression = state.lastExpression ?: latest?.expression,
                        lastResult = state.lastResult ?: latest?.formattedResult,
                        lastRawResult = state.lastRawResult ?: latest?.rawResult,
                    )
                }
            }
        }
    }

    fun onDigit(digit: Char) {
        updateExpression { state ->
            CalculatorEngine.appendDigit(state.expression, digit, state.isResultDisplayed)
        }
    }

    fun onDecimal() {
        updateExpression { state ->
            CalculatorEngine.appendDecimal(state.expression, state.isResultDisplayed)
        }
    }

    fun onOperator(operator: Char) {
        updateExpression { state ->
            CalculatorEngine.appendOperator(state.expression, operator, state.isResultDisplayed)
        }
    }

    fun onPercent() {
        updateExpression { state ->
            CalculatorEngine.appendPercent(state.expression)
        }
    }

    fun onPower() {
        updateExpression { state ->
            CalculatorEngine.appendPower(state.expression, state.isResultDisplayed)
        }
    }

    fun onSquare() {
        updateExpression { state ->
            CalculatorEngine.appendSquare(state.expression, state.isResultDisplayed)
        }
    }

    fun onLeftParen() {
        updateExpression { state ->
            CalculatorEngine.appendLeftParen(state.expression, state.isResultDisplayed)
        }
    }

    fun onRightParen() {
        updateExpression { state ->
            CalculatorEngine.appendRightParen(state.expression)
        }
    }

    fun onSqrt() {
        updateExpression { state ->
            CalculatorEngine.appendSqrt(state.expression, state.isResultDisplayed)
        }
    }

    fun onFunction(functionName: String) {
        updateExpression { state ->
            CalculatorEngine.appendFunction(state.expression, functionName, state.isResultDisplayed)
        }
    }

    fun onConstant(constantSymbol: Char) {
        updateExpression { state ->
            CalculatorEngine.appendConstant(state.expression, constantSymbol, state.isResultDisplayed)
        }
    }

    fun onNegate() {
        updateExpression { state ->
            CalculatorEngine.toggleNegate(state.expression)
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.isResultDisplayed || state.error != null) {
                state.copy(
                    expression = if (state.error != null) {
                        CalculatorEngine.backspace(state.expression)
                    } else {
                        ""
                    },
                    livePreview = "",
                    isResultDisplayed = false,
                    error = null,
                )
            } else {
                val updated = CalculatorEngine.backspace(state.expression)
                state.copy(
                    expression = updated,
                    livePreview = computeLivePreview(updated, state.angleUnit),
                    error = null,
                )
            }
        }
    }

    fun onAllClear() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                livePreview = "",
                isResultDisplayed = false,
                error = null,
            )
        }
    }

    fun onEquals() {
        val current = _uiState.value
        if (current.expression.isBlank()) return

        val missingParens = CalculatorEngine.unclosedParenCount(current.expression)
        val normalizedExpression = if (missingParens > 0) {
            current.expression + ")".repeat(missingParens)
        } else {
            current.expression
        }

        val result = CalculatorEngine.evaluate(
            expression = normalizedExpression,
            autoCloseParens = false,
            angleUnit = current.angleUnit,
        )
        when (result) {
            is EvaluationResult.Success -> {
                val now = System.currentTimeMillis()
                val shouldRecord = CalculatorEngine.hasCalculableOperation(normalizedExpression)
                if (shouldRecord) {
                    val record = CalculationRecord(
                        id = now,
                        expression = normalizedExpression,
                        rawResult = result.rawString,
                        formattedResult = result.formattedDisplay,
                        timestampMillis = now,
                    )
                    viewModelScope.launch {
                        storage.addHistoryRecord(record)
                    }
                }
                _uiState.update { state ->
                    state.copy(
                        expression = result.rawString,
                        livePreview = "",
                        isResultDisplayed = true,
                        error = null,
                        lastExpression = if (shouldRecord) normalizedExpression else state.lastExpression,
                        lastResult = if (shouldRecord) result.formattedDisplay else state.lastResult,
                        lastRawResult = if (shouldRecord) result.rawString else state.lastRawResult,
                    )
                }
            }
            is EvaluationResult.Error -> {
                _uiState.update { state ->
                    state.copy(
                        error = result.error,
                        livePreview = "",
                        isResultDisplayed = false,
                    )
                }
            }
            null -> Unit
        }
    }

    fun onToggleTheme() {
        val newDark = !_uiState.value.isDarkTheme
        _uiState.update { it.copy(isDarkTheme = newDark) }
        viewModelScope.launch {
            storage.setDarkTheme(newDark)
        }
    }

    fun onToggleScientificMode() {
        val newScientific = !_uiState.value.isScientificMode
        _uiState.update { it.copy(isScientificMode = newScientific) }
        viewModelScope.launch {
            storage.setScientificMode(newScientific)
        }
    }

    fun onToggleAngleUnit() {
        val newUnit = if (_uiState.value.angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
        _uiState.update { state ->
            state.copy(
                angleUnit = newUnit,
                livePreview = computeLivePreview(state.expression, newUnit),
                error = null,
            )
        }
        viewModelScope.launch {
            storage.setAngleUnit(newUnit)
        }
    }

    fun onOpenHistory() {
        _uiState.update { it.copy(isHistorySheetOpen = true) }
    }

    fun onDismissHistory() {
        _uiState.update { it.copy(isHistorySheetOpen = false) }
    }

    fun onSelectHistoryItem(record: CalculationRecord) {
        _uiState.update { state ->
            val nextExpr = if (state.isResultDisplayed || state.expression.isEmpty()) {
                record.rawResult
            } else {
                val last = state.expression.last()
                if (last in setOf('+', '−', '×', '÷', '^', '(')) {
                    state.expression + record.rawResult
                } else {
                    record.rawResult
                }
            }
            state.copy(
                expression = nextExpr,
                livePreview = computeLivePreview(nextExpr, state.angleUnit),
                isResultDisplayed = false,
                error = null,
                isHistorySheetOpen = false,
            )
        }
    }

    fun onLoadHistoryExpression(record: CalculationRecord) {
        _uiState.update { state ->
            state.copy(
                expression = record.expression,
                livePreview = computeLivePreview(record.expression, state.angleUnit),
                isResultDisplayed = false,
                error = null,
                isHistorySheetOpen = false,
            )
        }
    }

    fun onReuseLastResult() {
        val raw = _uiState.value.lastRawResult ?: return
        _uiState.update { state ->
            val nextExpr = if (state.isResultDisplayed || state.expression.isEmpty()) {
                raw
            } else {
                val last = state.expression.last()
                if (last in setOf('+', '−', '×', '÷', '^', '(')) {
                    state.expression + raw
                } else {
                    raw
                }
            }
            state.copy(
                expression = nextExpr,
                livePreview = computeLivePreview(nextExpr, state.angleUnit),
                isResultDisplayed = false,
                error = null,
            )
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            storage.clearHistory()
        }
        _uiState.update { state ->
            state.copy(
                history = emptyList(),
                lastExpression = null,
                lastResult = null,
                lastRawResult = null,
            )
        }
    }

    private fun updateExpression(transform: (CalculatorUiState) -> String) {
        _uiState.update { state ->
            val updated = transform(state)
            state.copy(
                expression = updated,
                livePreview = computeLivePreview(updated, state.angleUnit),
                isResultDisplayed = false,
                error = null,
            )
        }
    }

    private fun computeLivePreview(expression: String, angleUnit: AngleUnit): String {
        if (expression.isBlank() || !CalculatorEngine.hasCalculableOperation(expression)) {
            return ""
        }
        return when (
            val result = CalculatorEngine.evaluate(
                expression = expression,
                autoCloseParens = true,
                angleUnit = angleUnit,
            )
        ) {
            is EvaluationResult.Success -> result.formattedDisplay
            else -> ""
        }
    }

    class Factory(
        private val storage: CalculatorStorage,
        private val initialDarkTheme: Boolean,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalculatorViewModel(storage, initialDarkTheme) as T
        }
    }
}
