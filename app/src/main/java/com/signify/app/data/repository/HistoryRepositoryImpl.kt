package com.signify.app.data.repository

import com.signify.app.data.local.HistoryDao
import com.signify.app.data.local.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<TranslationHistoryEntity>> =
        historyDao.getAllHistory()

    override suspend fun insertHistory(entry: TranslationHistoryEntity) {
        historyDao.insertHistory(entry)
    }
}
