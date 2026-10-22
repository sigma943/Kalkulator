package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val RiderGold = Color(0xFFFFCE75)
private val RiderCream = Color(0xFFFFF2D5)
private val RiderFire = Color(0xFFFF4B16)
private const val ShowDurationMillis = 6000f
private data class RiderSpark(val x: Float, val y: Float, val speed: Float, val size: Float)

/** A single clock drives the scene; particles are bounded and never accumulate on taps. */
@Composable
fun HogRiderEasterEggOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    soundEnabled: Boolean = false,
    vibrateEnabled: Boolean = true
) {
    val view = LocalView.current
    val dismiss by rememberUpdatedState(onDismiss)
    val reducedMotion = remember(view) {
        runCatching { Settings.Global.getFloat(view.context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false)
    }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var burstAt by remember { mutableFloatStateOf(-2000f) }
    var lastTapAt by remember { mutableFloatStateOf(-1000f) }
    val sparks = remember {
        val random = Random(943)
        List(72) { RiderSpark(random.nextFloat(), random.nextFloat(),
            0.025f + random.nextFloat() * 0.045f, 0.8f + random.nextFloat() * 1.8f) }
    }
    val tone = remember(soundEnabled) {
        if (soundEnabled) runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 35) }.getOrNull() else null
    }
    DisposableEffect(tone, vibrateEnabled) {
        if (vibrateEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        runCatching { tone?.startTone(ToneGenerator.TONE_PROP_ACK, 110) }
        onDispose { runCatching { tone?.release() } }
    }
    LaunchedEffect(Unit) {
        var previous = withFrameNanos { it }
        while (elapsed < ShowDurationMillis) {
            val now = withFrameNanos { it }
            // Pause across long frame gaps (for example when the app is backgrounded).
            elapsed += ((now - previous) / 1_000_000f).coerceIn(0f, 50f)
            previous = now
        }
        dismiss()
    }
    val time = if (reducedMotion) 0f else elapsed / 1000f
    val entrance = if (reducedMotion) 1f else (elapsed / 750f).coerceIn(0f, 1f)
    val ease = 1f - (1f - entrance) * (1f - entrance) * (1f - entrance)
    val exit = ((ShowDurationMillis - elapsed) / 450f).coerceIn(0f, 1f)
    val burst = if (reducedMotion) 1f else ((elapsed - burstAt) / 950f).coerceIn(0f, 1f)
    val impact = if (reducedMotion) 0f else sin(burst * PI.toFloat()) * (1f - burst)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().zIndex(9999f)
            .background(Color(0xFF080B16)).testTag("hog_rider_overlay")
    ) {
        val sceneWidth = maxWidth
        val sceneHeight = maxHeight
        val heroSize = minOf(sceneWidth * 0.9f, sceneHeight * 0.60f, 420.dp)
        Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = exit }) {
            val center = Offset(size.width * 0.5f, size.height * 0.47f)
            val halo = minOf(size.width * 0.68f, size.height * 0.36f)
            drawRect(Brush.radialGradient(listOf(Color(0xFF4A1B15), Color(0xFF080B16)), center, halo * 1.8f))
            drawCircle(Brush.radialGradient(listOf(RiderGold.copy(alpha = 0.16f), Color.Transparent),
                center, halo), halo, center)
            // Layered flame tongues form a moving corona behind the rider.
            val flameTime = if (reducedMotion) 0.7f else time
            repeat(32) { index ->
                val angle = index * 2f * PI.toFloat() / 32f
                val flicker = 0.5f + 0.5f * sin(flameTime * 8f + index * 2.7f)
                val base = halo * 0.62f
                val reach = halo * (0.86f + flicker * 0.30f + impact * 0.3f)
                val spread = 0.075f
                val flame = Path().apply {
                    moveTo(center.x + cos(angle - spread) * base, center.y + sin(angle - spread) * base)
                    quadraticTo(center.x + cos(angle - 0.15f) * reach * 0.9f,
                        center.y + sin(angle - 0.15f) * reach * 0.9f,
                        center.x + cos(angle) * reach, center.y + sin(angle) * reach)
                    quadraticTo(center.x + cos(angle + 0.12f) * reach * 0.8f,
                        center.y + sin(angle + 0.12f) * reach * 0.8f,
                        center.x + cos(angle + spread) * base, center.y + sin(angle + spread) * base)
                    close()
                }
                drawPath(flame, Brush.radialGradient(
                    listOf(RiderCream, RiderGold, RiderFire.copy(alpha = 0.75f), Color.Transparent),
                    center, reach * 1.08f), alpha = 0.85f * ease)
            }
            // Soft orange aura and a hot core keep the fire from looking like flat triangles.
            drawCircle(Brush.radialGradient(listOf(RiderGold.copy(alpha = 0.22f),
                RiderFire.copy(alpha = 0.18f), Color.Transparent), center, halo * 1.25f), halo * 1.25f, center)
            drawCircle(RiderGold.copy(alpha = 0.65f * ease), halo * 0.63f, center,
                style = Stroke(2.dp.toPx()))
            // Low flames lick upward from the ground, with a fixed particle budget.
            repeat(24) { index ->
                val phase = (flameTime * 0.9f + index * 0.137f) % 1f
                val x = size.width * (index + 0.5f) / 24f
                val y = size.height - phase * size.height * 0.22f
                val radius = (10f + 14f * (1f - phase)).dp.toPx()
                drawCircle(Brush.radialGradient(listOf(RiderGold.copy(alpha = (1f - phase) * 0.6f),
                    RiderFire.copy(alpha = (1f - phase) * 0.4f), Color.Transparent), Offset(x, y), radius), radius, Offset(x, y))
            }
            sparks.forEachIndexed { index, spark ->
                val y = ((spark.y - time * spark.speed) % 1f + 1f) % 1f
                val x = spark.x * size.width + sin(time * 0.7f + index) * 8.dp.toPx()
                drawCircle(if (index % 4 == 0) RiderFire.copy(alpha = 0.8f) else RiderGold.copy(alpha = 0.65f),
                    spark.size.dp.toPx(), Offset(x, y * size.height))
            }
            // Tap shockwave and a fixed set of sparks: no growing particle lists.
            if (burst < 1f) {
                drawCircle(RiderGold.copy(alpha = (1f - burst) * 0.65f),
                    halo * (0.35f + burst * 1.1f), center, style = Stroke(2.dp.toPx()))
                repeat(26) { index ->
                    val angle = index / 26f * 2f * PI.toFloat()
                    val distance = halo * (0.25f + burst * (0.8f + (index % 3) * 0.12f))
                    val start = center + Offset(cos(angle) * distance, sin(angle) * distance + burst * burst * 50.dp.toPx())
                    val end = start + Offset(cos(angle) * 10.dp.toPx(), sin(angle) * 10.dp.toPx())
                    drawLine(RiderCream.copy(alpha = (1f - burst) * 0.9f), start, end, 2.dp.toPx(), StrokeCap.Round)
                }
            }
            // Grounding shadow follows the portrait, rather than shaking the whole screen.
            drawOval(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent),
                Offset(center.x, center.y + heroSize.toPx() * 0.48f), heroSize.toPx() * 0.55f),
                topLeft = Offset(center.x - heroSize.toPx() * 0.45f, center.y + heroSize.toPx() * 0.38f),
                size = Size(heroSize.toPx() * 0.9f, heroSize.toPx() * 0.22f))
        }

        Image(painter = painterResource(R.drawable.hog_rider), contentDescription = "Hog Rider — dotknij, aby wywołać ogień i lasery",
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.Center)
                .offset(y = -(sceneHeight * 0.03f)).size(heroSize)
                .graphicsLayer {
                    alpha = ease * exit
                    val sizeScale = (0.35f + ease * 0.65f) * (1f + impact * 0.15f)
                    scaleX = sizeScale; scaleY = sizeScale
                    translationY = (1f - ease) * 70.dp.toPx() + sin(time * 7f) * 4.dp.toPx()
                    rotationZ = sin(time * 3f) * 1.5f + impact * 5f
                }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    if (elapsed - lastTapAt >= 180f) {
                        lastTapAt = elapsed
                        burstAt = elapsed
                        if (vibrateEnabled) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        runCatching { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 70) }
                    }
                }.testTag("hog_rider_portrait"))
        Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = ease * exit }) {
            val hero = heroSize.toPx()
            val center = Offset(size.width / 2f, size.height * 0.47f)
            val scale = (0.35f + ease * 0.65f) * (1f + impact * 0.15f)
            val rotation = (sin(time * 3f) * 1.5f + impact * 5f) * PI.toFloat() / 180f
            val bob = (1f - ease) * 70.dp.toPx() + sin(time * 7f) * 4.dp.toPx()
            // A smooth charge/fire cycle avoids full-screen flashes.
            val cycle = if (reducedMotion) 0f else ((time - 0.8f).coerceAtLeast(0f) % 1.8f)
            val charge = if (reducedMotion || time < 0.8f) 0f else
                (cycle / 0.2f).coerceIn(0f, 1f) * ((1.1f - cycle) / 0.3f).coerceIn(0f, 1f)
            val power = maxOf(charge, impact * 2f).coerceIn(0f, 1f)
            if (power > 0f) {
                repeat(2) { eye ->
                    val local = Offset(hero * (0.094f + eye * 0.052f) * scale, -hero * 0.335f * scale)
                    val origin = center + Offset(local.x * cos(rotation) - local.y * sin(rotation),
                        local.x * sin(rotation) + local.y * cos(rotation) + bob)
                    val target = Offset(size.width * (if (eye == 0) -0.1f else 1.1f),
                        size.height * (0.24f + 0.12f * sin(time * 1.6f + eye)))
                    drawLine(RiderFire.copy(alpha = power * 0.12f), origin, target, 26.dp.toPx(), StrokeCap.Round)
                    drawLine(RiderFire.copy(alpha = power * 0.32f), origin, target, 12.dp.toPx(), StrokeCap.Round)
                    drawLine(RiderGold.copy(alpha = power * 0.9f), origin, target, 4.dp.toPx(), StrokeCap.Round)
                    drawLine(RiderCream.copy(alpha = power), origin, target, 1.5f.dp.toPx(), StrokeCap.Round)
                    drawCircle(Brush.radialGradient(listOf(RiderCream.copy(alpha = power),
                        RiderFire.copy(alpha = power * 0.65f), Color.Transparent), origin, 18.dp.toPx()), 18.dp.toPx(), origin)
                }
            }
        }
        IconButton(onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp)
                .size(48.dp).testTag("hog_rider_close")) {
            Icon(Icons.Default.Close, contentDescription = "Zamknij animację", tint = RiderCream)
        }
    }
}
