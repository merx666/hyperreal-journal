package info.hyperreal.journal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import info.hyperreal.journal.domain.model.DurationParameters
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment

@Composable
fun TimelineChart(
    duration: DurationParameters?,
    timeSinceIngestionMs: Long,
    modifier: Modifier = Modifier
) {
    if (duration == null) return

    val onsetMin = if (duration.onset != null && duration.onset > 0f) duration.onset else 30f
    val comeupMin = if (duration.comeup != null && duration.comeup > 0f) duration.comeup else 60f
    val peakMin = if (duration.peak != null && duration.peak > 0f) duration.peak else 120f
    val offsetMin = if (duration.offset != null && duration.offset > 0f) duration.offset else 120f
    val afterglowMin = if (duration.afterglow != null && duration.afterglow > 0f) duration.afterglow else 120f

    val computedTotal = onsetMin + comeupMin + peakMin + offsetMin + afterglowMin
    val totalDurationMin = if ((duration.total ?: 0f) > 0f) duration.total!! else computedTotal
    
    if (totalDurationMin <= 0f) return

    val progressMin = TimeUnit.MILLISECONDS.toMinutes(timeSinceIngestionMs).toFloat().coerceAtLeast(0f)
    val progressRatio = (progressMin / totalDurationMin).coerceIn(0f, 1f)
    
    val onsetColor = Color(0xFFFFD54F) // Yellow
    val comeupColor = Color(0xFFFF8A65) // Orange
    val peakColor = Color(0xFFE57373) // Red
    val offsetColor = Color(0xFFBA68C8) // Purple
    val afterglowColor = Color(0xFF64B5F6) // Blue

    var touchX by remember { mutableStateOf<Float?>(null) }
    var chartWidth by remember { mutableStateOf(1f) }

    Box(modifier = modifier.fillMaxWidth().height(120.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> touchX = offset.x.coerceIn(0f, size.width.toFloat()) },
                        onDragEnd = { touchX = null },
                        onDragCancel = { touchX = null },
                        onDrag = { change, _ ->
                            touchX = change.position.x.coerceIn(0f, size.width.toFloat())
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x.coerceIn(0f, size.width.toFloat())
                            tryAwaitRelease()
                            touchX = null
                        }
                    )
                }
        ) {
        val width = size.width
        val height = size.height
        chartWidth = width

        val onsetRatio = onsetMin / totalDurationMin
        val comeupRatio = comeupMin / totalDurationMin
        val peakRatio = peakMin / totalDurationMin
        val offsetRatio = offsetMin / totalDurationMin
        val afterglowRatio = afterglowMin / totalDurationMin

        var currentX = 0f

        val pts = mutableListOf<Pair<Float, Float>>()
        pts.add(0f to 0f) // start

        // Onset ends at small intensity
        currentX += onsetMin
        pts.add((currentX / totalDurationMin) to 0.1f)

        // Comeup ends at max intensity
        currentX += comeupMin
        pts.add((currentX / totalDurationMin) to 1.0f)

        // Peak ends at max intensity
        currentX += peakMin
        pts.add((currentX / totalDurationMin) to 1.0f)

        // Offset drops intensity
        currentX += offsetMin
        pts.add((currentX / totalDurationMin) to 0.05f)

        // Afterglow to zero
        currentX += afterglowMin
        pts.add((currentX / totalDurationMin) to 0f)

        val path = Path()
        path.moveTo(0f, height)

        var prevX = 0f
        var prevY = height

        for (i in 1 until pts.size) {
            val (pxRaw, pyRaw) = pts[i]
            val x = pxRaw * width
            val y = height - (pyRaw * height * 0.8f) // leave some margin at top

            // Cubic bezier for smooth curve
            val controlPointX = (prevX + x) / 2
            path.cubicTo(
                controlPointX, prevY,
                controlPointX, y,
                x, y
            )
            prevX = x
            prevY = y
        }

        // Create dynamic brush based on phases
        val colorStops = arrayOf(
            0.0f to onsetColor,
            onsetRatio to comeupColor,
            (onsetRatio + comeupRatio) to peakColor,
            (onsetRatio + comeupRatio + peakRatio) to offsetColor,
            (onsetRatio + comeupRatio + peakRatio + offsetRatio) to afterglowColor,
            1.0f to afterglowColor
        )
        val strokeBrush = Brush.horizontalGradient(colorStops = colorStops)

        val fillPath = Path().apply {
            addPath(path)
            lineTo(prevX, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha=0.15f), Color.Transparent)
            )
        )

        // Stroke with glow
        drawPath(
            path = path,
            brush = strokeBrush,
            style = Stroke(width = 12f),
            alpha = 0.5f
        )
        drawPath(
            path = path,
            brush = strokeBrush,
            style = Stroke(width = 5f)
        )

        // Current progress indicator (only if time > 0)
        if (timeSinceIngestionMs > 0) {
            val indicatorX = progressRatio * width
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(indicatorX, 0f),
                end = Offset(indicatorX, height),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(indicatorX, height) // bottom line
            )
        }

        // Draw touch indicator
        touchX?.let { tx ->
            drawLine(
                color = Color.White,
                start = Offset(tx, 0f),
                end = Offset(tx, height),
                strokeWidth = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(tx, height / 2)
            )
        }
    }

    // Tooltip overlay
    touchX?.let { tx ->
        val touchMin = (tx / chartWidth) * totalDurationMin
        val phaseName = when {
            touchMin <= onsetMin -> "Onset"
            touchMin <= onsetMin + comeupMin -> "Comeup"
            touchMin <= onsetMin + comeupMin + peakMin -> "Peak"
            touchMin <= onsetMin + comeupMin + peakMin + offsetMin -> "Offset"
            touchMin <= totalDurationMin -> "Afterglow"
            else -> "Baseline"
        }
        val h = touchMin.toInt() / 60
        val m = touchMin.toInt() % 60
        val timeStr = if (h > 0) "+${h}h ${m}m" else "+${m}m"

        Text(
            text = "$timeStr : $phaseName",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            modifier = Modifier
                .padding(start = 8.dp, top = 0.dp)
                .background(Color.White, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .align(Alignment.TopStart)
        )
    }
}
}
