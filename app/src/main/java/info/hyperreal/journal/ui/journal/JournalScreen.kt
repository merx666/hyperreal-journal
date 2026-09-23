package info.hyperreal.journal.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.ShulginRating
import info.hyperreal.journal.domain.usecase.TimelinePhase
import info.hyperreal.journal.ui.components.TimelineChart
import info.hyperreal.journal.ui.theme.HyperrealTokens
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
        TimelinePhase.BASELINE -> "Zakończone (Baseline)"
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
    val counts by viewModel.counts.collectAsState()
    val activeMixInteractions by viewModel.activeMixInteractions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val haptic = LocalHapticFeedback.current

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    var ingestionToDelete by remember { mutableStateOf<Ingestion?>(null) }
    var ingestionToArchive by remember { mutableStateOf<Ingestion?>(null) }
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

    // Confirmation dialog: Delete entry
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

    // Confirmation dialog: Archive / End session
    if (ingestionToArchive != null) {
        AlertDialog(
            onDismissRequest = { ingestionToArchive = null },
            title = { Text("Zakończyć sesję?") },
            text = { Text("Sesja zostanie oznaczona jako zakończona i zarchiwizowana. Natychmiast zniknie z ekranu aktywnych sesji i trafi do zakładki 'Zakończone'.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        ingestionToArchive?.let { viewModel.archiveSession(it) }
                        ingestionToArchive = null
                    }
                ) {
                    Text("Zakończ i archiwizuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { ingestionToArchive = null }) {
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
        containerColor = HyperrealTokens.Canvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = HyperrealTokens.Canvas
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj sesję")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Search & Filter Tab Bar
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
                        placeholder = { Text("Szukaj w dzienniku...", color = HyperrealTokens.TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = HyperrealTokens.TextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = HyperrealTokens.TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = HyperrealTokens.SurfaceDark,
                            unfocusedContainerColor = HyperrealTokens.SurfaceDark,
                            focusedBorderColor = HyperrealTokens.BrandGreen,
                            unfocusedBorderColor = HyperrealTokens.BorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = HyperrealTokens.BrandGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented Filter Tabs: Aktywne | Zakończone | Wszystkie
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentFilter == JournalFilter.ACTIVE_ONLY,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setFilter(JournalFilter.ACTIVE_ONLY)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = HyperrealTokens.SurfaceDark,
                                selectedContainerColor = HyperrealTokens.SurfaceRaised,
                                labelColor = HyperrealTokens.TextSecondary,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (currentFilter == JournalFilter.ACTIVE_ONLY) HyperrealTokens.BrandGreen else HyperrealTokens.BorderSubtle
                            ),
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (counts.active > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(HyperrealTokens.BrandGreen, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text("Aktywne (${counts.active})", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        )
                        FilterChip(
                            selected = currentFilter == JournalFilter.COMPLETED_ONLY,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setFilter(JournalFilter.COMPLETED_ONLY)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = HyperrealTokens.SurfaceDark,
                                selectedContainerColor = HyperrealTokens.SurfaceRaised,
                                labelColor = HyperrealTokens.TextSecondary,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (currentFilter == JournalFilter.COMPLETED_ONLY) HyperrealTokens.BrandGreen else HyperrealTokens.BorderSubtle
                            ),
                            label = {
                                Text("Zakończone (${counts.completed})", fontWeight = FontWeight.SemiBold)
                            }
                        )
                        FilterChip(
                            selected = currentFilter == JournalFilter.ALL,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setFilter(JournalFilter.ALL)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = HyperrealTokens.SurfaceDark,
                                selectedContainerColor = HyperrealTokens.SurfaceRaised,
                                labelColor = HyperrealTokens.TextSecondary,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (currentFilter == JournalFilter.ALL) HyperrealTokens.BrandGreen else HyperrealTokens.BorderSubtle
                            ),
                            label = {
                                Text("Wszystkie (${counts.all})", fontWeight = FontWeight.SemiBold)
                            }
                        )
                    }
                }
            }

            // 2. Multi-substance active mix warnings (if active interactions exist)
            if (activeMixInteractions.isNotEmpty() && currentFilter != JournalFilter.COMPLETED_ONLY) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        activeMixInteractions.forEach { interaction ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = getSeverityColor(interaction.status).copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(1.dp, getSeverityColor(interaction.status).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
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
                                    Spacer(modifier = Modifier.width(10.dp))
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

            // 3. Entries List or Dedicated Tab Empty State
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (currentFilter) {
                                JournalFilter.ACTIVE_ONLY -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = HyperrealTokens.BrandGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "Brak pasujących aktywnych sesji" else "Brak aktywnych sesji",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank())
                                            "Żadna trwająca sesja nie pasuje do wyszukiwanego hasła."
                                        else
                                            "Wszystkie poprzednie sesje zakończyły swoje działanie lub zostały zarchiwizowane.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = onAddClick,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rozpocznij nową sesję")
                                    }
                                }
                                JournalFilter.COMPLETED_ONLY -> {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "Brak pasujących zakończonych sesji" else "Brak zakończonych sesji",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Gdy sesja zakończy czas działania substancji lub zostanie zarchiwizowana, pojawi się tutaj.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                JournalFilter.ALL -> {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "Brak pasujących wpisów" else "Twój dziennik jest pusty",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Dodaj przyjęcie substancji, aby monitorować fazy działania i potencjalne interakcje.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = onAddClick,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Dodaj zażycie")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(entries, key = { it.ingestion.id }) { entry ->
                    val isActive = entry.isActive
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = HyperrealTokens.SurfaceDark
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isActive) HyperrealTokens.BrandGreen.copy(alpha = 0.5f)
                            else HyperrealTokens.BorderSubtle
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Title, Dose, Date, Edit, Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(HyperrealTokens.BrandGreen, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = entry.substance?.name ?: entry.ingestion.substanceId,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    val timeStr = dateFormat.format(Date(entry.ingestion.timestamp))
                                    Text(
                                        text = "${entry.ingestion.doseAmount} ${entry.ingestion.doseUnit} • ${entry.ingestion.roa} • $timeStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            ingestionToEdit = entry.ingestion
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edytuj",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            ingestionToDelete = entry.ingestion
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Phase / Status Badge
                            val phase = entry.timelineStatus?.phase
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (entry.isArchived) {
                                        Box(
                                            modifier = Modifier
                                                .background(HyperrealTokens.TextMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Zarchiwizowana",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = HyperrealTokens.TextSecondary
                                            )
                                        }
                                    } else if (phase != null) {
                                        Box(
                                            modifier = Modifier
                                                .background(getPhaseColor(phase).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
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

                                if (isActive) {
                                    Text(
                                        text = "Na żywo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HyperrealTokens.BrandGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Notes
                            if (!entry.ingestion.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notatka: ${entry.ingestion.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                            }

                            // Bio-Timeline Chart
                            val duration = entry.substance?.roas?.find {
                                it.name.equals(entry.ingestion.roa, ignoreCase = true)
                            }?.duration

                            if (duration != null) {
                                Spacer(modifier = Modifier.height(10.dp))
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
                                    text = "Check-iny (${entry.checkIns.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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

                            // Bottom Card Actions: Check-in, Archive/End Session, Restore
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isActive) {
                                    OutlinedButton(
                                        onClick = { ingestionToArchive = entry.ingestion },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = HyperrealTokens.TextSecondary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Zakończ i archiwizuj",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            checkInTargetEntry = entry
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check-in (Shulgin)", style = MaterialTheme.typography.labelSmall)
                                    }
                                } else {
                                    if (entry.isArchived) {
                                        TextButton(
                                            onClick = { viewModel.restoreSession(entry.ingestion) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Przywróć jako aktywną", style = MaterialTheme.typography.labelSmall)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    TextButton(
                                        onClick = { checkInTargetEntry = entry }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dodaj notatkę", style = MaterialTheme.typography.labelSmall)
                                    }
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
                        border = if (isSelected) BorderStroke(2.dp, ratingColor) else null,
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
