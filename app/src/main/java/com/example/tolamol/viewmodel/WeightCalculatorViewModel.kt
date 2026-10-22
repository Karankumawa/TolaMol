package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.data.local.ItemRateDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeightCalculatorState(
    val pricePerKg: String = "",
    val weightKg: String = "",
    val weightGrams: String = "",
    val totalAmount: Double = 0.0
)

class WeightCalculatorViewModel(
    private val dao: ItemRateDao? = null
) : ViewModel() {
    private val _state = MutableStateFlow(WeightCalculatorState())
    val state: StateFlow<WeightCalculatorState> = _state.asStateFlow()

    val savedItems: StateFlow<List<ItemRate>> = dao?.getAllItemRates()
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) ?: MutableStateFlow(emptyList())

    fun updatePrice(price: String) {
        _state.update { it.copy(pricePerKg = price) }
        calculateTotal()
    }

    fun updateWeightKg(kg: String) {
        _state.update { it.copy(weightKg = kg) }
        calculateTotal()
    }

    fun updateWeightGrams(grams: String) {
        _state.update { it.copy(weightGrams = grams) }
        calculateTotal()
    }

    fun addGrams(gramsToAdd: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentGrams = _state.value.weightGrams.toIntOrNull() ?: 0
                val newGrams = (currentGrams + gramsToAdd).toString()
                _state.update { it.copy(weightGrams = newGrams) }
                calculateTotal()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateTotal() {
        try {
            val price = _state.value.pricePerKg.toDoubleOrNull() ?: 0.0
            val kg = _state.value.weightKg.toDoubleOrNull() ?: 0.0
            val grams = _state.value.weightGrams.toDoubleOrNull() ?: 0.0

            val total = (price * kg) + (price * (grams / 1000.0))
            _state.update { it.copy(totalAmount = total) }
        } catch (e: Throwable) {
            e.printStackTrace()
            _state.update { it.copy(totalAmount = 0.0) }
        }
    }
}

class WeightCalculatorViewModelFactory(
    private val dao: ItemRateDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightCalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeightCalculatorViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
