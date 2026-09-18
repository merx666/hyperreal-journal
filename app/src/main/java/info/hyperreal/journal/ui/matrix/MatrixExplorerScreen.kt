package info.hyperreal.journal.ui.matrix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.SubstanceInteraction

fun getInteractionStatusColor(status: InteractionStatus): Color {
    return when (status) {
        InteractionStatus.DANGEROUS -> Color(0xFFEF4444)
        InteractionStatus.UNSAFE -> Color(0xFFF97316)
        InteractionStatus.CAUTION -> Color(0xFFFBBF24)
        InteractionStatus.LOW_RISK_SYNERGY -> Color(0xFF10B981)
        InteractionStatus.LOW_RISK_DECREASE -> Color(0xFF3B82F6)
        InteractionStatus.LOW_RISK_NO_SYNERGY -> Color(0xFF64748B)
        InteractionStatus.UNKNOWN -> Color(0xFF94A3B8)
    }
}

fun getInteractionStatusLabel(status: InteractionStatus): String {
    return when (status) {
        InteractionStatus.DANGEROUS -> "Zagrożenie życia"
        InteractionStatus.UNSAFE -> "Niebezpieczne"
        InteractionStatus.CAUTION -> "Ostrożność"
        InteractionStatus.LOW_RISK_SYNERGY -> "Synergia"
        InteractionStatus.LOW_RISK_DECREASE -> "Spadek działania"
        InteractionStatus.LOW_RISK_NO_SYNERGY -> "Brak synergii"
        InteractionStatus.UNKNOWN -> "Brak danych"
    }
}

@Composable
fun MatrixExplorerScreen(
    viewModel: MatrixExplorerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MatrixExplorerContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onStatusFilterChange = viewModel::setStatusFilter,
        onSubstanceSelect = viewModel::selectSubstance,
        onInteractionClick = viewModel::showInteractionDetail,
        onDismissDetail = { viewModel.showInteractionDetail(null) },
        onResetFilters = viewModel::resetFilters
    )
}

