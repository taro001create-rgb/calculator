package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CalculatorPreferencesRepository
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            val storage = remember { CalculatorPreferencesRepository(applicationContext) }
            val calculatorViewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModel.Factory(
                    storage = storage,
                    initialDarkTheme = systemDark,
                )
            )
            val uiState by calculatorViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (uiState.isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (uiState.isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                )
            }

            CalculatorTheme(darkTheme = uiState.isDarkTheme) {
                CalculatorScreen(
                    uiState = uiState,
                    onDigit = calculatorViewModel::onDigit,
                    onDecimal = calculatorViewModel::onDecimal,
                    onOperator = calculatorViewModel::onOperator,
                    onPercent = calculatorViewModel::onPercent,
                    onPower = calculatorViewModel::onPower,
                    onSquare = calculatorViewModel::onSquare,
                    onLeftParen = calculatorViewModel::onLeftParen,
                    onRightParen = calculatorViewModel::onRightParen,
                    onSqrt = calculatorViewModel::onSqrt,
                    onFunction = calculatorViewModel::onFunction,
                    onConstant = calculatorViewModel::onConstant,
                    onNegate = calculatorViewModel::onNegate,
                    onAllClear = calculatorViewModel::onAllClear,
                    onBackspace = calculatorViewModel::onBackspace,
                    onEquals = calculatorViewModel::onEquals,
                    onToggleTheme = calculatorViewModel::onToggleTheme,
                    onToggleScientificMode = calculatorViewModel::onToggleScientificMode,
                    onToggleAngleUnit = calculatorViewModel::onToggleAngleUnit,
                    onOpenHistory = calculatorViewModel::onOpenHistory,
                    onDismissHistory = calculatorViewModel::onDismissHistory,
                    onSelectHistoryItem = calculatorViewModel::onSelectHistoryItem,
                    onLoadHistoryExpression = calculatorViewModel::onLoadHistoryExpression,
                    onReuseLastResult = calculatorViewModel::onReuseLastResult,
                    onClearHistory = calculatorViewModel::onClearHistory,
                )
            }
        }
    }
}
