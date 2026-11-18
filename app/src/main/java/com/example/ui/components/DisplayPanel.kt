package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AngleUnit
import com.example.domain.CalcError
import com.example.domain.CalculatorEngine
import com.example.ui.theme.LocalCalculatorPalette
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay

@Composable
fun DisplayPanel(
    expression: String,
    livePreview: String,
    isResultDisplayed: Boolean,
    error: CalcError?,
    lastExpression: String?,
    lastResult: String?,
    isScientificMode: Boolean,
    angleUnit: AngleUnit,
    onToggleAngleUnit: () -> Unit,
    onReuseLastResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCalculatorPalette.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    var justCopied by remember { mutableStateOf(false) }

    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1500)
            justCopied = false
        }
    }

    val formattedExpression = remember(expression, isResultDisplayed) {
        if (expression.isEmpty()) {
            ""
        } else if (isResultDisplayed) {
            val parsed = expression.replace('−', '-').toBigDecimalOrNull()
            if (parsed != null) CalculatorEngine.formatForDisplay(parsed) else expression
        } else {
            expression
        }
    }

    val targetFontSize = when {
        formattedExpression.length <= 9 -> 44f
        formattedExpression.length <= 14 -> 36f
        formattedExpression.length <= 20 -> 28f
        else -> 22f
    }
    val animatedFontSize by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = tween(durationMillis = 150),
        label = "displayFontSize",
    )

    val shape = RoundedCornerShape(28.dp)
    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            palette.surfaceCard,
            palette.surfaceElevated.copy(alpha = if (palette.isDark) 0.9f else 0.7f),
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush = cardBrush, shape = shape)
            .border(BorderStroke(1.dp, palette.borderSubtle), shape)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top bar inside display: DEG/RAD badge on left, Previous calculation pill on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    val angleCd = stringResource(R.string.cd_angle_unit)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isScientificMode,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Surface(
                            onClick = onToggleAngleUnit,
                            color = palette.keyOperatorBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, palette.accentPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .testTag("display_angle_badge")
                                .semantics { contentDescription = angleCd },
                        ) {
                            Text(
                                text = stringResource(
                                    if (angleUnit == AngleUnit.DEG) R.string.angle_deg else R.string.angle_rad
                                ),
                                color = palette.keyOperatorFg,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = lastExpression != null && lastResult != null,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut(),
                ) {
                    if (lastExpression != null && lastResult != null) {
                        Surface(
                            color = palette.surfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, palette.borderSubtle.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onReuseLastResult)
                                .testTag("previous_calculation_pill"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Replay,
                                    contentDescription = stringResource(R.string.tap_to_reuse),
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$lastExpression = $lastResult",
                                    color = palette.textSecondary,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary expression / result readout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = formattedExpression.ifEmpty { "0" },
                    color = when {
                        error != null -> palette.errorFg
                        formattedExpression.isEmpty() -> palette.textMuted.copy(alpha = 0.65f)
                        else -> palette.textPrimary
                    },
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = if (isResultDisplayed) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = animatedFontSize.sp,
                    lineHeight = (animatedFontSize * 1.15f).sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("display_expression"),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom row: Copy button on left, Live preview or Error pill on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (formattedExpression.isNotEmpty() && error == null) {
                    IconButton(
                        onClick = {
                            val valueToCopy = if (livePreview.isNotEmpty() && !isResultDisplayed) {
                                livePreview
                            } else {
                                formattedExpression
                            }
                            clipboard.setText(AnnotatedString(valueToCopy))
                            justCopied = true
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("copy_display_button"),
                    ) {
                        Icon(
                            imageVector = if (justCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.copy_result),
                            tint = if (justCopied) palette.accentPrimary else palette.textMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    if (error != null) {
                        val errorText = when (error) {
                            CalcError.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
                            CalcError.NEGATIVE_SQRT -> stringResource(R.string.error_negative_sqrt)
                            CalcError.INVALID_EXPRESSION -> stringResource(R.string.error_invalid_expression)
                            CalcError.OVERFLOW -> stringResource(R.string.error_overflow)
                            CalcError.DOMAIN_ERROR -> stringResource(R.string.error_domain)
                        }
                        Surface(
                            color = palette.errorBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, palette.errorFg.copy(alpha = 0.3f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = errorText,
                                    tint = palette.errorFg,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorText,
                                    color = palette.errorFg,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("display_error"),
                                )
                            }
                        }
                    } else if (livePreview.isNotEmpty()) {
                        Text(
                            text = "= $livePreview",
                            color = palette.accentPrimary,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.testTag("display_preview"),
                        )
                    }
                }
            }
        }
    }
}
