package com.example.tolamol.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tolamol.viewmodel.AmountCalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountCalculatorScreen(
    viewModel: AmountCalculatorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedItemName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amount to Weight") },
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

            OutlinedTextField(
                value = state.amountPaid,
                onValueChange = viewModel::updateAmount,
                label = { Text("Customer Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Weight to Give",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val gramsFormatted = if (state.exactWeightGrams % 1.0 == 0.0) {
                        "${state.exactWeightGrams.toInt()} g"
                    } else {
                        String.format(java.util.Locale.US, "%.3f g", state.exactWeightGrams)
                    }

                    Text(
                        if (state.exactWeightKg > 0) "${state.exactWeightKg} Kg  $gramsFormatted" else gramsFormatted,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    if (state.exactWeightTola > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Equivalent: ${state.exactWeightTola} Tola",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
