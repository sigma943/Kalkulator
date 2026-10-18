package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onOpenThemes: (() -> Unit)? = null,
    onBack: () -> Unit,
    onClearHistory: () -> Unit
) {
    BackHandler { onBack() }

    val settings by settingsManager.settings.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showTextScaleDialog by remember { mutableStateOf(false) }
    var showAngleUnitDialog by remember { mutableStateOf(false) }
    var showDecimalsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // SEKCJA: WYGLĄD
            SettingsSectionHeader("Wygląd")

            if (onOpenThemes != null) {
                SettingsItemClickable(
                    icon = Icons.Outlined.Palette,
                    title = "Motyw i styl",
                    subtitle = "${settings.themeMode.title} • ${settings.accentTheme.label}",
                    testTag = "setting_themes_hub",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(settings.accentTheme.hex))
                        )
                    },
                    onClick = onOpenThemes
                )
            } else {
                val currentTheme = settings.themeMode
                SettingsItemClickable(
                    icon = Icons.Outlined.DarkMode,
                    title = "Motyw",
                    subtitle = currentTheme.title,
                    testTag = "setting_theme",
                    onClick = { showThemeDialog = true }
                )

                SettingsItemClickable(
                    icon = Icons.Outlined.ColorLens,
                    title = "Kolor wiodący",
                    subtitle = settings.accentTheme.label,
                    testTag = "setting_accent",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(settings.accentTheme.hex))
                        )
                    },
                    onClick = { showAccentDialog = true }
                )
            }

            SettingsItemClickable(
                icon = Icons.Outlined.FormatSize,
                title = "Rozmiar tekstu",
                subtitle = settings.textScale.label,
                testTag = "setting_text_scale",
                onClick = { showTextScaleDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            // SEKCJA: KALKULATOR
            SettingsSectionHeader("Kalkulator")

            val angleSubtitle = if (settings.defaultAngleUnit == "DEG") "Stopnie (DEG)" else "Radiany (RAD)"
            SettingsItemClickable(
                icon = Icons.Outlined.SquareFoot,
                title = "Domyślna jednostka kąta",
                subtitle = angleSubtitle,
                testTag = "setting_angle_unit",
                onClick = { showAngleUnitDialog = true }
            )

            SettingsItemSwitch(
                icon = Icons.Outlined.Functions,
                title = "Pokazuj funkcje naukowe",
                subtitle = "Rozwiń pasek naukowy automatycznie",
                checked = settings.showScientificByDefault,
                testTag = "setting_show_sci",
                onCheckedChange = { settingsManager.updateShowScientificByDefault(it) }
            )

            SettingsItemSwitch(
                icon = Icons.Outlined.Pin,
                title = "Formatowanie dużych liczb",
                subtitle = "Wstawiaj odstępy między tysiącami (np. 1 000 000)",
                checked = settings.formatLargeNumbers,
                testTag = "setting_format_large",
                onCheckedChange = { settingsManager.updateFormatLargeNumbers(it) }
            )

            val decimalsSubtitle = if (settings.decimalPlaces == -1) "Automatycznie" else "${settings.decimalPlaces} miejsca"
            SettingsItemClickable(
                icon = Icons.Outlined.ExposureZero,
                title = "Miejsca po przecinku",
                subtitle = decimalsSubtitle,
                testTag = "setting_decimals",
                onClick = { showDecimalsDialog = true }
            )

            SettingsItemSwitch(
                icon = Icons.Outlined.Save,
                title = "Zachowanie ostatniego działania",
                subtitle = "Przywróć ostatnie wyrażenie po ponownym uruchomieniu",
                checked = settings.keepLastExpression,
                testTag = "setting_keep_last",
                onCheckedChange = { settingsManager.updateKeepLastExpression(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            // SEKCJA: INTERAKCJA
            SettingsSectionHeader("Interakcja")

            SettingsItemSwitch(
                icon = Icons.Outlined.Vibration,
                title = "Wibracje przy naciskaniu",
                subtitle = "Wyczuwalna haptyka po kliknięciu przycisku",
                checked = settings.vibrateOnClick,
                testTag = "setting_vibrate",
                onCheckedChange = { settingsManager.updateVibrateOnClick(it) }
            )

            SettingsItemSwitch(
                icon = Icons.Outlined.VolumeUp,
                title = "Dźwięk przy naciskaniu",
                subtitle = "Subtelny dźwięk kliknięcia klawisza",
                checked = settings.soundOnClick,
                testTag = "setting_sound",
                onCheckedChange = { settingsManager.updateSoundOnClick(it) }
            )

            SettingsItemSwitch(
                icon = Icons.Outlined.ScreenLockPortrait,
                title = "Zachowaj ekran włączony",
                subtitle = "Nie wygaszaj ekranu podczas korzystania z kalkulatora",
                checked = settings.keepScreenOn,
                testTag = "setting_keep_screen_on",
                onCheckedChange = { settingsManager.updateKeepScreenOn(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            // SEKCJA: HISTORIA
            SettingsSectionHeader("Historia")

            SettingsItemSwitch(
                icon = Icons.Outlined.History,
                title = "Zapisuj historię",
                subtitle = "Automatycznie rejestruj wykonane obliczenia",
                checked = settings.saveHistory,
                testTag = "setting_save_history",
                onCheckedChange = { settingsManager.updateSaveHistory(it) }
            )

            SettingsItemClickable(
                icon = Icons.Outlined.DeleteSweep,
                title = "Wyczyść historię",
                subtitle = "Usuń wszystkie dotychczasowe obliczenia",
                testTag = "setting_clear_history_btn",
                onClick = { showClearHistoryDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            // SEKCJA: INFORMACJE
            SettingsSectionHeader("Informacje")

            SettingsItemClickable(
                icon = Icons.Outlined.Info,
                title = "O aplikacji Kalkulator",
                subtitle = "Wersja ${com.example.BuildConfig.VERSION_NAME} • Material 3 Expressive",
                testTag = "setting_about_app",
                onClick = { showAboutDialog = true }
            )

            SettingsItemClickable(
                icon = Icons.Outlined.Security,
                title = "Prywatność",
                subtitle = "100% lokalnie na Twoim urządzeniu, brak śledzenia",
                testTag = "setting_privacy",
                onClick = { showAboutDialog = true }
            )
        }
    }

    // DIALOGI WYBORU

    // Motyw Dialog z pełnymi opisami ("napis w motywach")
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Wybierz motyw aplikacji", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    settingsManager.updateThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (settings.themeMode == mode),
                                onClick = {
                                    settingsManager.updateThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Anuluj") }
            }
        )
    }

    // Kolor Akcentu Dialog (12 bogatych akcentów z opisami)
    if (showAccentDialog) {
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            title = { Text("Wybierz kolor akcentu", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AccentTheme.values().forEach { accent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    settingsManager.updateAccentTheme(accent)
                                    showAccentDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(accent.hex))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = accent.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = accent.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (settings.accentTheme == accent) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentDialog = false }) { Text("Zamknij") }
            }
        )
    }

    // Rozmiar Tekstu Dialog
    if (showTextScaleDialog) {
        AlertDialog(
            onDismissRequest = { showTextScaleDialog = false },
            title = { Text("Rozmiar tekstu") },
            text = {
                Column {
                    DisplayTextScale.values().forEach { scale ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsManager.updateTextScale(scale)
                                    showTextScaleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (settings.textScale == scale),
                                onClick = {
                                    settingsManager.updateTextScale(scale)
                                    showTextScaleDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(scale.label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTextScaleDialog = false }) { Text("Anuluj") }
            }
        )
    }

    // Jednostka Kąta Dialog
    if (showAngleUnitDialog) {
        AlertDialog(
            onDismissRequest = { showAngleUnitDialog = false },
            title = { Text("Domyślna jednostka kąta") },
            text = {
                Column {
                    listOf("DEG" to "Stopnie (DEG)", "RAD" to "Radiany (RAD)").forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsManager.updateDefaultAngleUnit(code)
                                    showAngleUnitDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (settings.defaultAngleUnit == code),
                                onClick = {
                                    settingsManager.updateDefaultAngleUnit(code)
                                    showAngleUnitDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAngleUnitDialog = false }) { Text("Anuluj") }
            }
        )
    }

    // Miejsca po Przecinku Dialog
    if (showDecimalsDialog) {
        AlertDialog(
            onDismissRequest = { showDecimalsDialog = false },
            title = { Text("Miejsca po przecinku") },
            text = {
                Column {
                    listOf(
                        -1 to "Automatycznie",
                        2 to "2 miejsca po przecinku",
                        4 to "4 miejsca po przecinku",
                        6 to "6 miejsc po przecinku",
                        8 to "8 miejsc po przecinku"
                    ).forEach { (places, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsManager.updateDecimalPlaces(places)
                                    showDecimalsDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (settings.decimalPlaces == places),
                                onClick = {
                                    settingsManager.updateDecimalPlaces(places)
                                    showDecimalsDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDecimalsDialog = false }) { Text("Anuluj") }
            }
        )
    }

    // Wyczyść Historię Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Wyczyścić historię?") },
            text = { Text("Wszystkie zapisane działania zostaną usunięte. Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Wyczyść wszystko")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Anuluj") }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItemClickable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
