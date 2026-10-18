package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String, val description: String) {
    SYSTEM("Domyślny systemu", "Zgodny z ustawieniami urządzenia"),
    DARK("Ciemny", "Ciemne tło"),
    LIGHT("Jasny", "Jasne tło")
}

enum class AccentTheme(val label: String, val subtitle: String, val hex: Long) {
    TEAL("Morski (Turkusowy)", "Domyślny morski", 0xFF00897B),
    LAVENDER("Lawendowy Material You", "Niebieski i lawenda", 0xFFD0BCFF),
    BLUE("Błękitny", "Niebieski", 0xFF0288D1),
    INDIGO("Indygo", "Fioletowy", 0xFF3F51B5),
    CORAL("Koralowy", "Ciepły pomarańcz", 0xFFE64A19),
    EMERALD("Szmaragdowy", "Zieleń", 0xFF2E7D32),
    LIME("Limonkowy", "Limonka", 0xFF827717),
    VIOLET("Fioletowy", "Ametyst", 0xFF7B1FA2),
    CRIMSON("Karminowy", "Rubin", 0xFFC2185B),
    AMBER("Bursztynowy", "Złoty", 0xFFF57C00),
    PINK("Różowy", "Pastelowy", 0xFFD81B60),
    MINT("Miętowy", "Mięta", 0xFF00A896),
    MONOCHROME("Grafitowy", "Neutralny", 0xFF455A64),
    CYBER_CYAN("Cyjan", "Jasnoniebieski", 0xFF00B4D8),
    SUNSET_GOLD("Złoty", "Bursztyn", 0xFFFF9100),
    DEEP_PURPLE("Śliwkowy", "Głęboki fiolet", 0xFF651FFF),
    NEON_GREEN("Zielony", "Jasna zieleń", 0xFF00C853),
    RUBY_RED("Czerwony", "Szkarłat", 0xFFD50000),
    ROSE_GOLD("Różowe złoto", "Łososiowy", 0xFFFF4081),
    ELECTRIC_BLUE("Kobaltowy", "Ciemnoniebieski", 0xFF2979FF),
    PEACH_BLOSSOM("Brzoskwiniowy", "Pastel", 0xFFFF6E40)
}

enum class DisplayTextScale(val label: String, val scaleFactor: Float) {
    SMALL("Mały", 0.85f),
    NORMAL("Normalny", 1.0f),
    LARGE("Duży", 1.2f)
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentTheme: AccentTheme = AccentTheme.TEAL,
    val textScale: DisplayTextScale = DisplayTextScale.NORMAL,
    val defaultAngleUnit: String = "DEG", // DEG or RAD
    val showScientificByDefault: Boolean = false,
    val formatLargeNumbers: Boolean = true,
    val decimalPlaces: Int = -1, // -1 means Auto
    val keepLastExpression: Boolean = true,
    val vibrateOnClick: Boolean = true,
    val soundOnClick: Boolean = false,
    val keepScreenOn: Boolean = false,
    val saveHistory: Boolean = true,
    val lastExpression: String = ""
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calculator_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        val accentStr = prefs.getString("accent_theme", AccentTheme.TEAL.name) ?: AccentTheme.TEAL.name
        val scaleStr = prefs.getString("text_scale", DisplayTextScale.NORMAL.name) ?: DisplayTextScale.NORMAL.name

        val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.DARK)
        val accentTheme = runCatching { AccentTheme.valueOf(accentStr) }.getOrDefault(AccentTheme.TEAL)
        val textScale = runCatching { DisplayTextScale.valueOf(scaleStr) }.getOrDefault(DisplayTextScale.NORMAL)

        return AppSettings(
            themeMode = themeMode,
            accentTheme = accentTheme,
            textScale = textScale,
            defaultAngleUnit = prefs.getString("default_angle_unit", "DEG") ?: "DEG",
            showScientificByDefault = prefs.getBoolean("show_scientific_default", false),
            formatLargeNumbers = prefs.getBoolean("format_large_numbers", true),
            decimalPlaces = prefs.getInt("decimal_places", -1),
            keepLastExpression = prefs.getBoolean("keep_last_expression", true),
            vibrateOnClick = prefs.getBoolean("vibrate_on_click", true),
            soundOnClick = prefs.getBoolean("sound_on_click", false),
            keepScreenOn = prefs.getBoolean("keep_screen_on", false),
            saveHistory = prefs.getBoolean("save_history", true),
            lastExpression = prefs.getString("last_expression", "") ?: ""
        )
    }

    fun updateThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updateAccentTheme(accent: AccentTheme) {
        prefs.edit().putString("accent_theme", accent.name).apply()
        _settings.value = _settings.value.copy(accentTheme = accent)
    }

    fun updateTextScale(scale: DisplayTextScale) {
        prefs.edit().putString("text_scale", scale.name).apply()
        _settings.value = _settings.value.copy(textScale = scale)
    }

    fun updateDefaultAngleUnit(unit: String) {
        prefs.edit().putString("default_angle_unit", unit).apply()
        _settings.value = _settings.value.copy(defaultAngleUnit = unit)
    }

    fun updateShowScientificByDefault(show: Boolean) {
        prefs.edit().putBoolean("show_scientific_default", show).apply()
        _settings.value = _settings.value.copy(showScientificByDefault = show)
    }

    fun updateFormatLargeNumbers(format: Boolean) {
        prefs.edit().putBoolean("format_large_numbers", format).apply()
        _settings.value = _settings.value.copy(formatLargeNumbers = format)
    }

    fun updateDecimalPlaces(places: Int) {
        prefs.edit().putInt("decimal_places", places).apply()
        _settings.value = _settings.value.copy(decimalPlaces = places)
    }

    fun updateKeepLastExpression(keep: Boolean) {
        prefs.edit().putBoolean("keep_last_expression", keep).apply()
        _settings.value = _settings.value.copy(keepLastExpression = keep)
    }

    fun updateVibrateOnClick(vibrate: Boolean) {
        prefs.edit().putBoolean("vibrate_on_click", vibrate).apply()
        _settings.value = _settings.value.copy(vibrateOnClick = vibrate)
    }

    fun updateSoundOnClick(sound: Boolean) {
        prefs.edit().putBoolean("sound_on_click", sound).apply()
        _settings.value = _settings.value.copy(soundOnClick = sound)
    }

    fun updateKeepScreenOn(keep: Boolean) {
        prefs.edit().putBoolean("keep_screen_on", keep).apply()
        _settings.value = _settings.value.copy(keepScreenOn = keep)
    }

    fun updateSaveHistory(save: Boolean) {
        prefs.edit().putBoolean("save_history", save).apply()
        _settings.value = _settings.value.copy(saveHistory = save)
    }

    fun saveLastExpression(expr: String) {
        if (_settings.value.keepLastExpression) {
            prefs.edit().putString("last_expression", expr).apply()
            _settings.value = _settings.value.copy(lastExpression = expr)
        }
    }
}
