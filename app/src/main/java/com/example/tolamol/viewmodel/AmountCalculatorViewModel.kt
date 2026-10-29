package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.data.remote.MongoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class AmountCalculatorState(
    val priceInput: String = "",
    val rateUnit: RateUnit = RateUnit.PER_KG,
    val amountPaid: String = "",
    val exactWeightKg: Int = 0,
    val exactWeightGrams: Double = 0.0,
    val exactWeightTola: Double = 0.0
) {
    val pricePerKg: String get() = priceInput
}

class AmountCalculatorViewModel(
    private val mongoRepo: MongoRepository? = null,
    private val settingsRepo: SettingsRepository? = null
) : ViewModel() {
    private val _state = MutableStateFlow(AmountCalculatorState())
    val state: StateFlow<AmountCalculatorState> = _state.asStateFlow()

    private val _savedItems = MutableStateFlow<List<ItemRate>>(emptyList())
    val savedItems: StateFlow<List<ItemRate>> = _savedItems.asStateFlow()

    init {
        loadSavedItems()
    }

    fun loadSavedItems() {
        val userId = settingsRepo?.userId?.value ?: ""
        if (mongoRepo != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val items = mongoRepo.fetchItemsForUser(userId)
                    _savedItems.value = items
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updatePrice(price: String) {
        _state.update { computeState(it.copy(priceInput = price)) }
    }

    fun updateRateUnit(unit: RateUnit) {
        _state.update { computeState(it.copy(rateUnit = unit)) }
    }

    fun updateAmount(amount: String) {
        _state.update { computeState(it.copy(amountPaid = amount)) }
    }

    private fun computeState(state: AmountCalculatorState): AmountCalculatorState {
        return try {
            val price = state.priceInput.toDoubleOrNull() ?: 0.0
            val amount = state.amountPaid.toDoubleOrNull() ?: 0.0

            val ratePerGram = if (state.rateUnit.gramsPerUnit > 0) price / state.rateUnit.gramsPerUnit else 0.0

            if (ratePerGram > 0 && amount >= 0) {
                val totalWeightInGrams = amount / ratePerGram
                val kg = (totalWeightInGrams / 1000.0).toInt()
                val remainingGrams = totalWeightInGrams - (kg * 1000.0)
                val roundedGrams = (remainingGrams * 1000.0).roundToInt() / 1000.0
                val tola = totalWeightInGrams / 11.664

                state.copy(
                    exactWeightKg = kg,
                    exactWeightGrams = roundedGrams,
                    exactWeightTola = (tola * 1000.0).roundToInt() / 1000.0
                )
            } else {
                state.copy(exactWeightKg = 0, exactWeightGrams = 0.0, exactWeightTola = 0.0)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            state.copy(exactWeightKg = 0, exactWeightGrams = 0.0, exactWeightTola = 0.0)
        }
    }
}

class AmountCalculatorViewModelFactory(
    private val mongoRepo: MongoRepository? = null,
    private val settingsRepo: SettingsRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AmountCalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AmountCalculatorViewModel(mongoRepo, settingsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
