package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.data.local.ItemRateDao
import com.example.tolamol.data.remote.MongoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedItemsViewModel(
    private val dao: ItemRateDao,
    private val mongoRepo: MongoRepository
) : ViewModel() {

    val itemRates: StateFlow<List<ItemRate>> = dao.getAllItemRates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun syncWithCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncState.value = SyncState.Syncing
            try {
                val remoteItems = mongoRepo.fetchRemoteItems()
                if (remoteItems.isNotEmpty()) {
                    dao.insertItemRates(remoteItems)
                }
                
                val localItems = dao.getAllItemRates().first()
                mongoRepo.syncLocalToRemote(localItems)
                
                _syncState.value = SyncState.Success("Sync complete!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                e.printStackTrace()
                _syncState.value = SyncState.Error("Sync failed: ${e.message ?: "Network or connection error"}")
            }
        }
    }

    fun addItemRate(name: String, price: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val item = ItemRate(name = name, pricePerKg = price)
                dao.insertItemRate(item)
                val updatedItems = dao.getAllItemRates().first()
                try {
                    mongoRepo.syncLocalToRemote(updatedItems)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
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
                dao.deleteItemRate(itemRate)
                val remainingItems = dao.getAllItemRates().first()
                try {
                    mongoRepo.syncLocalToRemote(remainingItems)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
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
    private val dao: ItemRateDao,
    private val mongoRepo: MongoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedItemsViewModel(dao, mongoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
