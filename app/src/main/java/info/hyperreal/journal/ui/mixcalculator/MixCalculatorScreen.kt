package info.hyperreal.journal.ui.mixcalculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.ui.matrix.MatrixExplorerScreen
import info.hyperreal.journal.ui.theme.HyperrealTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixCalculatorScreen(
    viewModel: MixCalculatorViewModel = hiltViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTabIndex) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Kalkulator pary", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Eksplorator Macierzy SIN", fontWeight = FontWeight.SemiBold) }
            )
        }

        when (selectedTab) {
            0 -> PairMixCalculatorContent(viewModel = viewModel)
            1 -> MatrixExplorerScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairMixCalculatorContent(
    viewModel: MixCalculatorViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sprawdź bezpieczeństwo połączenia dwóch substancji",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SubstanceSelector(
                label = "Substancja A",
                selectedSubstance = uiState.substanceA,
                substances = uiState.substances,
                onSubstanceSelected = viewModel::selectSubstanceA
            )

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.swapSubstances()
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Zamień",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            SubstanceSelector(
                label = "Substancja B",
                selectedSubstance = uiState.substanceB,
                substances = uiState.substances,
                onSubstanceSelected = viewModel::selectSubstanceB
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (uiState.substanceA != null && uiState.substanceB != null) {
                InteractionResultCard(
                    interaction = uiState.interactionResult,
                    subA = uiState.substanceA!!.name,
                    subB = uiState.substanceB!!.name
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceSelector(
    label: String,
    selectedSubstance: Substance?,
    substances: List<Substance>,
    onSubstanceSelected: (Substance) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSubstance?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            substances.forEach { substance ->
                DropdownMenuItem(
                    text = { Text(substance.name) },
                    onClick = {
                        onSubstanceSelected(substance)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InteractionResultCard(interaction: SubstanceInteraction?, subA: String, subB: String) {
    if (interaction == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = HyperrealTokens.TelemetryNotice,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Brak Danych w Bazie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nie mamy danych o interakcji $subA z $subB. Zachowaj szczególną ostrożność!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val statusColor = when (interaction.status) {
        InteractionStatus.LOW_RISK_SYNERGY -> HyperrealTokens.TelemetrySafe
        InteractionStatus.LOW_RISK_NO_SYNERGY -> HyperrealTokens.TelemetryNotice
        InteractionStatus.LOW_RISK_DECREASE -> HyperrealTokens.TelemetryNotice
        InteractionStatus.CAUTION -> HyperrealTokens.TelemetryWarning
        InteractionStatus.UNSAFE -> HyperrealTokens.TelemetryDanger
        InteractionStatus.DANGEROUS -> HyperrealTokens.TelemetrySevere
        InteractionStatus.UNKNOWN -> HyperrealTokens.TextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
                Text(
                    text = interaction.status.displayName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 0.08.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$subA + $subB",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!interaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = interaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
