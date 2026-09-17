package info.hyperreal.journal.ui.addingestion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@Composable
fun ChooseSubstanceScreen(
    substances: List<Substance>,
    onSubstanceSelected: (Substance) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) substances else substances.filter {
        it.name.contains(query, ignoreCase = true) || it.aliases.any { alias -> alias.contains(query, ignoreCase = true) }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Wybierz substancję") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        LazyColumn {
            items(filtered, key = { it.id }) { substance ->
                ListItem(
                    headlineContent = { Text(substance.name) },
                    modifier = Modifier.clickable { onSubstanceSelected(substance) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChooseRoaScreen(
    substance: Substance,
    onRoaSelected: (Roa) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Wybierz drogę podania dla ${substance.name}", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(substance.roas, key = { it.name }) { roa ->
                ListItem(
                    headlineContent = { Text(roa.name) },
                    modifier = Modifier.clickable { onRoaSelected(roa) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun EnterDoseScreen(
    roa: Roa,
    onDoseEntered: (Float) -> Unit
) {
    var doseStr by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Wprowadź dawkę (${roa.name})", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        roa.dose?.let { dose ->
            Text("Proponowane: Light: ${dose.light}, Common: ${dose.common}, Strong: ${dose.strong}")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = doseStr,
            onValueChange = { doseStr = it },
            label = { Text("Dawka (np. 150.0)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { 
            doseStr.toFloatOrNull()?.let { onDoseEntered(it) } 
        }) {
            Text("Dalej")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmIngestionScreen(
    viewModel: AddIngestionViewModel,
    onSaveComplete: () -> Unit
) {
    val substance by viewModel.selectedSubstance.collectAsState()
    val roa by viewModel.selectedRoa.collectAsState()
    val dose by viewModel.doseAmount.collectAsState()
    val warnings by viewModel.interactionWarnings.collectAsState()
    val ingestionTimeMs by viewModel.ingestionTime.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = ingestionTimeMs)
    
    val cal = Calendar.getInstance().apply { timeInMillis = ingestionTimeMs }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = true
    )

    LaunchedEffect(Unit) {
        viewModel.checkInteractions()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMs ->
                        // Combine with current time selection
                        val timeCal = Calendar.getInstance().apply { timeInMillis = ingestionTimeMs }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = dateMs
                            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        }
                        viewModel.setIngestionTime(newCal.timeInMillis)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val dateCal = Calendar.getInstance().apply { timeInMillis = ingestionTimeMs }
                    dateCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    dateCal.set(Calendar.MINUTE, timePickerState.minute)
                    viewModel.setIngestionTime(dateCal.timeInMillis)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wybierz godzinę", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                TimePicker(state = timePickerState)
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val notes by viewModel.notes.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Potwierdzenie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Substancja: ${substance?.name}", style = MaterialTheme.typography.bodyLarge)
        Text("Droga podania: ${roa?.name}", style = MaterialTheme.typography.bodyLarge)
        Text("Dawka: $dose ${roa?.dose?.units ?: "mg"}", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Czas przyjęcia:", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                Text(dateFormatter.format(Date(ingestionTimeMs)))
            }
            OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                Text(timeFormatter.format(Date(ingestionTimeMs)))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = viewModel::setNotes,
            label = { Text("Notatki / Set & Setting (opcjonalnie)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        if (warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("⚠️ Ostrzeżenia o interakcjach:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    warnings.forEach { warn ->
                        Text("• Z ${warn.pastSubstanceName}: ${warn.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        warn.notes?.let { n ->
                            Text("   $n", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { viewModel.saveIngestion(onComplete = onSaveComplete) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Zapisz wpis")
        }
    }
}
