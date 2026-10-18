package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DisplayTextScale
import com.example.data.HistoryEntity
import com.example.ui.EvaluationEvent
import com.example.ui.HistoryViewMode
import com.example.ui.theme.LocalCalculatorPalette
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * f,
        green = start.green + (stop.green - start.green) * f,
        blue = start.blue + (stop.blue - start.blue) * f,
        alpha = start.alpha + (stop.alpha - start.alpha) * f
    )
}

@Composable
fun CalculatorDisplay(
    modifier: Modifier = Modifier,
    expression: String,
    resultPreview: String?,
    lastEvaluation: EvaluationEvent? = null,
    errorMessage: String?,
    angleUnit: String,
    textScale: DisplayTextScale,
    isScientificExpanded: Boolean,
    historyViewMode: HistoryViewMode,
    historyProgress: Float,
    historyList: List<HistoryEntity>,
    onToggleHistory: () -> Unit,
    onToggleHistoryFull: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: (Float) -> Unit,
    onDragStart: () -> Unit,
    onSelectHistoryItem: (HistoryEntity) -> Unit,
    onClearAllHistory: () -> Unit,
    onAngleUnitToggle: () -> Unit,
    onMenuClickConverter: () -> Unit,
    onMenuClickThemes: () -> Unit,
    onMenuClickPrivacy: () -> Unit,
    onMenuClickFeedback: () -> Unit,
    onMenuClickHelp: () -> Unit
) {
    val palette = LocalCalculatorPalette.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showClearConfirmationDialog by remember { mutableStateOf(false) }
    val isHistoryOpen = historyProgress > 0.01f

    // Smooth Result Elevation Animation on Equals ('=')
    var animatingEvaluation by remember { mutableStateOf<EvaluationEvent?>(null) }
    val evalAnim = remember { Animatable(1f) }

    LaunchedEffect(lastEvaluation) {
        if (lastEvaluation != null) {
            animatingEvaluation = lastEvaluation
            evalAnim.snapTo(0f)
            evalAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
            animatingEvaluation = null
        } else {
            animatingEvaluation = null
            evalAnim.snapTo(1f)
        }
    }

    // Subtle Error Shake Animation
    val errorShake = remember { Animatable(0f) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            errorShake.snapTo(0f)
            errorShake.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = LinearEasing)
            )
        }
    }
    val shakeOffsetX = if (errorMessage != null && errorShake.value < 1f) {
        (sin(errorShake.value * PI * 6) * (1f - errorShake.value) * 10f).dp
    } else {
        0.dp
    }

    // Blinking cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    val scrollState = rememberScrollState()
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val historyListState = rememberLazyListState()
    LaunchedEffect(historyList.size, isHistoryOpen) {
        if (historyList.isNotEmpty() && isHistoryOpen) {
            historyListState.animateScrollToItem(historyList.size - 1)
        }
    }

    // Dynamic fluid font sizing with spring animation
    val targetBaseFontSize = when {
        isHistoryOpen -> 32f
        expression.length > 20 -> 28f
        expression.length > 14 -> 36f
        expression.length > 9 -> 44f
        else -> 56f
    } * textScale.scaleFactor

    val animatedFontSize by animateFloatAsState(
        targetValue = targetBaseFontSize,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 550f),
        label = "fontSizeAnim"
    )

    var isDraggingHandle by remember { mutableStateOf(false) }
    var totalDragDelta by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background)
            .statusBarsPadding()
    ) {
        // TOP APP BAR: History on Left, 3-dots on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // History Icon Button (Left)
            IconButton(
                onClick = onToggleHistory,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isHistoryOpen) palette.operatorBtn.copy(alpha = 0.6f) else Color.Transparent)
                    .testTag("top_btn_history")
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = if (isHistoryOpen) "Zamknij historię" else "Historia",
                    tint = palette.numberText.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3-dots Menu Button (Right)
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .testTag("top_btn_more")
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
                        text = { Text("Wyczyść historię", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        enabled = historyList.isNotEmpty(),
                        onClick = {
                            menuExpanded = false
                            showClearConfirmationDialog = true
                        },
                        modifier = Modifier.testTag("menu_clear_history")
                    )

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
                            onMenuClickThemes()
                        },
                        modifier = Modifier.testTag("menu_themes")
                    )

                    DropdownMenuItem(
                        text = { Text("Przelicznik jednostek", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.85f)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onMenuClickConverter()
                        },
                        modifier = Modifier.testTag("menu_converter")
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
                            onMenuClickPrivacy()
                        },
                        modifier = Modifier.testTag("menu_privacy")
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
                            onMenuClickFeedback()
                        },
                        modifier = Modifier.testTag("menu_feedback")
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
                            onMenuClickHelp()
                        },
                        modifier = Modifier.testTag("menu_help")
                    )
                }
            }
        }

        if (isHistoryOpen) {
            // WIDOK HISTORII (Screenshot 1 i 2)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                // Lista historii lub "Brak historii"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (historyList.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = palette.numberText.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Brak historii",
                                color = palette.numberText.copy(alpha = 0.7f),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    } else {
                        LazyColumn(
                            state = historyListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(historyList, key = { it.id }) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectHistoryItem(item) }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = item.expression,
                                        fontSize = 18.sp * textScale.scaleFactor,
                                        color = palette.numberText.copy(alpha = 0.75f),
                                        fontWeight = FontWeight.Normal,
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.result,
                                        fontSize = 26.sp * textScale.scaleFactor,
                                        color = palette.accentEquals,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

                // Pasek / uchwyt przeciągania (prostokąt z radiusem / romb) — animowany za palcem w czasie rzeczywistym!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .pointerInput(Unit) {
                            val velocityTracker = VelocityTracker()
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    velocityTracker.resetTracking()
                                    isDraggingHandle = true
                                    totalDragDelta = 0f
                                    onDragStart()
                                },
                                onDragEnd = {
                                    isDraggingHandle = false
                                    val vy = velocityTracker.calculateVelocity().y
                                    if (abs(totalDragDelta) < 8f) {
                                        onToggleHistoryFull()
                                    } else {
                                        onDragEnd(vy)
                                    }
                                },
                                onDragCancel = {
                                    isDraggingHandle = false
                                    onDragEnd(0f)
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDragDelta += dragAmount
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    onDragDelta(dragAmount)
                                }
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleHistoryFull() }
                        .testTag("history_drag_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(palette.numberText.copy(alpha = 0.45f))
                    )
                }

                // Dolna sekcja: "Bieżące wyrażenie" + aktywne wyrażenie
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Bieżące wyrażenie",
                        fontSize = 13.sp,
                        color = palette.numberText.copy(alpha = 0.65f),
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expression.ifEmpty { "0" },
                            color = palette.numberText,
                            fontSize = animatedFontSize.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.testTag("display_expression_history")
                        )

                        // Kursor
                        Box(
                            modifier = Modifier
                                .padding(start = 3.dp)
                                .width(2.5.dp)
                                .height((animatedFontSize * 0.8f).dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(palette.cursor.copy(alpha = cursorAlpha))
                        )
                    }
                }
            }
        } else {
            // WIDOK NORMALNY KALKULATORA
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 22.dp, top = 2.dp, bottom = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // RAD indicator (smooth AnimatedVisibility)
                AnimatedVisibility(
                    visible = angleUnit == "RAD",
                    enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.85f),
                    exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.85f),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAngleUnitToggle() }
                            .testTag("angle_unit_pill"),
                        color = palette.operatorBtn.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = "RAD",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = palette.operatorText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Obszar wyrażenia oraz wyniku
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .offset(x = shakeOffsetX),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    val currentAnim = animatingEvaluation
                    
                    // Unified height top container (84.dp) ensures zero layout shift when switching states
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (currentAnim != null && evalAnim.value < 1f) {
                            val p = evalAnim.value
                            // Animacja wyniesienia wyniku po naciśnięciu '=' (Google Calculator Style)
                            
                            // 1. Poprzednie wyrażenie ucieka w górę i zanika
                            Text(
                                text = currentAnim.previousExpression,
                                color = palette.numberText,
                                fontSize = (targetBaseFontSize * 0.85f).sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(y = (-36f * p).dp)
                                    .alpha((1f - p).coerceIn(0f, 1f))
                            )

                            // 2. Wynik płynnie wznosi się z dołu, powiększa i rozjaśnia do głównego koloru
                            val currentSize = (28f * textScale.scaleFactor + (targetBaseFontSize - 28f * textScale.scaleFactor) * p).sp
                            val currentColor = lerpColor(palette.resultPreview, palette.numberText, p)

                            Text(
                                text = currentAnim.result,
                                color = currentColor,
                                fontSize = currentSize,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-5.5f * p).dp, y = (23f * (1f - p)).dp)
                                    .testTag("display_expression")
                            )
                        } else {
                            // Standardowy widok wyrażenia z płynnie skalowanym rozmiarem czcionki
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = expression.ifEmpty { "0" },
                                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else (if (expression.isEmpty()) palette.numberText.copy(alpha = 0.5f) else palette.numberText),
                                    fontSize = animatedFontSize.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    modifier = Modifier.testTag("display_expression")
                                )

                                // Kursor
                                Box(
                                    modifier = Modifier
                                        .padding(start = 3.dp, bottom = (animatedFontSize * 0.12f).dp)
                                        .width(2.5.dp)
                                        .height((animatedFontSize * 0.8f).dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(palette.cursor.copy(alpha = cursorAlpha))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Podgląd wyniku
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.testTag("display_error")
                            )
                        } else if (resultPreview != null && (animatingEvaluation == null || evalAnim.value >= 1f)) {
                            AnimatedContent(
                                targetState = resultPreview,
                                transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(140)) },
                                label = "previewAnim"
                            ) { preview ->
                                Text(
                                    text = preview,
                                    color = palette.resultPreview,
                                    fontSize = 28.sp * textScale.scaleFactor,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    modifier = Modifier.testTag("display_result_preview")
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            title = {
                Text(
                    text = "Wyczyścić historię i pamięć?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmationDialog = false
                        onClearAllHistory()
                    }
                ) {
                    Text("Wyczyść", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmationDialog = false }) {
                    Text("Zamknij")
                }
            }
        )
    }
}

