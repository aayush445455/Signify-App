package com.signify.app.data.repository

import com.signify.app.data.local.LessonEntity
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    /** Stream all lessons from the local cache. */
    fun getAllLessons(): Flow<List<LessonEntity>>

    /** Replace the current set of lessons with this new list. */
    suspend fun insertLessons(lessons: List<LessonEntity>)
}
