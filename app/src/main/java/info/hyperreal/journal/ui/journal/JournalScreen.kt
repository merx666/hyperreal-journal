package info.hyperreal.journal.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import info.hyperreal.journal.ui.theme.HyperrealTokens
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.ShulginRating
import info.hyperreal.journal.domain.usecase.TimelinePhase
import info.hyperreal.journal.ui.components.TimelineChart
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun getShulginRatingColor(rating: ShulginRating): Color {
    return when (rating) {
        ShulginRating.PLUS_MINUS -> Color(0xFF94A3B8)
        ShulginRating.PLUS_ONE -> Color(0xFF38BDF8)
        ShulginRating.PLUS_TWO -> Color(0xFFFBBF24)
        ShulginRating.PLUS_THREE -> Color(0xFFF43F5E)
        ShulginRating.PLUS_FOUR -> Color(0xFFA855F7)
    }
}

private fun getPhaseLabel(phase: TimelinePhase?): String {
    return when (phase) {
        TimelinePhase.NOT_STARTED -> "Oczekuje"
        TimelinePhase.ONSET -> "Wejście (Onset)"
        TimelinePhase.COMEUP -> "Wzrost (Comeup)"
        TimelinePhase.PEAK -> "Szczyt (Peak)"
        TimelinePhase.OFFSET -> "Zejście (Offset)"
        TimelinePhase.AFTERGLOW -> "Powrót (Afterglow)"
        TimelinePhase.BASELINE -> "Zakończone"
        null -> ""
    }
}

private fun getPhaseColor(phase: TimelinePhase?): Color {
    return when (phase) {
        TimelinePhase.ONSET -> HyperrealTokens.TelemetryNotice
        TimelinePhase.COMEUP -> HyperrealTokens.TelemetryWarning
        TimelinePhase.PEAK -> HyperrealTokens.TelemetryDanger
        TimelinePhase.OFFSET -> HyperrealTokens.TelemetrySevere
        TimelinePhase.AFTERGLOW -> HyperrealTokens.TelemetrySafe
        TimelinePhase.BASELINE, TimelinePhase.NOT_STARTED, null -> HyperrealTokens.TextMuted
    }
}

private fun formatMinutes(minutes: Long?): String {
    if (minutes == null) return ""
    val hours = minutes / 60
    val rem = minutes % 60
    return if (hours > 0) "${hours}h ${rem}m" else "${rem}m"
}

