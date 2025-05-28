// File: app/src/main/java/com/signify/app/auth/repository/AuthRepository.kt
package com.signify.app.auth.repository

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val exception: Throwable) : AuthResult()
}

interface AuthRepository {
    suspend fun register(email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    fun logout()
    fun currentUserId(): String?
}
