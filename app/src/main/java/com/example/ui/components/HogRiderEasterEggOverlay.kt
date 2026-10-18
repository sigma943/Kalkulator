package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

// Fire Particle (sparks, embers, flame tongues)
private class EmberFlame(
    var x: Float,
    var y: Float,
    var size: Float,
    var speedY: Float,
    var speedX: Float,
    var alpha: Float,
    var maxLife: Float,
    var currentLife: Float,
    var colorType: Int
)

// Fiery Ray - fast, bright streak of fire streaking across screen
private class FireRay(
    var x: Float,
    var y: Float,
    var length: Float,
    var angleDeg: Float,
    var speed: Float,
    var thickness: Float,
    var alpha: Float,
    var life: Float,
    var decay: Float,
    var color1: Color,
    var color2: Color
)

// Shrapnel / Bomb burst particle
private class BombParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotSpeed: Float,
    var size: Float,
    var life: Float,
    var decay: Float,
    var color: Color,
    var symbol: String? = null
)

@Composable
fun HogRiderEasterEggOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Audio synthesizer for lasers and explosion booms
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 95)
        } catch (_: Exception) {
            null
        }
    }

    // Exact 5-second countdown timer
    var remainingMillis by remember { mutableLongStateOf(5000L) }

    // Screen Shake Animatable
    val shakeOffset = remember { Animatable(0f) }
    // Screen Flash Animatable
    val flashAlpha = remember { Animatable(0.85f) }

    fun triggerScreenShake(magnitude: Float = 24f) {
        coroutineScope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            shakeOffset.snapTo(magnitude)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.28f, stiffness = 850f)
            )
        }
    }

    // Bomb of effects explosion state
    val bombParticles = remember { mutableStateListOf<BombParticle>() }
    var bombTriggerCount by remember { mutableIntStateOf(0) }
    val bombShockwaveRadius = remember { Animatable(0f) }
    val bombShockwaveAlpha = remember { Animatable(0f) }
    val hogTapSpin = remember { Animatable(0f) }
    val hogTapScale = remember { Animatable(1f) }

    // Screen dimensions
    var screenWidthPx by remember { mutableFloatStateOf(1080f) }
    var screenHeightPx by remember { mutableFloatStateOf(2200f) }

    // Hog Rider image bounds
    var hogImageOffset by remember { mutableStateOf(Offset.Zero) }
    var hogImageSize by remember { mutableStateOf(Offset.Zero) }

    // Initial entrance sound and flash
    DisposableEffect(Unit) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 250)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {}

        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        triggerScreenShake(30f)
        flashAlpha.animateTo(0f, tween(400))
    }

    // 5-second automatic countdown
    LaunchedEffect(Unit) {
        val interval = 50L
        while (remainingMillis > 0L) {
            delay(interval)
            remainingMillis = (remainingMillis - interval).coerceAtLeast(0L)
        }
        onDismiss()
    }

    // Infinite transitions for rotating laser fans and color shifting
    val infiniteTransition = rememberInfiniteTransition(label = "laserConcert")

    // Laser fan rotation (sweeping across the stage/screen like real concert show)
    val laserFanRotation by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fanRot"
    )

    // Smooth laser color hue cycling (Cyan, Crimson, Emerald, Amber, Violet)
    val laserColorHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorHue"
    )

    val laserPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPulse"
    )

    // Fire phase for rolling flame waves
    val firePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "firePhase"
    )

    // Dynamic Hog Rider gallop bounce & tilt
    val gallopBounce by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gallopBounce"
    )

    val gallopTilt by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gallopTilt"
    )

    val chargeX by infiniteTransition.animateFloat(
        initialValue = -65f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chargeX"
    )

    // 1. Fire Rays (Fast, clearly visible bright beams of fire streaking all across the screen)
    val fireRays = remember {
        val list = mutableStateListOf<FireRay>()
        repeat(36) {
            list.add(
                FireRay(
                    x = Random.nextFloat() * 1100f,
                    y = Random.nextFloat() * 2200f,
                    length = Random.nextFloat() * 140f + 70f,
                    angleDeg = Random.nextFloat() * 360f,
                    speed = Random.nextFloat() * 22f + 14f,
                    thickness = Random.nextFloat() * 7f + 4f,
                    alpha = Random.nextFloat() * 0.4f + 0.6f,
                    life = 1f,
                    decay = Random.nextFloat() * 0.016f + 0.009f,
                    color1 = if (Random.nextBoolean()) Color(0xFFFFF176) else Color(0xFFFFD54F),
                    color2 = if (Random.nextBoolean()) Color(0xFFFF3D00) else Color(0xFFFF1744)
                )
            )
        }
        list
    }

    // 2. Rising Fire Embers / Sparks (fiery particles rising through the whole display)
    val fireEmbers = remember {
        val list = mutableStateListOf<EmberFlame>()
        repeat(90) {
            list.add(
                EmberFlame(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    size = Random.nextFloat() * 18f + 8f,
                    speedY = Random.nextFloat() * 0.016f + 0.008f,
                    speedX = (Random.nextFloat() - 0.5f) * 0.012f,
                    alpha = Random.nextFloat() * 0.8f + 0.2f,
                    maxLife = Random.nextFloat() * 50f + 30f,
                    currentLife = Random.nextFloat() * 40f,
                    colorType = Random.nextInt(4)
                )
            )
        }
        list
    }

    // Trigger Bomb of Effects upon clicking Hog Rider
    fun triggerBombOfEffects() {
        bombTriggerCount++
        if (remainingMillis < 2500L) {
            remainingMillis = 2500L
        }

        // Violent screen rumble & blinding explosion flash
        triggerScreenShake(48f)
        coroutineScope.launch {
            flashAlpha.snapTo(0.95f)
            flashAlpha.animateTo(0f, tween(350))
        }

        // Audio tones
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 220)
            coroutineScope.launch {
                delay(90)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 190)
            }
        } catch (_: Exception) {}

        // Hog Rider spin & scale explosion
        coroutineScope.launch {
            launch {
                hogTapSpin.snapTo(0f)
                hogTapSpin.animateTo(1080f, tween(550, easing = FastOutSlowInEasing))
            }
            launch {
                hogTapScale.snapTo(1.75f)
                hogTapScale.animateTo(1.0f, spring(dampingRatio = 0.32f, stiffness = 650f))
            }
            launch {
                bombShockwaveRadius.snapTo(20f)
                bombShockwaveAlpha.snapTo(1f)
                bombShockwaveAlpha.animateTo(0f, tween(650))
            }
            launch {
                bombShockwaveRadius.animateTo(screenWidthPx * 1.5f, tween(650, easing = FastOutLinearInEasing))
            }
        }

        val center = Offset(
            x = if (hogImageSize.x > 0) hogImageOffset.x + hogImageSize.x / 2f else screenWidthPx / 2f,
            y = if (hogImageSize.y > 0) hogImageOffset.y + hogImageSize.y / 2f else screenHeightPx / 2f
        )

        // Spawn 55 explosive shrapnel pieces (bombs, fires, hammers, lightning)
        val symbols = listOf("💣", "💥", "⚡", "🔥", "🐗", "🔨", "999", "☢️")
        val colors = listOf(
            Color(0xFFFFEB3B),
            Color(0xFFFF1744),
            Color(0xFFFF6D00),
            Color(0xFF00E5FF),
            Color(0xFFFFFFFF)
        )

        for (i in 0 until 55) {
            val angle = (Random.nextFloat() * 2 * PI).toFloat()
            val speed = Random.nextFloat() * 40f + 15f
            bombParticles.add(
                BombParticle(
                    x = center.x,
                    y = center.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 40f,
                    size = Random.nextFloat() * 26f + 16f,
                    life = 1f,
                    decay = Random.nextFloat() * 0.03f + 0.018f,
                    color = colors[Random.nextInt(colors.size)],
                    symbol = if (i % 2 == 0) symbols[Random.nextInt(symbols.size)] else null
                )
            )
        }

        // Add 35 burst fire rays exploding outward
        for (i in 0 until 35) {
            val angle = Random.nextFloat() * 360f
            fireRays.add(
                FireRay(
                    x = center.x,
                    y = center.y,
                    length = Random.nextFloat() * 150f + 80f,
                    angleDeg = angle,
                    speed = Random.nextFloat() * 34f + 20f,
                    thickness = Random.nextFloat() * 9f + 5f,
                    alpha = 1f,
                    life = 1f,
                    decay = Random.nextFloat() * 0.035f + 0.02f,
                    color1 = Color(0xFFFFFFFF),
                    color2 = Color(0xFFFF3D00)
                )
            )
        }
    }

    // Step frame by frame: fire rays, fire embers, and bomb particles
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                // 1. Step fire rays
                val rayIter = fireRays.iterator()
                while (rayIter.hasNext()) {
                    val ray = rayIter.next()
                    val rad = Math.toRadians(ray.angleDeg.toDouble())
                    ray.x += (cos(rad) * ray.speed).toFloat()
                    ray.y += (sin(rad) * ray.speed).toFloat()
                    ray.life -= ray.decay

                    if (ray.life <= 0f ||
                        ray.x < -200f || ray.x > screenWidthPx + 200f ||
                        ray.y < -200f || ray.y > screenHeightPx + 200f
                    ) {
                        ray.x = Random.nextFloat() * screenWidthPx
                        ray.y = Random.nextFloat() * screenHeightPx
                        ray.angleDeg = Random.nextFloat() * 360f
                        ray.speed = Random.nextFloat() * 24f + 14f
                        ray.length = Random.nextFloat() * 150f + 70f
                        ray.thickness = Random.nextFloat() * 8f + 4f
                        ray.life = 1f
                        ray.alpha = Random.nextFloat() * 0.4f + 0.6f
                    }
                }

                // 2. Step fire embers
                for (ember in fireEmbers) {
                    ember.y -= ember.speedY
                    ember.x += ember.speedX
                    ember.currentLife += 1f
                    ember.alpha = ((1f - (ember.currentLife / ember.maxLife)) * 0.95f).coerceIn(0f, 1f)

                    if (ember.currentLife >= ember.maxLife || ember.y < -0.05f) {
                        ember.x = Random.nextFloat()
                        ember.y = 1.05f
                        ember.currentLife = 0f
                        ember.alpha = Random.nextFloat() * 0.85f + 0.15f
                        ember.size = Random.nextFloat() * 20f + 8f
                    }
                }

                // 3. Step bomb particles
                val bombIter = bombParticles.iterator()
                while (bombIter.hasNext()) {
                    val p = bombIter.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.85f // gravity
                    p.vx *= 0.96f
                    p.rotation += p.rotSpeed
                    p.life -= p.decay
                    if (p.life <= 0f) {
                        bombIter.remove()
                    }
                }
            }
        }
    }

    val shakeVal = shakeOffset.value * sin(System.currentTimeMillis() * 0.08f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(9999f)
            .background(Color(0xFF030104))
            .offset(x = shakeVal.dp, y = (shakeVal * 0.65f).dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping anywhere triggers bomb of effects!
                triggerBombOfEffects()
            }
            .onGloballyPositioned { coords ->
                screenWidthPx = coords.size.width.toFloat()
                screenHeightPx = coords.size.height.toFloat()
            }
    ) {
        // LAYER 1: CANVAS - Fiery Inferno Backdrop, Visible Fire Rays, Clean Concert Laser Fans
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. INFERNO: Raging fire background glow from bottom to mid-screen
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x33FF1744),
                        Color(0x66FF5722),
                        Color(0xAAFF6D00),
                        Color(0xEEFF3D00)
                    ),
                    startY = canvasH * 0.35f,
                    endY = canvasH
                )
            )

            // Animated roaring fire tongues on the bottom
            drawRagingFire(canvasW, canvasH, firePhase)

            // 2. CONCERT LASER SHOW (CLEAN & BALANCED)
            // Exactly matching the concert photo: 2 main ground projectors creating an intersecting criss-cross fan
            // Controlled beam count (11 beams per fan) so it is clean, striking, and NOT overcrowded
            drawConcertLaserShowClean(
                canvasW = canvasW,
                canvasH = canvasH,
                rotationAngle = laserFanRotation,
                colorHue = laserColorHue,
                pulse = laserPulse,
                isMega = bombTriggerCount > 0
            )

            // 3. FIRE: Small fiery rays shooting all across the screen in animation
            for (ray in fireRays) {
                val rad = Math.toRadians(ray.angleDeg.toDouble())
                val dx = (cos(rad) * ray.length).toFloat()
                val dy = (sin(rad) * ray.length).toFloat()
                val start = Offset(ray.x, ray.y)
                val end = Offset(ray.x + dx, ray.y + dy)

                // Fiery outer glow
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, ray.color2.copy(alpha = ray.alpha * ray.life)),
                        start = start,
                        end = end
                    ),
                    start = start,
                    end = end,
                    strokeWidth = (ray.thickness * 2.5f).dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Bright fiery core ray
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, ray.color1.copy(alpha = ray.alpha * ray.life)),
                        start = start,
                        end = end
                    ),
                    start = start,
                    end = end,
                    strokeWidth = ray.thickness.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Fiery spark head (burning comet tip)
                drawCircle(
                    color = Color.White.copy(alpha = ray.alpha * ray.life),
                    radius = (ray.thickness * 1.2f).dp.toPx(),
                    center = end
                )
                drawCircle(
                    color = Color(0xFFFF9100).copy(alpha = ray.alpha * ray.life * 0.8f),
                    radius = (ray.thickness * 2.2f).dp.toPx(),
                    center = end
                )
            }

            // 4. Floating fire embers throughout the screen
            for (ember in fireEmbers) {
                val emberX = ember.x * canvasW
                val emberY = ember.y * canvasH
                val emberColor = when (ember.colorType) {
                    0 -> Color(0xFFFFF59D) // Yellow
                    1 -> Color(0xFFFF9800) // Orange
                    2 -> Color(0xFFFF1744) // Red
                    else -> Color(0xFFFFD54F) // Gold
                }.copy(alpha = ember.alpha)

                drawCircle(
                    color = emberColor,
                    radius = ember.size,
                    center = Offset(emberX, emberY)
                )
            }

            // 5. BOMBA EFEKTÓW: Huge Shockwave Ring on Tap
            if (bombShockwaveAlpha.value > 0.01f) {
                val center = Offset(
                    x = if (hogImageSize.x > 0) hogImageOffset.x + hogImageSize.x / 2f else canvasW / 2f,
                    y = if (hogImageSize.y > 0) hogImageOffset.y + hogImageSize.y / 2f else canvasH / 2f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFEB3B).copy(alpha = bombShockwaveAlpha.value * 0.9f),
                            Color(0xFFFF1744).copy(alpha = bombShockwaveAlpha.value * 0.6f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = bombShockwaveRadius.value
                    ),
                    radius = bombShockwaveRadius.value,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = bombShockwaveAlpha.value),
                    radius = bombShockwaveRadius.value,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            // 6. Draw Bomb Shrapnel Particles
            for (p in bombParticles) {
                if (p.symbol == null) {
                    drawCircle(
                        color = p.color.copy(alpha = p.life),
                        radius = p.size * p.life,
                        center = Offset(p.x, p.y)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = p.life * 0.8f),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x - p.vx * 1.6f, p.y - p.vy * 1.6f),
                        strokeWidth = (p.size * 0.35f * p.life).coerceAtLeast(1f)
                    )
                }
            }
        }

        // LAYER 2: Floating Emoji Bomb Pieces
        for (p in bombParticles) {
            if (p.symbol != null && p.life > 0.05f) {
                Text(
                    text = p.symbol!!,
                    fontSize = (p.size * 1.35f).sp,
                    modifier = Modifier
                        .offset(
                            x = (p.x / density.density).dp,
                            y = (p.y / density.density).dp
                        )
                        .rotate(p.rotation)
                        .scale(p.life)
                        .alpha(p.life)
                )
            }
        }

        // LAYER 3: HOG RIDER IN THE CENTER
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = chargeX.dp, y = gallopBounce.dp)
                    .rotate(gallopTilt + hogTapSpin.value)
                    .scale(hogTapScale.value)
                    .onGloballyPositioned { coords ->
                        hogImageOffset = coords.positionInRoot()
                        hogImageSize = Offset(coords.size.width.toFloat(), coords.size.height.toFloat())
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerBombOfEffects()
                    }
            ) {
                // Fiery aura behind Hog Rider
                Box(
                    modifier = Modifier
                        .fillMaxSize(1.25f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFEB3B).copy(alpha = 0.6f),
                                    Color(0xFFFF5722).copy(alpha = 0.5f),
                                    Color(0xFFFF1744).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.hog_rider),
                    contentDescription = "Hog Rider",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }
        }

        // LAYER 4: Flash overlay (White flash on tap bomb)
        if (flashAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha.value))
            )
        }
    }
}

