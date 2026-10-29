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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val context = LocalContext.current
    val settingsRepo = remember(context) { SettingsRepository.getInstance(context) }
    val mongoRepo = remember { MongoRepository() }
    val isUserLoggedIn by settingsRepo.isUserLoggedIn.collectAsStateWithLifecycle()

    var pendingRoute by remember { mutableStateOf<NavKey?>(null) }

    fun navigateProtected(targetRoute: NavKey) {
        if (isUserLoggedIn) {
            backStack.add(targetRoute)
        } else {
            pendingRoute = targetRoute
            backStack.add(AuthRoute)
        }
    }

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
                    onNavigateToCashCounter = { navigateProtected(CashCounterRoute) },
                    onNavigateToQuickBilling = { navigateProtected(QuickBillingRoute) },
                    onNavigateToSavedItems = { navigateProtected(SavedItemsRoute) },
                    onNavigateToSettings = { backStack.add(SettingsRoute) }
                )
            }

            entry<AuthRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(mongoRepo, settingsRepo)
                )
                com.example.tolamol.ui.auth.AuthScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onAuthSuccess = {
                        backStack.removeLastOrNull()
                        pendingRoute?.let { target ->
                            backStack.add(target)
                            pendingRoute = null
                        }
                    }
                )
            }

            entry<WeightToAmountRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: WeightCalculatorViewModel = viewModel(
                    factory = WeightCalculatorViewModelFactory(mongoRepo, settingsRepo)
                )
                WeightCalculatorScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AmountToWeightRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: AmountCalculatorViewModel = viewModel(
                    factory = AmountCalculatorViewModelFactory(mongoRepo, settingsRepo)
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
                val viewModel: CashCounterViewModel = viewModel(
                    factory = CashCounterViewModelFactory(mongoRepo, settingsRepo)
                )
                CashCounterScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<QuickBillingRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: QuickBillingViewModel = viewModel(
                    factory = QuickBillingViewModelFactory(settingsRepo, mongoRepo)
                )
                QuickBillingScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<SavedItemsRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: SavedItemsViewModel = viewModel(
                    factory = SavedItemsViewModelFactory(mongoRepo, settingsRepo)
                )
                SavedItemsScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<SettingsRoute>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(settingsRepo)
                )
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
