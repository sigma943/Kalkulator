package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.AccentTheme
import com.example.data.ThemeMode

@Immutable
data class CalculatorPalette(
    val background: Color,
    val displayBg: Color,
    val keypadBg: Color,
    val numberBtn: Color,
    val numberText: Color,
    val operatorBtn: Color,
    val operatorText: Color,
    val clearBtn: Color,
    val clearText: Color,
    val accentEquals: Color,
    val equalsText: Color,
    val scientificIcon: Color,
    val cursor: Color,
    val resultPreview: Color,
    val historyBg: Color,
    val cardBg: Color,
    val headerText: Color,
    val accentGlow: Color = accentEquals.copy(alpha = 0.28f),
    val cardBorder: Color = accentEquals.copy(alpha = 0.22f),
    val tabIndicatorBg: Color = accentEquals,
    val tabSelectedText: Color = equalsText,
    val tabUnselectedText: Color = numberText.copy(alpha = 0.65f)
)

val LocalCalculatorPalette = staticCompositionLocalOf {
    getCalculatorPalette(isDark = true, accent = AccentTheme.TEAL)
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f
    )
}

fun getCalculatorPalette(isDark: Boolean, accent: AccentTheme): CalculatorPalette {
    return if (isDark) {
        when (accent) {
            AccentTheme.LAVENDER -> {
                // Dokładne kolory ze zdjęcia Google Calculator
                CalculatorPalette(
                    background = Color(0xFF101316),
                    displayBg = Color(0xFF101316),
                    keypadBg = Color(0xFF101316),
                    numberBtn = Color(0xFF2B3137),       // Szarawe przyciski cyfr ze zdjęcia
                    numberText = Color(0xFFFFFFFF),      // Wyraźny biały tekst
                    operatorBtn = Color(0xFF374857),     // Nieco ciemniejszy, szaro-niebieskawy odcień operacji
                    operatorText = Color(0xFFD6E4F0),    // Jasny chłodny błękit
                    clearBtn = Color(0xFF185987),        // Niebieski przycisk AC ze zdjęcia
                    clearText = Color(0xFFD7E8FF),       // Jasnoniebieski tekst AC
                    accentEquals = Color(0xFFE3C6E8),    // Pastelowy fiolet/lawenda znaku =
                    equalsText = Color(0xFF391F44),      // Ciemny fioletowy tekst znaku =
                    scientificIcon = Color(0xFFD6E4F0),
                    cursor = Color(0xFF7FA8ED),
                    resultPreview = Color(0xFF90A4AE),
                    historyBg = Color(0xFF1A1F24),
                    cardBg = Color(0xFF222830),
                    headerText = Color(0xFFC4C7C5),
                    accentGlow = Color(0x33E3C6E8),
                    cardBorder = Color(0xFF2E3540),
                    tabIndicatorBg = Color(0xFFE3C6E8),
                    tabSelectedText = Color(0xFF391F44),
                    tabUnselectedText = Color(0xFFE2E2E6).copy(alpha = 0.65f)
                )
            }
            AccentTheme.TEAL -> {
                // Motyw Morski / Turkusowy z dopasowanymi szarymi cyframi
                CalculatorPalette(
                    background = Color(0xFF101314),
                    displayBg = Color(0xFF101314),
                    keypadBg = Color(0xFF101314),
                    numberBtn = Color(0xFF2A3132),       // Szary z subtelną morską nutą
                    numberText = Color(0xFFFFFFFF),
                    operatorBtn = Color(0xFF344648),     // Ciemniejszy szaro-morski dla działań
                    operatorText = Color(0xFFD2F4F2),
                    clearBtn = Color(0xFF0E635F),        // Morski przycisk AC
                    clearText = Color(0xFFD0F8F6),
                    accentEquals = Color(0xFF4CD7D0),    // Turkusowy znak =
                    equalsText = Color(0xFF003735),
                    scientificIcon = Color(0xFFD2F4F2),
                    cursor = Color(0xFF4CD7D0),
                    resultPreview = Color(0xFF8B9392),
                    historyBg = Color(0xFF171B1B),
                    cardBg = Color(0xFF1F2525),
                    headerText = Color(0xFFBFC9C7),
                    accentGlow = Color(0x334CD7D0),
                    cardBorder = Color(0xFF2E3A39),
                    tabIndicatorBg = Color(0xFF4CD7D0),
                    tabSelectedText = Color(0xFF003735),
                    tabUnselectedText = Color(0xFFE0E3E2).copy(alpha = 0.65f)
                )
            }
            else -> {
                // Pozostałe akcenty
                val primaryColor = Color(accent.hex)
                val tintedOpBtn = lerpColor(Color(0xFF222830), primaryColor, 0.15f)
                val tintedOpText = lerpColor(Color(0xFFE2E2E6), primaryColor, 0.45f)
                CalculatorPalette(
                    background = Color(0xFF101316),
                    displayBg = Color(0xFF101316),
                    keypadBg = Color(0xFF101316),
                    numberBtn = Color(0xFF2B3137),       // Szarawe cyfry
                    numberText = Color(0xFFFFFFFF),
                    operatorBtn = tintedOpBtn,           // Dynamicznie zabarwiony przycisk działania!
                    operatorText = tintedOpText,          // Dynamicznie zabarwiony tekst działania!
                    clearBtn = primaryColor.copy(alpha = 0.38f),
                    clearText = Color.White,
                    accentEquals = primaryColor,
                    equalsText = Color.White,
                    scientificIcon = tintedOpText,
                    cursor = primaryColor,
                    resultPreview = Color(0xFF90A4AE),
                    historyBg = Color(0xFF1A1F24),
                    cardBg = Color(0xFF222830),
                    headerText = Color(0xFFC4C7C5),
                    accentGlow = primaryColor.copy(alpha = 0.28f),
                    cardBorder = Color(0xFF2E3540),
                    tabIndicatorBg = primaryColor,
                    tabSelectedText = Color.White,
                    tabUnselectedText = Color(0xFFE2E2E6).copy(alpha = 0.65f)
                )
            }
        }
    } else {
        // Light mode
        when (accent) {
            AccentTheme.LAVENDER -> {
                CalculatorPalette(
                    background = Color(0xFFFDFBFF),
                    displayBg = Color(0xFFFDFBFF),
                    keypadBg = Color(0xFFFDFBFF),
                    numberBtn = Color(0xFFEFF1F8),
                    numberText = Color(0xFF191C20),
                    operatorBtn = Color(0xFFD3E4FD),
                    operatorText = Color(0xFF041E49),
                    clearBtn = Color(0xFFC2E7FF),
                    clearText = Color(0xFF001D35),
                    accentEquals = Color(0xFF6750A4),
                    equalsText = Color.White,
                    scientificIcon = Color(0xFF041E49),
                    cursor = Color(0xFF0B57D0),
                    resultPreview = Color(0xFF5A5F6B),
                    historyBg = Color(0xFFEFF1F8),
                    cardBg = Color(0xFFE6E9F0),
                    headerText = Color(0xFF44474E),
                    accentGlow = Color(0x226750A4),
                    cardBorder = Color(0xFFD8DCE4),
                    tabIndicatorBg = Color(0xFF6750A4),
                    tabSelectedText = Color.White,
                    tabUnselectedText = Color(0xFF191C20).copy(alpha = 0.65f)
                )
            }
            AccentTheme.TEAL -> {
                CalculatorPalette(
                    background = Color(0xFFFAFDFB),
                    displayBg = Color(0xFFFAFDFB),
                    keypadBg = Color(0xFFFAFDFB),
                    numberBtn = Color(0xFFEDF2F0),
                    numberText = Color(0xFF161D1C),
                    operatorBtn = Color(0xFFCCE8E5),
                    operatorText = Color(0xFF004F4D),
                    clearBtn = Color(0xFFB2DFDB),
                    clearText = Color(0xFF003735),
                    accentEquals = Color(0xFF00897B),
                    equalsText = Color.White,
                    scientificIcon = Color(0xFF004F4D),
                    cursor = Color(0xFF00897B),
                    resultPreview = Color(0xFF5A6361),
                    historyBg = Color(0xFFEDF2F0),
                    cardBg = Color(0xFFE4ECE9),
                    headerText = Color(0xFF3F4948),
                    accentGlow = Color(0x3300897B),
                    cardBorder = Color(0xFFCCD6D3),
                    tabIndicatorBg = Color(0xFF00897B),
                    tabSelectedText = Color.White,
                    tabUnselectedText = Color(0xFF161D1C).copy(alpha = 0.65f)
                )
            }
            else -> {
                val primaryColor = Color(accent.hex)
                CalculatorPalette(
                    background = Color(0xFFFDFBFF),
                    displayBg = Color(0xFFFDFBFF),
                    keypadBg = Color(0xFFFDFBFF),
                    numberBtn = Color(0xFFEFF1F8),
                    numberText = Color(0xFF191C20),
                    operatorBtn = primaryColor.copy(alpha = 0.15f),
                    operatorText = primaryColor,
                    clearBtn = primaryColor.copy(alpha = 0.22f),
                    clearText = primaryColor,
                    accentEquals = primaryColor,
                    equalsText = Color.White,
                    scientificIcon = primaryColor,
                    cursor = primaryColor,
                    resultPreview = Color(0xFF5A5F6B),
                    historyBg = Color(0xFFEFF1F8),
                    cardBg = Color(0xFFE6E9F0),
                    headerText = Color(0xFF44474E),
                    accentGlow = primaryColor.copy(alpha = 0.22f),
                    cardBorder = Color(0xFFD8DCE4),
                    tabIndicatorBg = primaryColor,
                    tabSelectedText = Color.White,
                    tabUnselectedText = Color(0xFF191C20).copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.TEAL,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDark
    }

    val palette = getCalculatorPalette(isDark, accentTheme)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = palette.accentEquals,
            secondary = palette.operatorBtn,
            background = palette.background,
            surface = palette.displayBg,
            onBackground = palette.numberText,
            onSurface = palette.numberText
        )
    } else {
        lightColorScheme(
            primary = palette.accentEquals,
            secondary = palette.operatorBtn,
            background = palette.background,
            surface = palette.displayBg,
            onBackground = palette.numberText,
            onSurface = palette.numberText
        )
    }

    CompositionLocalProvider(LocalCalculatorPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
