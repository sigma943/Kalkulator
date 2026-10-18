package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculatorRepository(
    private val historyDao: HistoryDao,
    val settingsManager: SettingsManager
) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun saveHistory(expression: String, result: String) {
        if (settingsManager.settings.value.saveHistory) {
            historyDao.insert(HistoryEntity(expression = expression, result = result))
        }
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        historyDao.clearAll()
    }
}
