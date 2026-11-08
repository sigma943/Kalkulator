package com.example.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalCalculatorPalette
import java.math.RoundingMode

@Composable
internal fun BmiDetailView(onBack: () -> Unit) {
    val palette = LocalCalculatorPalette.current
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    val result = remember(weight, height) {
        if (weight.isBlank() || height.isBlank()) null else
            runCatching { UnitConverterEngine.calculateBmi(weight, height) }
    }
    val bmi = result?.getOrNull()?.let { value ->
        val rounded = value.setScale(2, RoundingMode.HALF_UP)
        UnitConverterEngine.formatDecimal(if (rounded.signum() == 0) value else rounded)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(palette.background)
            .statusBarsPadding().navigationBarsPadding().imePadding()
    ) {
        ConverterHeader("Kalkulator BMI", onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardBg,
                contentColor = palette.numberText
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BmiField("Masa ciała", "kg", weight, ImeAction.Next, "bmi_weight") { weight = it }
                    BmiField("Wzrost", "cm", height, ImeAction.Done, "bmi_height") { height = it }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = palette.operatorBtn,
                contentColor = palette.numberText
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Wynik BMI", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = bmi ?: "—",
                        color = if (bmi == null) palette.resultPreview else palette.accentEquals,
                        style = if ((bmi?.length ?: 0) > 14) MaterialTheme.typography.headlineMedium
                            else MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("bmi_result")
                    )
                    result?.exceptionOrNull()?.message?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun BmiField(
    label: String,
    unit: String,
    value: String,
    imeAction: ImeAction,
    tag: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 128) onValueChange(it) },
        label = { Text(label) },
        suffix = { Text(unit) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    )
}
