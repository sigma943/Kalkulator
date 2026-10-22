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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
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
private const val ShowDurationMillis = 8000f
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

    // Automatic impacts drive the demolition even without tapping.
    val beat = if (reducedMotion) 1f else ((time - 0.65f).coerceAtLeast(0f) % 1.35f) / 1.35f
    val slam = if (reducedMotion || time < 0.65f) 0f else (1f - beat * 5f).coerceIn(0f, 1f)
    val force = maxOf(slam, impact)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().zIndex(9999f)
            .background(Color(0xFF080B16)).testTag("hog_rider_overlay")
    ) {
        val heroWidth = minOf(maxWidth * 0.86f, maxHeight * 0.62f, 420.dp)
        Canvas(Modifier.fillMaxSize().graphicsLayer {
            alpha = exit
            translationX = sin(time * 63f) * force * 6.dp.toPx()
            translationY = cos(time * 51f) * force * 4.dp.toPx()
        }) {
            val center = Offset(size.width * 0.5f, size.height * 0.48f)
            val span = minOf(size.width, size.height)
            drawRect(Brush.radialGradient(listOf(Color(0xFF5C1B14), Color(0xFF080B16)), center, span))
            // Fast radial streaks sell the charge before the first impact.
            if (!reducedMotion) repeat(28) { i ->
                val angle = i * 2f * PI.toFloat() / 28f + 0.12f * sin(time)
                val travel = (time * 1.6f + i * 0.173f) % 1f
                val start = center + Offset(cos(angle), sin(angle)) * span * (0.35f + travel)
                val end = start + Offset(cos(angle), sin(angle)) * span * 0.22f
                drawLine(RiderGold.copy(alpha = (1f - travel) * 0.32f), start, end, 1.dp.toPx())
            }
            // A skyline of blocks breaks apart and flies away on every automatic slam.
            repeat(36) { i ->
                val column = i % 12
                val row = i / 12
                val width = size.width / 12f
                val flight = if (reducedMotion) 0f else beat
                val base = Offset((column + 0.5f) * width, size.height * 0.84f - row * width * 0.65f)
                val spread = (column - 5.5f) * span * 0.12f * flight
                val lift = sin(flight * PI.toFloat()) * span * (0.3f + row * 0.1f)
                val position = base + Offset(spread, -lift + flight * flight * span * 0.2f)
                rotate(if (reducedMotion) 0f else (i % 2 * 2 - 1) * flight * 260f, position) {
                    drawRect(Color(0xFF302F3E), position - Offset(width * 0.4f, width * 0.25f),
                        androidx.compose.ui.geometry.Size(width * 0.8f, width * 0.5f))
                    drawLine(RiderFire.copy(alpha = 0.8f), position - Offset(width * 0.4f, width * 0.25f),
                        position + Offset(width * 0.4f, -width * 0.25f), 2.dp.toPx())
                }
            }
            // Turbulent fire jets rise from the base rather than forming a static petal ring.
            repeat(42) { i ->
                val age = if (reducedMotion) (i * 0.117f) % 1f else (time * 1.4f + i * 0.117f) % 1f
                val x = size.width * ((i * 0.618f) % 1f) + sin(time * 8f + i) * 12.dp.toPx()
                val y = size.height * 0.94f - age * span * (0.6f + i % 4 * 0.12f)
                val radius = span * (0.025f + (1f - age) * 0.055f)
                val point = Offset(x, y)
                drawCircle(Brush.radialGradient(listOf(RiderGold.copy(alpha = (1f - age) * 0.85f),
                    RiderFire.copy(alpha = (1f - age) * 0.55f), Color.Transparent), point, radius), radius, point)
            }
            // Staggered fireworks: rockets, expanding trails, gravity and fading embers.
            if (!reducedMotion) repeat(7) { shell ->
                val cycle = (time * 0.62f + shell * 0.193f) % 1f
                val origin = Offset(size.width * (0.12f + ((shell * 0.381f) % 0.76f)),
                    size.height * (0.16f + (shell % 3) * 0.22f))
                val color = listOf(RiderGold, Color(0xFF52E7FF), Color(0xFFFF68C8), RiderFire)[shell % 4]
                if (cycle < 0.18f) {
                    val rocket = origin + Offset(0f, (1f - cycle / 0.18f) * span * 0.65f)
                    drawLine(color.copy(alpha = 0.65f), rocket, rocket + Offset(0f, 26.dp.toPx()), 2.dp.toPx())
                } else {
                    val age = (cycle - 0.18f) / 0.82f
                    val radius = span * 0.44f * (1f - (1f - age) * (1f - age))
                    val fall = span * age * age * 0.24f
                    repeat(32) { particle ->
                        val angle = particle * 2f * PI.toFloat() / 32f + shell
                        val direction = Offset(cos(angle), sin(angle))
                        val point = origin + direction * radius * (0.7f + particle % 3 * 0.15f) + Offset(0f, fall)
                        val tail = point - direction * span * (0.025f + age * 0.05f)
                        drawLine(color.copy(alpha = (1f - age) * 0.22f), tail, point, 6.dp.toPx(), StrokeCap.Round)
                        drawLine(color.copy(alpha = 1f - age), tail, point, 1.4f.dp.toPx(), StrokeCap.Round)
                        drawCircle(RiderCream.copy(alpha = 1f - age), 1.5f.dp.toPx(), point)
                    }
                }
            }
            sparks.forEachIndexed { i, spark ->
                val travel = ((spark.y - time * spark.speed * 5f) % 1f + 1f) % 1f
                val point = Offset(spark.x * size.width, travel * size.height)
                drawLine(RiderGold.copy(alpha = 0.65f), point,
                    point + Offset(sin(i.toFloat()) * 4.dp.toPx(), 7.dp.toPx()), spark.size.dp.toPx())
            }
            if (!reducedMotion) {
                val wave = minOf(beat * 1.8f, 1f)
                drawCircle(RiderFire.copy(alpha = (1f - wave) * 0.65f), span * (0.1f + wave * 0.95f), center,
                    style = Stroke((1f + (1f - wave) * 6f).dp.toPx()))
                if (burst < 1f) repeat(48) { i ->
                    val angle = i * 2f * PI.toFloat() / 48f
                    val direction = Offset(cos(angle), sin(angle))
                    val point = center + direction * span * burst * (0.7f + i % 4 * 0.1f)
                    drawLine(RiderCream.copy(alpha = 1f - burst), point, point + direction * 18.dp.toPx(), 2.dp.toPx())
                }
            }
        }

        // Image and laser emitters share ONE layer: all movement, scale and rotation stay locked.
        Box(Modifier.align(Alignment.Center).offset(y = -(maxHeight * 0.02f))
            .size(width = heroWidth, height = heroWidth * (207f / 220f))
            .graphicsLayer {
                alpha = ease * exit
                val surge = if (reducedMotion) 0f else sin(time * 9f) * 0.025f + force * 0.11f
                scaleX = (0.35f + ease * 0.65f) * (1f + surge)
                scaleY = scaleX
                translationX = if (reducedMotion) 0f else sin(time * 4.5f) * 12.dp.toPx() + sin(time * 59f) * force * 5.dp.toPx()
                translationY = (1f - ease) * 100.dp.toPx() + sin(time * 9f) * 9.dp.toPx() - force * 15.dp.toPx()
                rotationZ = if (reducedMotion) 0f else sin(time * 4.5f) * 4f + sin(time * 19f) * force * 3f
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (elapsed - lastTapAt >= 180f) {
                    lastTapAt = elapsed
                    burstAt = elapsed
                    if (vibrateEnabled) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    runCatching { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 70) }
                }
            }.testTag("hog_rider_portrait")) {
            Image(painterResource(R.drawable.hog_rider),
                contentDescription = "Hog Rider — dotknij, aby wywołać eksplozję",
                contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
            Canvas(Modifier.matchParentSize()) {
                val power = if (reducedMotion || time < 0.65f) 0f else
                    (0.65f + 0.25f * sin(time * 7f) + force).coerceIn(0f, 1f)
                if (power > 0f) repeat(2) { eye ->
                    // Measured pixel centers in the original 220 x 207 bitmap, without letterboxing.
                    val origin = if (eye == 0) Offset(size.width * 139f / 220f, size.height * 31f / 207f)
                        else Offset(size.width * 151f / 220f, size.height * 37f / 207f)
                    val sweep = sin(time * 3.8f) * size.height * 0.85f
                    val target = Offset(if (eye == 0) -size.width * 1.5f else size.width * 2.5f,
                        size.height * 0.25f + sweep)
                    drawLine(RiderFire.copy(alpha = power * 0.18f), origin, target, 18.dp.toPx(), StrokeCap.Round)
                    drawLine(RiderFire.copy(alpha = power * 0.55f), origin, target, 8.dp.toPx(), StrokeCap.Round)
                    drawLine(RiderGold.copy(alpha = power), origin, target, 3.dp.toPx(), StrokeCap.Round)
                    drawLine(RiderCream.copy(alpha = power), origin, target, 1.dp.toPx(), StrokeCap.Round)
                    val glow = 7.dp.toPx()
                    drawCircle(Brush.radialGradient(listOf(RiderCream.copy(alpha = power),
                        RiderFire.copy(alpha = power * 0.8f), Color.Transparent), origin, glow), glow, origin)
                    drawCircle(RiderCream.copy(alpha = power), 1.8f.dp.toPx(), origin)
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
