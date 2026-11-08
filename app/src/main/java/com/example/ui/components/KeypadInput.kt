package com.example.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** A single-line value edited by the app's keypad. Taps position the caret without opening the IME. */
@Composable
internal fun KeypadInput(
    value: String,
    cursor: Int,
    onCursorChange: (Int) -> Unit,
    color: Color,
    cursorColor: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
    valueModifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val onMove by rememberUpdatedState(onCursorChange)
    val shownValue = value.ifEmpty { "0" }
    var layout by remember(shownValue) { mutableStateOf<TextLayoutResult?>(null) }
    val caret = layout?.getCursorRect(cursor.coerceIn(0, value.length))
    val scrollLimit = scroll.maxValue
    LaunchedEffect(caret?.left, scrollLimit) {
        if (caret == null || scrollLimit == Int.MAX_VALUE || scrollLimit == 0) return@LaunchedEffect
        val x = caret.left.roundToInt()
        val visibleWidth = scroll.viewportSize
        val target = when {
            x < scroll.value -> x
            x >= scroll.value + visibleWidth - 4 -> x - visibleWidth + 4
            else -> scroll.value
        }.coerceIn(0, scrollLimit)
        scroll.scrollTo(target)
    }

    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(scroll),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = shownValue,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { layout = it },
            modifier = valueModifier
                .padding(end = 3.dp)
                .pointerInput(shownValue) {
                    detectTapGestures { tap ->
                        layout?.let { onMove(it.getOffsetForPosition(tap)) }
                    }
                }
                .drawWithContent {
                    drawContent()
                    caret?.let {
                        val inset = it.height * 0.12f
                        drawLine(
                            cursorColor,
                            Offset(it.left, it.top + inset),
                            Offset(it.left, it.bottom - inset),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
        )
    }
}
