// File: app/src/main/java/com/signify/app/di/SignifyViewModelFactory.kt
package com.signify.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.signify.app.auth.repository.AuthRepository
import com.signify.app.auth.viemodel.AuthViewModel
import com.signify.app.data.repository.*
import com.signify.app.history.viewmodel.HistoryViewModel
import com.signify.app.learn.viewmodel.LessonViewModel
import com.signify.app.translator.viewmodel.TranslatorViewModel

class SignifyViewModelFactory(
    private val authRepo       : AuthRepository,
    private val lessonRepo     : LessonRepository,
    private val historyRepo    : HistoryRepository,
    private val translatorRepo : TranslatorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(authRepo) as T

        modelClass.isAssignableFrom(LessonViewModel::class.java) ->
            LessonViewModel(lessonRepo) as T

        modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
            HistoryViewModel(historyRepo) as T

        modelClass.isAssignableFrom(TranslatorViewModel::class.java) ->
            TranslatorViewModel(translatorRepo, historyRepo) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
