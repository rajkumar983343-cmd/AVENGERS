package com.jarvis.assistant.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.jarvis.assistant.CoreState
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The animated holographic AI core. Pass a square-ish modifier (e.g. aspectRatio(1f)). */
@Composable
fun AiCore(state: CoreState, modifier: Modifier = Modifier) {
    val targetSpeed = when (state) {
        CoreState.IDLE -> 14f
        CoreState.LISTENING -> 40f
        CoreState.THINKING -> 140f
        CoreState.SPEAKING -> 60f
    }
    val speed by animateFloatAsState(targetSpeed, tween(700), label = "speed")
    val energy by animateFloatAsState(
        when (state) {
            CoreState.IDLE -> 0.25f
            CoreState.LISTENING -> 0.7f
            CoreState.THINKING -> 0.5f
            CoreState.SPEAKING -> 1f
        },
        tween(500),
        label = "energy"
    )
    val accent by animateColorAsState(
        when (state) {
            CoreState.IDLE -> JarvisColors.Cyan
            CoreState.LISTENING -> Color(0xFF4DF0FF)
            CoreState.THINKING -> JarvisColors.Blue
            CoreState.SPEAKING -> Color(0xFF9BF3FF)
        },
        tween(500),
        label = "accent"
    )

    var angle by remember { mutableFloatStateOf(0f) }
    var clock by remember { mutableFloatStateOf(0f) }
    val currentSpeed by rememberUpdatedState(speed)

    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = (now - last) / 1_000_000_000f
            last = now
            angle = (angle + currentSpeed * dt) % 360f
            clock = (clock + dt) % 1000f
        }
    }

    Canvas(modifier = modifier) {
        val c = center
        val r = size.minDimension / 2f
        val stroke = (r * 0.012f).coerceAtLeast(1f)

        val breath = sin(clock * 2.2f) * 0.5f + 0.5f
        val voice = if (state == CoreState.SPEAKING) {
            abs(sin(clock * 9f)) * 0.6f + abs(sin(clock * 5.3f)) * 0.4f
        } else {
            breath
        }
        val corePulse = 1f + 0.10f * energy * voice

        // Ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.22f + 0.25f * energy), Color.Transparent),
                center = c,
                radius = r
            ),
            radius = r,
            center = c
        )

        // Listening ripples
        if (state == CoreState.LISTENING) {
            for (i in 0..1) {
                val p = (clock * 0.7f + i * 0.5f) % 1f
                drawCircle(
                    color = accent.copy(alpha = (1f - p) * 0.5f),
                    radius = r * (0.35f + 0.6f * p),
                    center = c,
                    style = Stroke(width = stroke * 2f)
                )
            }
        }

        // Outer dashed ring
        rotate(degrees = angle, pivot = c) {
            drawCircle(
                color = accent.copy(alpha = 0.55f),
                radius = r * 0.95f,
                center = c,
                style = Stroke(
                    width = stroke * 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(r * 0.06f, r * 0.05f))
                )
            )
        }

        // Counter-rotating arc segments
        rotate(degrees = -angle * 1.5f, pivot = c) {
            val rr = r * 0.80f
            for (i in 0 until 3) {
                drawArc(
                    color = accent.copy(alpha = 0.9f),
                    startAngle = i * 120f + 10f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(c.x - rr, c.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = stroke * 4f, cap = StrokeCap.Round)
                )
            }
        }

        // Static thin ring
        drawCircle(
            color = accent.copy(alpha = 0.25f),
            radius = r * 0.68f,
            center = c,
            style = Stroke(width = stroke)
        )

        // Tick ring
        rotate(degrees = angle * 0.6f, pivot = c) {
            val inner = r * 0.56f
            for (i in 0 until 60) {
                val a = Math.toRadians(i * 6.0).toFloat()
                val major = i % 5 == 0
                val len = if (major) r * 0.07f else r * 0.035f
                drawLine(
                    color = accent.copy(alpha = if (major) 0.9f else 0.4f),
                    start = Offset(c.x + cos(a) * inner, c.y + sin(a) * inner),
                    end = Offset(c.x + cos(a) * (inner + len), c.y + sin(a) * (inner + len)),
                    strokeWidth = stroke
                )
            }
        }

        // Thinking: fast inner scanner
        if (state == CoreState.THINKING) {
            rotate(degrees = angle * 2f, pivot = c) {
                val rr = r * 0.48f
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = 0f,
                    sweepAngle = 80f,
                    useCenter = false,
                    topLeft = Offset(c.x - rr, c.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = stroke * 3f, cap = StrokeCap.Round)
                )
            }
        }

        // Speaking: equalizer bars around the core
        if (state == CoreState.SPEAKING) {
            for (i in 0 until 48) {
                val a = Math.toRadians(i * 7.5).toFloat()
                val len = r * 0.04f + r * 0.08f * abs(sin(clock * 7f + i * 0.7f))
                val startR = r * 0.40f
                drawLine(
                    color = accent.copy(alpha = 0.9f),
                    start = Offset(c.x + cos(a) * startR, c.y + sin(a) * startR),
                    end = Offset(c.x + cos(a) * (startR + len), c.y + sin(a) * (startR + len)),
                    strokeWidth = stroke * 1.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Core
        val coreR = r * 0.30f * corePulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    accent,
                    JarvisColors.Blue.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = c,
                radius = coreR * 1.5f
            ),
            radius = coreR * 1.5f,
            center = c
        )
        drawCircle(
            color = accent,
            radius = coreR,
            center = c,
            style = Stroke(width = stroke * 2f)
        )
    }
}
