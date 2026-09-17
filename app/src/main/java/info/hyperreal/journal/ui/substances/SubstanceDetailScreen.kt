package info.hyperreal.journal.ui.substances

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.ui.components.TimelineChart

@Composable
fun SubstanceDetailScreen(
    viewModel: SubstanceDetailViewModel = hiltViewModel()
) {
    val substance by viewModel.substance.collectAsState()

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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "ROA (Drogi podania):", style = MaterialTheme.typography.titleMedium)
        s.roas.forEach { roa ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "• ${roa.name}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            roa.dose?.let { dose ->
                Text(text = "  Dawki: Light: ${dose.light}, Common: ${dose.common}, Strong: ${dose.strong}", style = MaterialTheme.typography.bodySmall)
            }
            roa.duration?.let { duration ->
                Text(text = "  Czas działania: onset=${duration.onset}m, peak=${duration.peak}m, total=${duration.total}m", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                TimelineChart(
                    duration = duration,
                    timeSinceIngestionMs = 0L, // static display, no progress
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (s.interactions.isNotEmpty()) {
            Text(text = "Interakcje:", style = MaterialTheme.typography.titleMedium)
            s.interactions.forEach { interaction ->
                Text(text = "• ${interaction.substanceId}: ${interaction.status}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
