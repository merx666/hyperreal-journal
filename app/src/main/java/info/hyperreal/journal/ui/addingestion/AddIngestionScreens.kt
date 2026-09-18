package info.hyperreal.journal.ui.addingestion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.usecase.DoseConverter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterDoseScreen(
    substance: Substance? = null,
    roa: Roa,
    onDoseEntered: (Float, String?) -> Unit
) {
    val doseConverter = remember { DoseConverter() }
    val isCannabis = substance?.id.equals("Cannabinoidy", ignoreCase = true) ||
            substance?.classes?.any { it.contains("Kannabinoid", ignoreCase = true) } == true
    val isAlcohol = substance?.id.equals("Alkohol", ignoreCase = true) ||
            substance?.aliases?.any { it.contains("Alkohol", ignoreCase = true) } == true

    var manualMode by remember { mutableStateOf(!isCannabis && !isAlcohol) }
    var manualDoseStr by remember { mutableStateOf("") }

    // Cannabis calculator states
    var herbGramsStr by remember { mutableStateOf("0.25") }
    var thcPercentStr by remember { mutableStateOf("18.0") }

    // Alcohol calculator states
    var selectedBeverage by remember { mutableStateOf("Piwo") }
    var volumeMlStr by remember { mutableStateOf("500") }
    var alcoholPercentStr by remember { mutableStateOf("5.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Wprowadź dawkę: ${substance?.name ?: ""} (${roa.name})",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isCannabis && !manualMode) {
            // THC Calculator
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🌿 Kalkulator suszu (THC)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Przelicz wagę suszu na czyste mg THC",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = herbGramsStr,
                        onValueChange = { herbGramsStr = it },
                        label = { Text("Waga suszu (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0.1", "0.2", "0.25", "0.5", "1.0").forEach { weight ->
                            FilterChip(
                                selected = herbGramsStr == weight,
                                onClick = { herbGramsStr = weight },
                                label = { Text("${weight}g") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = thcPercentStr,
                        onValueChange = { thcPercentStr = it },
                        label = { Text("Zawartość THC (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("10", "15", "18", "20", "25").forEach { pct ->
                            FilterChip(
                                selected = thcPercentStr == pct,
                                onClick = { thcPercentStr = pct },
                                label = { Text("$pct%") }
                            )
                        }
                    }

                    val herbGrams = herbGramsStr.replace(",", ".").toFloatOrNull() ?: 0f
                    val thcPercent = thcPercentStr.replace(",", ".").toFloatOrNull() ?: 0f
                    val calculatedThcMg = doseConverter.calculateThcMg(herbGrams, thcPercent)
                    val generatedNote = doseConverter.formatThcNote(herbGrams, thcPercent)

                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Dawka: $calculatedThcMg mg THC",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Wpis do dziennika: $generatedNote",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (calculatedThcMg > 0f) {
                                onDoseEntered(calculatedThcMg, generatedNote)
                            }
                        },
                        enabled = calculatedThcMg > 0f,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zatwierdź dawkę ($calculatedThcMg mg)")
                    }

                    TextButton(
                        onClick = { manualMode = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Wpisz mg czystego THC ręcznie")
                    }
                }
            }
        } else if (isAlcohol && !manualMode) {
            // Alcohol Calculator
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🍺 Kalkulator alkoholu (etanol)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Przelicz napój na gramy czystego etanolu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Wybierz trunek:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("Piwo", "500", "5.0"),
                            Triple("Wino", "150", "12.0"),
                            Triple("Shot", "50", "40.0"),
                            Triple("Drink", "250", "8.0"),
                            Triple("Własny", volumeMlStr, alcoholPercentStr)
                        ).forEach { (bev, ml, pct) ->
                            FilterChip(
                                selected = selectedBeverage == bev,
                                onClick = {
                                    selectedBeverage = bev
                                    if (bev != "Własny") {
                                        volumeMlStr = ml
                                        alcoholPercentStr = pct
                                    }
                                },
                                label = { Text(bev) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = volumeMlStr,
                        onValueChange = {
                            volumeMlStr = it
                            if (selectedBeverage != "Własny") selectedBeverage = "Własny"
                        },
                        label = { Text("Objętość (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = alcoholPercentStr,
                        onValueChange = {
                            alcoholPercentStr = it
                            if (selectedBeverage != "Własny") selectedBeverage = "Własny"
                        },
                        label = { Text("Zawartość alkoholu (% vol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val volumeMl = volumeMlStr.replace(",", ".").toFloatOrNull() ?: 0f
                    val alcoholPercent = alcoholPercentStr.replace(",", ".").toFloatOrNull() ?: 0f
                    val calculatedEthanolGrams = doseConverter.calculateEthanolGrams(volumeMl, alcoholPercent)
                    val generatedNote = doseConverter.formatAlcoholNote(selectedBeverage, volumeMl, alcoholPercent)

                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Czysty etanol: $calculatedEthanolGrams g",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val stdDrinks = (calculatedEthanolGrams / 10f * 10f).roundToInt() / 10f
                            Text(
                                text = "Około $stdDrinks standardowych porcji alkoholu (10g)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Wpis do dziennika: $generatedNote",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (calculatedEthanolGrams > 0f) {
                                onDoseEntered(calculatedEthanolGrams, generatedNote)
                            }
                        },
                        enabled = calculatedEthanolGrams > 0f,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zatwierdź dawkę ($calculatedEthanolGrams g)")
                    }

                    TextButton(
                        onClick = { manualMode = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Wpisz gramy etanolu ręcznie")
                    }
                }
            }
        } else {
            // Standard / Manual Dose Entry
            roa.dose?.let { dose ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Sugerowane dawkowanie (${dose.units.ifBlank { "mg" }}):", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            dose.light?.let { Text("Light: $it", style = MaterialTheme.typography.bodySmall) }
                            dose.common?.let { Text("Common: $it", style = MaterialTheme.typography.bodySmall) }
                            dose.strong?.let { Text("Strong: $it", style = MaterialTheme.typography.bodySmall) }
                            dose.heavy?.let { Text("Heavy: $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = manualDoseStr,
                onValueChange = { manualDoseStr = it },
                label = { Text("Dawka (${roa.dose?.units ?: "mg"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    manualDoseStr.replace(",", ".").toFloatOrNull()?.let { onDoseEntered(it, null) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dalej")
            }

            if (isCannabis || isAlcohol) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { manualMode = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Wróć do asystenta kalkulatora")
                }
            }
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
