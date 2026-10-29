package com.example.tolamol.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.viewmodel.AmountCalculatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountCalculatorScreen(
    viewModel: AmountCalculatorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedItemName by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "HI") "राशि से वज़न" else "Amount to Weight") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    selectedItemName = ""
                    viewModel.updatePrice("")
                    viewModel.updateAmount("")
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        label = { Text(if (language == "HI") "सहेजा गया आइटम चुनें" else "Select Saved Item") },
                        placeholder = { Text(if (language == "HI") "भाव चुनें..." else "Choose item rate...") },
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

            Text(if (language == "HI") "भाव इकाई:" else "Rate Unit:", style = MaterialTheme.typography.labelLarge)
            val rateEntries = com.example.tolamol.viewmodel.RateUnit.entries
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = state.rateUnit == rateEntries[0],
                        onClick = { viewModel.updateRateUnit(rateEntries[0]) },
                        label = { Text(rateEntries[0].label(language), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.rateUnit == rateEntries[1],
                        onClick = { viewModel.updateRateUnit(rateEntries[1]) },
                        label = { Text(rateEntries[1].label(language), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = state.rateUnit == rateEntries[2],
                        onClick = { viewModel.updateRateUnit(rateEntries[2]) },
                        label = { Text(rateEntries[2].label(language), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.rateUnit == rateEntries[3],
                        onClick = { viewModel.updateRateUnit(rateEntries[3]) },
                        label = { Text(rateEntries[3].label(language), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                        modifier = Modifier.weight(1f)
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
                label = { Text(if (language == "HI") "भाव ${state.rateUnit.label(language)} (₹)" else "Price ${state.rateUnit.label(language)} (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.amountPaid,
                onValueChange = viewModel::updateAmount,
                label = { Text(if (language == "HI") "ग्राहक राशि (₹)" else "Customer Amount (₹)") },
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
                        if (language == "HI") "देने योग्य वज़न" else "Weight to Give",
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
                            text = if (language == "HI") "समकक्ष: ${state.exactWeightTola} तोला" else "Equivalent: ${state.exactWeightTola} Tola",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
}
