package info.hyperreal.journal.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.R
import info.hyperreal.journal.ui.theme.HyperrealTokens

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
    val uriHandler = LocalUriHandler.current
    val sponsorUrl = stringResource(R.string.sponsor_url)

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
            title = { Text(stringResource(R.string.pin_set_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.pin_set_desc), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text(stringResource(R.string.pin_new)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinConfirmInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinConfirmInput = it },
                        label = { Text(stringResource(R.string.pin_confirm)) },
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
                            pinError = context.getString(R.string.pin_error_length)
                        } else if (pinInput != pinConfirmInput) {
                            pinError = context.getString(R.string.pin_error_mismatch)
                        } else {
                            viewModel.setPin(pinInput)
                            showSetPinDialog = false
                            pinInput = ""
                            pinConfirmInput = ""
                            pinError = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.pin_save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSetPinDialog = false
                    pinInput = ""
                    pinConfirmInput = ""
                    pinError = null
                }) {
                    Text(stringResource(R.string.pin_cancel))
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
            title = { Text(stringResource(R.string.pin_disable_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.pin_disable_desc), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinInput = it },
                        label = { Text(stringResource(R.string.pin_current)) },
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
                            pinError = context.getString(R.string.pin_error_invalid)
                        }
                    }
                ) {
                    Text(stringResource(R.string.pin_disable_action), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDisablePinDialog = false
                    pinInput = ""
                    pinError = null
                }) {
                    Text(stringResource(R.string.pin_cancel))
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteAll() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text(stringResource(R.string.pin_cancel))
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
        Text(stringResource(R.string.settings_section_appearance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Dark mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.settings_dark_mode), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Security
        Text(stringResource(R.string.settings_section_security), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_security_lock), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.settings_security_lock_desc),
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
                Text(stringResource(R.string.settings_change_pin))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Widget Personalization
        Text(stringResource(R.string.settings_section_widget), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_widget_discrete), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.settings_widget_discrete_desc),
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
                Text(stringResource(R.string.settings_widget_countdown), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.settings_widget_countdown_desc),
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

        // Export / Data
        Text(stringResource(R.string.settings_section_data), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.exportData(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_export))
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
            Text(stringResource(R.string.settings_delete_all))
        }

        if (deleteComplete) {
            Spacer(modifier = Modifier.height(12.dp))
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.dismissDeleteComplete() }) {
                        Text(stringResource(R.string.action_ok))
                    }
                }
            ) {
                Text(stringResource(R.string.settings_delete_complete))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // About
        Text(stringResource(R.string.settings_section_about), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_version),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_about_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "github.com/merx666/hyperreal-journal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // ── Sponsorship Card ──────────────────────────────────────────────
        SponsorCard(onSponsorClick = { uriHandler.openUri(sponsorUrl) })

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SponsorCard(onSponsorClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HyperrealTokens.SurfaceRaised
        ),
        border = BorderStroke(1.dp, HyperrealTokens.BorderHighlight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = HyperrealTokens.TelemetryDanger,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.sponsor_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.sponsor_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSponsorClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HyperrealTokens.TelemetrySafe
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.sponsor_button),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
