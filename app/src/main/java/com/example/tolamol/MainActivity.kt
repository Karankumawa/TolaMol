package com.example.tolamol

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.tolamol.data.local.AppDatabase
import com.example.tolamol.data.local.SettingsRepository
import com.example.tolamol.data.remote.MongoRepository
import com.example.tolamol.navigation.*
import com.example.tolamol.ui.billing.QuickBillingScreen
import com.example.tolamol.ui.calculator.*
import com.example.tolamol.ui.cash.CashCounterScreen
import com.example.tolamol.ui.dashboard.DashboardScreen
import com.example.tolamol.ui.saveditems.SavedItemsScreen
import com.example.tolamol.ui.settings.SettingsScreen
import com.example.tolamol.ui.theme.TolaMolTheme
import com.example.tolamol.viewmodel.*
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
            val language by settingsRepo.language.collectAsStateWithLifecycle()
            val themeMode by settingsRepo.themeMode.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemDark
            }

            val locale = if (language == "HI") Locale.forLanguageTag("hi-IN") else Locale.forLanguageTag("en-US")
            val currentConfig = LocalConfiguration.current
            val config = remember(currentConfig, locale) {
                Configuration(currentConfig).apply {
                    setLocale(locale)
                }
            }
            val localizedContext = remember(context, config) {
                context.createConfigurationContext(config)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                TolaMolTheme(darkTheme = darkTheme) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(DashboardRoute)

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<DashboardRoute>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a calculator from the list")
                        }
                    }
                )
            ) {
                DashboardScreen(
                    onNavigateToWeightToAmount = { backStack.add(WeightToAmountRoute) },
                    onNavigateToAmountToWeight = { backStack.add(AmountToWeightRoute) },
                    onNavigateToPriceFinder = { backStack.add(PriceFinderRoute) },
                    onNavigateToCashCounter = { backStack.add(CashCounterRoute) },
                    onNavigateToQuickBilling = { backStack.add(QuickBillingRoute) },
                    onNavigateToSavedItems = { backStack.add(SavedItemsRoute) },
                    onNavigateToSettings = { backStack.add(SettingsRoute) }
                )
            }

            entry<WeightToAmountRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val context = LocalContext.current
                val database = remember(context) { AppDatabase.getDatabase(context) }
                val viewModel: WeightCalculatorViewModel = viewModel(
                    factory = WeightCalculatorViewModelFactory(database.itemRateDao())
                )
                WeightCalculatorScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AmountToWeightRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val context = LocalContext.current
                val database = remember(context) { AppDatabase.getDatabase(context) }
                val viewModel: AmountCalculatorViewModel = viewModel(
                    factory = AmountCalculatorViewModelFactory(database.itemRateDao())
                )
                AmountCalculatorScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<PriceFinderRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: PriceFinderViewModel = viewModel()
                PriceFinderScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<CashCounterRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: CashCounterViewModel = viewModel()
                CashCounterScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<QuickBillingRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val context = LocalContext.current
                val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
                val viewModel: QuickBillingViewModel = viewModel(
                    factory = QuickBillingViewModelFactory(settingsRepo)
                )
                QuickBillingScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<SavedItemsRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val context = LocalContext.current
                val database = remember(context) { AppDatabase.getDatabase(context) }
                val mongoRepo = remember { MongoRepository() }

                val viewModel: SavedItemsViewModel = viewModel(
                    factory = SavedItemsViewModelFactory(database.itemRateDao(), mongoRepo)
                )
                SavedItemsScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<SettingsRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val context = LocalContext.current
                val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
                val database = remember(context) { AppDatabase.getDatabase(context) }
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(settingsRepo, database.itemRateDao())
                )
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
