package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCalculatorPalette
import com.example.ui.theme.SpaceGroteskFontFamily

enum class CalculatorKeyStyle {
    NUMBER,
    FUNCTION,
    OPERATOR,
    CLEAR,
    EQUALS,
}

@Composable
fun CalculatorButton(
    label: String,
    keyStyle: CalculatorKeyStyle,
    testTag: String,
    accessibilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
) {
    val palette = LocalCalculatorPalette.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "keyScale",
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 24.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "keyCorner",
    )

    val containerColor = when (keyStyle) {
        CalculatorKeyStyle.NUMBER -> palette.keyNumberBg
        CalculatorKeyStyle.FUNCTION -> palette.keyFunctionBg
        CalculatorKeyStyle.OPERATOR -> palette.keyOperatorBg
        CalculatorKeyStyle.CLEAR -> palette.keyClearBg
        CalculatorKeyStyle.EQUALS -> palette.keyEqualsBg
    }

    val contentColor = when (keyStyle) {
        CalculatorKeyStyle.NUMBER -> palette.keyNumberFg
        CalculatorKeyStyle.FUNCTION -> palette.keyFunctionFg
        CalculatorKeyStyle.OPERATOR -> palette.keyOperatorFg
        CalculatorKeyStyle.CLEAR -> palette.keyClearFg
        CalculatorKeyStyle.EQUALS -> palette.keyEqualsFg
    }

    val borderStroke = when {
        keyStyle == CalculatorKeyStyle.EQUALS -> null
        !palette.isDark && keyStyle == CalculatorKeyStyle.NUMBER -> BorderStroke(1.dp, palette.borderSubtle)
        palette.isDark -> BorderStroke(1.dp, palette.borderSubtle.copy(alpha = 0.55f))
        else -> null
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (!palette.isDark && (keyStyle == CalculatorKeyStyle.NUMBER || keyStyle == CalculatorKeyStyle.EQUALS)) {
                    Modifier.shadow(
                        elevation = if (isPressed) 1.dp else 3.dp,
                        shape = shape,
                        ambientColor = palette.textPrimary.copy(alpha = 0.08f),
                        spotColor = palette.textPrimary.copy(alpha = 0.12f),
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(color = containerColor, shape = shape)
            .then(
                if (borderStroke != null) {
                    Modifier.border(borderStroke, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor.copy(alpha = 0.22f)),
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .testTag(testTag)
            .semantics {
                contentDescription = accessibilityLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = contentColor,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = when (keyStyle) {
                CalculatorKeyStyle.EQUALS, CalculatorKeyStyle.OPERATOR -> FontWeight.Bold
                CalculatorKeyStyle.CLEAR, CalculatorKeyStyle.FUNCTION -> FontWeight.SemiBold
                CalculatorKeyStyle.NUMBER -> FontWeight.Medium
            },
            fontSize = fontSize,
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
