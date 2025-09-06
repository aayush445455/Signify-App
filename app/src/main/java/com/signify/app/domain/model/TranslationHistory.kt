package com.signify.app.domain.model

data class TranslationHistory(
    val id: Long,
    val inputText: String,
    val outputText: String,
    val timestamp: Long
)
