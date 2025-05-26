// File: app/src/main/java/com/signify/app/learn/ui/LessonsScreen.kt
package com.signify.app.learn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.di.SignifyViewModelFactory
import com.signify.app.learn.viewmodel.LessonViewModel
import com.signify.app.domain.model.Lesson as DomainLesson

@Composable
fun LessonsScreen(container: AppContainer) {
    // 1) ViewModel
    val factory = remember {
        SignifyViewModelFactory(
            container.lessonRepository,
            container.historyRepository,
            container.translatorRepository
        )
    }
    val vm: LessonViewModel = viewModel(factory = factory)
    val lessons by vm.lessons.collectAsState(initial = emptyList())

    // 2) UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lessons", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Start learning sign language fundamentals",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(lessons) { lesson: DomainLesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: navigate */ },
                    shape    = RoundedCornerShape(12.dp),
                    elevation= CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text  = lesson.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text  = lesson.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            imageVector     = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint            = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
