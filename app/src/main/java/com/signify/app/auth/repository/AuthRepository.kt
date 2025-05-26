// AuthRepository.kt
package com.signify.app.auth.repository

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    fun logout()
    fun currentUserId(): String?
}
