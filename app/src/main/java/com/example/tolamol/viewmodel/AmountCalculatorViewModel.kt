package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.data.local.ItemRateDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AmountCalculatorState(
    val pricePerKg: String = "",
    val amountPaid: String = "",
    val exactWeightKg: Int = 0,
    val exactWeightGrams: Int = 0
)

class AmountCalculatorViewModel(
    private val dao: ItemRateDao? = null
) : ViewModel() {
    private val _state = MutableStateFlow(AmountCalculatorState())
    val state: StateFlow<AmountCalculatorState> = _state.asStateFlow()

    val savedItems: StateFlow<List<ItemRate>> = dao?.getAllItemRates()
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) ?: MutableStateFlow(emptyList())

    fun updatePrice(price: String) {
        _state.update { it.copy(pricePerKg = price) }
        calculateWeight()
    }

    fun updateAmount(amount: String) {
        _state.update { it.copy(amountPaid = amount) }
        calculateWeight()
    }

    private fun calculateWeight() {
        try {
            val price = _state.value.pricePerKg.toDoubleOrNull() ?: 0.0
            val amount = _state.value.amountPaid.toDoubleOrNull() ?: 0.0

            if (price > 0 && amount >= 0) {
                val totalWeightInKg = amount / price
                val kg = totalWeightInKg.toInt()
                val grams = ((totalWeightInKg - kg) * 1000).toInt()
                _state.update { it.copy(exactWeightKg = kg, exactWeightGrams = grams) }
            } else {
                _state.update { it.copy(exactWeightKg = 0, exactWeightGrams = 0) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            _state.update { it.copy(exactWeightKg = 0, exactWeightGrams = 0) }
        }
    }
}

class AmountCalculatorViewModelFactory(
    private val dao: ItemRateDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AmountCalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AmountCalculatorViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
