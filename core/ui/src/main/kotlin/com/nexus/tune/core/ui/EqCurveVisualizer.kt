package com.nexus.tune.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun EqCurveVisualizer(
    bands: List<Int>,
    modifier: Modifier = Modifier,
    curveColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
    nodeColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw horizontal reference gridlines: +12dB (top), 0dB (middle), -12dB (bottom)
        val midY = h / 2f
        val topY = h * 0.1f
        val botY = h * 0.9f

        drawLine(
            color = gridColor,
            start = Offset(0f, topY),
            end = Offset(w, topY),
            strokeWidth = 1f
        )
        drawLine(
            color = gridColor.copy(alpha = gridColor.alpha * 2f),
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 2f
        )
        drawLine(
            color = gridColor,
            start = Offset(0f, botY),
            end = Offset(w, botY),
            strokeWidth = 1f
        )

        // 2. Calculate 5 band anchor points
        val safeBands = if (bands.size == 5) bands else listOf(0, 0, 0, 0, 0)
        val minGainMb = -1200f
        val maxGainMb = 1200f
        val range = maxGainMb - minGainMb
        val stepX = w / 4.0f

        val points = safeBands.mapIndexed { index, mb ->
            val clamped = mb.toFloat().coerceIn(minGainMb, maxGainMb)
            val fraction = (clamped - minGainMb) / range
            val y = h * (1.0f - fraction)
            val x = index * stepX
            Offset(x, y)
        }

        if (points.size < 2) return@Canvas

        // 3. Build smooth cubic Bézier spline
        val strokePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlX = (p0.x + p1.x) / 2f
                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            }
        }

        // Fill area under curve
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    curveColor.copy(alpha = 0.35f),
                    curveColor.copy(alpha = 0.05f)
                ),
                startY = 0f,
                endY = h
            )
        )

        // Draw curve outline
        drawPath(
            path = strokePath,
            color = curveColor,
            style = Stroke(width = 3.5f)
        )

        // 4. Draw interactive node markers
        points.forEach { pt ->
            // Outer ring
            drawCircle(
                color = curveColor,
                radius = 7f,
                center = pt
            )
            // Inner core
            drawCircle(
                color = nodeColor,
                radius = 4f,
                center = pt
            )
        }
    }
}
