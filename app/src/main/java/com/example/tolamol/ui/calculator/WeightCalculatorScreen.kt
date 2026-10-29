package com.example.tolamol.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
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
import com.example.tolamol.utils.TtsHelper
import com.example.tolamol.viewmodel.WeightCalculatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val settingsRepo = remember(context) { com.example.tolamol.data.local.SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()
    val ttsHelper = remember { TtsHelper(context) }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedItemName by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "HI") "वज़न से राशि" else "Weight to Amount") },
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
                    viewModel.updateWeightKg("")
                    viewModel.updateWeightGrams("")
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

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.weightKg,
                    onValueChange = viewModel::updateWeightKg,
                    label = { Text(if (language == "HI") "वज़न (किलो)" else "Weight (Kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.weightGrams,
                    onValueChange = viewModel::updateWeightGrams,
                    label = { Text(if (language == "HI") "वज़न (ग्राम)" else "Weight (Grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(if (language == "HI") "वज़न जोड़ें:" else "Quick add weight:", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                data class QuickAddChip(val label: String, val grams: Double)
                val chips = listOf(
                    QuickAddChip("+100g", 100.0),
                    QuickAddChip("+200g", 200.0),
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
                        if (language == "HI") "कुल देय राशि" else "Total Payable Amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        String.format(Locale.US, "₹ %.2f", state.totalAmount),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FilledTonalButton(
                        onClick = {
                            val amountInt = state.totalAmount.toInt()
                            val paiseInt = ((state.totalAmount - amountInt) * 100).toInt()
                            val textToSpeak = if (language == "HI") {
                                if (paiseInt > 0) "$amountInt रुपये और $paiseInt पैसे" else "$amountInt रुपये"
                            } else {
                                if (paiseInt > 0) "$amountInt rupees and $paiseInt paise" else "$amountInt rupees"
                            }
                            ttsHelper.speak(textToSpeak)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Speak Total")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (language == "HI") "राशि बोलें" else "Speak Total")
                    }
                }
            }
        }
    }
}
}
