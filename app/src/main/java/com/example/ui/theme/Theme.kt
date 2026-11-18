package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CalculatorPalette(
    val isDark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val keyNumberBg: Color,
    val keyNumberFg: Color,
    val keyFunctionBg: Color,
    val keyFunctionFg: Color,
    val keyOperatorBg: Color,
    val keyOperatorFg: Color,
    val keyClearBg: Color,
    val keyClearFg: Color,
    val keyEqualsBg: Color,
    val keyEqualsFg: Color,
    val accentPrimary: Color,
    val accentWarm: Color,
    val errorFg: Color,
    val errorBg: Color,
)

private val LightCalculatorPalette = CalculatorPalette(
    isDark = false,
    bgTop = LightBgTop,
    bgBottom = LightBgBottom,
    surfaceCard = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    borderSubtle = LightBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    keyNumberBg = LightKeyNumberBg,
    keyNumberFg = LightKeyNumberFg,
    keyFunctionBg = LightKeyFunctionBg,
    keyFunctionFg = LightKeyFunctionFg,
    keyOperatorBg = LightKeyOperatorBg,
    keyOperatorFg = LightKeyOperatorFg,
    keyClearBg = LightKeyClearBg,
    keyClearFg = LightKeyClearFg,
    keyEqualsBg = LightKeyEqualsBg,
    keyEqualsFg = LightKeyEqualsFg,
    accentPrimary = LightAccentCyan,
    accentWarm = LightAccentAmber,
    errorFg = LightErrorFg,
    errorBg = LightErrorBg,
)

private val DarkCalculatorPalette = CalculatorPalette(
    isDark = true,
    bgTop = DarkBgTop,
    bgBottom = DarkBgBottom,
    surfaceCard = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    borderSubtle = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    keyNumberBg = DarkKeyNumberBg,
    keyNumberFg = DarkKeyNumberFg,
    keyFunctionBg = DarkKeyFunctionBg,
    keyFunctionFg = DarkKeyFunctionFg,
    keyOperatorBg = DarkKeyOperatorBg,
    keyOperatorFg = DarkKeyOperatorFg,
    keyClearBg = DarkKeyClearBg,
    keyClearFg = DarkKeyClearFg,
    keyEqualsBg = DarkKeyEqualsBg,
    keyEqualsFg = DarkKeyEqualsFg,
    accentPrimary = DarkAccentCyan,
    accentWarm = DarkAccentAmber,
    errorFg = DarkErrorFg,
    errorBg = DarkErrorBg,
)

val LocalCalculatorPalette = staticCompositionLocalOf { DarkCalculatorPalette }

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccentCyan,
    onPrimary = DarkKeyEqualsFg,
    secondary = DarkAccentAmber,
    background = DarkBgBottom,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = DarkErrorFg,
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccentCyan,
    onPrimary = LightKeyEqualsFg,
    secondary = LightKeyEqualsBg,
    background = LightBgBottom,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = LightErrorFg,
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val targetPalette = if (darkTheme) DarkCalculatorPalette else LightCalculatorPalette
    val animSpec = tween<Color>(durationMillis = 280)

    val animatedPalette = CalculatorPalette(
        isDark = darkTheme,
        bgTop = animateColorAsState(targetPalette.bgTop, animSpec, label = "bgTop").value,
        bgBottom = animateColorAsState(targetPalette.bgBottom, animSpec, label = "bgBottom").value,
        surfaceCard = animateColorAsState(targetPalette.surfaceCard, animSpec, label = "surfaceCard").value,
        surfaceElevated = animateColorAsState(targetPalette.surfaceElevated, animSpec, label = "surfaceElevated").value,
        borderSubtle = animateColorAsState(targetPalette.borderSubtle, animSpec, label = "borderSubtle").value,
        textPrimary = animateColorAsState(targetPalette.textPrimary, animSpec, label = "textPrimary").value,
        textSecondary = animateColorAsState(targetPalette.textSecondary, animSpec, label = "textSecondary").value,
        textMuted = animateColorAsState(targetPalette.textMuted, animSpec, label = "textMuted").value,
        keyNumberBg = animateColorAsState(targetPalette.keyNumberBg, animSpec, label = "keyNumberBg").value,
        keyNumberFg = animateColorAsState(targetPalette.keyNumberFg, animSpec, label = "keyNumberFg").value,
        keyFunctionBg = animateColorAsState(targetPalette.keyFunctionBg, animSpec, label = "keyFunctionBg").value,
        keyFunctionFg = animateColorAsState(targetPalette.keyFunctionFg, animSpec, label = "keyFunctionFg").value,
        keyOperatorBg = animateColorAsState(targetPalette.keyOperatorBg, animSpec, label = "keyOperatorBg").value,
        keyOperatorFg = animateColorAsState(targetPalette.keyOperatorFg, animSpec, label = "keyOperatorFg").value,
        keyClearBg = animateColorAsState(targetPalette.keyClearBg, animSpec, label = "keyClearBg").value,
        keyClearFg = animateColorAsState(targetPalette.keyClearFg, animSpec, label = "keyClearFg").value,
        keyEqualsBg = animateColorAsState(targetPalette.keyEqualsBg, animSpec, label = "keyEqualsBg").value,
        keyEqualsFg = animateColorAsState(targetPalette.keyEqualsFg, animSpec, label = "keyEqualsFg").value,
        accentPrimary = animateColorAsState(targetPalette.accentPrimary, animSpec, label = "accentPrimary").value,
        accentWarm = animateColorAsState(targetPalette.accentWarm, animSpec, label = "accentWarm").value,
        errorFg = animateColorAsState(targetPalette.errorFg, animSpec, label = "errorFg").value,
        errorBg = animateColorAsState(targetPalette.errorBg, animSpec, label = "errorBg").value,
    )

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalCalculatorPalette provides animatedPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CalculatorTypography,
            content = content,
        )
    }
}
