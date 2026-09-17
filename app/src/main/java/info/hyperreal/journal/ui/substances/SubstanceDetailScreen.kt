package info.hyperreal.journal.ui.substances

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.ui.components.TimelineChart

@Composable
fun SubstanceDetailScreen(
    viewModel: SubstanceDetailViewModel = hiltViewModel()
) {
    val substance by viewModel.substance.collectAsState()
    val interactions by viewModel.interactions.collectAsState()

    if (substance == null) {
        Text("Ładowanie...", modifier = Modifier.padding(16.dp))
        return
    }

    val s = substance!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = s.name, style = MaterialTheme.typography.headlineLarge)
        if (s.aliases.isNotEmpty()) {
            Text(text = s.aliases.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
        }

        s.summary?.let { summary ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Opis", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = summary, style = MaterialTheme.typography.bodyMedium)
        }

        s.harmReduction?.let { hr ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Redukcja Szkód", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = hr, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "ROA (Drogi podania):", style = MaterialTheme.typography.titleMedium)
        s.roas.forEach { roa ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• ${roa.name}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            roa.dose?.let { dose ->
                Text(
                    text = "  Dawki: Light: ${dose.light}, Common: ${dose.common}, Strong: ${dose.strong}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            roa.duration?.let { duration ->
                Text(
                    text = "  Czas działania: onset=${duration.onset}m, peak=${duration.peak}m, total=${duration.total}m",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                TimelineChart(
                    duration = duration,
                    timeSinceIngestionMs = 0L,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        if (interactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Interakcje (SIN):", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            interactions.forEach { interaction ->
                InteractionChip(interaction = interaction, currentSubstanceName = s.name)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InteractionChip(interaction: SubstanceInteraction, currentSubstanceName: String) {
    val otherSubstance = if (interaction.substanceA.equals(currentSubstanceName, ignoreCase = true)) {
        interaction.substanceB
    } else {
        interaction.substanceA
    }

    val (bgColor, textColor) = when (interaction.status) {
        InteractionStatus.LOW_RISK_SYNERGY -> Color(0xFF00C853) to Color.White
        InteractionStatus.LOW_RISK_NO_SYNERGY -> Color(0xFF64DD17) to Color.Black
        InteractionStatus.LOW_RISK_DECREASE -> Color(0xFF2962FF) to Color.White
        InteractionStatus.CAUTION -> Color(0xFFFFAB00) to Color.Black
        InteractionStatus.UNSAFE -> Color(0xFFFF3D00) to Color.White
        InteractionStatus.DANGEROUS -> Color(0xFFD50000) to Color.White
        InteractionStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "$otherSubstance — ${interaction.status.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (!interaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = interaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
