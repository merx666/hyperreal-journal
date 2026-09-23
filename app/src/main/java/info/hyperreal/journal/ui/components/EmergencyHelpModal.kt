package info.hyperreal.journal.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.hyperreal.journal.ui.theme.HyperrealTokens

data class EmergencyContact(
    val title: String,
    val subtitle: String,
    val phoneNumber: String,
    val isEmergency112: Boolean = false
)

val EmergencyContactsList = listOf(
    EmergencyContact(
        title = "Numer Ratunkowy (Pogotowie)",
        subtitle = "zagrożenie życia, ZRM, przedawkowanie",
        phoneNumber = "112",
        isEmergency112 = true
    ),
    EmergencyContact(
        title = "Interwencja kryzysowa",
        subtitle = "os. w trudnej sytuacji",
        phoneNumber = "514 202 619"
    ),
    EmergencyContact(
        title = "Kryzys samobójczy",
        subtitle = "osoby dorosłe",
        phoneNumber = "511 200 200"
    ),
    EmergencyContact(
        title = "Telefon Zaufania",
        subtitle = "wsparcie psychologiczne",
        phoneNumber = "116 123"
    ),
    EmergencyContact(
        title = "Dla dzieci i młodzieży",
        subtitle = "wsparcie dla osób poniżej 18 r.ż.",
        phoneNumber = "116 111"
    )
)

/**
 * Prominent top bar button triggering the 24H Emergency Assistance modal.
 */
@Composable
fun EmergencyHelpTopBarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = HyperrealTokens.TelemetryDanger.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, HyperrealTokens.TelemetryDanger.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(HyperrealTokens.TelemetryDanger, CircleShape)
            )
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = HyperrealTokens.TelemetryDanger,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "NAGŁA POMOC 24H",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * BottomSheet displaying crisis contact list with mandatory confirmation dialog before dialing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyHelpBottomSheet(
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var contactToConfirm by remember { mutableStateOf<EmergencyContact?>(null) }

    // Mandatory Call Confirmation Dialog
    if (contactToConfirm != null) {
        val contact = contactToConfirm!!
        AlertDialog(
            onDismissRequest = { contactToConfirm = null },
            containerColor = HyperrealTokens.SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = HyperrealTokens.TextPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = HyperrealTokens.TelemetryDanger,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Potwierdzenie połączenia",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Czy na pewno chcesz zadzwonić do:\n\n${contact.title}?\n\nNumer: ${contact.phoneNumber}",
                        fontSize = 14.sp,
                        color = HyperrealTokens.TextPrimary
                    )
                    if (contact.isEmergency112) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Połączenie z numerem alarmowym 112 natychmiast powiadamia dyspozytora Zespołu Ratownictwa Medycznego.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HyperrealTokens.TelemetryWarning
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = contact
                        contactToConfirm = null
                        dialNumber(context, target.phoneNumber)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HyperrealTokens.TelemetryDanger,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Zadzwoń", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { contactToConfirm = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = HyperrealTokens.TextSecondary
                    )
                ) {
                    Text("Anuluj")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = HyperrealTokens.SurfaceDark,
        contentColor = HyperrealTokens.TextPrimary,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with red phone icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = HyperrealTokens.TelemetryDanger,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "POMOC KRYZYSOWA 24H",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = HyperrealTokens.TelemetryDanger,
                    letterSpacing = 0.8.sp
                )
            }

            // Accent dividing line (cyan/sky tint from original screenshot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFF38BDF8).copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Info banner about medical emergency
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = HyperrealTokens.SurfaceRaised,
                border = BorderStroke(1.dp, HyperrealTokens.BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = HyperrealTokens.TelemetryWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "W sytuacji bezpośredniego zagrożenia życia, utraty przytomności lub drgawek natychmiast wybierz 112.",
                        fontSize = 12.sp,
                        color = HyperrealTokens.TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact list
            EmergencyContactsList.forEachIndexed { index, contact ->
                EmergencyContactRow(
                    contact = contact,
                    onCallClick = { contactToConfirm = contact }
                )
                if (index < EmergencyContactsList.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = HyperrealTokens.BorderSubtle,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactRow(
    contact: EmergencyContact,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = contact.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = contact.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = HyperrealTokens.TextSecondary
            )
        }

        // Red pill button matching user's screenshot
        Button(
            onClick = onCallClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = HyperrealTokens.TelemetryDanger,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = contact.phoneNumber,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

private fun dialNumber(context: Context, phoneNumber: String) {
    val clean = phoneNumber.replace(" ", "")
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
