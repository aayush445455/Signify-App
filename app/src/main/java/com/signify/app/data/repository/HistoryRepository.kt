package com.signify.app.data.repository

import com.signify.app.data.local.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    /** Stream all translation history entries. */
    fun getAllHistory(): Flow<List<TranslationHistoryEntity>>

    /** Add a new history entry. */
    suspend fun insertHistory(entry: TranslationHistoryEntity)
}
