package com.example.tolamol.ui.saveditems

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tolamol.data.local.ItemRate
import com.example.tolamol.viewmodel.SavedItemsViewModel
import com.example.tolamol.viewmodel.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedItemsScreen(
    viewModel: SavedItemsViewModel,
    onBack: () -> Unit
) {
    val itemRates by viewModel.itemRates.collectAsStateWithLifecycle(initialValue = emptyList())
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settingsRepo = remember(context) { com.example.tolamol.data.local.SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()

    LaunchedEffect(syncState) {
        when (val state = syncState) {
            is SyncState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetSyncState()
            }
            is SyncState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetSyncState()
            }
            else -> {}
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddItemDialog(
            language = language,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price ->
                viewModel.addItemRate(name, price)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "HI") "सहेजे गए आइटम भाव" else "Saved Item Rates", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncWithCloud() }) {
                        if (syncState is SyncState.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = "Sync with Cloud"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = syncState is SyncState.Syncing,
            onRefresh = { viewModel.syncWithCloud() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (itemRates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == "HI") "अभी तक कोई सहेजा गया आइटम नहीं है।" else "No saved items yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(itemRates, key = { "${it.id}_${it.name}" }) { item ->
                        ItemRateCard(
                            item = item,
                            onDelete = { viewModel.deleteItemRate(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemRateCard(item: ItemRate, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "₹${item.pricePerKg} / kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddItemDialog(language: String = "EN", onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == "HI") "आइटम भाव जोड़ें" else "Add Item Rate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (language == "HI") "आइटम का नाम" else "Item Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(if (language == "HI") "प्रति किलो भाव" else "Price per Kg") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceVal = price.toDoubleOrNull()
                    if (name.isNotBlank() && priceVal != null) {
                        onConfirm(name, priceVal)
                    }
                }
            ) {
                Text(if (language == "HI") "सेव करें" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == "HI") "रद्द करें" else "Cancel")
            }
        }
    )
}
