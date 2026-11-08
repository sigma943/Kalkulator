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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.Screen
import com.example.ui.components.CalculatorButton
import com.example.ui.components.KeypadInput
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
    var cursor by remember(category.id) { mutableIntStateOf(1) }

    var pickerRow by remember { mutableStateOf<Int?>(null) }

    val fromUnit = category.units.getOrElse(fromUnitIndex) { category.units.first() }
    val toUnit = category.units.getOrElse(toUnitIndex) { category.units.last() }

    val isCurrency = category.id == "currency"
    val currencyRepository = remember(context, isCurrency) {
        if (isCurrency) CurrencyRateRepository(context) else null
    }
    var snapshot by remember(currencyRepository) { mutableStateOf(currencyRepository?.cached()) }
    var refreshRequest by remember { mutableIntStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    var rateMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(category.id, refreshRequest) {
        if (currencyRepository != null) {
            refreshing = true
            rateMessage = null
            try {
                snapshot = currencyRepository.refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                snapshot = currencyRepository.cached()
                rateMessage = if (snapshot == null) "Brak kursów NBP"
                    else "Kursy zapisane offline"
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

    fun setInput(value: String, position: Int = value.length) {
        inputValue = value
        cursor = position
    }

    fun activateRow(row: Int) {
        if (row == activeRow) return
        val result = conversion as? UnitConverterEngine.Result.Success ?: return
        val transferred = if (category.id == "numeral") result.raw else
            result.raw.toBigDecimal().toPlainString().let { plain ->
                (if (plain.length <= 256) plain else result.raw).replace('.', ',')
            }
        setInput(transferred)
        activeRow = row
    }

    fun insert(text: String) {
        if (inputValue.length + text.length <= 128) {
            setInput(inputValue.take(cursor) + text + inputValue.drop(cursor), cursor + text.length)
        }
    }

    fun handleDigit(digit: String) {
        if (category.id == "numeral") {
            val radix = UnitConverterEngine.numeralRadix(inputUnit.id)
            if (!digit.all { Character.digit(it, radix) in 0 until radix }) return
        }
        if (inputValue == "0") {
            if (digit != "00") setInput(digit)
        } else {
            insert(digit)
        }
    }

    fun handleDot() {
        if (category.id == "numeral" || inputValue.any { it in ".,Ee" }) return
        if (cursor == 0 && inputValue.startsWith('-')) return
        insert(if (inputValue.isEmpty() || inputValue == "-") "0," else ",")
    }

    fun handleBackspace() {
        if (cursor == 0) return
        val updated = inputValue.removeRange(cursor - 1, cursor)
        if (updated.isEmpty()) setInput("0") else setInput(updated, cursor - 1)
    }

    val fields: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier.verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isCurrency) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val source = snapshot?.let { "NBP · ${it.date}" } ?: "NBP"
                    Text(
                        text = when {
                            refreshing -> "$source · Pobieranie kursów"
                            rateMessage != null -> if (snapshot == null) rateMessage.orEmpty() else "$source · Offline"
                            else -> source
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.resultPreview,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { refreshRequest++ }, enabled = !refreshing) {
                        Text("Odśwież")
                    }
                }
            }
            ConverterValueCard(
                unit = fromUnit,
                value = if (activeRow == 0) inputValue else convertedValue,
                active = activeRow == 0,
                cursor = cursor,
                onCursorChange = { cursor = it },
                onActivate = { activateRow(0) },
                onPick = { pickerRow = 0 }
            )
            ConverterValueCard(
                unit = toUnit,
                value = if (activeRow == 1) inputValue else convertedValue,
                active = activeRow == 1,
                cursor = cursor,
                onCursorChange = { cursor = it },
                onActivate = { activateRow(1) },
                onPick = { pickerRow = 1 }
            )
            if (conversionError != null && !(isCurrency && snapshot == null)) {
                Text(
                    text = conversionError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("converter_error")
                )
            }
        }
    }
    val keypad: @Composable (Modifier) -> Unit = { modifier ->
        ConverterKeypad(
            modifier = modifier,
            hexInput = category.id == "numeral" && inputUnit.id == "hex",
            vibrateEnabled = vibrateEnabled,
            soundEnabled = soundEnabled,
            onDigit = ::handleDigit,
            onDot = ::handleDot,
            onClear = { setInput("0") },
            onBackspace = ::handleBackspace,
            onPercent = {
                if (category.id != "numeral") {
                    runCatching { UnitConverterEngine.percentInput(inputValue) }
                        .onSuccess { setInput(it.replace('.', ',')) }
                        .onFailure { Toast.makeText(context, "Niepoprawna liczba", Toast.LENGTH_SHORT).show() }
                }
            },
            onSwap = {
                val previousFrom = fromUnitIndex
                fromUnitIndex = toUnitIndex
                toUnitIndex = previousFrom
            },
            onSign = {
                if (inputValue.startsWith("-")) {
                    setInput(inputValue.drop(1).ifEmpty { "0" }, (cursor - 1).coerceAtLeast(0))
                } else if (inputValue != "0") {
                    setInput("-$inputValue", cursor + 1)
                }
            },
            onCopy = ::copyResult,
            onHundred = { setInput("100") }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(palette.background)
            .statusBarsPadding().navigationBarsPadding()
    ) {
        ConverterHeader(category.name, onBack)
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            if (maxWidth > maxHeight && maxWidth >= 540.dp) {
                Row(Modifier.fillMaxSize()) {
                    fields(Modifier.weight(1f).fillMaxHeight())
                    keypad(Modifier.weight(1.2f).fillMaxHeight())
                }
            } else {
                val keypadHeight = if (maxHeight < 440.dp) 0.55f else 0.60f
                Column(Modifier.fillMaxSize()) {
                    fields(Modifier.fillMaxWidth().weight(1f - keypadHeight))
                    keypad(Modifier.fillMaxWidth().weight(keypadHeight))
                }
            }
        }
    }

    pickerRow?.let { row ->
        UnitPickerDialog(
            title = if (isCurrency) "Wybierz walutę" else "Wybierz jednostkę",
            units = category.units,
            selectedIndex = if (row == 0) fromUnitIndex else toUnitIndex,
            onSelect = {
                if (row == 0) fromUnitIndex = it else toUnitIndex = it
                pickerRow = null
            },
            onDismiss = { pickerRow = null }
        )
    }
}

