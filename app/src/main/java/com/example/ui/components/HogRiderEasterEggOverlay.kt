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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val RiderGold = Color(0xFFFFCE75)
private val RiderCream = Color(0xFFFFF2D5)
private val RiderViolet = Color(0xFF9A81FF)
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
        List(44) { RiderSpark(random.nextFloat(), random.nextFloat(),
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
        val compact = maxWidth > maxHeight
        val heroSize = minOf(maxWidth * 0.8f, maxHeight * 0.44f, 360.dp)
        Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = exit }) {
            val center = Offset(size.width * (if (compact) 0.28f else 0.5f), size.height * 0.47f)
            val halo = minOf(size.width * 0.68f, size.height * 0.36f)
            drawRect(Brush.radialGradient(listOf(Color(0xFF30234A), Color(0xFF080B16)), center, halo * 1.8f))
            drawCircle(Brush.radialGradient(listOf(RiderGold.copy(alpha = 0.16f), Color.Transparent),
                center, halo), halo, center)
            // Slow orbital arcs leave the portrait legible, with a restrained violet counterlight.
            repeat(3) { index ->
                val radius = halo * (0.62f + index * 0.12f)
                drawArc(if (index == 1) RiderViolet.copy(alpha = 0.35f) else RiderGold.copy(alpha = 0.38f),
                    startAngle = time * (if (index == 1) -13f else 10f) + index * 100f,
                    sweepAngle = if (index == 1) 100f else 68f, useCenter = false,
                    topLeft = center - Offset(radius, radius), size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = (if (index == 1) 1.2f else 2f).dp.toPx(), cap = StrokeCap.Round))
            }
            sparks.forEachIndexed { index, spark ->
                val y = ((spark.y - time * spark.speed) % 1f + 1f) % 1f
                val x = spark.x * size.width + sin(time * 0.7f + index) * 8.dp.toPx()
                drawCircle(if (index % 4 == 0) RiderViolet.copy(alpha = 0.55f) else RiderGold.copy(alpha = 0.38f),
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

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("SEKRET ODBLOKOWANY", color = RiderGold, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp).testTag("hog_rider_close")) {
                    Icon(Icons.Default.Close, contentDescription = "Zamknij animację", tint = RiderCream)
                }
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth().padding(start = if (compact) maxWidth * 0.44f else 0.dp).graphicsLayer { alpha = ease * exit }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HOG RIDER", color = RiderCream, fontSize = minOf(40f, maxWidth.value / 9f).sp,
                    fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Spacer(Modifier.height(8.dp))
                Text("Mały sekret. Wielkie wejście.", color = Color(0xFFC3B8CE), fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))
                Text("DOTKNIJ JEŹDŹCA", color = RiderGold, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF2A2635))) {
                Box(Modifier.fillMaxWidth((1f - elapsed / ShowDurationMillis).coerceIn(0f, 1f))
                    .fillMaxHeight().background(Brush.horizontalGradient(listOf(RiderViolet, RiderGold))))
            }
        }

        Image(painter = painterResource(R.drawable.hog_rider), contentDescription = "Hog Rider — dotknij, aby wywołać iskry",
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(if (compact) Alignment.CenterStart else Alignment.Center)
                .offset(x = if (compact) maxWidth * 0.28f - heroSize / 2 else 0.dp, y = -(maxHeight * 0.03f)).size(heroSize)
                .graphicsLayer {
                    alpha = ease * exit
                    val sizeScale = (0.72f + ease * 0.28f) * (1f + impact * 0.15f)
                    scaleX = sizeScale; scaleY = sizeScale
                    translationY = (1f - ease) * 70.dp.toPx() + sin(time * 3.1f) * 5.dp.toPx()
                    rotationZ = sin(time * 2.2f) * 2.2f + impact * 5f
                }
                .clip(RoundedCornerShape(28.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    if (elapsed - lastTapAt >= 180f) {
                        lastTapAt = elapsed
                        burstAt = elapsed
                        if (vibrateEnabled) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        runCatching { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 70) }
                    }
                }.testTag("hog_rider_portrait"))
    }
}
