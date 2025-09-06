// LessonViewModel.kt
package com.signify.app.learn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signify.app.data.repository.LessonRepository
import com.signify.app.domain.model.Lesson
import com.signify.app.domain.model.toDomain
import com.signify.app.domain.model.toEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LessonViewModel(
    private val lessonRepo: LessonRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    init {
        viewModelScope.launch {
            lessonRepo.getAllLessons()
                .map { list -> list.map { it.toDomain() } }
                .collect { domainList ->
                    _lessons.value = domainList
                }
        }
    }

    /** Call this to replace the lesson set in the DB */
    fun insertLessons(newLessons: List<Lesson>) {
        viewModelScope.launch {
            lessonRepo.insertLessons(newLessons.map { it.toEntity() })
        }
    }
}
