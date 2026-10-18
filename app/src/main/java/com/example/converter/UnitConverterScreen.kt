package com.example.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.Screen
import com.example.ui.components.CalculatorButton
import com.example.ui.screens.FeedbackDialog
import com.example.ui.screens.HelpDialog
import com.example.ui.screens.PrivacyPolicyDialog
import com.example.ui.screens.ThemeSelectionDialog
import com.example.ui.theme.LocalCalculatorPalette
import kotlinx.coroutines.CancellationException

@Composable
fun UnitConverterScreen(
    onTabSelect: (Screen) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenThemes: (() -> Unit)? = null,
    onOpenSettings: () -> Unit = {},
    onBack: () -> Unit,
    vibrateEnabled: Boolean = true,
    soundEnabled: Boolean = false,
    settingsManager: SettingsManager? = null
) {
    val palette = LocalCalculatorPalette.current
    val categories = remember { UnitConverterEngine.categories }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) } // Starts with categories grid first

    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (selectedCategoryId != null) {
            selectedCategoryId = null
        } else {
            onBack()
        }
    }

    AnimatedContent(
        targetState = selectedCategoryId,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally(tween(260)) { it } + fadeIn(tween(200)))
                    .togetherWith(slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut(tween(180)))
            } else {
                (slideInHorizontally(tween(260)) { -it / 3 } + fadeIn(tween(200)))
                    .togetherWith(slideOutHorizontally(tween(220)) { it } + fadeOut(tween(180)))
            }
        },
        label = "converterScreenTransition"
    ) { catId ->
        if (catId == null) {
            // WIDOK GŁÓWNY: SIATKA KATEGORII KONWERTERA (Screenshot 1)
            ConverterHubView(
                categories = categories,
                onSelectCategory = { selectedCategoryId = it },
                onTabSelect = onTabSelect,
                onOpenThemeDialog = {
                    if (settingsManager != null) {
                        showThemeDialog = true
                    } else if (onOpenThemes != null) {
                        onOpenThemes()
                    }
                },
                onOpenPrivacyDialog = { showPrivacyDialog = true },
                onOpenFeedbackDialog = { showFeedbackDialog = true },
                onOpenHelpDialog = { showHelpDialog = true }
            )
        } else {
            val category = categories.find { it.id == catId } ?: categories.first()
            // WIDOK PRZELICZNIKA JEDNOSTKI Z KLAWIATURĄ (Screenshot 2)
            ConverterDetailView(
                category = category,
                onBack = { selectedCategoryId = null },
                vibrateEnabled = vibrateEnabled,
                soundEnabled = soundEnabled
            )
        }
    }

    if (showThemeDialog && settingsManager != null) {
        ThemeSelectionDialog(
            settingsManager = settingsManager,
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false })
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
}

