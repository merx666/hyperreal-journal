package info.hyperreal.journal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import info.hyperreal.journal.domain.model.DurationParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun TimelineChart(
    duration: DurationParameters?,
    timeSinceIngestionMs: Long,
    ingestionTimeMs: Long? = null,
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

    val onsetColor = Color(0xFF4FC3F7) // Light Blue
    val comeupColor = Color(0xFFFFF176) // Yellow
    val peakColor = Color(0xFFE57373) // Red
    val offsetColor = Color(0xFFBA68C8) // Purple
    val afterglowColor = Color(0xFF81C784) // Green

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

            val fillPath = Path().apply {
                addPath(path)
                lineTo(prevX, height)
                lineTo(0f, height)
                close()
            }

            val phases = listOf(
                Triple(onsetColor, 0f, onsetRatio),
                Triple(comeupColor, onsetRatio, onsetRatio + comeupRatio),
                Triple(peakColor, onsetRatio + comeupRatio, onsetRatio + comeupRatio + peakRatio),
                Triple(offsetColor, onsetRatio + comeupRatio + peakRatio, onsetRatio + comeupRatio + peakRatio + offsetRatio),
                Triple(afterglowColor, 1f - afterglowRatio, 1f)
            )

            // Draw each phase segmented by color
            for ((color, startRatio, endRatio) in phases) {
                val startX = startRatio * width
                val endX = endRatio * width
                
                clipRect(left = startX, right = endX, top = 0f, bottom = height) {
                    drawPath(
                        path = fillPath,
                        brush = SolidColor(color.copy(alpha = 0.25f))
                    )
                    drawPath(
                        path = path,
                        brush = SolidColor(color.copy(alpha = 0.6f)),
                        style = Stroke(width = 12f)
                    )
                    drawPath(
                        path = path,
                        brush = SolidColor(color),
                        style = Stroke(width = 5f)
                    )
                }
            }

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
            
            val absoluteTimeStr = if (ingestionTimeMs != null) {
                val absoluteTime = ingestionTimeMs + (touchMin * 60 * 1000).toLong()
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateStr = format.format(Date(absoluteTime))
                "O godzinie $dateStr"
            } else {
                val h = touchMin.toInt() / 60
                val m = touchMin.toInt() % 60
                if (h > 0) "+${h}h ${m}m" else "+${m}m"
            }

            Text(
                text = "$absoluteTimeStr będziesz w fazie: $phaseName",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Black,
                modifier = Modifier
                    .padding(top = 0.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopCenter)
            )
        }
    }
}
