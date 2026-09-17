package info.hyperreal.journal.ui.journal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import info.hyperreal.journal.ui.components.TimelineChart
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onAddClick: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn {
                items(entries, key = { it.ingestion.id }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.substance?.name ?: entry.ingestion.substanceId) },
                        supportingContent = {
                            val timeStr = dateFormat.format(Date(entry.ingestion.timestamp))
                            val statusStr = entry.timelineStatus?.phase?.name ?: ""
                            
                            Column {
                                Text("${entry.ingestion.doseAmount} ${entry.ingestion.doseUnit} ${entry.ingestion.roa} • $timeStr")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Faza: $statusStr", style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                                
                                val duration = entry.substance?.roas?.find { it.name == entry.ingestion.roa }?.duration
                                if (duration != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TimelineChart(
                                        duration = duration,
                                        timeSinceIngestionMs = System.currentTimeMillis() - entry.ingestion.timestamp
                                    )
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