// -------------------------------------------------------------
// 1. HUB KATEGORII (Screenshot 1)
// -------------------------------------------------------------
@Composable
private fun ConverterHubView(
    categories: List<UnitCategory>,
    onSelectCategory: (String) -> Unit,
    onTabSelect: (Screen) -> Unit,
    onOpenThemeDialog: () -> Unit,
    onOpenPrivacyDialog: () -> Unit,
    onOpenFeedbackDialog: () -> Unit,
    onOpenHelpDialog: () -> Unit
) {
    val palette = LocalCalculatorPalette.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Górny pasek: 3 kropki po prawej stronie
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Przycisk 3 kropek z menu opcji (prawa strona)
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .testTag("conv_top_more")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Więcej opcji",
                        tint = palette.numberText.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(palette.cardBg)
                        .padding(vertical = 4.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Wybierz motyw", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenThemeDialog()
                        },
                        modifier = Modifier.testTag("conv_menu_themes")
                    )

                    DropdownMenuItem(
                        text = { Text("Kalkulator", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Calculate,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onTabSelect(Screen.Calculator)
                        },
                        modifier = Modifier.testTag("conv_menu_calculator")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = palette.cardBorder)

                    DropdownMenuItem(
                        text = { Text("Polityka prywatności", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenPrivacyDialog()
                        },
                        modifier = Modifier.testTag("conv_menu_privacy")
                    )

                    DropdownMenuItem(
                        text = { Text("Prześlij opinię", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenFeedbackDialog()
                        },
                        modifier = Modifier.testTag("conv_menu_feedback")
                    )

                    DropdownMenuItem(
                        text = { Text("Pomoc", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenHelpDialog()
                        },
                        modifier = Modifier.testTag("conv_menu_help")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Siatka kategorii (3 kolumny)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp)
        ) {
            items(categories) { cat ->
                CategoryGridItem(
                    category = cat,
                    onClick = { onSelectCategory(cat.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryGridItem(
    category: UnitCategory,
    onClick: () -> Unit
) {
    val palette = LocalCalculatorPalette.current
    val icon = getCategoryIcon(category.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(palette.cardBg)
                .border(1.dp, palette.cardBorder.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.name,
                tint = palette.accentEquals,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = palette.numberText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// 2. WIDOK PRZELICZNIKA JEDNOSTKI Z KLAWIATURĄ (Screenshot 2)
// -------------------------------------------------------------
@Composable
private fun ConverterDetailView(
    category: UnitCategory,
    onBack: () -> Unit,
    vibrateEnabled: Boolean,
    soundEnabled: Boolean
) {
    if (category.id == "bmi") {
        BmiDetailView(onBack)
        return
    }
    val palette = LocalCalculatorPalette.current
    val context = LocalContext.current

    var fromUnitIndex by remember(category.id) { mutableIntStateOf(0) }
    var toUnitIndex by remember(category.id) { mutableIntStateOf(1.coerceAtMost(category.units.size - 1)) }
    var activeRow by remember(category.id) { mutableIntStateOf(0) } // 0 = row1 (from), 1 = row2 (to)
    var inputValue by remember(category.id) { mutableStateOf("1") }

    var showFromDialog by remember { mutableStateOf(false) }
    var showToDialog by remember { mutableStateOf(false) }

    val fromUnit = category.units.getOrElse(fromUnitIndex) { category.units.first() }
    val toUnit = category.units.getOrElse(toUnitIndex) { category.units.last() }

    val currencyRepository = remember(context) { CurrencyRateRepository(context) }
    var snapshot by remember { mutableStateOf(currencyRepository.cached()) }
    var refreshRequest by remember { mutableIntStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    var rateMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(category.id, refreshRequest) {
        if (category.id == "currency") {
            refreshing = true
            rateMessage = null
            try {
                snapshot = currencyRepository.refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                snapshot = currencyRepository.cached()
                rateMessage = if (snapshot == null) "Brak kursów NBP. Sprawdź połączenie i odśwież."
                    else "Brak połączenia — używam zapisanej tabeli."
            } finally {
                refreshing = false
            }
        }
    }
    val inputUnit = if (activeRow == 0) fromUnit else toUnit
    val outputUnit = if (activeRow == 0) toUnit else fromUnit
    val conversion = remember(category.id, inputUnit.id, outputUnit.id, inputValue, snapshot) {
        UnitConverterEngine.convertText(category.id, inputUnit.id, outputUnit.id, inputValue, snapshot?.takeIf { it.isUsable() }?.rates ?: emptyMap())
    }
    val convertedValue = (conversion as? UnitConverterEngine.Result.Success)?.display ?: "—"
    val conversionError = (conversion as? UnitConverterEngine.Result.Error)?.message

    fun copyResult() {
        val result = conversion as? UnitConverterEngine.Result.Success
        if (result == null) {
            Toast.makeText(context, conversionError ?: "Brak wyniku", Toast.LENGTH_SHORT).show()
            return
        }
        val text = "${result.display} ${outputUnit.symbol}"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Wynik", text))
        Toast.makeText(context, "Skopiowano: $text", Toast.LENGTH_SHORT).show()
    }

    fun activateRow(row: Int) {
        if (row == activeRow) return
        val result = conversion as? UnitConverterEngine.Result.Success ?: return
        // Transfer the unrounded result instead of reinterpreting the original input.
        inputValue = if (category.id == "numeral") result.raw else
            result.raw.toBigDecimal().toPlainString().let { plain ->
                (if (plain.length <= 256) plain else result.raw).replace('.', ',')
            }
        activeRow = row
    }

    // Input handlers
    fun handleDigit(d: String) {
        if (category.id == "numeral") {
            val radix = UnitConverterEngine.numeralRadix(inputUnit.id)
            if (!d.all { Character.digit(it, radix) in 0 until radix }) return
        }
        if (inputValue == "0") inputValue = d
        else if (inputValue.length + d.length <= 128) inputValue += d
    }

    fun handleDot() {
        if (category.id == "numeral" || inputValue.contains('E', ignoreCase = true)) return
        if (!inputValue.contains(".") && !inputValue.contains(",")) {
            inputValue = if (inputValue.isEmpty()) "0," else "$inputValue,"
        }
    }

    fun handleBackspace() {
        if (inputValue.isNotEmpty()) {
            inputValue = inputValue.dropLast(1)
            if (inputValue.isEmpty()) inputValue = "0"
        }
    }

    fun handleClear() {
        inputValue = "0"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Pasek górny: Powrót + Tytuł kategorii
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Powrót",
                    tint = palette.numberText
                )
            }
            Text(
                text = category.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.numberText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        val note = when (category.id) {
            "currency" -> snapshot?.let { "NBP, kurs średni z ${it.date}. Bez spreadu i prowizji." } ?: "Kursy średnie NBP"
            "data" -> "kB/MB/GB: wielokrotności 1000; KiB/MiB/GiB: wielokrotności 1024."
            "date", "time" -> "Miesiąc = średnio 30,4375 dnia; rok = 365,25 dnia. To okresy, nie daty kalendarzowe."
            "finance" -> "Wybierz kwotę bazową/netto lub kwotę z wybranym VAT/napiwkiem."
            "discount" -> "Wybierz cenę przed rabatem lub cenę po określonym rabacie."
            "speed" -> "Mach: stałe przybliżenie 340,29 m/s dla powietrza przy 15°C."
            "angle" -> "Radiany wykorzystują przybliżenie π."
            "numeral" -> "Liczby całkowite. HEX obsługuje cyfry A–F."
            else -> null
        }
        if (note != null) Text(note, color = palette.numberText, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))
        if (category.id == "currency") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(rateMessage ?: if (refreshing) "Pobieranie kursów…" else "Tabela NBP; kursy mogą pochodzić z poprzedniego dnia roboczego.",
                    modifier = Modifier.weight(1f), fontSize = 11.sp, color = palette.numberText)
                TextButton(onClick = { refreshRequest++ }, enabled = !refreshing) { Text("Odśwież") }
            }
        }
        if (conversionError != null) Text(conversionError, color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp).testTag("converter_error"))
        if (category.id == "numeral" && inputUnit.id == "hex") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                "ABCDEF".forEach { digit -> TextButton(onClick = { handleDigit(digit.toString()) }) { Text(digit.toString()) } }
            }
        }

        // Górny panel wartości i jednostek (dokładnie jak na zrzucie ekranu 2)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // RZĄD 1 (Jednostka 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { activateRow(0) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wybór jednostki z ikoną przełączania ⇳
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFromDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${fromUnit.name} ${fromUnit.symbol}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.numberText
                    )
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Zmień jednostkę",
                        tint = palette.numberText.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Wartość jednostki 1 (podświetlona gdy aktywna) - scrollable, non-wrapping
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeRow == 0) (inputValue.ifEmpty { "0" }) else convertedValue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeRow == 0) palette.accentEquals else palette.numberText,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            // RZĄD 2 (Jednostka 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { activateRow(1) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wybór jednostki 2 z ikoną przełączania ⇳
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showToDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${toUnit.name} ${toUnit.symbol}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.numberText
                    )
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = "Zmień jednostkę",
                        tint = palette.numberText.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Wartość jednostki 2 (podświetlona gdy aktywna) - scrollable, non-wrapping
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeRow == 1) (inputValue.ifEmpty { "0" }) else convertedValue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeRow == 1) palette.accentEquals else palette.numberText,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        // Dolny panel z wbudowaną klawiaturą numeryczną (Screenshot 2)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f)
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rząd 1: C, ⌫, %, ⇅
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    text = "C",
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.accentEquals,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    shape = CircleShape,
                    testTag = "conv_btn_clear",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = { handleClear() }
                )
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                            contentDescription = "Backspace",
                            tint = palette.accentEquals,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.accentEquals,
                    shape = CircleShape,
                    testTag = "conv_btn_backspace",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = { handleBackspace() }
                )
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    text = "%",
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.operatorText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    shape = CircleShape,
                    testTag = "conv_btn_percent",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = {
                        if (category.id != "numeral") {
                            runCatching { UnitConverterEngine.percentInput(inputValue) }
                                .onSuccess { inputValue = it.replace('.', ',') }
                                .onFailure { Toast.makeText(context, "Niepoprawna liczba", Toast.LENGTH_SHORT).show() }
                        }
                    }
                )
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Zamień jednostki",
                            tint = palette.accentEquals,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.accentEquals,
                    shape = CircleShape,
                    testTag = "conv_btn_swap",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = {
                        val temp = fromUnitIndex
                        fromUnitIndex = toUnitIndex
                        toUnitIndex = temp
                    }
                )
            }

            // Rząd 2: 7, 8, 9, ±
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "7", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_7", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("7") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "8", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_8", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("8") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "9", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_9", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("9") })
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    text = "±",
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.operatorText,
                    fontSize = 24.sp,
                    shape = CircleShape,
                    testTag = "conv_btn_sign",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = {
                        if (inputValue.startsWith("-")) {
                            inputValue = inputValue.drop(1)
                        } else if (inputValue != "0") {
                            inputValue = "-$inputValue"
                        }
                    }
                )
            }

            // Rząd 3: 4, 5, 6, Kopiuj
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "4", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_4", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("4") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "5", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_5", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("5") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "6", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_6", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("6") })
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Kopiuj",
                            tint = palette.operatorText,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.operatorText,
                    shape = CircleShape,
                    testTag = "conv_btn_copy",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = {
                        copyResult()
                    }
                )
            }

            // Rząd 4: 1, 2, 3, 100
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "1", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_1", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("1") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "2", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_2", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("2") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "3", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_3", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("3") })
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    text = "100",
                    backgroundColor = palette.operatorBtn,
                    contentColor = palette.operatorText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    shape = CircleShape,
                    testTag = "conv_btn_100",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = { inputValue = "100" }
                )
            }

            // Rząd 5: 00, 0, ,, =
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "00", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 24.sp, shape = CircleShape, testTag = "conv_btn_00", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { if (inputValue != "0") handleDigit("00") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = "0", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, shape = CircleShape, testTag = "conv_btn_0", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDigit("0") })
                CalculatorButton(modifier = Modifier.weight(1f).fillMaxHeight(), text = ",", backgroundColor = palette.numberBtn, contentColor = palette.numberText, fontSize = 28.sp, fontWeight = FontWeight.Bold, shape = CircleShape, testTag = "conv_btn_comma", vibrateEnabled = vibrateEnabled, soundEnabled = soundEnabled, onClick = { handleDot() })
                CalculatorButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    text = "=",
                    backgroundColor = palette.accentEquals,
                    contentColor = palette.equalsText,
                    fontSize = 30.sp,
                    shape = CircleShape,
                    testTag = "conv_btn_equals",
                    vibrateEnabled = vibrateEnabled,
                    soundEnabled = soundEnabled,
                    onClick = {
                        copyResult()
                    }
                )
            }
        }
    }

    // DIALOG WYBORU JEDNOSTKI 1
    if (showFromDialog) {
        UnitPickerDialog(
            title = "Wybierz jednostkę",
            units = category.units,
            selectedIndex = fromUnitIndex,
            onSelect = {
                fromUnitIndex = it
                showFromDialog = false
            },
            onDismiss = { showFromDialog = false }
        )
    }

    // DIALOG WYBORU JEDNOSTKI 2
    if (showToDialog) {
        UnitPickerDialog(
            title = "Wybierz jednostkę",
            units = category.units,
            selectedIndex = toUnitIndex,
            onSelect = {
                toUnitIndex = it
                showToDialog = false
            },
            onDismiss = { showToDialog = false }
        )
    }
}

