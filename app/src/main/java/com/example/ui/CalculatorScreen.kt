package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CalculationRecord
import com.example.domain.AngleUnit
import com.example.ui.components.CalculatorButton
import com.example.ui.components.CalculatorKeyStyle
import com.example.ui.components.DisplayPanel
import com.example.ui.components.HistoryPanel
import com.example.ui.theme.LocalCalculatorPalette
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    uiState: CalculatorUiState,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onPercent: () -> Unit,
    onPower: () -> Unit,
    onSquare: () -> Unit,
    onLeftParen: () -> Unit,
    onRightParen: () -> Unit,
    onSqrt: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (Char) -> Unit,
    onNegate: () -> Unit,
    onAllClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleScientificMode: () -> Unit,
    onToggleAngleUnit: () -> Unit,
    onOpenHistory: () -> Unit,
    onDismissHistory: () -> Unit,
    onSelectHistoryItem: (CalculationRecord) -> Unit,
    onLoadHistoryExpression: (CalculationRecord) -> Unit,
    onReuseLastResult: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCalculatorPalette.current
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(palette.bgTop, palette.bgBottom),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            val isWideLayout = maxWidth >= 640.dp || (maxWidth > maxHeight && maxWidth >= 540.dp)
            val isCompactHeight = maxHeight < 540.dp

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                CalculatorTopBar(
                    isDarkTheme = uiState.isDarkTheme,
                    isScientificMode = uiState.isScientificMode,
                    historyCount = uiState.history.size,
                    showHistoryButton = !isWideLayout,
                    onToggleTheme = onToggleTheme,
                    onToggleScientificMode = onToggleScientificMode,
                    onOpenHistory = onOpenHistory,
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 12.dp))

                if (isWideLayout) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Left Pane: Persistent Calculation History
                        Surface(
                            color = palette.surfaceCard,
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, palette.borderSubtle),
                            modifier = Modifier
                                .weight(0.36f)
                                .fillMaxHeight(),
                        ) {
                            HistoryPanel(
                                history = uiState.history,
                                onSelectResult = onSelectHistoryItem,
                                onLoadExpression = onLoadHistoryExpression,
                                onClearHistory = onClearHistory,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        // Right Pane: Display + (Optional Horizontal/Vertical Scientific) + Keypad
                        Box(
                            modifier = Modifier
                                .weight(0.64f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .widthIn(max = 620.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 12.dp),
                            ) {
                                DisplayPanel(
                                    expression = uiState.expression,
                                    livePreview = uiState.livePreview,
                                    isResultDisplayed = uiState.isResultDisplayed,
                                    error = uiState.error,
                                    lastExpression = uiState.lastExpression,
                                    lastResult = uiState.lastResult,
                                    isScientificMode = uiState.isScientificMode,
                                    angleUnit = uiState.angleUnit,
                                    onToggleAngleUnit = onToggleAngleUnit,
                                    onReuseLastResult = onReuseLastResult,
                                )

                                if (isCompactHeight) {
                                    // Expand scientific functions horizontally in short landscape windows
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        AnimatedVisibility(
                                            visible = uiState.isScientificMode,
                                            enter = expandHorizontally(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMedium,
                                                )
                                            ) + fadeIn(),
                                            exit = shrinkHorizontally() + fadeOut(),
                                        ) {
                                            ScientificSideStrip(
                                                angleUnit = uiState.angleUnit,
                                                keySpacing = 6.dp,
                                                onToggleAngleUnit = onToggleAngleUnit,
                                                onFunction = onFunction,
                                                onPower = onPower,
                                                onSquare = onSquare,
                                                onConstant = onConstant,
                                                modifier = Modifier
                                                    .width(136.dp)
                                                    .fillMaxHeight(),
                                            )
                                        }

                                        KeypadGrid(
                                            keySpacing = 6.dp,
                                            isCompactHeight = true,
                                            onDigit = onDigit,
                                            onDecimal = onDecimal,
                                            onOperator = onOperator,
                                            onPercent = onPercent,
                                            onLeftParen = onLeftParen,
                                            onRightParen = onRightParen,
                                            onSqrt = onSqrt,
                                            onNegate = onNegate,
                                            onAllClear = onAllClear,
                                            onBackspace = onBackspace,
                                            onEquals = onEquals,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                        )
                                    }
                                } else {
                                    // Tall tablet layout: expandable 5x2 drawer above keypad
                                    AnimatedVisibility(
                                        visible = uiState.isScientificMode,
                                        enter = expandVertically(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            )
                                        ) + fadeIn(),
                                        exit = shrinkVertically() + fadeOut(),
                                    ) {
                                        ScientificTopDrawer(
                                            angleUnit = uiState.angleUnit,
                                            keySpacing = 8.dp,
                                            onToggleAngleUnit = onToggleAngleUnit,
                                            onFunction = onFunction,
                                            onPower = onPower,
                                            onSquare = onSquare,
                                            onConstant = onConstant,
                                        )
                                    }

                                    KeypadGrid(
                                        keySpacing = 10.dp,
                                        isCompactHeight = false,
                                        onDigit = onDigit,
                                        onDecimal = onDecimal,
                                        onOperator = onOperator,
                                        onPercent = onPercent,
                                        onLeftParen = onLeftParen,
                                        onRightParen = onRightParen,
                                        onSqrt = onSqrt,
                                        onNegate = onNegate,
                                        onAllClear = onAllClear,
                                        onBackspace = onBackspace,
                                        onEquals = onEquals,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Compact / Handheld Portrait Layout
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 500.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            DisplayPanel(
                                expression = uiState.expression,
                                livePreview = uiState.livePreview,
                                isResultDisplayed = uiState.isResultDisplayed,
                                error = uiState.error,
                                lastExpression = uiState.lastExpression,
                                lastResult = uiState.lastResult,
                                isScientificMode = uiState.isScientificMode,
                                angleUnit = uiState.angleUnit,
                                onToggleAngleUnit = onToggleAngleUnit,
                                onReuseLastResult = onReuseLastResult,
                            )

                            AnimatedVisibility(
                                visible = uiState.isScientificMode,
                                enter = expandVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    )
                                ) + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                ScientificTopDrawer(
                                    angleUnit = uiState.angleUnit,
                                    keySpacing = 8.dp,
                                    onToggleAngleUnit = onToggleAngleUnit,
                                    onFunction = onFunction,
                                    onPower = onPower,
                                    onSquare = onSquare,
                                    onConstant = onConstant,
                                )
                            }

                            KeypadGrid(
                                keySpacing = if (uiState.isScientificMode) 8.dp else 10.dp,
                                isCompactHeight = uiState.isScientificMode,
                                onDigit = onDigit,
                                onDecimal = onDecimal,
                                onOperator = onOperator,
                                onPercent = onPercent,
                                onLeftParen = onLeftParen,
                                onRightParen = onRightParen,
                                onSqrt = onSqrt,
                                onNegate = onNegate,
                                onAllClear = onAllClear,
                                onBackspace = onBackspace,
                                onEquals = onEquals,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isHistorySheetOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ModalBottomSheet(
                onDismissRequest = onDismissHistory,
                sheetState = sheetState,
                containerColor = palette.surfaceCard,
                contentColor = palette.textPrimary,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            ) {
                HistoryPanel(
                    history = uiState.history,
                    onSelectResult = onSelectHistoryItem,
                    onLoadExpression = onLoadHistoryExpression,
                    onClearHistory = onClearHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun CalculatorTopBar(
    isDarkTheme: Boolean,
    isScientificMode: Boolean,
    historyCount: Int,
    showHistoryButton: Boolean,
    onToggleTheme: () -> Unit,
    onToggleScientificMode: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val palette = LocalCalculatorPalette.current
    val scientificCd = stringResource(R.string.toggle_scientific_mode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = palette.keyOperatorBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Calculate,
                        contentDescription = null,
                        tint = palette.accentPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = palette.textPrimary,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onToggleScientificMode,
                color = if (isScientificMode) palette.keyOperatorBg else palette.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isScientificMode) palette.accentPrimary.copy(alpha = 0.65f) else palette.borderSubtle,
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("scientific_toggle_button")
                    .semantics { contentDescription = scientificCd },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Functions,
                        contentDescription = null,
                        tint = if (isScientificMode) palette.accentPrimary else palette.textSecondary,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.scientific_badge),
                        color = if (isScientificMode) palette.accentPrimary else palette.textSecondary,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }

            if (showHistoryButton) {
                Surface(
                    onClick = onOpenHistory,
                    color = palette.surfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, palette.borderSubtle),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("history_button"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = stringResource(R.string.open_history),
                            tint = palette.textSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                        if (historyCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = palette.accentPrimary.copy(alpha = 0.18f),
                                shape = CircleShape,
                            ) {
                                Text(
                                    text = historyCount.toString(),
                                    color = palette.accentPrimary,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                onClick = onToggleTheme,
                color = palette.surfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, palette.borderSubtle),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("theme_toggle_button"),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = isDarkTheme,
                        transitionSpec = {
                            (fadeIn(tween(180)) + scaleIn(initialScale = 0.8f))
                                .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.8f))
                        },
                        label = "themeIcon",
                    ) { dark ->
                        Icon(
                            imageVector = if (dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = stringResource(
                                if (dark) R.string.toggle_theme_light else R.string.toggle_theme_dark
                            ),
                            tint = if (dark) palette.accentWarm else palette.accentPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScientificTopDrawer(
    angleUnit: AngleUnit,
    keySpacing: Dp,
    onToggleAngleUnit: () -> Unit,
    onFunction: (String) -> Unit,
    onPower: () -> Unit,
    onSquare: () -> Unit,
    onConstant: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val angleLabel = stringResource(if (angleUnit == AngleUnit.DEG) R.string.angle_deg else R.string.angle_rad)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scientific_panel"),
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        // Row S1: DEG/RAD, sin, cos, tan, log
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = angleLabel,
                keyStyle = CalculatorKeyStyle.OPERATOR,
                testTag = "key_angle_unit",
                accessibilityLabel = stringResource(R.string.cd_angle_unit),
                onClick = onToggleAngleUnit,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "sin",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_sin",
                accessibilityLabel = stringResource(R.string.cd_sin),
                onClick = { onFunction("sin") },
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "cos",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_cos",
                accessibilityLabel = stringResource(R.string.cd_cos),
                onClick = { onFunction("cos") },
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "tan",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_tan",
                accessibilityLabel = stringResource(R.string.cd_tan),
                onClick = { onFunction("tan") },
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "log",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_log",
                accessibilityLabel = stringResource(R.string.cd_log),
                onClick = { onFunction("log") },
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row S2: xʸ (^), x², ln, π, e
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "xʸ",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_power",
                accessibilityLabel = stringResource(R.string.cd_power),
                onClick = onPower,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "x²",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_square",
                accessibilityLabel = stringResource(R.string.cd_square),
                onClick = onSquare,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "ln",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_ln",
                accessibilityLabel = stringResource(R.string.cd_ln),
                onClick = { onFunction("ln") },
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "π",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_pi",
                accessibilityLabel = stringResource(R.string.cd_pi),
                onClick = { onConstant('π') },
                fontSize = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "e",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_euler",
                accessibilityLabel = stringResource(R.string.cd_euler),
                onClick = { onConstant('e') },
                fontSize = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun ScientificSideStrip(
    angleUnit: AngleUnit,
    keySpacing: Dp,
    onToggleAngleUnit: () -> Unit,
    onFunction: (String) -> Unit,
    onPower: () -> Unit,
    onSquare: () -> Unit,
    onConstant: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val angleLabel = stringResource(if (angleUnit == AngleUnit.DEG) R.string.angle_deg else R.string.angle_rad)

    Column(
        modifier = modifier.testTag("scientific_panel"),
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = angleLabel,
                keyStyle = CalculatorKeyStyle.OPERATOR,
                testTag = "key_angle_unit",
                accessibilityLabel = stringResource(R.string.cd_angle_unit),
                onClick = onToggleAngleUnit,
                fontSize = 13.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "sin",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_sin",
                accessibilityLabel = stringResource(R.string.cd_sin),
                onClick = { onFunction("sin") },
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "cos",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_cos",
                accessibilityLabel = stringResource(R.string.cd_cos),
                onClick = { onFunction("cos") },
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "tan",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_tan",
                accessibilityLabel = stringResource(R.string.cd_tan),
                onClick = { onFunction("tan") },
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "log",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_log",
                accessibilityLabel = stringResource(R.string.cd_log),
                onClick = { onFunction("log") },
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "ln",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_ln",
                accessibilityLabel = stringResource(R.string.cd_ln),
                onClick = { onFunction("ln") },
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "xʸ",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_power",
                accessibilityLabel = stringResource(R.string.cd_power),
                onClick = onPower,
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "x²",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_square",
                accessibilityLabel = stringResource(R.string.cd_square),
                onClick = onSquare,
                fontSize = 15.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "π",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_pi",
                accessibilityLabel = stringResource(R.string.cd_pi),
                onClick = { onConstant('π') },
                fontSize = 17.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "e",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_euler",
                accessibilityLabel = stringResource(R.string.cd_euler),
                onClick = { onConstant('e') },
                fontSize = 17.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun KeypadGrid(
    keySpacing: Dp,
    isCompactHeight: Boolean,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onPercent: () -> Unit,
    onLeftParen: () -> Unit,
    onRightParen: () -> Unit,
    onSqrt: () -> Unit,
    onNegate: () -> Unit,
    onAllClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numSize = if (isCompactHeight) 21.sp else 25.sp
    val fnSize = if (isCompactHeight) 17.sp else 20.sp
    val opSize = if (isCompactHeight) 22.sp else 26.sp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        // Row 1: √, (, ), %
        Row(
            modifier = Modifier
                .weight(0.86f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "√",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_sqrt",
                accessibilityLabel = stringResource(R.string.cd_square_root),
                onClick = onSqrt,
                fontSize = fnSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "(",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_lparen",
                accessibilityLabel = stringResource(R.string.cd_left_paren),
                onClick = onLeftParen,
                fontSize = fnSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = ")",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_rparen",
                accessibilityLabel = stringResource(R.string.cd_right_paren),
                onClick = onRightParen,
                fontSize = fnSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "%",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_percent",
                accessibilityLabel = stringResource(R.string.cd_percent),
                onClick = onPercent,
                fontSize = fnSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 2: AC, DEL, ±, ÷
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "AC",
                keyStyle = CalculatorKeyStyle.CLEAR,
                testTag = "key_ac",
                accessibilityLabel = stringResource(R.string.cd_all_clear),
                onClick = onAllClear,
                fontSize = fnSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "DEL",
                keyStyle = CalculatorKeyStyle.CLEAR,
                testTag = "key_del",
                accessibilityLabel = stringResource(R.string.cd_backspace),
                onClick = onBackspace,
                fontSize = fnSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "±",
                keyStyle = CalculatorKeyStyle.FUNCTION,
                testTag = "key_negate",
                accessibilityLabel = stringResource(R.string.cd_negate),
                onClick = onNegate,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "÷",
                keyStyle = CalculatorKeyStyle.OPERATOR,
                testTag = "key_divide",
                accessibilityLabel = stringResource(R.string.cd_divide),
                onClick = { onOperator('÷') },
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 3: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            for (digit in listOf('7', '8', '9')) {
                CalculatorButton(
                    label = digit.toString(),
                    keyStyle = CalculatorKeyStyle.NUMBER,
                    testTag = "key_$digit",
                    accessibilityLabel = digit.toString(),
                    onClick = { onDigit(digit) },
                    fontSize = numSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            CalculatorButton(
                label = "×",
                keyStyle = CalculatorKeyStyle.OPERATOR,
                testTag = "key_multiply",
                accessibilityLabel = stringResource(R.string.cd_multiply),
                onClick = { onOperator('×') },
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 4: 4, 5, 6, −
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            for (digit in listOf('4', '5', '6')) {
                CalculatorButton(
                    label = digit.toString(),
                    keyStyle = CalculatorKeyStyle.NUMBER,
                    testTag = "key_$digit",
                    accessibilityLabel = digit.toString(),
                    onClick = { onDigit(digit) },
                    fontSize = numSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            CalculatorButton(
                label = "−",
                keyStyle = CalculatorKeyStyle.OPERATOR,
                testTag = "key_subtract",
                accessibilityLabel = stringResource(R.string.cd_subtract),
                onClick = { onOperator('−') },
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 5: 1, 2, 3, +
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            for (digit in listOf('1', '2', '3')) {
                CalculatorButton(
                    label = digit.toString(),
                    keyStyle = CalculatorKeyStyle.NUMBER,
                    testTag = "key_$digit",
                    accessibilityLabel = digit.toString(),
                    onClick = { onDigit(digit) },
                    fontSize = numSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            CalculatorButton(
                label = "+",
                keyStyle = CalculatorKeyStyle.OPERATOR,
                testTag = "key_add",
                accessibilityLabel = stringResource(R.string.cd_add),
                onClick = { onOperator('+') },
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

        // Row 6: 0 (wide), ., =
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
        ) {
            CalculatorButton(
                label = "0",
                keyStyle = CalculatorKeyStyle.NUMBER,
                testTag = "key_0",
                accessibilityLabel = "0",
                onClick = { onDigit('0') },
                fontSize = numSize,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = ".",
                keyStyle = CalculatorKeyStyle.NUMBER,
                testTag = "key_decimal",
                accessibilityLabel = stringResource(R.string.cd_decimal),
                onClick = onDecimal,
                fontSize = numSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            CalculatorButton(
                label = "=",
                keyStyle = CalculatorKeyStyle.EQUALS,
                testTag = "key_equals",
                accessibilityLabel = stringResource(R.string.cd_equals),
                onClick = onEquals,
                fontSize = opSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}
