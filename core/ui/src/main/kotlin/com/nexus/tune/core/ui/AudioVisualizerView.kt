package com.nexus.tune.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nexus.tune.core.model.VisualizerData
import com.nexus.tune.core.model.VisualizerMode
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun AudioVisualizerView(
    visualizerData: VisualizerData,
    mode: VisualizerMode,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    if (mode == VisualizerMode.OFF) {
        return
    }

    Box(modifier = modifier) {
        when (mode) {
            VisualizerMode.SPECTRUM_BARS -> {
                SpectrumBarsVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.CIRCULAR_WAVE -> {
                CircularWaveVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.GLOW_PULSE -> {
                GlowPulseVisualizer(
                    amplitude = visualizerData.amplitude,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.OFF -> Unit
        }
    }
}

@Composable
private fun SpectrumBarsVisualizer(
    magnitudes: FloatArray,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val barCount = 24
    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        val spacing = totalWidth * 0.015f
        val barWidth = ((totalWidth - spacing * (barCount - 1)) / barCount).coerceAtLeast(2f)

        for (i in 0 until barCount) {
            val magIndex = (i * magnitudes.size / barCount).coerceIn(0, magnitudes.size - 1)
            val magnitude = magnitudes[magIndex].coerceIn(0.05f, 1.0f)
            val barHeight = totalHeight * magnitude
            val left = i * (barWidth + spacing)
            val top = totalHeight - barHeight

            val brush = Brush.verticalGradient(
                colors = listOf(secondaryColor, primaryColor),
                startY = top,
                endY = totalHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
private fun CircularWaveVisualizer(
    magnitudes: FloatArray,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = min(size.width, size.height) * 0.32f
        val maxDisplacement = min(size.width, size.height) * 0.12f

        val path = Path()
        val numPoints = 36
        val angleStep = (2 * Math.PI / numPoints)

        for (i in 0 until numPoints) {
            val magIndex = (i * magnitudes.size / numPoints) % magnitudes.size
            val displacement = magnitudes[magIndex] * maxDisplacement
            val r = baseRadius + displacement
            val angle = i * angleStep
            val x = center.x + (r * cos(angle)).toFloat()
            val y = center.y + (r * sin(angle)).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(primaryColor, secondaryColor, primaryColor),
                center = center
            ),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
private fun GlowPulseVisualizer(
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedAmp by animateFloatAsState(
        targetValue = amplitude.coerceIn(0.1f, 1.0f),
        animationSpec = tween(durationMillis = 60),
        label = "glowPulseAmp"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) * 0.45f
        val currentRadius = maxRadius * (0.4f + animatedAmp * 0.6f)

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.45f * animatedAmp),
                    primaryColor.copy(alpha = 0.2f * animatedAmp),
                    Color.Transparent
                ),
                center = center,
                radius = currentRadius
            ),
            radius = currentRadius,
            center = center
        )

        // Core ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.6f * animatedAmp + 0.2f),
            radius = currentRadius * 0.5f,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private val Int.dp: androidx.compose.ui.unit.Dp
    get() = androidx.compose.ui.unit.Dp(this.toFloat())
