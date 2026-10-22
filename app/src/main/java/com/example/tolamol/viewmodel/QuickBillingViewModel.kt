package com.example.tolamol.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tolamol.data.local.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class BillItem(
    val name: String,
    val quantity: String,
    val subtotal: String
)

data class QuickBillingState(
    val items: List<BillItem> = emptyList(),
    val currentItemName: String = "",
    val currentItemQty: String = "",
    val currentItemSubtotal: String = "",
    val discount: String = "",
    val grandTotal: Double = 0.0,
    val shopName: String = SettingsRepository.DEFAULT_SHOP_NAME,
    val shopUpiId: String = SettingsRepository.DEFAULT_SHOP_UPI_ID
)

class QuickBillingViewModel(
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {
    private val _state = MutableStateFlow(QuickBillingState())
    val state: StateFlow<QuickBillingState> = _state.asStateFlow()

    init {
        settingsRepository?.let { repo ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    combine(repo.shopName, repo.shopUpiId) { name, upi ->
                        Pair(
                            SettingsRepository.getValidShopName(name),
                            SettingsRepository.getValidShopUpiId(upi)
                        )
                    }.collect { (name, upi) ->
                        _state.update { it.copy(shopName = name, shopUpiId = upi) }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateCurrentItemName(name: String) { _state.update { it.copy(currentItemName = name) } }
    fun updateCurrentItemQty(qty: String) { _state.update { it.copy(currentItemQty = qty) } }
    fun updateCurrentItemSubtotal(subtotal: String) { _state.update { it.copy(currentItemSubtotal = subtotal) } }
    
    fun updateDiscount(discount: String) { 
        _state.update { it.copy(discount = discount) }
        calculateTotal()
    }

    fun addItem() {
        try {
            val s = _state.value
            if (s.currentItemName.isNotBlank() && s.currentItemSubtotal.isNotBlank()) {
                val newItem = BillItem(s.currentItemName, s.currentItemQty, s.currentItemSubtotal)
                _state.update { 
                    it.copy(
                        items = it.items + newItem,
                        currentItemName = "",
                        currentItemQty = "",
                        currentItemSubtotal = ""
                    ) 
                }
                calculateTotal()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    
    fun removeItem(item: BillItem) {
        try {
            _state.update { it.copy(items = it.items - item) }
            calculateTotal()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun calculateTotal() {
        try {
            val s = _state.value
            val itemsTotal = s.items.sumOf { it.subtotal.toDoubleOrNull() ?: 0.0 }
            val discountVal = s.discount.toDoubleOrNull() ?: 0.0
            val grandTotal = (itemsTotal - discountVal).coerceAtLeast(0.0)
            
            _state.update { it.copy(grandTotal = grandTotal) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getUpiPaymentUri(): String {
        return try {
            val s = _state.value
            val vpa = SettingsRepository.getValidShopUpiId(s.shopUpiId)
            val shopName = SettingsRepository.getValidShopName(s.shopName)
            val encodedName = Uri.encode(shopName)
            val formattedAmount = String.format(Locale.US, "%.2f", s.grandTotal)
            val encodedNote = Uri.encode("TolaMol Bill Payment")
            val trRef = "TM" + System.currentTimeMillis()
            "upi://pay?pa=$vpa&pn=$encodedName&am=$formattedAmount&cu=INR&tn=$encodedNote&tr=$trRef"
        } catch (e: Throwable) {
            e.printStackTrace()
            "upi://pay?pa=${SettingsRepository.DEFAULT_SHOP_UPI_ID}&pn=${Uri.encode(SettingsRepository.DEFAULT_SHOP_NAME)}&am=0.00&cu=INR"
        }
    }
}

class QuickBillingViewModelFactory(
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickBillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickBillingViewModel(settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
