// File: app/src/main/java/com/signify/app/auth/repository/FirebaseAuthRepository.kt
package com.signify.app.auth.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun register(email: String, password: String): AuthResult =
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }

    override suspend fun login(email: String, password: String): AuthResult =
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }

    override fun logout() {
        auth.signOut()
    }

    override fun currentUserId(): String? =
        auth.currentUser?.uid
}
