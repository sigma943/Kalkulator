package com.example.ui.components

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCalculatorPalette

@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: (@Composable () -> Unit)? = null,
    backgroundColor: Color,
    contentColor: Color,
    fontSize: TextUnit = 28.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    shape: Shape = CircleShape,
    minHeight: Dp = 48.dp,
    testTag: String,
    vibrateEnabled: Boolean = true,
    soundEnabled: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val palette = LocalCalculatorPalette.current
    var isPressed by remember { mutableStateOf(false) }

    // Energetic spring bounce animation on click
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 1100f),
        label = "btnScale"
    )

    // Animated glow border on press
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) palette.accentEquals.copy(alpha = 0.8f) else Color.Transparent,
        animationSpec = tween(durationMillis = 100),
        label = "btnBorderColor"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = minHeight)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(width = 1.5.dp, color = borderColor, shape = shape)
            .testTag(testTag)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (vibrateEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        if (soundEnabled) {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = {
                        if (onLongClick != null) {
                            if (vibrateEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                            onLongClick()
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (icon != null) {
                icon()
            } else if (text != null) {
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = contentColor
                )
            }
        }
    }
}