@Composable
private fun UnitPickerDialog(
    title: String,
    units: List<ConversionUnit>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalCalculatorPalette.current
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, units) {
        if (searchQuery.isBlank()) {
            units.mapIndexed { idx, u -> idx to u }
        } else {
            units.mapIndexed { idx, u -> idx to u }.filter { (_, u) ->
                u.name.contains(searchQuery, ignoreCase = true) ||
                u.symbol.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Szukaj jednostki...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
            ) {
                items(filtered) { (origIdx, u) ->
                    val isSelected = (origIdx == selectedIndex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(origIdx) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(origIdx) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = u.name,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = u.symbol,
                                fontSize = 12.sp,
                                color = palette.accentEquals
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

private fun getCategoryIcon(id: String): ImageVector {
    return when (id) {
        "currency" -> Icons.Outlined.MonetizationOn
        "length" -> Icons.Outlined.Straighten
        "mass" -> Icons.Outlined.ShoppingBag
        "area" -> Icons.Outlined.CropSquare
        "time" -> Icons.Outlined.AccessTime
        "finance" -> Icons.Outlined.Savings
        "data" -> Icons.Outlined.SdCard
        "date" -> Icons.Outlined.CalendarMonth
        "discount" -> Icons.Outlined.LocalOffer
        "volume" -> Icons.Outlined.ViewInAr
        "numeral" -> Icons.Outlined.Pin
        "speed" -> Icons.Outlined.Speed
        "temperature" -> Icons.Outlined.DeviceThermostat
        "pressure" -> Icons.Outlined.Compress
        "angle" -> Icons.Outlined.RotateRight
        "bmi" -> Icons.Outlined.MonitorWeight
        else -> Icons.Outlined.Calculate
    }
}
