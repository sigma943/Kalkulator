package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccentTheme
import com.example.data.SettingsManager
import com.example.data.ThemeMode

@Composable
fun ThemeSelectionDialog(
    settingsManager: SettingsManager,
    onDismiss: () -> Unit
) {
    val settings by settingsManager.settings.collectAsState()
    var selectedMode by remember { mutableStateOf(settings.themeMode) }
    var selectedAccent by remember { mutableStateOf(settings.accentTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Wybierz motyw",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Radio buttons for ThemeMode
                listOf(
                    ThemeMode.SYSTEM to "Domyślny systemu",
                    ThemeMode.LIGHT to "Jasny",
                    ThemeMode.DARK to "Ciemny"
                ).forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedMode = mode }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedMode == mode),
                            onClick = { selectedMode = mode }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = label, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Kolor wiodący:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Swatches row
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf(
                        AccentTheme.TEAL,
                        AccentTheme.BLUE,
                        AccentTheme.INDIGO,
                        AccentTheme.CORAL,
                        AccentTheme.EMERALD,
                        AccentTheme.VIOLET,
                        AccentTheme.CRIMSON,
                        AccentTheme.AMBER,
                        AccentTheme.PINK,
                        AccentTheme.MINT,
                        AccentTheme.CYBER_CYAN,
                        AccentTheme.SUNSET_GOLD
                    )) { accent ->
                        val isAccentSelected = (selectedAccent == accent)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(accent.hex))
                                .clickable { selectedAccent = accent }
                                .border(
                                    width = if (isAccentSelected) 2.5.dp else 0.dp,
                                    color = if (isAccentSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAccentSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    settingsManager.updateThemeMode(selectedMode)
                    settingsManager.updateAccentTheme(selectedAccent)
                    onDismiss()
                }
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Polityka prywatności", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Obliczenia, historia i przeliczanie jednostek działają lokalnie. Przelicznik walut łączy się przez HTTPS z api.nbp.pl, aby pobrać tabelę kursów. Nie wysyła wpisanych kwot ani historii obliczeń. Dostawca usługi widzi standardowe dane połączenia, w tym adres IP.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = "Historia obliczeń oraz ustawienia motywu są zapisywane w bezpiecznej bazie danych urządzenia i mogą zostać w każdej chwili wyczyszczone.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Rozumiem")
            }
        }
    )
}

@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Prześlij opinię", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Opisz swoje uwagi lub zgłoś propozycję ulepszenia kalkulatora:",
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text("Wpisz swoją opinię...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Toast.makeText(context, "Dziękujemy za przesłanie opinii!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Wyślij")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pomoc", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Podstawowe wskazówki:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "• Przeciągnij w dół z góry ekranu lub kliknij ikonę zegara, aby otworzyć historię.\n• Kliknij ikonę ze strzałkami (⌃/⌄), aby rozwinąć funkcje naukowe (sin, cos, pierwiastki, logarytmy).\n• Dłuższe przytrzymanie przycisku AC lub Backspace czyści całe wyrażenie.\n• Procenty: 200 + 10% = 220, 200 × 10% = 20.\n• Po wyniku cyfra zaczyna nowe działanie, a operator kontynuuje obliczenia.\n• 🐗 Sekret: Przytrzymaj znak '=', aby obudzić bestię...",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    PrivacyPolicyDialog(onDismiss = onDismiss)
}