/**
 * Draws a clean, balanced concert laser show based on the user's photo:
 * Exactly 2 main stage projectors shooting upward and inward to create a striking criss-crossing diamond grid,
 * with rotating beams, color shifts and razor-sharp cores.
 */
private fun DrawScope.drawConcertLaserShowClean(
    canvasW: Float,
    canvasH: Float,
    rotationAngle: Float,
    colorHue: Float,
    pulse: Float,
    isMega: Boolean
) {
    val baseHue = colorHue % 360f
    // Two primary contrasting laser colors like the stage photo (Cyan & Red)
    val colorCyan = Color.hsv(baseHue, 0.95f, 1.0f)
    val colorRed = Color.hsv((baseHue + 180f) % 360f, 0.95f, 1.0f)
    val coreWhite = Color(0xFFFFFFFF)

    // Left and Right Projector points on the stage (lower 20% of screen)
    val leftOrigin = Offset(canvasW * 0.20f, canvasH * 0.82f)
    val rightOrigin = Offset(canvasW * 0.80f, canvasH * 0.82f)

    // Balanced beam count so it does NOT look messy or overcrowded (11 beams per projector)
    val beamCount = if (isMega) 15 else 11
    val spreadAngleDeg = if (isMega) 85f else 70f
    val beamLength = canvasH * 1.5f

    // 1. Left Projector: Shoots upward-right (+/- rotation swing)
    val angleLeft = -65f + rotationAngle * 0.6f
    drawCleanLaserFan(
        origin = leftOrigin,
        baseAngleDeg = angleLeft,
        spreadDeg = spreadAngleDeg,
        beamCount = beamCount,
        beamLength = beamLength,
        colorPrimary = colorCyan,
        colorSecondary = colorRed,
        coreWhite = coreWhite,
        pulse = pulse
    )

    // 2. Right Projector: Shoots upward-left (-/+ rotation swing, intersecting left beams!)
    val angleRight = -115f - rotationAngle * 0.6f
    drawCleanLaserFan(
        origin = rightOrigin,
        baseAngleDeg = angleRight,
        spreadDeg = spreadAngleDeg,
        beamCount = beamCount,
        beamLength = beamLength,
        colorPrimary = colorRed,
        colorSecondary = colorCyan,
        coreWhite = coreWhite,
        pulse = pulse
    )

    // Projector lens flares at the stage origins
    drawProjectorHeadClean(leftOrigin, colorCyan, pulse)
    drawProjectorHeadClean(rightOrigin, colorRed, pulse)
}

