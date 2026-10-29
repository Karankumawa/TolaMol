package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.data.remote.MongoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val mongoRepo: MongoRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            try {
                val user = mongoRepo.loginUser(email, pass)
                settingsRepo.saveUserSession(user.userId, user.email, user.shopName, user.shopUpiId)
                _authState.value = AuthState.Success("Login successful!")
                onSuccess()
            } catch (e: Throwable) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, pass: String, shopName: String, shopUpi: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            try {
                val user = mongoRepo.registerUser(email, pass, shopName, shopUpi)
                settingsRepo.saveUserSession(user.userId, user.email, user.shopName, user.shopUpiId)
                _authState.value = AuthState.Success("Account created successfully!")
                onSuccess()
            } catch (e: Throwable) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class AuthViewModelFactory(
    private val mongoRepo: MongoRepository,
    private val settingsRepo: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(mongoRepo, settingsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
