package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val language: StateFlow<String> = repository.language
    val themeMode: StateFlow<String> = repository.themeMode
    val shopName: StateFlow<String> = repository.shopName
    val shopUpiId: StateFlow<String> = repository.shopUpiId
    val isAdminLoggedIn: StateFlow<Boolean> = repository.isAdminLoggedIn
    val isUserLoggedIn: StateFlow<Boolean> = repository.isUserLoggedIn
    val userEmail: StateFlow<String> = repository.userEmail
    val userId: StateFlow<String> = repository.userId
    val soundAndHaptic: StateFlow<Boolean> = repository.soundAndHaptic

    fun logoutUser() {
        repository.logoutUser()
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setLanguage(lang)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setThemeMode(mode)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun setShopName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setShopName(name)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun setShopUpiId(upiId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setShopUpiId(upiId)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun verifyAdminPin(pin: String): Boolean {
        return repository.verifyAdminPin(pin)
    }

    fun logoutAdmin() {
        repository.logoutAdmin()
    }

    fun clearLocalData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