@Composable
fun MatrixExplorerContent(
    uiState: MatrixExplorerUiState,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (InteractionStatus?) -> Unit,
    onSubstanceSelect: (String?) -> Unit,
    onInteractionClick: (SubstanceInteraction) -> Unit,
    onDismissDetail: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Szukaj substancji lub powikłań...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Risk Severity Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedStatusFilter == null,
                    onClick = { onStatusFilterChange(null) },
                    label = { Text("Wszystkie (${uiState.allInteractions.size})", style = MaterialTheme.typography.labelSmall) }
                )
            }

            val statuses = listOf(
                InteractionStatus.DANGEROUS,
                InteractionStatus.UNSAFE,
                InteractionStatus.CAUTION,
                InteractionStatus.LOW_RISK_SYNERGY,
                InteractionStatus.LOW_RISK_DECREASE,
                InteractionStatus.LOW_RISK_NO_SYNERGY
            )

            items(statuses) { status ->
                val count = uiState.statusCounts[status] ?: 0
                val color = getInteractionStatusColor(status)
                FilterChip(
                    selected = uiState.selectedStatusFilter == status,
                    onClick = { onStatusFilterChange(status) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${getInteractionStatusLabel(status)} ($count)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Central Substance Selector Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedSubstance == null,
                    onClick = { onSubstanceSelect(null) },
                    label = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) }
                )
            }

            items(uiState.matrixSubstances) { substance ->
                FilterChip(
                    selected = uiState.selectedSubstance == substance,
                    onClick = { onSubstanceSelect(substance) },
                    label = { Text(substance, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // 4. Focus banner if substance is selected
        if (!uiState.selectedSubstance.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profil interakcji: ${uiState.selectedSubstance}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Znaleziono ${uiState.filteredInteractions.size} połączeń w tabeli SIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { onSubstanceSelect(null) }) {
                        Text("Wyczyść", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Interactions List
        if (uiState.filteredInteractions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Brak pasujących interakcji",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spróbuj zmienić filtr ryzyka lub wyszukiwane hasło.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onResetFilters) {
                        Text("Resetuj filtry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.filteredInteractions) { interaction ->
                    InteractionCard(
                        interaction = interaction,
                        onClick = { onInteractionClick(interaction) }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    if (uiState.selectedInteractionForDetail != null) {
        InteractionDetailBottomSheet(
            interaction = uiState.selectedInteractionForDetail,
            onDismiss = onDismissDetail
        )
    }
}

@Composable
private fun InteractionCard(
    interaction: SubstanceInteraction,
    onClick: () -> Unit
) {
    val statusColor = getInteractionStatusColor(interaction.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (interaction.status == InteractionStatus.DANGEROUS) {
            BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
        } else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${interaction.substanceA} + ${interaction.substanceB}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = getInteractionStatusLabel(interaction.status),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if (!interaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = interaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionDetailBottomSheet(
    interaction: SubstanceInteraction,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val statusColor = getInteractionStatusColor(interaction.status)

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${interaction.substanceA} + ${interaction.substanceB}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tabela Miksów SIN / TripSit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = getInteractionStatusLabel(interaction.status),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clinical Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (interaction.status == InteractionStatus.DANGEROUS || interaction.status == InteractionStatus.UNSAFE)
                                Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Opis kliniczny interakcji",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val noteText = interaction.note ?: "Brak szczegółowej notatki. Zachowaj szczególną ostrożność."
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Harm Reduction First Aid Protocol
            Text(
                text = "Zalecenia Harm Reduction i Pierwsza Pomoc",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val protocol = getHarmReductionProtocol(interaction)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = protocol,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Zamknij", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getHarmReductionProtocol(interaction: SubstanceInteraction): String {
    val note = interaction.note?.lowercase() ?: ""
    val subA = interaction.substanceA.lowercase()
    val subB = interaction.substanceB.lowercase()

    return when {
        note.contains("oddech") || (subA == "alkohol" && (subB.contains("opioid") || subB.contains("benzo") || subB.contains("ghb"))) ||
        (subB == "alkohol" && (subA.contains("opioid") || subA.contains("benzo") || subA.contains("ghb"))) -> {
            "⚠️ KRYTYCZNE ZAGROŻENIE DEPRESJĄ ODDECHOWĄ:\n\n" +
            "1. Nigdy nie zostawiaj osoby samej. Zapewnij obecność trzeźwego opiekuna.\n" +
            "2. W razie utraty przytomności natychmiast ułóż poszkodowanego w pozycji bocznej ustalonej, aby uniknąć zachłyśnięcia wymiocinami.\n" +
            "3. Kontroluj oddech. Jeśli spada poniżej 10 oddechów/min lub występuje bezdech, dzwoń pod 112 i rozpocznij RKO (30 uciśnięć, 2 wdechy).\n" +
            "4. W przypadku podejrzenia opioidów podaj Nalokson (Nyxoid / Prenoxad) donosowo lub domięśniowo."
        }
        note.contains("serotonin") || (subA.contains("maoi") || subB.contains("maoi")) || (note.contains("hiperterm")) -> {
            "⚠️ ZAGROŻENIE ZESPOŁEM SEROTONINOWYM:\n\n" +
            "1. Objawy alarmowe: drżenie, sztywność mięśni, gorączka >38°C, skrajne rozszerzenie źrenic, splątanie, delirium.\n" +
            "2. Zespół serotoninowy jest stanem bezpośredniego zagrożenia życia — natychmiast wezwij pogotowie ratunkowe (112).\n" +
            "3. Chłodź ciało poszkodowanego (zimne okłady, wietrzenie). Leki przeciwgorączkowe (paracetamol) nie działają na hipertermię mózgową.\n" +
            "4. Nie podawaj żadnych stymulantów ani leków psychotropowych."
        }
        note.contains("drgaw") || subA.contains("tramadol") || subB.contains("tramadol") -> {
            "⚠️ OBNIŻENIE PROGU DRGAWKOWEGO:\n\n" +
            "1. Połączenie istotnie zwiększa ryzyko napadu padaczkowego.\n" +
            "2. W razie ataku drgawek: chroń głowę przed uderzeniami, usuń ostre przedmioty z otoczenia.\n" +
            "3. NIE wkładaj niczego do ust poszkodowanego.\n" +
            "4. Po ustaniu drgawek ułóż w pozycji bocznej ustalonej i wezwij 112."
        }
        interaction.status == InteractionStatus.DANGEROUS -> {
            "⚠️ BEZPOŚREDNIE ZAGROŻENIE ŻYCIA:\n\n" +
            "1. Unikaj tego połączenia pod jakimkolwiek pozorem.\n" +
            "2. Jeśli doszło do przypadkowego zażycia, monitoruj parametry życiowe i natychmiast poinformuj osoby trzecie.\n" +
            "3. W razie utraty przytomności, bólu w klatce piersiowej lub problemów z oddychaniem wezwij 112."
        }
        interaction.status == InteractionStatus.UNSAFE -> {
            "🟠 WYSOKIE RYZYKO POWIKŁAŃ:\n\n" +
            "1. Połączenie wysoce nieprzewidywalne, z ryzykiem znacznego obciążenia układu krążenia lub psychiki.\n" +
            "2. Zapewnij obecność zaufanego, trzeźwego opiekuna (tripsittera).\n" +
            "3. Pij wodę lub elektrolity małymi łykami (ok. 250-300 ml/h)."
        }
        interaction.status == InteractionStatus.CAUTION -> {
            "🟡 WYMAGANA OSTROŻNOŚĆ:\n\n" +
            "1. Efekty mogą się wzajemnie maskować lub nieoczekiwanie eskalować.\n" +
            "2. Zredukuj dawki obu substancji o minimum 30-50% względem dawek przyjmowanych osobno.\n" +
            "3. Zachowaj bezpieczny odstęp czasowy między dawkami."
        }
        interaction.status == InteractionStatus.LOW_RISK_DECREASE -> {
            "🔵 SPADEK DZIAŁANIA:\n\n" +
            "1. Jedna substancja osłabia działanie drugiej (np. sedatywy na psychodeliki).\n" +
            "2. Nie bierz dodatkowych dawek (tzw. redosingu) substancji słabiej odczuwanej, gdyż po osłabieniu blokera może dojść do nagłego i niebezpiecznego przedawkowania."
        }
        else -> {
            "🟢 ZASADY BEZPIECZEŃSTWA:\n\n" +
            "1. Pamiętaj, że nawet połączenia o niskim ryzyku mogą być niebezpieczne w przypadku zbyt wysokich dawek lub złego stanu zdrowia.\n" +
            "2. Dbaj o nawodnienie i odpoczynek."
        }
    }
}