@Composable
internal fun ConverterHeader(title: String, onBack: () -> Unit) {
    val palette = LocalCalculatorPalette.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Powrót", tint = palette.numberText)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = palette.numberText,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ConverterValueCard(
    unit: ConversionUnit,
    value: String,
    active: Boolean,
    cursor: Int,
    onCursorChange: (Int) -> Unit,
    onActivate: () -> Unit,
    onPick: () -> Unit
) {
    val palette = LocalCalculatorPalette.current
    val shape = RoundedCornerShape(18.dp)
    val fontSize = when {
        value.length > 18 -> 20.sp
        value.length > 12 -> 25.sp
        else -> 31.sp
    }
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).background(palette.cardBg)
            .border(1.dp, if (active) palette.accentEquals.copy(alpha = 0.65f) else palette.cardBorder, shape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onPick)
                .heightIn(min = 44.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitSymbol(unit.symbol)
            Text(
                text = unit.name,
                color = palette.numberText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ExpandMore, "Zmień jednostkę", tint = palette.resultPreview)
        }
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp)
                .padding(horizontal = 4.dp)
                .then(if (active) Modifier else Modifier.clickable(onClick = onActivate)),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (active) {
                KeypadInput(
                    value = value,
                    cursor = cursor,
                    onCursorChange = onCursorChange,
                    color = palette.accentEquals,
                    cursorColor = palette.cursor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    valueModifier = Modifier.testTag("converter_input")
                )
            } else {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.End) {
                    Text(
                        text = value,
                        color = palette.numberText,
                        fontSize = fontSize,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("converter_result")
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitSymbol(symbol: String) {
    val palette = LocalCalculatorPalette.current
    Box(
        modifier = Modifier.widthIn(min = 42.dp, max = 88.dp)
            .clip(RoundedCornerShape(8.dp)).background(palette.operatorBtn)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = palette.operatorText,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConverterKeypad(
    modifier: Modifier,
    hexInput: Boolean,
    vibrateEnabled: Boolean,
    soundEnabled: Boolean,
    onDigit: (String) -> Unit,
    onDot: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onPercent: () -> Unit,
    onSwap: () -> Unit,
    onSign: () -> Unit,
    onCopy: () -> Unit,
    onHundred: () -> Unit
) {
    val palette = LocalCalculatorPalette.current
    val rows = listOf(
        listOf("C", "backspace", "%", "swap"),
        listOf("7", "8", "9", "±"),
        listOf("4", "5", "6", "copy"),
        listOf("1", "2", "3", "100"),
        listOf("00", "0", ",", "=")
    )
    Column(
        modifier = modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (hexInput) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                "ABCDEF".forEach { digit ->
                    TextButton(
                        onClick = { onDigit(digit.toString()) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text(digit.toString()) }
                }
            }
        }
        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { index, key ->
                    val numeric = rowIndex > 0 && index < 3
                    val color = when {
                        numeric -> palette.numberText
                        key == "=" -> palette.equalsText
                        key == "C" || key == "backspace" || key == "swap" -> palette.accentEquals
                        else -> palette.operatorText
                    }
                    val background = when {
                        numeric -> palette.numberBtn
                        key == "=" -> palette.accentEquals
                        else -> palette.operatorBtn
                    }
                    val image = when (key) {
                        "backspace" -> Icons.AutoMirrored.Outlined.Backspace
                        "swap" -> Icons.Default.SwapVert
                        "copy" -> Icons.Outlined.ContentCopy
                        else -> null
                    }
                    val label = when (key) {
                        "backspace" -> "Usuń znak"
                        "swap" -> "Zamień jednostki"
                        "copy" -> "Kopiuj wynik"
                        else -> key
                    }
                    val action = when (key) {
                        "C" -> onClear
                        "backspace" -> onBackspace
                        "%" -> onPercent
                        "swap" -> onSwap
                        "±" -> onSign
                        "copy", "=" -> onCopy
                        "100" -> onHundred
                        "," -> onDot
                        else -> { { onDigit(key) } }
                    }
                    val tag = when (key) {
                        "C" -> "clear"
                        "%" -> "percent"
                        "±" -> "sign"
                        "," -> "comma"
                        "=" -> "equals"
                        else -> key
                    }
                    val keyIcon: (@Composable () -> Unit)? = if (image == null) null else {
                        { Icon(image, label, tint = color, modifier = Modifier.size(22.dp)) }
                    }
                    CalculatorButton(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        text = key,
                        icon = keyIcon,
                        backgroundColor = background,
                        contentColor = color,
                        fontSize = when {
                            key == "100" -> 18.sp
                            numeric || key == "=" -> 26.sp
                            else -> 22.sp
                        },
                        shape = CircleShape,
                        testTag = "conv_btn_$tag",
                        vibrateEnabled = vibrateEnabled,
                        soundEnabled = soundEnabled,
                        onClick = action
                    )
                }
            }
        }
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
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardBg,
        titleContentColor = palette.numberText,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 330.dp).selectableGroup(),
                state = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0)),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(units, key = { _, unit -> unit.id }) { index, unit ->
                    val selected = index == selectedIndex
                    val shape = RoundedCornerShape(12.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(shape)
                            .background(if (selected) palette.operatorBtn else Color.Transparent)
                            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(index) })
                            .heightIn(min = 52.dp).padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitSymbol(unit.symbol)
                        Text(
                            text = unit.name,
                            color = palette.numberText,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(Icons.Default.Check, null, tint = palette.accentEquals, modifier = Modifier.size(19.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
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
