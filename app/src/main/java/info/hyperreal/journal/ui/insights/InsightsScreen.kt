package info.hyperreal.journal.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.ui.theme.HyperrealGreen

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val data by viewModel.insights.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Time range filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightsTimeRange.entries.forEach { range ->
                androidx.compose.material3.FilterChip(
                    selected = data.selectedTimeRange == range,
                    onClick = { viewModel.setTimeRange(range) },
                    label = { Text(range.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (data.totalIngestions == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Brak danych do analizy",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dodaj wpisy w Dzienniku, aby zobaczyć statystyki, częstotliwość zażywania i wykresy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Stats cards row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Wszystkie",
                    value = "${data.totalIngestions}",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Substancje",
                    value = "${data.uniqueSubstances}",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = data.selectedTimeRange.label,
                    value = "${data.periodIngestionsCount}",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top substance
            data.topSubstance?.let { top ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Najczęściej w wybranym okresie",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            top,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Average gap
            data.averageDaysBetween?.let { avg ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Średni odstęp między przyjęciami",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "%.1f dni".format(avg),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Histogram
            if (data.last30DaysCounts.isNotEmpty()) {
                val chartTitle = when (data.selectedTimeRange) {
                    InsightsTimeRange.DAYS_7 -> "Aktywność w ostatnich 7 dniach"
                    InsightsTimeRange.DAYS_30 -> "Aktywność w ostatnich 30 dniach"
                    InsightsTimeRange.DAYS_90 -> "Aktywność w ostatnich 90 dniach"
                    InsightsTimeRange.ALL -> "Aktywność dzienna (ostatnie 30 dni)"
                }
                Text(
                    chartTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BarChart(
                    dayCounts = data.last30DaysCounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Substance breakdown
            if (data.substanceBreakdown.isNotEmpty()) {
                Text(
                    "Podział według substancji",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                data.substanceBreakdown.forEach { item ->
                    SubstanceBar(
                        name = item.name,
                        count = item.count,
                        maxCount = data.substanceBreakdown.first().count
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BarChart(
    dayCounts: List<DayCount>,
    modifier: Modifier = Modifier
) {
    val maxCount = dayCounts.maxOf { it.count }.coerceAtLeast(1)
    val barColor = HyperrealGreen

    Canvas(modifier = modifier) {
        val barWidth = size.width / dayCounts.size
        val maxBarHeight = size.height * 0.85f

        dayCounts.forEachIndexed { index, dayCount ->
            if (dayCount.count > 0) {
                val barHeight = (dayCount.count.toFloat() / maxCount) * maxBarHeight
                drawRect(
                    color = barColor,
                    topLeft = Offset(
                        x = index * barWidth + barWidth * 0.15f,
                        y = size.height - barHeight
                    ),
                    size = Size(
                        width = barWidth * 0.7f,
                        height = barHeight
                    )
                )
            }
        }

        // baseline
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun SubstanceBar(
    name: String,
    count: Int,
    maxCount: Int
) {
    val fraction = count.toFloat() / maxCount.coerceAtLeast(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(100.dp),
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).height(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.White.copy(alpha = 0.1f),
                    size = size
                )
                drawRect(
                    color = HyperrealGreen,
                    size = Size(size.width * fraction, size.height)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
