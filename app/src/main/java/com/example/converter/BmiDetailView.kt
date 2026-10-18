package com.example.converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
internal fun BmiDetailView(onBack: () -> Unit) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val result = remember(weight, height) {
        runCatching { UnitConverterEngine.calculateBmi(weight, height) }
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onBack) { Text("Wróć") }
            Text("Kalkulator BMI", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(value = weight, onValueChange = { if (it.length <= 128) weight = it },
                label = { Text("Masa (kg)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("bmi_weight"))
            OutlinedTextField(value = height, onValueChange = { if (it.length <= 128) height = it },
                label = { Text("Wzrost (cm)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("bmi_height"))
            result.getOrNull()?.let {
                Text("BMI: ${UnitConverterEngine.formatDecimal(it)}", style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.testTag("bmi_result"))
            }
            if (weight.isNotBlank() && height.isNotBlank() && result.isFailure) {
                Text(result.exceptionOrNull()?.message ?: "Sprawdź dane", color = MaterialTheme.colorScheme.error)
            }
            Text("BMI = masa w kg ÷ (wzrost w metrach)².")
        }
    }
}
