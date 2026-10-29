package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.data.remote.MongoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedItemsViewModel(
    private val mongoRepo: MongoRepository,
    private val settingsRepo: SettingsRepository? = null
) : ViewModel() {

    private val _itemRates = MutableStateFlow<List<ItemRate>>(emptyList())
    val itemRates: StateFlow<List<ItemRate>> = _itemRates.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        syncWithCloud()
    }

    fun syncWithCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncState.value = SyncState.Syncing
            try {
                val userId = settingsRepo?.userId?.value ?: ""
                val remoteItems = mongoRepo.fetchItemsForUser(userId)
                _itemRates.value = remoteItems
                _syncState.value = SyncState.Success("Data loaded from Cloud!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                e.printStackTrace()
                _syncState.value = SyncState.Error("Sync failed: ${e.message ?: "Network error"}")
            }
        }
    }

    fun addItemRate(name: String, price: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = settingsRepo?.userId?.value ?: ""
                mongoRepo.addItemRateForUser(userId, name, price)
                val updatedItems = mongoRepo.fetchItemsForUser(userId)
                _itemRates.value = updatedItems
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteItemRate(itemRate: ItemRate) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = settingsRepo?.userId?.value ?: ""
                mongoRepo.deleteItemRateForUser(userId, itemRate.id)
                val updatedItems = mongoRepo.fetchItemsForUser(userId)
                _itemRates.value = updatedItems
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
    
    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

class SavedItemsViewModelFactory(
    private val mongoRepo: MongoRepository,
    private val settingsRepo: SettingsRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedItemsViewModel(mongoRepo, settingsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
