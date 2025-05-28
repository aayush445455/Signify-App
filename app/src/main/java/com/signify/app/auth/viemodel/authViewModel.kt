// File: app/src/main/java/com/signify/app/auth/viewmodel/AuthViewModel.kt
package com.signify.app.auth.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signify.app.auth.repository.AuthRepository
import com.signify.app.auth.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String?) : AuthUiState()
}

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val res = repo.register(email, password)) {
                AuthResult.Success   -> _uiState.value = AuthUiState.Success
                is AuthResult.Failure -> _uiState.value = AuthUiState.Error(res.exception.message)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val res = repo.login(email, password)) {
                AuthResult.Success   -> _uiState.value = AuthUiState.Success
                is AuthResult.Failure -> _uiState.value = AuthUiState.Error(res.exception.message)
            }
        }
    }

    fun logout() = repo.logout()
    fun currentUserId() = repo.currentUserId()
}
