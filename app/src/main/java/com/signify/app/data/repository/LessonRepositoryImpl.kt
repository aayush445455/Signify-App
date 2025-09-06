package com.signify.app.data.repository

import com.signify.app.data.local.LessonDao
import com.signify.app.data.local.LessonEntity
import kotlinx.coroutines.flow.Flow

class LessonRepositoryImpl(
    private val lessonDao: LessonDao
) : LessonRepository {

    override fun getAllLessons(): Flow<List<LessonEntity>> =
        lessonDao.getAllLessons()

    override suspend fun insertLessons(lessons: List<LessonEntity>) {
        lessonDao.insertLessons(lessons)
    }
}
