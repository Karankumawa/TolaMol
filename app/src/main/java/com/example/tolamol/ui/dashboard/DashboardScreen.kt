package com.example.tolamol.ui.dashboard

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tolamol.data.local.SettingsRepository

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
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
    val language by settingsRepo.language.collectAsStateWithLifecycle()
    val themeMode by settingsRepo.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemDark
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 18.dp, horizontal = 12.dp)
                ) {
                    IconButton(
                        onClick = {
                            val nextMode = if (isDark) "LIGHT" else "DARK"
                            settingsRepo.setThemeMode(nextMode)
                        },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TolaMol",
                            style = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF9933),
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    offset = Offset(2f, 3f),
                                    blurRadius = 4f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == "HI") "स्मार्ट वज़न, भाव और कैश टूलकिट" else "Smart Weight, Rate & Cash Toolkit",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
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
                    title = if (language == "HI") "वज़न से राशि" else "Weight to Amount",
                    icon = Icons.Rounded.MonitorWeight,
                    onClick = onNavigateToWeightToAmount
                )
            }
            item {
                DashboardCard(
                    title = if (language == "HI") "राशि से वज़न" else "Amount to Weight",
                    icon = Icons.Rounded.CurrencyRupee,
                    onClick = onNavigateToAmountToWeight
                )
            }
            item(span = { GridItemSpan(2) }) {
                DashboardCard(
                    title = if (language == "HI") "प्रति किलो भाव खोजें" else "Per Kg Price Finder",
                    icon = Icons.Rounded.Search,
                    onClick = onNavigateToPriceFinder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                DashboardCard(
                    title = if (language == "HI") "नोट्स गिनें" else "Count Notes",
                    icon = Icons.AutoMirrored.Rounded.Notes,
                    onClick = onNavigateToCashCounter
                )
            }
            item {
                DashboardCard(
                    title = if (language == "HI") "त्वरित बिलिंग" else "Quick Billing",
                    icon = Icons.Rounded.Receipt,
                    onClick = onNavigateToQuickBilling
                )
            }
            item {
                DashboardCard(
                    title = if (language == "HI") "सहेजे गए आइटम भाव" else "Saved Item Rates",
                    icon = Icons.Rounded.Bookmarks,
                    onClick = onNavigateToSavedItems
                )
            }
            item {
                DashboardCard(
                    title = if (language == "HI") "सेटिंग्स और भाषा" else "Settings & Language",
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
            .aspectRatio(if (modifier == Modifier) 1f else 2f),
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
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
