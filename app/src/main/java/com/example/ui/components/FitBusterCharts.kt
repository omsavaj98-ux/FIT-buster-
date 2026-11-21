package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class BarChartPoint(
    val label: String,
    val value: Float
)

@Composable
fun WeeklyBarChart(
    points: List<BarChartPoint>,
    barColor: Color = MaterialTheme.colorScheme.primary,
    unitSuffix: String = "",
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(points) { animateTrigger = true }
    val animFactor by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 480),
        label = "bar_chart_anim"
    )
    val maxVal = (points.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val trackColor = Color(0xFF1A212C)
    val gridColor = Color(0xFF2B3647)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .padding(horizontal = 4.dp)
        ) {
            val count = points.size
            val padV = 4.dp.toPx()
            val chartHeight = (size.height - padV * 2f).coerceAtLeast(1f)
            val strokePx = 1.dp.toPx()

            // Crisp horizontal reference lines
            for (step in 0..2) {
                val y = padV + chartHeight * (step / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokePx
                )
            }

            val slotWidth = size.width / count
            val barWidth = (slotWidth * 0.48f).coerceAtLeast(8.dp.toPx())
            val cornerPx = 5.dp.toPx()
            val corner = CornerRadius(cornerPx, cornerPx)

            points.forEachIndexed { index, pt ->
                val x = index * slotWidth + (slotWidth - barWidth) / 2f
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(x, padV),
                    size = Size(barWidth, chartHeight),
                    cornerRadius = corner
                )
                val ratio = (pt.value / maxVal).coerceIn(0f, 1f) * animFactor
                val barHeight = (chartHeight * ratio).coerceAtLeast(if (pt.value > 0f) 4.dp.toPx() else 0f)
                if (barHeight > 0f) {
                    val topY = padV + chartHeight - barHeight
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(barColor, barColor.copy(alpha = 0.78f))
                        ),
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = corner
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (pt.value > 0f) "${pt.value.toInt()}$unitSuffix" else "-",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pt.value > 0f) barColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LineProgressChart(
    points: List<BarChartPoint>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    val maxVal = (points.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val minVal = (points.minOfOrNull { it.value } ?: 0f)
    val span = (maxVal - minVal).coerceAtLeast(1f)
    val gridColor = Color(0xFF2B3647)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val gridStrokePx = 1.dp.toPx()
            val padY = 6.dp.toPx()
            val innerHeight = (size.height - padY * 2f).coerceAtLeast(1f)

            for (step in 0..2) {
                val y = padY + innerHeight * (step / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = gridStrokePx
                )
            }
            if (points.size == 1) {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = center)
                return@Canvas
            }
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val coords = points.mapIndexed { idx, pt ->
                val norm = (pt.value - minVal) / span
                val y = padY + innerHeight - (norm * innerHeight)
                Offset(idx * stepX, y)
            }
            val linePath = Path().apply {
                coords.forEachIndexed { idx, offset ->
                    if (idx == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(coords.last().x, size.height)
                lineTo(coords.first().x, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.24f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            coords.forEach { offset ->
                drawCircle(color = Color(0xFF040507), radius = 4.5.dp.toPx(), center = offset)
                drawCircle(color = lineColor, radius = 3.dp.toPx(), center = offset)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
