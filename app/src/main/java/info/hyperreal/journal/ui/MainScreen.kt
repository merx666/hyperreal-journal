package info.hyperreal.journal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import info.hyperreal.journal.ui.journal.JournalScreen
import info.hyperreal.journal.ui.addingestion.*
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import info.hyperreal.journal.ui.navigation.Screen
import info.hyperreal.journal.ui.substances.SubstancesListScreen
import info.hyperreal.journal.ui.substances.SubstanceDetailScreen
import info.hyperreal.journal.ui.mixcalculator.MixCalculatorScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple(Screen.Journal, "Dziennik", Icons.Default.List),
                    Triple(Screen.Substances, "Substancje", Icons.Default.Info),
                    Triple(Screen.MixCalculator, "HR Miksy", Icons.Default.Favorite),
                    Triple(Screen.Settings, "Ustawienia", Icons.Default.Settings)
                )
                items.forEach { (screen, title, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route || (currentRoute?.startsWith("substance_detail") == true && screen == Screen.Substances),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Journal.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Journal.route) { 
                JournalScreen(
                    onAddClick = { navController.navigate(Screen.AddIngestionFlow.route) }
                ) 
            }

            navigation(
                startDestination = Screen.ChooseSubstance.route,
                route = Screen.AddIngestionFlow.route
            ) {
                composable(Screen.ChooseSubstance.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.AddIngestionFlow.route)
                    }
                    val viewModel: AddIngestionViewModel = hiltViewModel(parentEntry)
                    val substances by viewModel.allSubstances.collectAsState()
                    ChooseSubstanceScreen(
                        substances = substances,
                        onSubstanceSelected = {
                            viewModel.selectSubstance(it)
                            navController.navigate(Screen.ChooseRoa.route)
                        }
                    )
                }
                composable(Screen.ChooseRoa.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.AddIngestionFlow.route)
                    }
                    val viewModel: AddIngestionViewModel = hiltViewModel(parentEntry)
                    val substance by viewModel.selectedSubstance.collectAsState()
                    substance?.let {
                        ChooseRoaScreen(
                            substance = it,
                            onRoaSelected = { roa ->
                                viewModel.selectRoa(roa)
                                navController.navigate(Screen.EnterDose.route)
                            }
                        )
                    }
                }
                composable(Screen.EnterDose.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.AddIngestionFlow.route)
                    }
                    val viewModel: AddIngestionViewModel = hiltViewModel(parentEntry)
                    val roa by viewModel.selectedRoa.collectAsState()
                    roa?.let {
                        EnterDoseScreen(
                            roa = it,
                            onDoseEntered = { dose ->
                                viewModel.setDoseAmount(dose)
                                navController.navigate(Screen.ConfirmIngestion.route)
                            }
                        )
                    }
                }
                composable(Screen.ConfirmIngestion.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Screen.AddIngestionFlow.route)
                    }
                    val viewModel: AddIngestionViewModel = hiltViewModel(parentEntry)
                    ConfirmIngestionScreen(
                        viewModel = viewModel,
                        onSaveComplete = {
                            navController.popBackStack(Screen.Journal.route, inclusive = false)
                        }
                    )
                }
            }

            composable(Screen.Substances.route) { 
                SubstancesListScreen(
                    onSubstanceClick = { id -> 
                        navController.navigate(Screen.SubstanceDetail.createRoute(id))
                    }
                ) 
            }
            composable(
                route = Screen.SubstanceDetail.route,
                arguments = listOf(navArgument("substanceId") { type = NavType.StringType })
            ) { 
                SubstanceDetailScreen() 
            }
            composable(Screen.MixCalculator.route) { MixCalculatorScreen() }
            composable(Screen.Insights.route) { info.hyperreal.journal.ui.insights.InsightsScreen() }
            composable(Screen.Settings.route) { info.hyperreal.journal.ui.settings.SettingsScreen() }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title)
    }
}
