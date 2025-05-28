// File: app/src/main/java/com/signify/app/di/AuthViewModelFactory.kt
package com.signify.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.signify.app.auth.repository.AuthRepository
import com.signify.app.auth.viemodel.AuthViewModel

class AuthViewModelFactory(
    private val authRepo: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
