package com.signify.app.di

import android.content.Context
import androidx.room.Room
import com.signify.app.data.local.AppDatabase
import com.signify.app.data.local.LessonEntity
import com.signify.app.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class AppContainer(context: Context) {
    // 1) Build the Room database (with destructive fallback for now)
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "signify_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    // 2) Grab your DAOs
    private val lessonDao  = db.lessonDao()
    private val historyDao = db.historyDao()

    // 3) Instantiate your repositories
    val lessonRepository     = LessonRepositoryImpl(lessonDao)
    val historyRepository    = HistoryRepositoryImpl(historyDao)
    val translatorRepository = TranslatorRepositoryImpl()

    // In AppContainer.kt, right after you construct your repositories:



}
