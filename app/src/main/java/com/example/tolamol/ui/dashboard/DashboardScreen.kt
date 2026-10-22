package com.example.tolamol.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWeightToAmount: () -> Unit,
    onNavigateToAmountToWeight: () -> Unit,
    onNavigateToPriceFinder: () -> Unit,
    onNavigateToCashCounter: () -> Unit,
    onNavigateToQuickBilling: () -> Unit,
    onNavigateToSavedItems: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TolaMol", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                DashboardCard(
                    title = "Weight to Amount",
                    icon = Icons.Rounded.MonitorWeight,
                    onClick = onNavigateToWeightToAmount
                )
            }
            item {
                DashboardCard(
                    title = "Amount to Weight",
                    icon = Icons.Rounded.CurrencyRupee,
                    onClick = onNavigateToAmountToWeight
                )
            }
            item(span = { GridItemSpan(2) }) {
                DashboardCard(
                    title = "Per Kg Price Finder",
                    icon = Icons.Rounded.Search,
                    onClick = onNavigateToPriceFinder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DashboardCard(
                    title = "Count Notes",
                    icon = Icons.AutoMirrored.Rounded.Notes,
                    onClick = onNavigateToCashCounter
                )
            }
            item {
                DashboardCard(
                    title = "Quick Billing",
                    icon = Icons.Rounded.Receipt,
                    onClick = onNavigateToQuickBilling
                )
            }
            item {
                DashboardCard(
                    title = "Saved Item Rates",
                    icon = Icons.Rounded.Bookmarks,
                    onClick = onNavigateToSavedItems
                )
            }
            item {
                DashboardCard(
                    title = "Settings & Language",
                    icon = Icons.Rounded.Settings,
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (modifier == Modifier) 1f else 2f), // makes full-width card rectangular
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
