package com.example

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalculationRecord
import com.example.data.CalculatorStorage
import com.example.domain.AngleUnit
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.CalculatorTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakeCalculatorStorage : CalculatorStorage {
    private val darkThemeState = MutableStateFlow<Boolean?>(true)
    private val scientificModeState = MutableStateFlow(false)
    private val angleUnitState = MutableStateFlow(AngleUnit.DEG)
    private val historyState = MutableStateFlow<List<CalculationRecord>>(emptyList())

    override val isDarkThemeFlow: Flow<Boolean?> = darkThemeState
    override val isScientificModeFlow: Flow<Boolean> = scientificModeState
    override val angleUnitFlow: Flow<AngleUnit> = angleUnitState
    override val historyFlow: Flow<List<CalculationRecord>> = historyState

    override suspend fun setDarkTheme(isDark: Boolean) {
        darkThemeState.value = isDark
    }

    override suspend fun setScientificMode(isScientific: Boolean) {
        scientificModeState.value = isScientific
    }

    override suspend fun setAngleUnit(unit: AngleUnit) {
        angleUnitState.value = unit
    }

    override suspend fun addHistoryRecord(record: CalculationRecord) {
        historyState.value = listOf(record) + historyState.value
    }

    override suspend fun clearHistory() {
        historyState.value = emptyList()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalculatorUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun launchCalculator(): CalculatorViewModel {
        val viewModel = CalculatorViewModel(storage = FakeCalculatorStorage(), initialDarkTheme = true)
        composeRule.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CalculatorTheme(darkTheme = state.isDarkTheme) {
                CalculatorScreen(
                    uiState = state,
                    onDigit = viewModel::onDigit,
                    onDecimal = viewModel::onDecimal,
                    onOperator = viewModel::onOperator,
                    onPercent = viewModel::onPercent,
                    onPower = viewModel::onPower,
                    onSquare = viewModel::onSquare,
                    onLeftParen = viewModel::onLeftParen,
                    onRightParen = viewModel::onRightParen,
                    onSqrt = viewModel::onSqrt,
                    onFunction = viewModel::onFunction,
                    onConstant = viewModel::onConstant,
                    onNegate = viewModel::onNegate,
                    onAllClear = viewModel::onAllClear,
                    onBackspace = viewModel::onBackspace,
                    onEquals = viewModel::onEquals,
                    onToggleTheme = viewModel::onToggleTheme,
                    onToggleScientificMode = viewModel::onToggleScientificMode,
                    onToggleAngleUnit = viewModel::onToggleAngleUnit,
                    onOpenHistory = viewModel::onOpenHistory,
                    onDismissHistory = viewModel::onDismissHistory,
                    onSelectHistoryItem = viewModel::onSelectHistoryItem,
                    onLoadHistoryExpression = viewModel::onLoadHistoryExpression,
                    onReuseLastResult = viewModel::onReuseLastResult,
                    onClearHistory = viewModel::onClearHistory,
                )
            }
        }
        composeRule.waitForIdle()
        return viewModel
    }

    @Test
    fun `performs basic arithmetic with brackets, sqrt, percentage, and clear`() {
        launchCalculator()

        // (2 + 3) × 4 = 20
        composeRule.onNodeWithTag("key_lparen").performClick()
        composeRule.onNodeWithTag("key_2").performClick()
        composeRule.onNodeWithTag("key_add").performClick()
        composeRule.onNodeWithTag("key_3").performClick()
        composeRule.onNodeWithTag("key_rparen").performClick()
        composeRule.onNodeWithTag("key_multiply").performClick()
        composeRule.onNodeWithTag("key_4").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("display_preview").assertTextEquals("= 20")
        composeRule.onNodeWithTag("key_equals").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("display_expression").assertTextEquals("20")

        composeRule.onNodeWithTag("previous_calculation_pill").assertIsDisplayed()

        // Clear all and test √(9) + 50% = 3.5
        composeRule.onNodeWithTag("key_ac").performClick()
        composeRule.onNodeWithTag("key_sqrt").performClick()
        composeRule.onNodeWithTag("key_9").performClick()
        composeRule.onNodeWithTag("key_rparen").performClick()
        composeRule.onNodeWithTag("key_add").performClick()
        composeRule.onNodeWithTag("key_5").performClick()
        composeRule.onNodeWithTag("key_0").performClick()
        composeRule.onNodeWithTag("key_percent").performClick()
        composeRule.onNodeWithTag("key_equals").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("display_expression").assertTextEquals("3.5")
    }

    @Test
    fun `scientific toggle expands panel and evaluates trig, log, and exponentiation`() {
        val viewModel = launchCalculator()
        assertFalse(viewModel.uiState.value.isScientificMode)

        composeRule.onNodeWithTag("scientific_toggle_button").performClick()
        composeRule.waitForIdle()
        assertTrue(viewModel.uiState.value.isScientificMode)
        composeRule.onNodeWithTag("scientific_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("display_angle_badge").assertTextEquals("DEG")

        // sin(30) + log(100) + 2^3 = 0.5 + 2 + 8 = 10.5
        composeRule.onNodeWithTag("key_sin").performClick()
        composeRule.onNodeWithTag("key_3").performClick()
        composeRule.onNodeWithTag("key_0").performClick()
        composeRule.onNodeWithTag("key_rparen").performClick()
        composeRule.onNodeWithTag("key_add").performClick()
        composeRule.onNodeWithTag("key_log").performClick()
        composeRule.onNodeWithTag("key_1").performClick()
        composeRule.onNodeWithTag("key_0").performClick()
        composeRule.onNodeWithTag("key_0").performClick()
        composeRule.onNodeWithTag("key_rparen").performClick()
        composeRule.onNodeWithTag("key_add").performClick()
        composeRule.onNodeWithTag("key_2").performClick()
        composeRule.onNodeWithTag("key_power").performClick()
        composeRule.onNodeWithTag("key_3").performClick()
        composeRule.onNodeWithTag("key_equals").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("display_expression").assertTextEquals("10.5")

        // Toggle angle unit to RAD and evaluate cos(π) = -1
        composeRule.onNodeWithTag("key_angle_unit").performClick()
        composeRule.waitForIdle()
        assertEquals(AngleUnit.RAD, viewModel.uiState.value.angleUnit)
        composeRule.onNodeWithTag("display_angle_badge").assertTextEquals("RAD")

        composeRule.onNodeWithTag("key_ac").performClick()
        composeRule.onNodeWithTag("key_cos").performClick()
        composeRule.onNodeWithTag("key_pi").performClick()
        composeRule.onNodeWithTag("key_equals").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("display_expression").assertTextEquals("−1")
    }

    @Test
    fun `del deletes single digit and division by zero shows error cleanly`() {
        launchCalculator()

        composeRule.onNodeWithTag("key_1").performClick()
        composeRule.onNodeWithTag("key_2").performClick()
        composeRule.onNodeWithTag("key_3").performClick()
        composeRule.onNodeWithTag("key_del").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("display_expression").assertTextEquals("12")

        composeRule.onNodeWithTag("key_divide").performClick()
        composeRule.onNodeWithTag("key_0").performClick()
        composeRule.onNodeWithTag("key_equals").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("display_error").assertIsDisplayed()

        composeRule.onNodeWithTag("key_ac").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("display_expression").assertTextEquals("0")
    }

    @Test
    fun `theme toggle switches between dark and light mode and history records calculations`() {
        val viewModel = launchCalculator()
        assertTrue(viewModel.uiState.value.isDarkTheme)

        composeRule.onNodeWithTag("theme_toggle_button").performClick()
        composeRule.waitForIdle()
        assertFalse(viewModel.uiState.value.isDarkTheme)

        composeRule.onNodeWithTag("key_7").performClick()
        composeRule.onNodeWithTag("key_multiply").performClick()
        composeRule.onNodeWithTag("key_8").performClick()
        composeRule.onNodeWithTag("key_equals").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("history_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("history_list").assertIsDisplayed()

        composeRule.onNodeWithTag("clear_history_button").performClick()
        composeRule.waitForIdle()
        assertTrue(viewModel.uiState.value.history.isEmpty())
    }
}
