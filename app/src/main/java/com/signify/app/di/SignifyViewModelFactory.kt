// com/signify/app/di/SignifyViewModelFactory.kt
package com.signify.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.signify.app.history.viewmodel.HistoryViewModel
import com.signify.app.learn.viewmodel.LessonViewModel
import com.signify.app.translator.viewmodel.TranslatorViewModel
import com.signify.app.data.repository.LessonRepository
import com.signify.app.data.repository.HistoryRepository
import com.signify.app.data.repository.TranslatorRepository

class SignifyViewModelFactory(
    private val lessonRepo: LessonRepository,
    private val historyRepo: HistoryRepository,
    private val translatorRepo: TranslatorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LessonViewModel::class.java) ->
                LessonViewModel(lessonRepo) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(historyRepo) as T

            modelClass.isAssignableFrom(TranslatorViewModel::class.java) ->
                TranslatorViewModel(translatorRepo, historyRepo) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
    }
}
