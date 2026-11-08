package com.example.converter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.ui.components.KeypadInput
import com.example.ui.theme.LocalCalculatorPalette
import java.math.RoundingMode

@Composable
internal fun BmiDetailView(onBack: () -> Unit, vibrateEnabled: Boolean, soundEnabled: Boolean) {
    val palette = LocalCalculatorPalette.current
    val context = LocalContext.current
    var weight by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var height by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var editingWeight by rememberSaveable { mutableStateOf(true) }

    val result = remember(weight.text, height.text) {
        if (weight.text.isEmpty() || height.text.isEmpty()) null else
            runCatching { UnitConverterEngine.calculateBmi(weight.text, height.text) }
    }
    val bmi = result?.getOrNull()?.let { value ->
        val rounded = value.setScale(2, RoundingMode.HALF_UP)
        UnitConverterEngine.formatDecimal(if (rounded.signum() == 0) value else rounded)
    }

    fun edit(transform: (TextFieldValue) -> TextFieldValue) {
        if (editingWeight) weight = transform(weight) else height = transform(height)
    }

    fun setInput(text: String) = edit { TextFieldValue(text, TextRange(text.length)) }

    fun insert(text: String) = edit { input ->
        val offset = input.selection.start
        val replacement = if (input.text == "0" && text.all(Char::isDigit)) {
            text.trimStart('0').ifEmpty { "0" }
        } else null
        when {
            replacement != null -> TextFieldValue(replacement, TextRange(replacement.length))
            input.text.length + text.length <= 128 -> TextFieldValue(
                input.text.replaceRange(offset, offset, text), TextRange(offset + text.length)
            )
            else -> input
        }
    }

    fun copyResult() {
        if (bmi != null) copyConverterResult(context, bmi)
        else Toast.makeText(context, "Wpisz poprawną masę i wzrost", Toast.LENGTH_SHORT).show()
    }

    ConverterLayout(
        title = "Kalkulator BMI",
        onBack = onBack,
        fields = { modifier ->
            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                ConverterPanel {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BmiField(
                            label = "Masa ciała",
                            unit = "kg",
                            input = weight,
                            selected = editingWeight,
                            onSelect = { editingWeight = true },
                            onCursorChange = { weight = weight.copy(selection = TextRange(it)) },
                            modifier = Modifier.weight(1f).testTag("bmi_weight")
                        )
                        BmiField(
                            label = "Wzrost",
                            unit = "cm",
                            input = height,
                            selected = !editingWeight,
                            onSelect = { editingWeight = false },
                            onCursorChange = { height = height.copy(selection = TextRange(it)) },
                            modifier = Modifier.weight(1f).testTag("bmi_height")
                        )
                    }
                    HorizontalDivider(color = palette.cardBorder)
                    ConverterResultRow(
                        label = "Wynik BMI",
                        value = bmi ?: "—",
                        tag = "bmi_result",
                        modifier = Modifier.background(palette.accentEquals.copy(alpha = 0.08f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                result?.exceptionOrNull()?.message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        keypad = { modifier ->
            ConverterKeypad(
                modifier = modifier,
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled,
                onDigit = ::insert,
                onDot = {
                    val input = if (editingWeight) weight else height
                    if (input.text.none { it in ".,Ee" } &&
                        (input.selection.start != 0 || !input.text.startsWith('-'))
                    ) {
                        insert(if (input.text.isEmpty() || input.text == "-") "0," else ",")
                    }
                },
                onClear = { setInput("") },
                onBackspace = {
                    edit { input ->
                        val offset = input.selection.start
                        if (offset == 0) input else TextFieldValue(
                            input.text.removeRange(offset - 1, offset), TextRange(offset - 1)
                        )
                    }
                },
                onPercent = {
                    val input = if (editingWeight) weight else height
                    runCatching { UnitConverterEngine.percentInput(input.text) }
                        .onSuccess { setInput(it.replace('.', ',')) }
                },
                onSwap = { editingWeight = !editingWeight },
                onSign = {
                    edit { input ->
                        val offset = input.selection.start
                        when {
                            input.text.startsWith('-') ->
                                TextFieldValue(input.text.drop(1), TextRange((offset - 1).coerceAtLeast(0)))
                            input.text.isNotEmpty() -> TextFieldValue("-${input.text}", TextRange(offset + 1))
                            else -> input
                        }
                    }
                },
                onCopy = ::copyResult,
                onHundred = { setInput("100") },
                onEquals = {
                    when {
                        weight.text.isEmpty() -> editingWeight = true
                        height.text.isEmpty() -> editingWeight = false
                        else -> copyResult()
                    }
                },
                swapLabel = "Przełącz masę i wzrost"
            )
        }
    )
}

@Composable
private fun BmiField(
    label: String,
    unit: String,
    input: TextFieldValue,
    selected: Boolean,
    onSelect: () -> Unit,
    onCursorChange: (Int) -> Unit,
    modifier: Modifier
) {
    val palette = LocalCalculatorPalette.current
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier.clip(shape)
            .background(if (selected) palette.operatorBtn else palette.background)
            .border(1.dp, if (selected) palette.accentEquals else palette.cardBorder, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "$label · $unit",
            color = if (selected) palette.operatorText else palette.resultPreview,
            style = MaterialTheme.typography.labelMedium
        )
        Box(Modifier.fillMaxWidth().heightIn(min = 38.dp), contentAlignment = Alignment.CenterEnd) {
            if (selected) {
                KeypadInput(
                    value = input.text,
                    cursor = input.selection.start,
                    onCursorChange = onCursorChange,
                    color = palette.numberText,
                    cursorColor = palette.cursor,
                    fontSize = converterAmountSize(input.text),
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = input.text.ifEmpty { "—" },
                    color = if (input.text.isEmpty()) palette.resultPreview else palette.numberText,
                    fontSize = converterAmountSize(input.text),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}
