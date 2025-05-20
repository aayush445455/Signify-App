package com.signify.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LessonEntity::class, TranslationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun historyDao(): HistoryDao
}
