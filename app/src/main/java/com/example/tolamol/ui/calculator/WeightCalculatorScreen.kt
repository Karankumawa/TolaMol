package com.example.tolamol.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tolamol.utils.TtsHelper
import com.example.tolamol.viewmodel.WeightCalculatorViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightCalculatorScreen(
    viewModel: WeightCalculatorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedItemName by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight to Amount") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (savedItems.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedItemName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Saved Item") },
                        placeholder = { Text("Choose item rate...") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        savedItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.name} - ₹${item.pricePerKg}/kg") },
                                onClick = {
                                    selectedItemName = item.name
                                    viewModel.updatePrice(item.pricePerKg.toString())
                                    viewModel.updateRateUnit(com.example.tolamol.viewmodel.RateUnit.PER_KG)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text("Rate Unit:", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                com.example.tolamol.viewmodel.RateUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = state.rateUnit == unit,
                        onClick = { viewModel.updateRateUnit(unit) },
                        label = { Text(unit.label) }
                    )
                }
            }

            OutlinedTextField(
                value = state.priceInput,
                onValueChange = { newPrice ->
                    viewModel.updatePrice(newPrice)
                    if (selectedItemName.isNotEmpty() && savedItems.none { it.pricePerKg.toString() == newPrice && it.name == selectedItemName }) {
                        selectedItemName = ""
                    }
                },
                label = { Text("Price ${state.rateUnit.label} (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.weightKg,
                    onValueChange = viewModel::updateWeightKg,
                    label = { Text("Weight (Kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.weightGrams,
                    onValueChange = viewModel::updateWeightGrams,
                    label = { Text("Weight (Grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Quick add weight:", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                data class QuickAddChip(val label: String, val grams: Double)
                val chips = listOf(
                    QuickAddChip("+1g", 1.0),
                    QuickAddChip("+5g", 5.0),
                    QuickAddChip("+10g", 10.0),
                    QuickAddChip("+1 Tola", 11.664),
                    QuickAddChip("+50g", 50.0),
                    QuickAddChip("+100g", 100.0),
                    QuickAddChip("+500g", 500.0)
                )
                chips.forEach { chip ->
                    AssistChip(
                        onClick = { viewModel.addGrams(chip.grams) },
                        label = { Text(chip.label) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Payable Amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        String.format(Locale.getDefault(), "₹ %.2f", state.totalAmount),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FilledTonalButton(
                        onClick = {
                            val amountInt = state.totalAmount.toInt()
                            val paiseInt = ((state.totalAmount - amountInt) * 100).toInt()
                            val textToSpeak = if (paiseInt > 0) {
                                "$amountInt rupees and $paiseInt paise"
                            } else {
                                "$amountInt rupees"
                            }
                            ttsHelper.speak(textToSpeak)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Speak Total")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Speak Total")
                    }
                }
            }
        }
    }
}
