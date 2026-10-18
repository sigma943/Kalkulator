package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.converter.UnitConverterScreen
import com.example.data.CalculatorDatabase
import com.example.data.CalculatorRepository
import com.example.data.SettingsManager
import com.example.ui.CalculatorViewModel
import com.example.ui.CalculatorViewModelFactory
import com.example.ui.Screen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private val database by lazy { CalculatorDatabase.getDatabase(applicationContext) }
    private val repository by lazy { CalculatorRepository(database.historyDao(), settingsManager) }

    private val viewModel: CalculatorViewModel by viewModels {
        CalculatorViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsManager.settings.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val historyList by viewModel.historyList.collectAsState()

            MyApplicationTheme(
                themeMode = settings.themeMode,
                accentTheme = settings.accentTheme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == Screen.Calculator) {
                                (slideInHorizontally(tween(260)) { -it } + fadeIn(tween(200)))
                                    .togetherWith(slideOutHorizontally(tween(260)) { it } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(tween(260)) { it } + fadeIn(tween(200)))
                                    .togetherWith(slideOutHorizontally(tween(260)) { -it } + fadeOut(tween(200)))
                            }
                        },
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            Screen.Calculator -> {
                                CalculatorScreen(viewModel = viewModel)
                            }
                            Screen.UnitConverter -> {
                                UnitConverterScreen(
                                    onTabSelect = { targetScreen ->
                                        viewModel.navigateTo(targetScreen)
                                    },
                                    onOpenHistory = {
                                        viewModel.navigateTo(Screen.History)
                                    },
                                    onOpenThemes = {
                                        viewModel.navigateTo(Screen.Themes)
                                    },
                                    onOpenSettings = {
                                        viewModel.navigateTo(Screen.Settings)
                                    },
                                    onBack = { viewModel.navigateBack() },
                                    vibrateEnabled = settings.vibrateOnClick,
                                    soundEnabled = settings.soundOnClick,
                                    settingsManager = settingsManager
                                )
                            }
                            Screen.Themes -> {
                                ThemesScreen(
                                    settingsManager = settingsManager,
                                    onBack = { viewModel.navigateBack() }
                                )
                            }
                            Screen.History -> {
                                HistoryScreen(
                                    historyList = historyList,
                                    onBack = { viewModel.navigateBack() },
                                    onSelectExpression = { item ->
                                        viewModel.onSelectHistory(item, useResultOnly = false)
                                    },
                                    onSelectResult = { item ->
                                        viewModel.onSelectHistory(item, useResultOnly = true)
                                    },
                                    onDeleteItem = { id ->
                                        viewModel.onDeleteHistory(id)
                                    },
                                    onClearAll = {
                                        viewModel.onClearAllHistory()
                                    }
                                )
                            }
                            Screen.Settings -> {
                                SettingsScreen(
                                    settingsManager = settingsManager,
                                    onOpenThemes = {
                                        viewModel.navigateTo(Screen.Themes)
                                    },
                                    onBack = { viewModel.navigateBack() },
                                    onClearHistory = {
                                        viewModel.onClearAllHistory()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
