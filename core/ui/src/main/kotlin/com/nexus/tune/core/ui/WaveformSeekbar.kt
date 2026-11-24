package com.nexus.tune.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.util.Random

object WaveformProfileGenerator {
    fun generateProfile(trackId: String, barCount: Int = 48): List<Float> {
        val seed = trackId.hashCode().toLong()
        val random = Random(seed)
        val profile = FloatArray(barCount)
        var current = 0.35f
        for (i in 0 until barCount) {
            val step = (random.nextFloat() - 0.49f) * 0.3f
            current = (current + step).coerceIn(0.18f, 0.98f)
            profile[i] = current
        }
        return profile.toList()
    }
}

@Composable
fun WaveformSeekbar(
    progress: Float,
    trackId: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barCount: Int = 48,
    playedColor: Color = MaterialTheme.colorScheme.primary,
    unplayedColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.primary
) {
    val profile = remember(trackId, barCount) {
        WaveformProfileGenerator.generateProfile(trackId, barCount)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .pointerInput(trackId) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
            .pointerInput(trackId) {
                detectDragGestures { change, _ ->
                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
    ) {
        val totalWidth = size.width
        val maxHeight = size.height
        val totalBars = profile.size
        if (totalBars == 0 || totalWidth <= 0) return@Canvas

        val barSpacing = 3.dp.toPx()
        val totalSpacing = barSpacing * (totalBars - 1)
        val barWidth = ((totalWidth - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

        for (i in 0 until totalBars) {
            val x = i * (barWidth + barSpacing)
            val barFraction = i.toFloat() / totalBars.toFloat()
            val isPlayed = barFraction <= progress

            val normHeight = profile[i]
            val barHeight = (normHeight * maxHeight).coerceAtLeast(4.dp.toPx())
            val y = (maxHeight - barHeight) / 2f

            drawRoundRect(
                color = if (isPlayed) playedColor else unplayedColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }

        // Draw thumb / playhead line
        val thumbX = progress * totalWidth
        drawLine(
            color = thumbColor,
            start = Offset(thumbX, 0f),
            end = Offset(thumbX, maxHeight),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}

