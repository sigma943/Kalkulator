package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.LocalCalculatorPalette

@Composable
fun TopNavTabBar(
    modifier: Modifier = Modifier,
    currentScreen: Screen,
    onTabSelect: (Screen) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenThemes: (() -> Unit)? = null,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    hasHistory: Boolean = false,
    onClearHistory: (() -> Unit)? = null
) {
    val palette = LocalCalculatorPalette.current
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Segmented Tabs: Kalkulator | Przelicznik
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(palette.numberBtn.copy(alpha = 0.7f))
                .border(1.dp, palette.cardBorder, RoundedCornerShape(22.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Kalkulator
                val isCalc = (currentScreen == Screen.Calculator)
                val calcBg by animateColorAsState(
                    targetValue = if (isCalc) palette.accentEquals else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "calcBg"
                )
                val calcTextColor by animateColorAsState(
                    targetValue = if (isCalc) palette.equalsText else palette.tabUnselectedText,
                    animationSpec = tween(durationMillis = 200),
                    label = "calcText"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(calcBg)
                        .clickable { onTabSelect(Screen.Calculator) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = null,
                            tint = calcTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Kalkulator",
                            fontSize = 13.sp,
                            fontWeight = if (isCalc) FontWeight.Bold else FontWeight.Medium,
                            color = calcTextColor
                        )
                    }
                }

                // Tab 2: Przelicznik jednostek
                val isConv = (currentScreen == Screen.UnitConverter)
                val convBg by animateColorAsState(
                    targetValue = if (isConv) palette.accentEquals else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "convBg"
                )
                val convTextColor by animateColorAsState(
                    targetValue = if (isConv) palette.equalsText else palette.tabUnselectedText,
                    animationSpec = tween(durationMillis = 200),
                    label = "convText"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(convBg)
                        .clickable { onTabSelect(Screen.UnitConverter) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = convTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Przelicznik",
                            fontSize = 13.sp,
                            fontWeight = if (isConv) FontWeight.Bold else FontWeight.Medium,
                            color = convTextColor
                        )
                    }
                }
            }
        }

        // Action Buttons: Historia, Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Shortcut: Historia
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("top_btn_history")
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "Historia obliczeń",
                    tint = palette.numberText.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Dropdown Menu ⋮
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_btn_more")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Więcej opcji",
                        tint = palette.numberText.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Historia obliczeń") },
                        leadingIcon = {
                            Icon(Icons.Outlined.History, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenHistory()
                        }
                    )
                    if (hasHistory && onClearHistory != null) {
                        DropdownMenuItem(
                            text = { Text("Wyczyść historię") },
                            leadingIcon = {
                                Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onClearHistory()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Ustawienia") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Settings, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenSettings()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("O aplikacji") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Info, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenAbout()
                        }
                    )
                }
            }
        }
    }
}
