package com.example.tolamol

import com.example.tolamol.viewmodel.AmountCalculatorViewModel
import com.example.tolamol.viewmodel.CashCounterViewModel
import com.example.tolamol.viewmodel.PriceFinderViewModel
import com.example.tolamol.viewmodel.QuickBillingViewModel
import com.example.tolamol.viewmodel.RateUnit
import com.example.tolamol.viewmodel.WeightCalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorViewModelsTest {

    @Test
    fun cashCounterViewModel_calculatesTotalsCorrectly() {
        val viewModel = CashCounterViewModel()
        viewModel.updateCount500("10") // 5000
        viewModel.updateCount200("5")  // 1000
        viewModel.updateCount100("2")  // 200
        viewModel.updateCoins("50")    // 50

        val state = viewModel.state.value
        assertEquals(17, state.totalNotes)
        assertEquals(6250, state.grandTotal)
    }

    @Test
    fun weightCalculatorViewModel_rateUnitAndGramsCarryover() {
        val viewModel = WeightCalculatorViewModel()
        viewModel.updatePrice("85000") // 85,000 per 10 Grams (Gold rate)
        viewModel.updateRateUnit(RateUnit.PER_10G)
        viewModel.updateWeightKg("0")
        viewModel.updateWeightGrams("10") // 10 grams = 85,000

        assertEquals(85000.0, viewModel.state.value.totalAmount, 0.01)

        // Quick add 500g twice -> +1000g -> carries over 1 Kg
        viewModel.addGrams(500.0)
        viewModel.addGrams(500.0)

        val updatedState = viewModel.state.value
        assertEquals("1", updatedState.weightKg)
        assertEquals("10", updatedState.weightGrams)
    }

    @Test
    fun amountCalculatorViewModel_exactWeightCalculations() {
        val viewModel = AmountCalculatorViewModel()
        viewModel.updatePrice("85000") // ₹85,000 per 10g
        viewModel.updateRateUnit(RateUnit.PER_10G)
        viewModel.updateAmount("8500")  // Should yield 1 gram

        val state = viewModel.state.value
        assertEquals(0, state.exactWeightKg)
        assertEquals(1.0, state.exactWeightGrams, 0.001)
    }

    @Test
    fun priceFinderViewModel_rateBreakdownCalculations() {
        val viewModel = PriceFinderViewModel()
        viewModel.updateWeightGrams("100") // 100 grams
        viewModel.updateAmount("850000")   // ₹8,50,000 total

        val state = viewModel.state.value
        assertEquals(85000.0, state.ratePer10Grams, 0.01)
        assertEquals(8500.0, state.ratePerGram, 0.01)
        assertEquals(8500000.0, state.ratePerKg, 0.01)
    }

    @Test
    fun quickBillingViewModel_calculatesGrandTotalAndUpiUri() {
        val viewModel = QuickBillingViewModel()
        viewModel.updateCurrentItemName("Gold Ring")
        viewModel.updateCurrentItemQty("1")
        viewModel.updateCurrentItemSubtotal("25000")
        viewModel.addItem()

        viewModel.updateDiscount("1000")

        val state = viewModel.state.value
        assertEquals(24000.0, state.grandTotal, 0.01)

        val upiUri = viewModel.getUpiPaymentUri()
        assertTrue(upiUri.startsWith("upi://pay?pa="))
        assertTrue(upiUri.contains("am=24000.00"))
    }
}
