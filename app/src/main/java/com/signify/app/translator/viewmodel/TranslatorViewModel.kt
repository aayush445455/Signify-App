// TranslatorViewModel.kt
package com.signify.app.translator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signify.app.data.local.TranslationHistoryEntity
import com.signify.app.data.repository.HistoryRepository
import com.signify.app.data.repository.TranslatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val translatorRepo: TranslatorRepository,
    private val historyRepo: HistoryRepository
) : ViewModel() {

    private val _translationResult = MutableStateFlow<String>("")
    val translationResult: StateFlow<String> = _translationResult.asStateFlow()

    /** Call this when you have a list of detected sign-codes */
    fun translateSignCodes(codes: List<String>) {
        viewModelScope.launch {
            val text = translatorRepo.signToText(codes)
            _translationResult.value = text

            // persist to history
            val entry = TranslationHistoryEntity(
                inputText = codes.joinToString(","),
                outputText = text,
                timestamp = System.currentTimeMillis()
            )
            historyRepo.insertHistory(entry)
        }
    }

    /** (Optional) if you ever want text→sign or speech→sign in the same ViewModel */
    fun translateText(text: String) {
        viewModelScope.launch {
            val signs = translatorRepo.textToSign(text)
            _translationResult.value = signs.joinToString(" ")
        }
    }
}
