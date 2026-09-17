package info.hyperreal.journal.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSecurityLockEnabled by viewModel.isSecurityLockEnabled.collectAsState()
    val widgetDiscreteMode by viewModel.widgetDiscreteMode.collectAsState()
    val widgetShowCountdown by viewModel.widgetShowCountdown.collectAsState()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsState()
    val deleteComplete by viewModel.deleteComplete.collectAsState()
    val context = LocalContext.current

    var showSetPinDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinConfirmInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Dialog: Set new PIN
    if (showSetPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showSetPinDialog = false
                pinInput = ""
                pinConfirmInput = ""
                pinError = null
            },
            title = { Text("Ustaw 4-cyfrowy kod PIN") },
            text = {
                Column {
                    Text("Kod PIN będzie wymagany przy otwieraniu aplikacji.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text("Nowy PIN (4 cyfry)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinConfirmInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinConfirmInput = it },
                        label = { Text("Powtórz PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinError = "PIN musi mieć dokładnie 4 cyfry"
                        } else if (pinInput != pinConfirmInput) {
                            pinError = "Podane kody PIN nie są identyczne"
                        } else {
                            viewModel.setPin(pinInput)
                            showSetPinDialog = false
                            pinInput = ""
                            pinConfirmInput = ""
                            pinError = null
                        }
                    }
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSetPinDialog = false
                    pinInput = ""
                    pinConfirmInput = ""
                    pinError = null
                }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Dialog: Disable PIN confirmation
    if (showDisablePinDialog) {
        AlertDialog(
            onDismissRequest = {
                showDisablePinDialog = false
                pinInput = ""
                pinError = null
            },
            title = { Text("Podaj aktualny PIN") },
            text = {
                Column {
                    Text("Wprowadź kod PIN, aby wyłączyć blokadę aplikacji.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text("Aktualny PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.verifyPin(pinInput)) {
                            viewModel.disableSecurityLock()
                            showDisablePinDialog = false
                            pinInput = ""
                            pinError = null
                        } else {
                            pinError = "Nieprawidłowy kod PIN"
                        }
                    }
                ) {
                    Text("Wyłącz blokadę", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDisablePinDialog = false
                    pinInput = ""
                    pinError = null
                }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Wyczyść dane") },
            text = { Text("Czy na pewno chcesz usunąć WSZYSTKIE wpisy z dziennika? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteAll() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Usuń wszystko")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Wygląd", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Dark mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tryb ciemny", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Security
        Text("Bezpieczeństwo i Prywatność", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Blokada aplikacji (PIN / Biometria)", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Wymagaj uwierzytelnienia przy otwarciu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isSecurityLockEnabled,
                onCheckedChange = { enable ->
                    if (enable) {
                        showSetPinDialog = true
                    } else {
                        showDisablePinDialog = true
                    }
                }
            )
        }

        if (isSecurityLockEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showSetPinDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zmień kod PIN")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Widget Personalization
        Text("Personalizacja Widgetu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tryb dyskretny widgetu", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Ukrywa nazwę substancji na ekranie głównym",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = widgetDiscreteMode,
                onCheckedChange = { viewModel.toggleWidgetDiscreteMode(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Odliczanie czasu sesji", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Pokazuj czas od przyjęcia i do zakończenia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = widgetShowCountdown,
                onCheckedChange = { viewModel.toggleWidgetShowCountdown(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Export
        Text("Zarządzanie Danymi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.exportData(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eksportuj dane do JSON")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Delete all
        OutlinedButton(
            onClick = { viewModel.requestDeleteAll() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Wyczyść wszystkie dane")
        }

        if (deleteComplete) {
            Spacer(modifier = Modifier.height(12.dp))
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.dismissDeleteComplete() }) {
                        Text("OK")
                    }
                }
            ) {
                Text("Wszystkie dane zostały usunięte")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // App info
        Text("O aplikacji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hyperreal Journal",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Wersja 1.1.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Aplikacja harm reduction. Wszystkie dane są przechowywane lokalnie na urządzeniu.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "github.com/merx666/hyperreal-journal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
