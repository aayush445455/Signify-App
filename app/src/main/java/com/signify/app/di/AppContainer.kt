// File: app/src/main/java/com/signify/app/di/AppContainer.kt
package com.signify.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.signify.app.auth.repository.AuthRepository
import com.signify.app.auth.repository.FirebaseAuthRepository
import com.signify.app.data.local.AppDatabase
import com.signify.app.data.repository.*

class AppContainer(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "signify_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    val lessonRepository     : LessonRepository     = LessonRepositoryImpl(db.lessonDao())
    val historyRepository    : HistoryRepository    = HistoryRepositoryImpl(db.historyDao())
    val translatorRepository : TranslatorRepository = TranslatorRepositoryImpl()

    private val firebaseAuth = FirebaseAuth.getInstance()
    val authRepository       : AuthRepository       = FirebaseAuthRepository(firebaseAuth)

    // one factory for _all_ your ViewModels
    val viewModelFactory = SignifyViewModelFactory(
        authRepository,
        lessonRepository,
        historyRepository,
        translatorRepository
    )
}
