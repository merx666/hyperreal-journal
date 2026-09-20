package info.hyperreal.journal.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.hyperreal.journal.domain.model.DurationParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import info.hyperreal.journal.ui.theme.HyperrealTokens

private val OnsetColor = HyperrealTokens.TelemetryNotice   // Sky Blue
private val ComeupColor = HyperrealTokens.TelemetryWarning  // Warm Amber
private val PeakColor = HyperrealTokens.TelemetryDanger    // Vibrant Coral / Crimson
private val OffsetColor = HyperrealTokens.TelemetrySevere  // Ultraviolet
private val AfterglowColor = HyperrealTokens.TelemetrySafe // Emerald Green

private data class ResolvedPhases(
    val onset: Float,
    val comeup: Float,
    val peak: Float,
    val offset: Float,
    val afterglow: Float,
    val total: Float
) {
    val tOnset: Float = onset
    val tComeup: Float = tOnset + comeup
    val tPeak: Float = tComeup + peak
    val tOffset: Float = tPeak + offset
    val tAfterglow: Float = total
}

private fun resolvePhases(duration: DurationParameters): ResolvedPhases? {
    val rawOnset = duration.onset
    val rawComeup = duration.comeup
    val rawPeak = duration.peak
    val rawOffset = duration.offset
    val rawAfterglow = duration.afterglow
    val rawTotal = duration.total

    val hasAnyPhase = rawOnset != null || rawComeup != null || rawPeak != null || rawOffset != null || rawAfterglow != null

    if (!hasAnyPhase && (rawTotal == null || rawTotal <= 0f)) {
        return null
    }

    if (!hasAnyPhase && rawTotal != null && rawTotal > 0f) {
        // Approximate phases based on total
        val onset = rawTotal * 0.10f
        val comeup = rawTotal * 0.20f
        val peak = rawTotal * 0.35f
        val offset = rawTotal * 0.25f
        val afterglow = rawTotal * 0.10f
        return ResolvedPhases(onset, comeup, peak, offset, afterglow, rawTotal)
    }

    val onset = (rawOnset ?: 0f).coerceAtLeast(0f)
    val comeup = (rawComeup ?: 0f).coerceAtLeast(0f)
    val peak = (rawPeak ?: 0f).coerceAtLeast(0f)
    val offset = (rawOffset ?: 0f).coerceAtLeast(0f)
    val afterglow = (rawAfterglow ?: 0f).coerceAtLeast(0f)

    val sum = onset + comeup + peak + offset + afterglow
    val total = if (rawTotal != null && rawTotal > sum) rawTotal else sum

    if (total <= 0f) return null

    return ResolvedPhases(onset, comeup, peak, offset, afterglow, total)
}

/**
 * Calculates the intensity of effect (0.0 to 1.0) at any given minute t.
 */
private fun calculateIntensity(t: Float, p: ResolvedPhases): Float {
    if (t <= 0f) return 0f
    if (t >= p.total) return 0f

    return when {
        t < p.tOnset -> {
            if (p.onset <= 0f) 0.1f
            else (t / p.onset) * 0.15f
        }
        t < p.tComeup -> {
            if (p.comeup <= 0f) 1.0f
            else {
                val ratio = (t - p.tOnset) / p.comeup
                // Smooth ease-in-out curve from 0.15 to 1.0
                val smoothRatio = (1f - cos(ratio * Math.PI.toFloat())) / 2f
                0.15f + (smoothRatio * 0.85f)
            }
        }
        t < p.tPeak -> 1.0f
        t < p.tOffset -> {
            if (p.offset <= 0f) 0.15f
            else {
                val ratio = (t - p.tPeak) / p.offset
                // Smooth drop from 1.0 to 0.15
                val smoothRatio = (1f - cos(ratio * Math.PI.toFloat())) / 2f
                1.0f - (smoothRatio * 0.85f)
            }
        }
        t < p.tAfterglow -> {
            val afterglowDuration = p.total - p.tOffset
            if (afterglowDuration <= 0f) 0f
            else {
                val ratio = (t - p.tOffset) / afterglowDuration
                val smoothRatio = (1f - cos(ratio * Math.PI.toFloat())) / 2f
                0.15f * (1f - smoothRatio)
            }
        }
        else -> 0f
    }
}

