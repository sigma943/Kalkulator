package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCalculatorPalette

@Composable
fun Keypad(
    modifier: Modifier = Modifier,
    onDigitClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onClear: () -> Unit,
    onClearAll: () -> Unit,
    onBackspace: () -> Unit,
    onParenthesesClick: () -> Unit,
    onEquals: () -> Unit,
    onEqualsLongClick: () -> Unit,
    vibrateEnabled: Boolean,
    soundEnabled: Boolean
) {
    val palette = LocalCalculatorPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: AC, ( ), %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "AC",
                backgroundColor = palette.clearBtn,
                contentColor = palette.clearText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                shape = CircleShape,
                testTag = "btn_ac",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onLongClick = onClearAll,
                onClick = onClear
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "( )",
                backgroundColor = palette.operatorBtn,
                contentColor = palette.operatorText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                shape = CircleShape,
                testTag = "btn_parens",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = onParenthesesClick
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "%",
                backgroundColor = palette.operatorBtn,
                contentColor = palette.operatorText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                shape = CircleShape,
                testTag = "btn_percent",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onOperatorClick("%") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "÷",
                backgroundColor = palette.operatorBtn,
                contentColor = palette.operatorText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_divide",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onOperatorClick("÷") }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "7",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_7",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("7") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "8",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_8",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("8") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "9",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_9",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("9") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "×",
                backgroundColor = palette.operatorBtn,
                contentColor = palette.operatorText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_multiply",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onOperatorClick("×") }
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "4",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_4",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("4") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "5",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_5",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("5") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "6",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_6",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("6") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "−",
                backgroundColor = palette.operatorBtn,
                contentColor = palette.operatorText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_minus",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onOperatorClick("−") }
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "1",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_1",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("1") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "2",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_2",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("2") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "3",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_3",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("3") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "+",
                backgroundColor = palette.operatorBtn,
                contentColor = palette.operatorText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_plus",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onOperatorClick("+") }
            )
        }

        // Row 5: 0, ,, ⌫, =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "0",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Normal,
                shape = CircleShape,
                testTag = "btn_0",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick("0") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = ",",
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                fontSize = 33.sp,
                fontWeight = FontWeight.Medium,
                shape = CircleShape,
                testTag = "btn_dot",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onClick = { onDigitClick(",") }
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "Usuń znak",
                        tint = palette.numberText,
                        modifier = Modifier.size(25.dp)
                    )
                },
                backgroundColor = palette.numberBtn,
                contentColor = palette.numberText,
                shape = CircleShape,
                testTag = "btn_backspace",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onLongClick = onClearAll,
                onClick = onBackspace
            )

            CalculatorButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "=",
                backgroundColor = palette.accentEquals,
                contentColor = palette.equalsText,
                fontSize = 35.sp,
                fontWeight = FontWeight.Medium,
                shape = CircleShape,
                testTag = "btn_equals",
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onLongClick = onEqualsLongClick,
                onClick = onEquals
            )
        }
    }
}
