package com.jarvis.assistant.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HudBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                listOf(JarvisColors.NavyLight, JarvisColors.Navy, JarvisColors.Black)
            )
        )
        val step = 36.dp.toPx()
        val gridColor = JarvisColors.Cyan.copy(alpha = 0.04f)
        var x = 0f
        while (x < size.width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
            y += step
        }
        val glowCenter = Offset(size.width / 2f, size.height * 0.28f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(JarvisColors.Cyan.copy(alpha = 0.12f), Color.Transparent),
                center = glowCenter,
                radius = size.width * 0.9f
            ),
            radius = size.width * 0.9f,
            center = glowCenter
        )
    }
}

@Composable
fun HudClock(modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()) }

    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(
            text = timeFormat.format(Date(now)),
            color = JarvisColors.Cyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = dateFormat.format(Date(now)).uppercase(),
            color = JarvisColors.TextDim,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp
        )
    }
}

/** Draws thin HUD corner brackets around a composable. */
fun Modifier.hudBrackets(
    color: Color = JarvisColors.Cyan.copy(alpha = 0.6f),
    length: Dp = 14.dp,
    width: Dp = 1.5.dp
): Modifier = drawBehind {
    val l = length.toPx()
    val w = width.toPx()
    val right = size.width
    val bottom = size.height
    drawLine(color, Offset(0f, 0f), Offset(l, 0f), w)
    drawLine(color, Offset(0f, 0f), Offset(0f, l), w)
    drawLine(color, Offset(right, 0f), Offset(right - l, 0f), w)
    drawLine(color, Offset(right, 0f), Offset(right, l), w)
    drawLine(color, Offset(0f, bottom), Offset(l, bottom), w)
    drawLine(color, Offset(0f, bottom), Offset(0f, bottom - l), w)
    drawLine(color, Offset(right, bottom), Offset(right - l, bottom), w)
    drawLine(color, Offset(right, bottom), Offset(right, bottom - l), w)
}
