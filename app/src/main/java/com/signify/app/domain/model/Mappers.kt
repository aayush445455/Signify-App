package com.signify.app.domain.model

import com.signify.app.data.local.LessonEntity
import com.signify.app.data.local.TranslationHistoryEntity

// Lesson conversions
fun LessonEntity.toDomain(): Lesson =
    Lesson(
        id = this.id,
        title = this.title,
        content = this.description      // map description → content
    )

fun Lesson.toEntity(): LessonEntity =
    LessonEntity(
        id = this.id,
        title = this.title,
        description = this.content      // map content → description
    )

// TranslationHistory conversions
fun TranslationHistoryEntity.toDomain(): TranslationHistory =
    TranslationHistory(
        id = this.id,
        inputText = this.inputText,
        outputText = this.outputText,
        timestamp = this.timestamp
    )

fun TranslationHistory.toEntity(): TranslationHistoryEntity =
    TranslationHistoryEntity(
        id = this.id,
        inputText = this.inputText,
        outputText = this.outputText,
        timestamp = this.timestamp
    )
