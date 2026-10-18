package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ui.CalculatorViewModel
import com.example.ui.HistoryViewMode
import com.example.ui.Screen
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.HogRiderEasterEggOverlay
import com.example.ui.components.Keypad
import com.example.ui.components.ScientificBar
import com.example.ui.theme.LocalCalculatorPalette
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val palette = LocalCalculatorPalette.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val expression by viewModel.expression.collectAsState()
    val resultPreview by viewModel.resultPreview.collectAsState()
    val lastEvaluation by viewModel.lastEvaluation.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val angleUnit by viewModel.angleUnit.collectAsState()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val historyViewMode by viewModel.historyViewMode.collectAsState()
    val isHistoryExpanded by viewModel.isHistoryExpanded.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val showHogRiderEasterEgg by viewModel.showHogRiderEasterEgg.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // History expansion progress: 0f (HIDDEN), 0.5f (HALF), 1.0f (FULL)
    val initialProgress = when (historyViewMode) {
        HistoryViewMode.HIDDEN -> 0f
        HistoryViewMode.HALF -> 0.5f
        HistoryViewMode.FULL -> 1f
    }
    val historyProgress = remember { Animatable(initialProgress) }

    // Synchronize historyProgress Animatable when historyViewMode state changes externally
    LaunchedEffect(historyViewMode) {
        val target = when (historyViewMode) {
            HistoryViewMode.HIDDEN -> 0f
            HistoryViewMode.HALF -> 0.5f
            HistoryViewMode.FULL -> 1f
        }
        if (abs(historyProgress.value - target) > 0.005f) {
            historyProgress.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // Intercept back button if easter egg is open
    BackHandler(enabled = showHogRiderEasterEgg) {
        viewModel.dismissHogRiderEasterEgg()
    }

    // Intercept back button if history is open
    BackHandler(enabled = !showHogRiderEasterEgg && (isHistoryExpanded || historyProgress.value > 0.01f)) {
        if (historyProgress.value > 0.65f || historyViewMode == HistoryViewMode.FULL) {
            viewModel.setHistoryViewMode(HistoryViewMode.HALF)
        } else {
            viewModel.setHistoryViewMode(HistoryViewMode.HIDDEN)
        }
    }

    // Keep screen on setting effect
    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        val isLandscape = maxWidth > maxHeight
        val density = LocalDensity.current
        val screenHeightPx = with(density) { maxHeight.toPx() }

        if (!isLandscape) {
            // PORTRAIT LAYOUT
            val progress = historyProgress.value

            // Interpolate display weight smoothly
            val minDisplayWeight = if (isScientificExpanded) 0.28f else 0.38f
            val halfDisplayWeight = if (isScientificExpanded) 0.46f else 0.50f
            val fullDisplayWeight = 1.0f

            val currentDisplayWeight = if (progress <= 0.5f) {
                val f = (progress / 0.5f).coerceIn(0f, 1f)
                minDisplayWeight + (halfDisplayWeight - minDisplayWeight) * f
            } else {
                val f = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)
                halfDisplayWeight + (fullDisplayWeight - halfDisplayWeight) * f
            }

            val currentKeypadWeight = (1.0f - currentDisplayWeight).coerceAtLeast(0.001f)
            val keypadAlpha = (1f - ((progress - 0.82f) / 0.18f)).coerceIn(0f, 1f)
            val isKeypadVisible = progress < 0.995f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                CalculatorDisplay(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(currentDisplayWeight),
                    expression = expression,
                    resultPreview = resultPreview,
                    lastEvaluation = lastEvaluation,
                    errorMessage = errorMessage,
                    angleUnit = angleUnit,
                    textScale = settings.textScale,
                    isScientificExpanded = isScientificExpanded,
                    historyViewMode = historyViewMode,
                    historyProgress = progress,
                    historyList = historyList,
                    onToggleHistory = { viewModel.toggleHistoryExpanded() },
                    onToggleHistoryFull = { viewModel.toggleHistoryFull() },
                    onDragStart = {
                        // Gesture started
                    },
                    onDragDelta = { deltaPx ->
                        // Moving downwards (deltaPx > 0) expands history towards FULL (progress 1.0)
                        // Moving upwards (deltaPx < 0) collapses history towards HIDDEN (progress 0.0)
                        val deltaProgress = (deltaPx / screenHeightPx) * 1.5f
                        coroutineScope.launch {
                            historyProgress.snapTo((historyProgress.value + deltaProgress).coerceIn(0f, 1f))
                        }
                    },
                    onDragEnd = { velocityY ->
                        coroutineScope.launch {
                            val current = historyProgress.value
                            val targetMode = when {
                                velocityY > 700f -> HistoryViewMode.FULL
                                velocityY < -700f -> if (current > 0.6f) HistoryViewMode.HALF else HistoryViewMode.HIDDEN
                                current >= 0.72f -> HistoryViewMode.FULL
                                current <= 0.28f -> HistoryViewMode.HIDDEN
                                else -> HistoryViewMode.HALF
                            }
                            viewModel.setHistoryViewMode(targetMode)
                            val targetProgress = when (targetMode) {
                                HistoryViewMode.HIDDEN -> 0f
                                HistoryViewMode.HALF -> 0.5f
                                HistoryViewMode.FULL -> 1f
                            }
                            historyProgress.animateTo(
                                targetValue = targetProgress,
                                animationSpec = spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    },
                    onSelectHistoryItem = { item ->
                        viewModel.onSelectHistory(item, useResultOnly = false)
                    },
                    onClearAllHistory = { viewModel.onClearAllHistory() },
                    onAngleUnitToggle = { viewModel.toggleAngleUnit() },
                    onMenuClickConverter = { viewModel.navigateTo(Screen.UnitConverter) },
                    onMenuClickThemes = { showThemeDialog = true },
                    onMenuClickPrivacy = { showPrivacyDialog = true },
                    onMenuClickFeedback = { showFeedbackDialog = true },
                    onMenuClickHelp = { showHelpDialog = true }
                )

                // Keypad & Scientific Bar are visible with smooth weight & opacity transitions
                if (isKeypadVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(currentKeypadWeight)
                            .alpha(keypadAlpha)
                    ) {
                        ScientificBar(
                            modifier = Modifier.fillMaxWidth(),
                            isExpanded = isScientificExpanded,
                            onToggleExpand = { viewModel.toggleScientific() },
                            angleUnit = angleUnit,
                            onToggleAngleUnit = { viewModel.toggleAngleUnit() },
                            onInsert = { viewModel.onInsert(it) },
                            vibrateEnabled = settings.vibrateOnClick,
                            soundEnabled = settings.soundOnClick
                        )

                        Keypad(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            onDigitClick = { viewModel.onDigit(it) },
                            onOperatorClick = { viewModel.onOperator(it) },
                            onClear = { viewModel.onClear() },
                            onClearAll = { viewModel.onClearAll() },
                            onBackspace = { viewModel.onBackspace() },
                            onParenthesesClick = { viewModel.onParentheses() },
                            onEquals = { viewModel.onEquals() },
                            vibrateEnabled = settings.vibrateOnClick,
                            soundEnabled = settings.soundOnClick
                        )
                    }
                }
            }
        } else {
            // LANDSCAPE LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                // Left pane: Display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    CalculatorDisplay(
                        modifier = Modifier.fillMaxSize(),
                        expression = expression,
                        resultPreview = resultPreview,
                        lastEvaluation = lastEvaluation,
                        errorMessage = errorMessage,
                        angleUnit = angleUnit,
                        textScale = settings.textScale,
                        isScientificExpanded = true,
                        historyViewMode = historyViewMode,
                        historyProgress = if (historyViewMode == HistoryViewMode.HIDDEN) 0f else 0.5f,
                        historyList = historyList,
                        onToggleHistory = { viewModel.toggleHistoryExpanded() },
                        onToggleHistoryFull = { viewModel.toggleHistoryFull() },
                        onDragStart = { },
                        onDragDelta = { },
                        onDragEnd = { },
                        onSelectHistoryItem = { item ->
                            viewModel.onSelectHistory(item, useResultOnly = false)
                        },
                        onClearAllHistory = { viewModel.onClearAllHistory() },
                        onAngleUnitToggle = { viewModel.toggleAngleUnit() },
                        onMenuClickConverter = { viewModel.navigateTo(Screen.UnitConverter) },
                        onMenuClickThemes = { showThemeDialog = true },
                        onMenuClickPrivacy = { showPrivacyDialog = true },
                        onMenuClickFeedback = { showFeedbackDialog = true },
                        onMenuClickHelp = { showHelpDialog = true }
                    )
                }

                // Right pane: Scientific + Keypad
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                ) {
                    ScientificBar(
                        modifier = Modifier.fillMaxWidth(),
                        isExpanded = true,
                        onToggleExpand = { },
                        angleUnit = angleUnit,
                        onToggleAngleUnit = { viewModel.toggleAngleUnit() },
                        onInsert = { viewModel.onInsert(it) },
                        vibrateEnabled = settings.vibrateOnClick,
                        soundEnabled = settings.soundOnClick
                    )

                    Keypad(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onDigitClick = { viewModel.onDigit(it) },
                        onOperatorClick = { viewModel.onOperator(it) },
                        onClear = { viewModel.onClear() },
                        onClearAll = { viewModel.onClearAll() },
                        onBackspace = { viewModel.onBackspace() },
                        onParenthesesClick = { viewModel.onParentheses() },
                        onEquals = { viewModel.onEquals() },
                        vibrateEnabled = settings.vibrateOnClick,
                        soundEnabled = settings.soundOnClick
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            settingsManager = viewModel.settingsManager,
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

    if (showHogRiderEasterEgg) {
        HogRiderEasterEggOverlay(
            onDismiss = { viewModel.dismissHogRiderEasterEgg() }
        )
    }
}
