package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.EcoChallenge
import com.example.ui.theme.EcoGreenLight
import com.example.ui.theme.EcoGreenPrimary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ConfettiShape {
    RECTANGLE, CIRCLE, STAR, LEAF
}

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var scale: Float,
    val color: Color,
    val shape: ConfettiShape,
    val size: Float,
    var alpha: Float = 1f
)

@Composable
fun ConfettiExplosionCanvas(
    modifier: Modifier = Modifier,
    particleCount: Int = 75
) {
    val confettiColors = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFF059669), // Eco Green
        Color(0xFFFBBF24), // Amber Gold
        Color(0xFFF59E0B), // Warm Gold
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFFEC4899), // Pink
        Color(0xFF8B5CF6), // Violet
        Color(0xFF34D399)  // Mint
    )

    var particles by remember {
        mutableStateOf(
            List(particleCount) {
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val speed = Random.nextFloat() * 600f + 250f
                Particle(
                    x = 0.5f,
                    y = 0.4f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 200f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                    scale = Random.nextFloat() * 0.6f + 0.7f,
                    color = confettiColors.random(),
                    shape = ConfettiShape.values().random(),
                    size = Random.nextFloat() * 12f + 8f
                )
            }
        )
    }

    var lastFrameTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTime != 0L) {
                    val dt = ((frameTimeNanos - lastFrameTime) / 1_000_000_000f).coerceAtMost(0.05f)
                    val gravity = 900f

                    particles = particles.map { p ->
                        val newVy = p.vy + gravity * dt
                        val newVx = p.vx * (1f - 0.8f * dt)
                        val newX = p.x + (newVx * dt) / 400f
                        val newY = p.y + (newVy * dt) / 600f
                        val newRot = p.rotation + p.rotationSpeed * dt
                        val newAlpha = (p.alpha - dt * 0.25f).coerceAtLeast(0f)

                        p.copy(
                            x = newX,
                            y = newY,
                            vx = newVx,
                            vy = newVy,
                            rotation = newRot,
                            alpha = newAlpha
                        )
                    }
                }
                lastFrameTime = frameTimeNanos
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            if (p.alpha > 0f) {
                val px = p.x * w
                val py = p.y * h

                if (px in -50f..(w + 50f) && py in -50f..(h + 50f)) {
                    drawParticle(p, px, py)
                }
            }
        }
    }
}

private fun DrawScope.drawParticle(p: Particle, px: Float, py: Float) {
    rotate(p.rotation, pivot = Offset(px, py)) {
        val pColor = p.color.copy(alpha = p.alpha)
        val s = p.size * p.scale

        when (p.shape) {
            ConfettiShape.CIRCLE -> {
                drawCircle(
                    color = pColor,
                    radius = s / 2f,
                    center = Offset(px, py)
                )
            }
            ConfettiShape.RECTANGLE -> {
                drawRect(
                    color = pColor,
                    topLeft = Offset(px - s / 2, py - s / 3),
                    size = Size(s, s * 0.6f)
                )
            }
            ConfettiShape.LEAF -> {
                val path = Path().apply {
                    moveTo(px, py - s)
                    cubicTo(px + s, py - s / 2, px + s, py + s / 2, px, py + s)
                    cubicTo(px - s, py + s / 2, px - s, py - s / 2, px, py - s)
                    close()
                }
                drawPath(path, color = pColor)
            }
            ConfettiShape.STAR -> {
                val path = Path().apply {
                    val spikes = 5
                    val outerRadius = s
                    val innerRadius = s * 0.45f
                    var rot = Math.PI / 2 * 3
                    val step = Math.PI / spikes

                    moveTo(px, py - outerRadius)
                    for (i in 0 until spikes) {
                        var x = px + cos(rot).toFloat() * outerRadius
                        var y = py + sin(rot).toFloat() * outerRadius
                        lineTo(x, y)
                        rot += step

                        x = px + cos(rot).toFloat() * innerRadius
                        y = py + sin(rot).toFloat() * innerRadius
                        lineTo(x, y)
                        rot += step
                    }
                    lineTo(px, py - outerRadius)
                    close()
                }
                drawPath(path, color = pColor)
            }
        }
    }
}

