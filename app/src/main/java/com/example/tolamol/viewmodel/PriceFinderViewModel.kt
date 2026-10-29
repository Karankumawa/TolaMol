package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PriceFinderState(
    val totalWeightKg: String = "",
    val totalWeightGrams: String = "",
    val amountPaid: String = "",
    val ratePerKg: Double = 0.0,
    val ratePer10Grams: Double = 0.0,
    val ratePerTola: Double = 0.0,
    val ratePerGram: Double = 0.0,
    val ratePerQuintal: Double = 0.0
)

class PriceFinderViewModel : ViewModel() {
    private val _state = MutableStateFlow(PriceFinderState())
    val state: StateFlow<PriceFinderState> = _state.asStateFlow()

    fun updateWeightKg(kg: String) {
        _state.update { it.copy(totalWeightKg = kg) }
        calculateRate()
    }

    fun updateWeightGrams(grams: String) {
        _state.update { it.copy(totalWeightGrams = grams) }
        calculateRate()
    }

    fun updateAmount(amount: String) {
        _state.update { it.copy(amountPaid = amount) }
        calculateRate()
    }

    private fun calculateRate() {
        try {
            val kg = _state.value.totalWeightKg.toDoubleOrNull() ?: 0.0
            val grams = _state.value.totalWeightGrams.toDoubleOrNull() ?: 0.0
            val amount = _state.value.amountPaid.toDoubleOrNull() ?: 0.0

            val totalWeightInGrams = (kg * 1000.0) + grams

            if (totalWeightInGrams > 0 && amount >= 0) {
                val rateGram = amount / totalWeightInGrams
                val rateKg = rateGram * 1000.0
                val rate10G = rateGram * 10.0
                val rateTola = rateGram * 11.664
                val rateQuintal = rateKg * 100.0
                _state.update {
                    it.copy(
                        ratePerKg = rateKg,
                        ratePer10Grams = rate10G,
                        ratePerTola = rateTola,
                        ratePerGram = rateGram,
                        ratePerQuintal = rateQuintal
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        ratePerKg = 0.0,
                        ratePer10Grams = 0.0,
                        ratePerTola = 0.0,
                        ratePerGram = 0.0,
                        ratePerQuintal = 0.0
                    )
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            _state.update {
                it.copy(
                    ratePerKg = 0.0,
                    ratePer10Grams = 0.0,
                    ratePerTola = 0.0,
                    ratePerGram = 0.0,
                    ratePerQuintal = 0.0
                )
            }
        }
    }
}
