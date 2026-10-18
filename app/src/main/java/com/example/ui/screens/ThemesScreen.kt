package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccentTheme
import com.example.data.SettingsManager
import com.example.data.ThemeMode
import com.example.ui.theme.LocalCalculatorPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val palette = LocalCalculatorPalette.current
    val settings by settingsManager.settings.collectAsState()

    val currentAccent = settings.accentTheme
    val currentMode = settings.themeMode

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Motyw i styl",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("themes_btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Powrót"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SEKCJA 1: TRYB MOTYWU
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tryb motywu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.accentEquals
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(ThemeMode.SYSTEM, "System", Icons.Outlined.SettingsBrightness),
                        Triple(ThemeMode.LIGHT, "Jasny", Icons.Outlined.LightMode),
                        Triple(ThemeMode.DARK, "Ciemny", Icons.Outlined.DarkMode)
                    ).forEach { (mode, label, icon) ->
                        val isSelected = (currentMode == mode)
                        val cardBg by animateColorAsState(
                            targetValue = if (isSelected) Color(currentAccent.hex).copy(alpha = 0.20f) else palette.cardBg,
                            animationSpec = tween(200),
                            label = "modeBg"
                        )
                        val borderCol by animateColorAsState(
                            targetValue = if (isSelected) Color(currentAccent.hex) else palette.cardBorder.copy(alpha = 0.4f),
                            animationSpec = tween(200),
                            label = "modeBorder"
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { settingsManager.updateThemeMode(mode) }
                                .border(if (isSelected) 1.5.dp else 1.dp, borderCol, RoundedCornerShape(16.dp)),
                            color = cardBg,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color(currentAccent.hex) else palette.numberText.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(currentAccent.hex) else palette.numberText
                                )
                            }
                        }
                    }
                }
            }

            // SEKCJA 2: KOLOR WIODĄCY (SIATKA KROPEK W STYLU MATERIAL YOU)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kolor wiodący",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.accentEquals
                    )
                    Text(
                        text = currentAccent.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.numberText.copy(alpha = 0.7f)
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(AccentTheme.values()) { accent ->
                        val isSelected = (currentAccent == accent)
                        val accentColor = Color(accent.hex)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { settingsManager.updateAccentTheme(accent) }
                                .padding(vertical = 6.dp, horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) palette.numberText else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Wybrano",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                text = accent.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) palette.numberText else palette.numberText.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
