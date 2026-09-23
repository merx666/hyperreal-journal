package info.hyperreal.journal.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.hyperreal.journal.R
import info.hyperreal.journal.ui.theme.HyperrealTokens

@Composable
fun OnboardingScreen(
    onAccept: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HyperrealTokens.Canvas
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 36.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Official Logo & Halo
            item {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    HyperrealTokens.BrandGreen.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Hyperreal Journal Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, HyperrealTokens.BrandGreen.copy(alpha = 0.5f), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HYPERREAL DZIENNIK",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Harm Reduction • Bio-Telemetry • Prywatność",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HyperrealTokens.BrandGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Core Pillars of the App
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PillarCard(
                        icon = Icons.Default.Lock,
                        title = "100% Prywatności Offline",
                        description = "Zero serwerów, zerowej analityki i zerowego śledzenia. Baza danych oraz Twoje wpisy pozostają wyłącznie na tym telefonie."
                    )
                    PillarCard(
                        icon = Icons.Default.Info,
                        title = "Bio-Timeline & Fazy Działania",
                        description = "Kalkulator dawek, monitorowanie etapów farmakokinetyki (Onset, Peak, Offset) oraz notatki wg skali Shulgina."
                    )
                    PillarCard(
                        icon = Icons.Default.Warning,
                        title = "Macierz Interakcji SIN",
                        description = "Oficjalna wiedza Społecznej Inicjatywy Narkopolityki o synergii i niebezpiecznych połączeniach substancji."
                    )
                    PillarCard(
                        icon = Icons.Default.Phone,
                        title = "Wsparcie Kryzysowe 24H",
                        description = "Szybki dostęp z każdego ekranu do numeru ratunkowego 112 oraz całodobowych linii interwencji psychologicznej."
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Official 18+ Harm Reduction Disclaimer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = HyperrealTokens.SurfaceDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HyperrealTokens.TelemetryDanger.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(HyperrealTokens.TelemetryDanger.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "18+",
                                    color = HyperrealTokens.TelemetryDanger,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Oświadczenie & Zasady Użytkowania",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DisclaimerBullet(
                            number = "1.",
                            text = "Aplikacja jest przeznaczona wyłącznie dla osób pełnoletnich (ukończone 18 lat)."
                        )
                        DisclaimerBullet(
                            number = "2.",
                            text = "Aplikacja służy wyznaczaniu parametrów redukcji szkód (harm reduction) i edukacji. Twórcy w żaden sposób nie zachęcają, nie promują ani nie nakłaniają do zażywania jakichkolwiek substancji."
                        )
                        DisclaimerBullet(
                            number = "3.",
                            text = "Prezentowane dane dawkowania i interakcji mają charakter wyłącznie poglądowo-statystyczny i NIE stanowią porady lekarskiej ani diagnozy medycznej."
                        )
                        DisclaimerBullet(
                            number = "4.",
                            text = "Użytkownik przyjmuje do wiadomości, że za wszelkie decyzje dotyczące swojego zdrowia i bezpieczeństwa ponosi wyłączną i osobistą odpowiedzialność."
                        )
                        DisclaimerBullet(
                            number = "5.",
                            text = "W sytuacji bezpośredniego zagrożenia życia, utraty przytomności lub zespołu serotoninowego należy natychmiast wezwać Pogotowie Ratunkowe (112)."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // 4. Accept CTA Button
            item {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAccept()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HyperrealTokens.BrandGreen,
                        contentColor = Color(0xFF07080B)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF07080B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rozumiem i akceptuję oświadczenie",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF07080B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aplikacja Open-Source • Bezpieczeństwo i Redukcja Szkód",
                    fontSize = 11.sp,
                    color = HyperrealTokens.TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PillarCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HyperrealTokens.SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, HyperrealTokens.BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(HyperrealTokens.SurfaceRaised, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HyperrealTokens.BrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = HyperrealTokens.TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun DisclaimerBullet(
    number: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontWeight = FontWeight.Bold,
            color = HyperrealTokens.TelemetryDanger,
            fontSize = 12.sp,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = HyperrealTokens.TextSecondary,
            lineHeight = 17.sp
        )
    }
}