/**
 * Draws a fan of clean laser beams radiating from an origin
 */
private fun DrawScope.drawCleanLaserFan(
    origin: Offset,
    baseAngleDeg: Float,
    spreadDeg: Float,
    beamCount: Int,
    beamLength: Float,
    colorPrimary: Color,
    colorSecondary: Color,
    coreWhite: Color,
    pulse: Float
) {
    val halfSpread = spreadDeg / 2f
    val step = spreadDeg / (beamCount - 1).coerceAtLeast(1)

    for (i in 0 until beamCount) {
        val currentAngleDeg = (baseAngleDeg - halfSpread) + (i * step)
        val rad = Math.toRadians(currentAngleDeg.toDouble())
        val target = Offset(
            x = origin.x + (cos(rad) * beamLength).toFloat(),
            y = origin.y + (sin(rad) * beamLength).toFloat()
        )

        // Alternating colors like the concert photo
        val beamColor = if (i % 2 == 0) colorPrimary else colorSecondary

        val glowWidth = (7.dp.toPx() * pulse).coerceAtLeast(2.5f)
        val beamWidth = (2.5.dp.toPx() * pulse).coerceAtLeast(1.2f)
        val coreWidth = (1.0.dp.toPx() * pulse).coerceAtLeast(0.6f)

        // Subtle atmospheric haze
        drawLine(
            color = beamColor.copy(alpha = 0.28f),
            start = origin,
            end = target,
            strokeWidth = glowWidth,
            cap = StrokeCap.Round
        )

        // Sharp neon beam
        drawLine(
            color = beamColor,
            start = origin,
            end = target,
            strokeWidth = beamWidth,
            cap = StrokeCap.Round
        )

        // Burning white core
        drawLine(
            color = coreWhite.copy(alpha = 0.9f),
            start = origin,
            end = target,
            strokeWidth = coreWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Draws projector head fixture with lens flare
 */
private fun DrawScope.drawProjectorHeadClean(
    pos: Offset,
    color: Color,
    pulse: Float
) {
    drawCircle(
        color = color.copy(alpha = 0.5f),
        radius = 24.dp.toPx() * pulse,
        center = pos
    )
    drawCircle(
        color = Color.White,
        radius = 8.dp.toPx() * pulse,
        center = pos
    )
    drawCircle(
        color = color,
        radius = 32.dp.toPx() * pulse,
        center = pos,
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * Draws roaring fiery flames at the bottom of the screen
 */
private fun DrawScope.drawRagingFire(
    canvasW: Float,
    canvasH: Float,
    phase: Float
) {
    val baseY = canvasH

    // Back Flame (Deep Red / Crimson)
    val path1 = Path().apply {
        moveTo(0f, baseY)
        val steps = 9
        for (i in 0..steps) {
            val x = (i / steps.toFloat()) * canvasW
            val h = 240.dp.toPx() + sin(phase + i * 1.2f) * 50.dp.toPx()
            val y = baseY - h
            if (i == 0) lineTo(x, y) else {
                val prevX = ((i - 1) / steps.toFloat()) * canvasW
                val cx = (prevX + x) / 2f
                quadraticTo(cx, y - 30.dp.toPx(), x, y)
            }
        }
        lineTo(canvasW, baseY)
        close()
    }
    drawPath(
        path = path1,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF1744), Color(0xDDFF5722), Color(0xCCBF360C)),
            startY = baseY - 290.dp.toPx(),
            endY = baseY
        )
    )

    // Front Flame (Vivid Yellow / Golden)
    val path2 = Path().apply {
        moveTo(0f, baseY)
        val steps = 11
        for (i in 0..steps) {
            val x = (i / steps.toFloat()) * canvasW
            val h = 150.dp.toPx() + sin(phase * 1.6f + i * 1.4f) * 40.dp.toPx()
            val y = baseY - h
            if (i == 0) lineTo(x, y) else {
                val prevX = ((i - 1) / steps.toFloat()) * canvasW
                val cx = (prevX + x) / 2f
                quadraticTo(cx, y - 25.dp.toPx(), x, y)
            }
        }
        lineTo(canvasW, baseY)
        close()
    }
    drawPath(
        path = path2,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFFF3D00)),
            startY = baseY - 190.dp.toPx(),
            endY = baseY
        )
    )
}
