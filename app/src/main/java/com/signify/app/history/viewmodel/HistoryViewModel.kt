// HistoryViewModel.kt
package com.signify.app.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signify.app.data.repository.HistoryRepository
import com.signify.app.domain.model.TranslationHistory
import com.signify.app.domain.model.toDomain
import com.signify.app.domain.model.toEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepo: HistoryRepository
) : ViewModel() {

    private val _history = MutableStateFlow<List<TranslationHistory>>(emptyList())
    val history: StateFlow<List<TranslationHistory>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepo.getAllHistory()
                .map { list -> list.map { it.toDomain() } }
                .collect { domainList ->
                    _history.value = domainList
                }
        }
    }

    /** Add a new translation to the history table */
    fun addHistory(entry: TranslationHistory) {
        viewModelScope.launch {
            historyRepo.insertHistory(entry.toEntity())
        }
    }
}
