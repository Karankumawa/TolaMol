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

enum class RateUnit(val label: String, val gramsPerUnit: Double) {
    PER_KG("Per 1 Kg", 1000.0),
    PER_10G("Per 10 Grams", 10.0),
    PER_1G("Per 1 Gram", 1.0),
    PER_TOLA("Per 1 Tola (11.664g)", 11.664)
}

data class WeightCalculatorState(
    val priceInput: String = "",
    val rateUnit: RateUnit = RateUnit.PER_KG,
    val weightKg: String = "",
    val weightGrams: String = "",
    val totalAmount: Double = 0.0
) {
    val pricePerKg: String get() = priceInput
}

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
        _state.update { computeState(it.copy(priceInput = price)) }
    }

    fun updateRateUnit(unit: RateUnit) {
        _state.update { computeState(it.copy(rateUnit = unit)) }
    }

    fun updateWeightKg(kg: String) {
        _state.update { computeState(it.copy(weightKg = kg)) }
    }

    fun updateWeightGrams(grams: String) {
        _state.update { computeState(it.copy(weightGrams = grams)) }
    }

    fun addGrams(gramsToAdd: Double) {
        _state.update { current ->
            val currentGrams = current.weightGrams.toDoubleOrNull() ?: 0.0
            val currentKg = current.weightKg.toDoubleOrNull() ?: 0.0
            val totalGrams = currentGrams + gramsToAdd
            val extraKg = (totalGrams / 1000.0).toInt()
            val remainingGrams = totalGrams % 1000.0

            val newKgStr = if (extraKg > 0) {
                if (currentKg > 0) (currentKg.toInt() + extraKg).toString() else extraKg.toString()
            } else current.weightKg

            val newGramsStr = if (remainingGrams % 1.0 == 0.0) {
                remainingGrams.toInt().toString()
            } else {
                String.format(java.util.Locale.US, "%.3f", remainingGrams).trimEnd('0').trimEnd('.')
            }

            computeState(current.copy(weightKg = newKgStr, weightGrams = newGramsStr))
        }
    }

    private fun computeState(state: WeightCalculatorState): WeightCalculatorState {
        return try {
            val price = state.priceInput.toDoubleOrNull() ?: 0.0
            val kg = state.weightKg.toDoubleOrNull() ?: 0.0
            val grams = state.weightGrams.toDoubleOrNull() ?: 0.0

            val totalWeightInGrams = (kg * 1000.0) + grams
            val ratePerGram = if (state.rateUnit.gramsPerUnit > 0) price / state.rateUnit.gramsPerUnit else 0.0
            val total = totalWeightInGrams * ratePerGram

            state.copy(totalAmount = total)
        } catch (e: Throwable) {
            e.printStackTrace()
            state.copy(totalAmount = 0.0)
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
