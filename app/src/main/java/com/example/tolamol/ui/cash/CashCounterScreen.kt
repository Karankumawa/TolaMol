package com.example.tolamol.ui.cash

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Share
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
import com.example.tolamol.viewmodel.CashCounterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashCounterScreen(
    viewModel: CashCounterViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "HI") "नोट्स गिनें" else "Count Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::clearAll) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear All")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (language == "HI") "कुल नोट्स: ${state.totalNotes}" else "Total Notes: ${state.totalNotes}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (language == "HI") "कुल योग: ₹${state.grandTotal}" else "Grand Total: ₹${state.grandTotal}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine(if (language == "HI") "*कैश विवरण*" else "*Cash Summary*")
                                appendLine("------------")
                                val c500 = state.count500.toIntOrNull() ?: 0
                                val c200 = state.count200.toIntOrNull() ?: 0
                                val c100 = state.count100.toIntOrNull() ?: 0
                                val c50 = state.count50.toIntOrNull() ?: 0
                                val c20 = state.count20.toIntOrNull() ?: 0
                                val c10 = state.count10.toIntOrNull() ?: 0
                                val coins = state.coins.toIntOrNull() ?: 0

                                if (c500 > 0) appendLine("₹500 x $c500 = ₹${c500 * 500}")
                                if (c200 > 0) appendLine("₹200 x $c200 = ₹${c200 * 200}")
                                if (c100 > 0) appendLine("₹100 x $c100 = ₹${c100 * 100}")
                                if (c50 > 0) appendLine("₹50 x $c50 = ₹${c50 * 50}")
                                if (c20 > 0) appendLine("₹20 x $c20 = ₹${c20 * 20}")
                                if (c10 > 0) appendLine("₹10 x $c10 = ₹${c10 * 10}")
                                if (coins > 0) appendLine(if (language == "HI") "सिक्के/खुले पैसे = ₹$coins" else "Coins/Loose = ₹$coins")
                                appendLine("------------")
                                appendLine(if (language == "HI") "कुल नोट्स: ${state.totalNotes}" else "Total Notes: ${state.totalNotes}")
                                appendLine(if (language == "HI") "कुल योग: ₹${state.grandTotal}" else "Grand Total: ₹${state.grandTotal}")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share")
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    viewModel.clearAll()
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            DenominationRow(denomination = 500, count = state.count500, onCountChange = viewModel::updateCount500)
            DenominationRow(denomination = 200, count = state.count200, onCountChange = viewModel::updateCount200)
            DenominationRow(denomination = 100, count = state.count100, onCountChange = viewModel::updateCount100)
            DenominationRow(denomination = 50, count = state.count50, onCountChange = viewModel::updateCount50)
            DenominationRow(denomination = 20, count = state.count20, onCountChange = viewModel::updateCount20)
            DenominationRow(denomination = 10, count = state.count10, onCountChange = viewModel::updateCount10)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = state.coins,
                onValueChange = viewModel::updateCoins,
                label = { Text(if (language == "HI") "सिक्के / खुले पैसे (₹)" else "Coins / Loose Change (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
}

@Composable
fun DenominationRow(
    denomination: Int,
    count: String,
    onCountChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "₹$denomination",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Text("x")
        OutlinedTextField(
            value = count,
            onValueChange = onCountChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1.5f),
            singleLine = true
        )
        Text("=")
        val subtotal = (count.toIntOrNull() ?: 0) * denomination
        Text(
            text = "₹$subtotal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