private fun getSeverityColor(status: InteractionStatus): Color {
    return when (status) {
        InteractionStatus.DANGEROUS -> HyperrealTokens.TelemetryDanger
        InteractionStatus.UNSAFE -> HyperrealTokens.TelemetryWarning
        InteractionStatus.CAUTION -> HyperrealTokens.TelemetryNotice
        InteractionStatus.LOW_RISK_DECREASE -> HyperrealTokens.TelemetryNotice
        InteractionStatus.LOW_RISK_NO_SYNERGY -> HyperrealTokens.TextMuted
        InteractionStatus.LOW_RISK_SYNERGY -> HyperrealTokens.TelemetrySafe
        InteractionStatus.UNKNOWN -> HyperrealTokens.TextMuted
    }
}

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onAddClick: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val activeEntries by viewModel.activeEntries.collectAsState()
    val activeMixInteractions by viewModel.activeMixInteractions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val haptic = LocalHapticFeedback.current

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    var ingestionToDelete by remember { mutableStateOf<Ingestion?>(null) }
    var ingestionToEdit by remember { mutableStateOf<Ingestion?>(null) }
    var checkInTargetEntry by remember { mutableStateOf<JournalEntry?>(null) }

    // Real-time tick — recompose every 30s so charts and phase labels update smoothly
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            now = System.currentTimeMillis()
        }
    }

    if (ingestionToDelete != null) {
        AlertDialog(
            onDismissRequest = { ingestionToDelete = null },
            title = { Text("Usunąć wpis?") },
            text = { Text("Czy na pewno chcesz usunąć ten wpis z dziennika? Operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingestionToDelete?.let { viewModel.deleteIngestion(it) }
                        ingestionToDelete = null
                    }
                ) {
                    Text("Usuń", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ingestionToDelete = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (ingestionToEdit != null) {
        EditIngestionDialog(
            ingestion = ingestionToEdit!!,
            onDismiss = { ingestionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateIngestion(updated)
                ingestionToEdit = null
            }
        )
    }

    if (checkInTargetEntry != null) {
        CheckInBottomSheet(
            entry = checkInTargetEntry!!,
            onDismiss = { checkInTargetEntry = null },
            onSaveCheckIn = { phase, rating, notes ->
                viewModel.addCheckIn(
                    ingestionId = checkInTargetEntry!!.ingestion.id,
                    phase = phase,
                    rating = rating,
                    notes = notes
                )
                checkInTargetEntry = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj wpis")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Live Active Sessions Dashboard Card
            if (activeEntries.isNotEmpty()) {
                item {
                    ActiveSessionsDashboardCard(
                        activeEntries = activeEntries,
                        activeInteractions = activeMixInteractions,
                        onCheckInClick = { checkInTargetEntry = it }
                    )
                }
            }

            // 2. Search & Filter Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Szukaj w dzienniku...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentFilter == JournalFilter.ALL,
                            onClick = { viewModel.setFilter(JournalFilter.ALL) },
                            label = { Text("Wszystkie") }
                        )
                        FilterChip(
                            selected = currentFilter == JournalFilter.ACTIVE_ONLY,
                            onClick = { viewModel.setFilter(JournalFilter.ACTIVE_ONLY) },
                            label = { Text("Aktywne (${activeEntries.size})") }
                        )
                        FilterChip(
                            selected = currentFilter == JournalFilter.COMPLETED_ONLY,
                            onClick = { viewModel.setFilter(JournalFilter.COMPLETED_ONLY) },
                            label = { Text("Zakończone") }
                        )
                    }
                }
            }

            // 3. Entries List or Empty State
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 32.dp),
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
                                text = if (searchQuery.isNotBlank() || currentFilter != JournalFilter.ALL)
                                    "Brak pasujących wpisów"
                                else
                                    "Twój dziennik jest pusty",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || currentFilter != JournalFilter.ALL)
                                    "Zmień filtr lub wyszukiwane hasło."
                                else
                                    "Dodaj przyjęcie substancji, aby monitorować fazy działania i potencjalne interakcje.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(entries, key = { it.ingestion.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = HyperrealTokens.SurfaceDark),
                        border = BorderStroke(1.dp, HyperrealTokens.BorderSubtle),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.substance?.name ?: entry.ingestion.substanceId,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = HyperrealTokens.TextPrimary
                                    )
                                    val timeStr = dateFormat.format(Date(entry.ingestion.timestamp))
                                    Text(
                                        text = "${entry.ingestion.doseAmount} ${entry.ingestion.doseUnit} • ${entry.ingestion.roa} • $timeStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HyperrealTokens.TextSecondary
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            ingestionToEdit = entry.ingestion
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edytuj",
                                            tint = HyperrealTokens.TextSecondary.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            ingestionToDelete = entry.ingestion
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń",
                                            tint = HyperrealTokens.TelemetryDanger.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            val phase = entry.timelineStatus?.phase
                            if (phase != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(getPhaseColor(phase).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = getPhaseLabel(phase),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = getPhaseColor(phase)
                                        )
                                    }
                                }
                            }

                            if (!entry.ingestion.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notatka: ${entry.ingestion.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            }

                            val duration = entry.substance?.roas?.find {
                                it.name.equals(entry.ingestion.roa, ignoreCase = true)
                            }?.duration

                            if (duration != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TimelineChart(
                                    duration = duration,
                                    timeSinceIngestionMs = now - entry.ingestion.timestamp,
                                    ingestionTimeMs = entry.ingestion.timestamp
                                )
                            }

                            // Check-ins timeline
                            if (entry.checkIns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Oś czasu sesji (check-iny: ${entry.checkIns.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    entry.checkIns.sortedBy { it.timestamp }.forEach { checkIn ->
                                        val offsetMin = (checkIn.timestamp - entry.ingestion.timestamp).coerceAtLeast(0L) / 60000L
                                        val offsetStr = "T+${formatMinutes(offsetMin)}"
                                        val ratingColor = getShulginRatingColor(checkIn.shulginRating)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(ratingColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = offsetStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(getPhaseColor(checkIn.phase).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = getPhaseLabel(checkIn.phase),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = getPhaseColor(checkIn.phase)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(ratingColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = checkIn.shulginRating.symbol,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = ratingColor
                                                )
                                            }
                                            if (!checkIn.notes.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "„${checkIn.notes}”",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteCheckIn(checkIn.id) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Usuń check-in",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { checkInTargetEntry = entry }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dodaj check-in", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionsDashboardCard(
    activeEntries: List<ActiveEntryInfo>,
    activeInteractions: List<info.hyperreal.journal.domain.model.SubstanceInteraction>,
    onCheckInClick: (JournalEntry) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HyperrealTokens.SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HyperrealTokens.BorderActiveGreen)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(HyperrealTokens.BrandGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aktywne sesje (${activeEntries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HyperrealTokens.TextPrimary
                    )
                }
                Text(
                    text = "Na żywo",
                    style = MaterialTheme.typography.labelSmall,
                    color = HyperrealTokens.BrandGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            activeEntries.forEachIndexed { index, active ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                val subName = active.entry.substance?.name ?: active.entry.ingestion.substanceId
                val phase = active.countdown.currentPhase
                val nextPhase = active.countdown.nextPhase
                val minToNext = active.countdown.minutesToNextPhase
                val minToBaseline = active.countdown.minutesToBaseline

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$subName (${active.entry.ingestion.doseAmount} ${active.entry.ingestion.doseUnit})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .background(getPhaseColor(phase).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = getPhaseLabel(phase),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = getPhaseColor(phase)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val countdownDetails = buildString {
                        if (nextPhase != null && minToNext != null && minToNext > 0) {
                            append("Do ${getPhaseLabel(nextPhase)}: ${formatMinutes(minToNext)}")
                        }
                        if (minToBaseline != null && minToBaseline > 0) {
                            if (isNotEmpty()) append(" • ")
                            append("Baseline: ~${formatMinutes(minToBaseline)}")
                        }
                    }

                    if (countdownDetails.isNotEmpty()) {
                        Text(
                            text = countdownDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { active.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = getPhaseColor(phase),
                        trackColor = HyperrealTokens.SurfaceRaised
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val latestCheckIn = active.entry.checkIns.maxByOrNull { it.timestamp }
                        if (latestCheckIn != null) {
                            val checkInOffset = (latestCheckIn.timestamp - active.entry.ingestion.timestamp).coerceAtLeast(0L) / 60000L
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ostatni:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(getShulginRatingColor(latestCheckIn.shulginRating).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = latestCheckIn.shulginRating.symbol,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = getShulginRatingColor(latestCheckIn.shulginRating)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(T+${formatMinutes(checkInOffset)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCheckInClick(active.entry)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Check-in (Shulgin)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Multi-substance active mix warnings
            if (activeInteractions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                activeInteractions.forEach { interaction ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = getSeverityColor(interaction.status).copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, getSeverityColor(interaction.status).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = getSeverityColor(interaction.status),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Interakcja: ${interaction.substanceA} + ${interaction.substanceB}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getSeverityColor(interaction.status)
                                )
                                Text(
                                    text = "Status: ${interaction.status.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (!interaction.note.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = interaction.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditIngestionDialog(
    ingestion: Ingestion,
    onDismiss: () -> Unit,
    onConfirm: (Ingestion) -> Unit
) {
    var doseAmountStr by remember { mutableStateOf(ingestion.doseAmount.toString()) }
    var doseUnit by remember { mutableStateOf(ingestion.doseUnit) }
    var notes by remember { mutableStateOf(ingestion.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj wpis") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = doseAmountStr,
                    onValueChange = { doseAmountStr = it },
                    label = { Text("Dawka") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = doseUnit,
                    onValueChange = { doseUnit = it },
                    label = { Text("Jednostka (np. mg, ug)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notatka") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = doseAmountStr.toFloatOrNull() ?: ingestion.doseAmount
                    onConfirm(
                        ingestion.copy(
                            doseAmount = amount,
                            doseUnit = doseUnit.trim(),
                            notes = notes.trim().ifEmpty { null }
                        )
                    )
                }
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInBottomSheet(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onSaveCheckIn: (phase: TimelinePhase, rating: ShulginRating, notes: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val substanceName = entry.substance?.name ?: entry.ingestion.substanceId
    val currentPhase = entry.timelineStatus?.phase ?: TimelinePhase.PEAK
    val defaultPhase = if (currentPhase in listOf(TimelinePhase.ONSET, TimelinePhase.COMEUP, TimelinePhase.PEAK, TimelinePhase.OFFSET, TimelinePhase.AFTERGLOW)) {
        currentPhase
    } else {
        TimelinePhase.PEAK
    }

    var selectedPhase by remember { mutableStateOf(defaultPhase) }
    var selectedRating by remember { mutableStateOf(ShulginRating.PLUS_TWO) }
    var notes by remember { mutableStateOf("") }

    val elapsedMinutes = (System.currentTimeMillis() - entry.ingestion.timestamp).coerceAtLeast(0L) / 60000L
    val elapsedStr = "T+${formatMinutes(elapsedMinutes)}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Check-in sesji",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$substanceName • $elapsedStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .background(getPhaseColor(selectedPhase).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = getPhaseLabel(selectedPhase),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getPhaseColor(selectedPhase)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Faza sesji
            Text(
                text = "Faza sesji",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val phases = listOf(
                    TimelinePhase.ONSET to "Wejście",
                    TimelinePhase.COMEUP to "Wzrost",
                    TimelinePhase.PEAK to "Szczyt",
                    TimelinePhase.OFFSET to "Zejście",
                    TimelinePhase.AFTERGLOW to "Powrót"
                )
                phases.forEach { (phase, label) ->
                    FilterChip(
                        selected = selectedPhase == phase,
                        onClick = { selectedPhase = phase },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Skala Shulgina
            Text(
                text = "Skala Oceny Shulgina",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShulginRating.entries.forEach { rating ->
                    val isSelected = selectedRating == rating
                    val ratingColor = getShulginRatingColor(rating)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRating = rating },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ratingColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ratingColor) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rating.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ratingColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wybrana ocena Shulgina - opis
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = selectedRating.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getShulginRatingColor(selectedRating)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedRating.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Notatka z chwili obecnej
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notatka z chwili obecnej (opcjonalna)") },
                placeholder = { Text("Wizuale, myśli, doznania somatyczne...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Przycisk Zapisz
            Button(
                onClick = {
                    onSaveCheckIn(selectedPhase, selectedRating, notes.trim().ifEmpty { null })
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Zapisz Check-in", fontWeight = FontWeight.Bold)
            }
        }
    }
}
