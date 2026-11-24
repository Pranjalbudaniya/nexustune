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
import com.nexus.tune.core.model.VisualizerPalette
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

fun resolvePaletteColors(
    palette: VisualizerPalette,
    fallbackPrimary: Color,
    fallbackSecondary: Color
): Pair<Color, Color> {
    return if (palette == VisualizerPalette.DYNAMIC || palette.primaryHex.isBlank()) {
        Pair(fallbackPrimary, fallbackSecondary)
    } else {
        try {
            Pair(
                Color(android.graphics.Color.parseColor(palette.primaryHex)),
                Color(android.graphics.Color.parseColor(palette.secondaryHex))
            )
        } catch (e: Exception) {
            Pair(fallbackPrimary, fallbackSecondary)
        }
    }
}

@Composable
fun AudioVisualizerView(
    visualizerData: VisualizerData,
    mode: VisualizerMode,
    modifier: Modifier = Modifier,
    palette: VisualizerPalette = VisualizerPalette.DYNAMIC,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    if (mode == VisualizerMode.OFF) {
        return
    }

    val (activePrimary, activeSecondary) = resolvePaletteColors(palette, primaryColor, secondaryColor)

    Box(modifier = modifier) {
        when (mode) {
            VisualizerMode.SPECTRUM_BARS -> {
                SpectrumBarsVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.CIRCULAR_WAVE -> {
                CircularWaveVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.GLOW_PULSE -> {
                GlowPulseVisualizer(
                    amplitude = visualizerData.amplitude,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.STEREO_VU -> {
                StereoVuVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    amplitude = visualizerData.amplitude,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
                    modifier = Modifier.fillMaxSize()
                )
            }
            VisualizerMode.LIQUID_WAVE -> {
                LiquidWaveVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
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

@Composable
private fun StereoVuVisualizer(
    magnitudes: FloatArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val half = (magnitudes.size / 2).coerceAtLeast(1)
    val leftMag = magnitudes.take(half).average().toFloat().coerceIn(0.05f, 1.0f)
    val rightMag = magnitudes.drop(half).average().toFloat().coerceIn(0.05f, 1.0f)

    val numSegments = 14
    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        val meterHeight = (totalHeight * 0.35f).coerceAtLeast(12f)
        val meterWidth = (totalWidth * 0.75f).coerceAtLeast(100f)
        val leftX = (totalWidth - meterWidth) / 2f

        val segmentSpacing = 3.dp.toPx()
        val segmentWidth = (meterWidth - (numSegments - 1) * segmentSpacing) / numSegments

        val channels = listOf(
            Pair("L", leftMag),
            Pair("R", rightMag)
        )

        val topY = (totalHeight - (meterHeight * 2 + 8.dp.toPx())) / 2f

        channels.forEachIndexed { chIdx, (label, mag) ->
            val chY = topY + chIdx * (meterHeight + 8.dp.toPx())
            val activeSegments = (mag * numSegments).toInt().coerceIn(1, numSegments)

            for (seg in 0 until numSegments) {
                val segX = leftX + seg * (segmentWidth + segmentSpacing)
                val isLit = seg < activeSegments

                val segColor = when {
                    seg >= 11 -> Color(0xFFEF4444) // Red Peak (0dB)
                    seg >= 8 -> Color(0xFFF59E0B) // Amber High (-3dB)
                    else -> primaryColor // Green/Primary Normal
                }

                drawRoundRect(
                    color = if (isLit) segColor else segColor.copy(alpha = 0.2f),
                    topLeft = Offset(segX, chY),
                    size = Size(segmentWidth, meterHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun LiquidWaveVisualizer(
    magnitudes: FloatArray,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val bassMag = magnitudes.take(4).average().toFloat().coerceIn(0.1f, 1.0f)
    val midMag = magnitudes.drop(4).take(12).average().toFloat().coerceIn(0.1f, 1.0f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height * 0.6f

        val path = Path()
        path.moveTo(0f, height)
        path.lineTo(0f, midY)

        val steps = 30
        for (i in 0..steps) {
            val progress = i / steps.toFloat()
            val x = progress * width
            val wave1 = sin(progress * Math.PI * 3 + bassMag * 2) * (height * 0.25f * bassMag)
            val wave2 = cos(progress * Math.PI * 5 + midMag * 3) * (height * 0.15f * midMag)
            val y = (midY + wave1 + wave2).toFloat().coerceIn(0f, height)
            path.lineTo(x, y)
        }

        path.lineTo(width, height)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.7f),
                    primaryColor.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )
    }
}

private val Int.dp: androidx.compose.ui.unit.Dp
    get() = androidx.compose.ui.unit.Dp(this.toFloat())
