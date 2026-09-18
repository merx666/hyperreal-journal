package info.hyperreal.journal.ui.substances

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import info.hyperreal.journal.domain.model.DurationParameters

object CustomSubstanceDurationHelper {
    fun parseValue(raw: String?, isHours: Boolean): Float? {
        if (raw.isNullOrBlank()) return null
        val normalized = raw.trim().replace(',', '.')
        val parsed = normalized.toFloatOrNull() ?: return null
        if (parsed <= 0f) return null
        return if (isHours) parsed * 60f else parsed
    }

    fun calculateDuration(
        isHours: Boolean,
        onset: String?,
        comeup: String?,
        peak: String?,
        offset: String?,
        afterglow: String?,
        customTotal: String? = null
    ): DurationParameters {
        val o = parseValue(onset, isHours)
        val c = parseValue(comeup, isHours)
        val p = parseValue(peak, isHours)
        val of = parseValue(offset, isHours)
        val a = parseValue(afterglow, isHours)
        val t = parseValue(customTotal, isHours)

        val sum = listOfNotNull(o, c, p, of, a).sum()
        val total = t ?: (if (sum > 0f) sum else null)

        return DurationParameters(
            onset = o,
            comeup = c,
            peak = p,
            offset = of,
            afterglow = a,
            total = total
        )
    }

    fun formatMinutesHuman(minutes: Float): String {
        return if (minutes >= 60f) {
            val hours = minutes / 60f
            if (hours % 1f == 0f) {
                "${hours.toInt()} godz."
            } else {
                String.format(java.util.Locale.US, "%.1f godz.", hours)
            }
        } else {
            "${minutes.toInt()} min."
        }
    }
}

@Composable
fun AddCustomSubstanceDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (name: String, roaName: String, duration: DurationParameters) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var roaName by remember { mutableStateOf("Doustnie") }
    var isCustomRoa by remember { mutableStateOf(false) }
    var customRoaInput by remember { mutableStateOf("") }

    var isHours by remember { mutableStateOf(false) }
    var onsetInput by remember { mutableStateOf("") }
    var comeupInput by remember { mutableStateOf("") }
    var peakInput by remember { mutableStateOf("") }
    var offsetInput by remember { mutableStateOf("") }
    var afterglowInput by remember { mutableStateOf("") }

    val standardRoas = listOf("Doustnie", "Donosowo", "Waporyzacja", "Podjęzykowo", "Inna")

    val duration = CustomSubstanceDurationHelper.calculateDuration(
        isHours = isHours,
        onset = onsetInput,
        comeup = comeupInput,
        peak = peakInput,
        offset = offsetInput,
        afterglow = afterglowInput
    )

    val isValid = name.isNotBlank() && (duration.total ?: 0f) > 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Dodaj własną substancję",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Dane wymagane wyłącznie do wygenerowania wykresu działania w czasie:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa substancji *") },
                    placeholder = { Text("np. 2-FMA, Kava, Modafinil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Droga podania (ROA):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    standardRoas.forEach { roa ->
                        val selected = if (roa == "Inna") isCustomRoa else (roaName == roa && !isCustomRoa)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (roa == "Inna") {
                                    isCustomRoa = true
                                } else {
                                    isCustomRoa = false
                                    roaName = roa
                                }
                            },
                            label = { Text(roa) }
                        )
                    }
                }

                if (isCustomRoa) {
                    OutlinedTextField(
                        value = customRoaInput,
                        onValueChange = { customRoaInput = it },
                        label = { Text("Wpisz drogę podania") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jednostka faz:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isHours,
                            onClick = { isHours = false },
                            label = { Text("Minuty (min)") }
                        )
                        FilterChip(
                            selected = isHours,
                            onClick = { isHours = true },
                            label = { Text("Godziny (h)") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val unitLabel = if (isHours) "godz." else "min."

                OutlinedTextField(
                    value = onsetInput,
                    onValueChange = { onsetInput = it },
                    label = { Text("Wejście / Onset ($unitLabel)") },
                    placeholder = { Text(if (isHours) "np. 0.5" else "np. 30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comeupInput,
                    onValueChange = { comeupInput = it },
                    label = { Text("Ładowanie / Comeup ($unitLabel)") },
                    placeholder = { Text(if (isHours) "np. 1.0" else "np. 60") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = peakInput,
                    onValueChange = { peakInput = it },
                    label = { Text("Szczyt / Peak ($unitLabel)") },
                    placeholder = { Text(if (isHours) "np. 2.0" else "np. 120") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = offsetInput,
                    onValueChange = { offsetInput = it },
                    label = { Text("Zejście / Offset ($unitLabel)") },
                    placeholder = { Text(if (isHours) "np. 1.5" else "np. 90") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = afterglowInput,
                    onValueChange = { afterglowInput = it },
                    label = { Text("Afterglow / Powrót ($unitLabel)") },
                    placeholder = { Text(if (isHours) "np. 1.0" else "np. 60") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val totalMin = duration.total ?: 0f
                    val summaryText = if (totalMin > 0f) {
                        "Szacowany łączny czas: ${CustomSubstanceDurationHelper.formatMinutesHuman(totalMin)}"
                    } else {
                        "Wprowadź przynajmniej jedną fazę działania"
                    }
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalRoa = if (isCustomRoa) customRoaInput.ifBlank { "Inna" } else roaName
                    onConfirm(name, finalRoa, duration)
                },
                enabled = isValid
            ) {
                Text("Zapisz substancję")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
