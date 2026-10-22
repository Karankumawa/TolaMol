package com.example.tolamol.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CashCounterState(
    val count500: String = "",
    val count200: String = "",
    val count100: String = "",
    val count50: String = "",
    val count20: String = "",
    val count10: String = "",
    val coins: String = "",
    
    val totalNotes: Int = 0,
    val grandTotal: Int = 0
)

class CashCounterViewModel : ViewModel() {
    private val _state = MutableStateFlow(CashCounterState())
    val state: StateFlow<CashCounterState> = _state.asStateFlow()

    fun updateCount500(count: String) { _state.update { it.copy(count500 = count) }; calculateTotal() }
    fun updateCount200(count: String) { _state.update { it.copy(count200 = count) }; calculateTotal() }
    fun updateCount100(count: String) { _state.update { it.copy(count100 = count) }; calculateTotal() }
    fun updateCount50(count: String) { _state.update { it.copy(count50 = count) }; calculateTotal() }
    fun updateCount20(count: String) { _state.update { it.copy(count20 = count) }; calculateTotal() }
    fun updateCount10(count: String) { _state.update { it.copy(count10 = count) }; calculateTotal() }
    fun updateCoins(count: String) { _state.update { it.copy(coins = count) }; calculateTotal() }

    fun clearAll() {
        _state.value = CashCounterState()
    }

    private fun calculateTotal() {
        try {
            val s = _state.value
            val c500 = s.count500.toIntOrNull() ?: 0
            val c200 = s.count200.toIntOrNull() ?: 0
            val c100 = s.count100.toIntOrNull() ?: 0
            val c50 = s.count50.toIntOrNull() ?: 0
            val c20 = s.count20.toIntOrNull() ?: 0
            val c10 = s.count10.toIntOrNull() ?: 0
            val coins = s.coins.toIntOrNull() ?: 0

            val totalNotes = c500 + c200 + c100 + c50 + c20 + c10
            val grandTotal = (c500 * 500) + (c200 * 200) + (c100 * 100) + (c50 * 50) + (c20 * 20) + (c10 * 10) + coins

            _state.update { it.copy(totalNotes = totalNotes, grandTotal = grandTotal) }
        } catch (e: Throwable) {
            e.printStackTrace()
            _state.update { it.copy(totalNotes = 0, grandTotal = 0) }
        }
    }
}
