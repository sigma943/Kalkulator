package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCalculatorPalette

@Composable
fun ScientificBar(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    angleUnit: String,
    onToggleAngleUnit: () -> Unit,
    onInsert: (String) -> Unit,
    vibrateEnabled: Boolean,
    soundEnabled: Boolean
) {
    val palette = LocalCalculatorPalette.current
    var isInvActive by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "chevronRot"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        // Toggle chevron row (positioned above AC button, moved left and up)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = (-6).dp, y = (-6).dp)
                .padding(start = 0.dp, top = 0.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isExpanded) palette.operatorBtn.copy(alpha = 0.5f) else Color.Transparent)
                    .clickable { onToggleExpand() }
                    .testTag("btn_toggle_scientific"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore,
                    contentDescription = if (isExpanded) "Zwiń funkcje naukowe" else "Rozwiń funkcje naukowe",
                    tint = palette.operatorText.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        // Expanded 3-row x 4-col panel with smooth spring physics
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
            ) + fadeIn(tween(durationMillis = 200)),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
            ) + fadeOut(tween(durationMillis = 160))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: √, π, ^, !
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row1 = listOf(
                        "√" to "√(",
                        "π" to "π",
                        "^" to "^",
                        "!" to "!"
                    )
                    for ((label, insertVal) in row1) {
                        CalculatorButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            text = label,
                            backgroundColor = palette.operatorBtn,
                            contentColor = palette.operatorText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            shape = CircleShape,
                            testTag = "btn_sci_$label",
                            vibrateEnabled = vibrateEnabled,
                            soundEnabled = soundEnabled,
                            onClick = { onInsert(insertVal) }
                        )
                    }
                }

                // Row 2: Rad (or Deg), sin, cos, tan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rad/Deg button
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = if (angleUnit == "DEG") "deg" else "Rad",
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_angle_unit",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = onToggleAngleUnit
                    )

                    val sinText = if (isInvActive) "sin⁻¹" else "sin"
                    val sinInsert = if (isInvActive) "asin(" else "sin("
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = sinText,
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_sin",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { onInsert(sinInsert) }
                    )

                    val cosText = if (isInvActive) "cos⁻¹" else "cos"
                    val cosInsert = if (isInvActive) "acos(" else "cos("
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = cosText,
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_cos",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { onInsert(cosInsert) }
                    )

                    val tanText = if (isInvActive) "tan⁻¹" else "tan"
                    val tanInsert = if (isInvActive) "atan(" else "tan("
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = tanText,
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_tan",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { onInsert(tanInsert) }
                    )
                }

                // Row 3: Inv, e, ln, log
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = "Inv",
                        backgroundColor = if (isInvActive) palette.accentEquals else palette.operatorBtn,
                        contentColor = if (isInvActive) palette.equalsText else palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_inv",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { isInvActive = !isInvActive }
                    )

                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = "e",
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_e",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { onInsert("e") }
                    )

                    val lnText = if (isInvActive) "eˣ" else "ln"
                    val lnInsert = if (isInvActive) "e^(" else "ln("
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = lnText,
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_ln",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { onInsert(lnInsert) }
                    )

                    val logText = if (isInvActive) "10ˣ" else "log"
                    val logInsert = if (isInvActive) "10^(" else "log("
                    CalculatorButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        text = logText,
                        backgroundColor = palette.operatorBtn,
                        contentColor = palette.operatorText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        shape = CircleShape,
                        testTag = "btn_sci_log",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = { onInsert(logInsert) }
                    )
                }
            }
        }
    }
}