@Composable
fun ChallengeCelebrationDialog(
    challenge: EcoChallenge,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onScanMore: () -> Unit
) {
    val isBm = language == AppLanguage.BM

    var visible by remember { mutableStateOf(false) }
    var displayedPoints by remember { mutableIntStateOf(0) }

    // Infinite Sunburst Rotation
    val infiniteTransition = rememberInfiniteTransition(label = "SunburstSpin")
    val sunburstAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst"
    )

    // Pulsing Glow Scale
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Entrance Spring Scale
    val entranceScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "entranceScale"
    )

    LaunchedEffect(Unit) {
        visible = true
        // Rolling points count-up animation
        val targetPts = challenge.pointsReward
        val steps = 15
        val delayTime = 30L
        for (i in 1..steps) {
            delay(delayTime)
            displayedPoints = (targetPts * i / steps)
        }
        displayedPoints = targetPts
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .testTag("challenge_celebration_dialog"),
            contentAlignment = Alignment.Center
        ) {
            // Full Screen Confetti Explosion Overlay
            ConfettiExplosionCanvas(modifier = Modifier.fillMaxSize())

            // Main Modal Card with Bouncy Spring Scale
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .scale(entranceScale)
                    .border(
                        2.dp,
                        Brush.verticalGradient(
                            listOf(
                                EcoGreenPrimary,
                                Color(0xFFFBBF24),
                                EcoGreenLight
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sunburst & Trophy Centerpiece
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Rotating Sunburst Halo
                        Canvas(
                            modifier = Modifier
                                .size(130.dp)
                                .rotate(sunburstAngle)
                                .scale(glowScale)
                        ) {
                            val rayCount = 12
                            val center = Offset(size.width / 2, size.height / 2)
                            val rayLength = size.width / 2

                            for (i in 0 until rayCount) {
                                val angle = (i * 360f / rayCount) * (Math.PI / 180f)
                                val x = center.x + cos(angle).toFloat() * rayLength
                                val y = center.y + sin(angle).toFloat() * rayLength
                                drawLine(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFDE047).copy(alpha = 0.7f), Color.Transparent),
                                        center = center,
                                        radius = rayLength
                                    ),
                                    start = center,
                                    end = Offset(x, y),
                                    strokeWidth = 14f
                                )
                            }
                        }

                        // Glowing Trophy Circle
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(14.dp, CircleShape, spotColor = Color(0xFFF59E0B))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFFEF3C7),
                                            Color(0xFFFDE68A),
                                            Color(0xFFF59E0B)
                                        )
                                    )
                                )
                                .border(2.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 42.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Celebration Title
                    Text(
                        text = if (isBm) "Tahniah! Cabaran Selesai!" else "Challenge Completed!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EcoGreenPrimary
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    // Challenge Name
                    Text(
                        text = if (isBm) challenge.titleBm else challenge.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Animated Points Reward Badge
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF047857),
                                        Color(0xFF059669),
                                        Color(0xFF10B981)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFA7F3D0), RoundedCornerShape(18.dp))
                            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "+$displayedPoints Eco Points",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Streak Booster Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isBm) "🔥 5 Hari Berturut-turut Aktif!" else "🔥 5-Day Streak Active!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = if (isBm)
                                        "Mata ditambah terus ke profil & papan markah anda."
                                    else
                                        "Points added directly to your profile & leaderboard.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Action Buttons
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("celebration_claim_continue_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBm) "Kumpul Mata & Teruskan ➔" else "Collect Points & Continue ➔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onScanMore()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("celebration_scan_more_button")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isBm) "Imbas Sisa Lain Sekarang" else "Scan Another Item Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