private fun getPhaseName(t: Float, p: ResolvedPhases): String {
    return when {
        t < p.tOnset -> "Wejście (Onset)"
        t < p.tComeup -> "Wzrost (Comeup)"
        t < p.tPeak -> "Szczyt (Peak)"
        t < p.tOffset -> "Zejście (Offset)"
        t < p.total -> "Powrót (Afterglow)"
        else -> "Koniec działania (Baseline)"
    }
}

private fun formatMinutes(minutes: Float): String {
    val totalM = minutes.toInt().coerceAtLeast(0)
    val h = totalM / 60
    val m = totalM % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
fun TimelineChart(
    duration: DurationParameters?,
    timeSinceIngestionMs: Long,
    ingestionTimeMs: Long? = null,
    modifier: Modifier = Modifier
) {
    if (duration == null) return

    val phases = remember(duration) { resolvePhases(duration) }

    if (phases == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Brak szczegółowych danych o czasie działania dla tej drogi podania",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        return
    }

    val totalDurationMin = phases.total
    val progressMin = TimeUnit.MILLISECONDS.toMinutes(timeSinceIngestionMs).toFloat().coerceAtLeast(0f)
    val progressRatio = (progressMin / totalDurationMin).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 800),
        label = "timeline_progress"
    )

    val haptic = LocalHapticFeedback.current
    var lastScrubbedPhase by remember { mutableStateOf<String?>(null) }

    // Pulsating beacon animation
    val infiniteTransition = rememberInfiniteTransition(label = "beacon_transition")
    val beaconPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_pulse"
    )
    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_alpha"
    )

    var touchX by remember { mutableStateOf<Float?>(null) }
    var chartWidth by remember { mutableFloatStateOf(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .onSizeChanged { chartWidth = it.width.toFloat() }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .align(Alignment.BottomCenter)
                .pointerInput(totalDurationMin) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downX = down.position.x.coerceIn(0f, size.width.toFloat())
                        touchX = downX
                        val currentPhase = getPhaseName((downX / size.width.toFloat()) * totalDurationMin, phases)
                        if (currentPhase != lastScrubbedPhase) {
                            lastScrubbedPhase = currentPhase
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }

                        do {
                            val event = awaitPointerEvent()
                            val move = event.changes.firstOrNull()
                            if (move != null && move.pressed) {
                                val moveX = move.position.x.coerceIn(0f, size.width.toFloat())
                                touchX = moveX
                                val phaseNow = getPhaseName((moveX / size.width.toFloat()) * totalDurationMin, phases)
                                if (phaseNow != lastScrubbedPhase) {
                                    lastScrubbedPhase = phaseNow
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        touchX = null
                        lastScrubbedPhase = null
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            val topMargin = 12.dp.toPx()
            val bottomMargin = 16.dp.toPx()
            val chartDrawHeight = height - topMargin - bottomMargin

            // Sample points along the width for a smooth curve
            val sampleCount = 80
            val curvePoints = ArrayList<Offset>(sampleCount + 1)
            for (i in 0..sampleCount) {
                val ratio = i.toFloat() / sampleCount
                val t = ratio * totalDurationMin
                val intensity = calculateIntensity(t, phases)
                val x = ratio * width
                val y = height - bottomMargin - (intensity * chartDrawHeight)
                curvePoints.add(Offset(x, y))
            }

            // Build fill path & stroke path
            val path = Path().apply {
                if (curvePoints.isNotEmpty()) {
                    moveTo(curvePoints[0].x, curvePoints[0].y)
                    for (i in 1 until curvePoints.size) {
                        val p0 = curvePoints[i - 1]
                        val p1 = curvePoints[i]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height - bottomMargin)
                lineTo(0f, height - bottomMargin)
                close()
            }

            // Phase intervals for colored segments
            val phaseSegments = listOf(
                Triple(OnsetColor, 0f, phases.tOnset / totalDurationMin),
                Triple(ComeupColor, phases.tOnset / totalDurationMin, phases.tComeup / totalDurationMin),
                Triple(PeakColor, phases.tComeup / totalDurationMin, phases.tPeak / totalDurationMin),
                Triple(OffsetColor, phases.tPeak / totalDurationMin, phases.tOffset / totalDurationMin),
                Triple(AfterglowColor, phases.tOffset / totalDurationMin, 1f)
            )

            // Draw segmented gradients and glow lines
            for ((color, startR, endR) in phaseSegments) {
                if (startR >= endR) continue
                val startX = startR * width
                val endX = endR * width

                clipRect(left = startX, right = endX, top = 0f, bottom = height) {
                    // Subtle vertical gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.35f),
                                color.copy(alpha = 0.03f)
                            ),
                            startY = topMargin,
                            endY = height - bottomMargin
                        )
                    )
                    // Outer glow stroke
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.30f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Inner sharp stroke
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            // Draw baseline line at bottom
            drawLine(
                color = HyperrealTokens.BorderSubtle,
                start = Offset(0f, height - bottomMargin),
                end = Offset(width, height - bottomMargin),
                strokeWidth = 1.dp.toPx()
            )

            // Current progress indicator (if ingestion started)
            if (timeSinceIngestionMs > 0 && animatedProgress in 0f..1f) {
                val progressX = animatedProgress * width
                val progressT = animatedProgress * totalDurationMin
                val progressIntensity = calculateIntensity(progressT, phases)
                val progressY = height - bottomMargin - (progressIntensity * chartDrawHeight)

                // Vertical indicator line
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = Offset(progressX, topMargin),
                    end = Offset(progressX, height - bottomMargin),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Breathing glowing beacon on curve
                val baseRadius = 12.dp.toPx()
                drawCircle(
                    color = primaryColor.copy(alpha = beaconPulseAlpha),
                    radius = baseRadius * beaconPulseScale,
                    center = Offset(progressX, progressY)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.55f),
                    radius = 7.dp.toPx(),
                    center = Offset(progressX, progressY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(progressX, progressY)
                )
            }

            // Touch scrubber indicator
            touchX?.let { tx ->
                val touchRatio = (tx / width).coerceIn(0f, 1f)
                val touchT = touchRatio * totalDurationMin
                val touchIntensity = calculateIntensity(touchT, phases)
                val touchY = height - bottomMargin - (touchIntensity * chartDrawHeight)

                // Dashed vertical scrubber line
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(tx, 0f),
                    end = Offset(tx, height - bottomMargin),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )

                // Scrubber handle circle on curve
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = 10.dp.toPx(),
                    center = Offset(tx, touchY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(tx, touchY)
                )
            }
        }

        // Floating interactive tooltip
        touchX?.let { tx ->
            val effectiveWidth = if (chartWidth > 0f) chartWidth else 1f
            val touchRatio = (tx / effectiveWidth).coerceIn(0f, 1f)
            val touchMin = touchRatio * totalDurationMin
            val phaseName = getPhaseName(touchMin, phases)

            val timeLabel = if (ingestionTimeMs != null) {
                val absoluteTime = ingestionTimeMs + (touchMin * 60 * 1000).toLong()
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                "${format.format(Date(absoluteTime))} (+${formatMinutes(touchMin)})"
            } else {
                "+${formatMinutes(touchMin)}"
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .border(BorderStroke(1.dp, HyperrealTokens.BorderHighlight), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = HyperrealTokens.SurfaceRaised.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  $phaseName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Total duration pill when not touching
        if (touchX == null) {
            Text(
                text = "Razem: ~${formatMinutes(totalDurationMin)}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 2.dp)
            )
        }
    }
}
