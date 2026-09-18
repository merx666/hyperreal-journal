package info.hyperreal.journal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import info.hyperreal.journal.ui.addingestion.*
import info.hyperreal.journal.ui.journal.JournalScreen
import info.hyperreal.journal.ui.matrix.MatrixExplorerScreen
import info.hyperreal.journal.ui.mixcalculator.MixCalculatorScreen
import info.hyperreal.journal.ui.navigation.Screen
import info.hyperreal.journal.ui.substances.SubstanceDetailScreen
import info.hyperreal.journal.ui.substances.SubstancesListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Bottom nav items (main screens)
    val bottomItems = listOf(
        Triple(Screen.Journal, "Dziennik", Icons.AutoMirrored.Filled.List),
        Triple(Screen.Substances, "Substancje", Icons.Default.Info),
        Triple(Screen.MixCalculator, "HR Miksy", Icons.Default.Favorite)
    )

    // Drawer items (secondary screens)
    val drawerItems = listOf(
        Triple(Screen.MatrixExplorer, "Tabela Miksów SIN", Icons.Default.Warning),
        Triple(Screen.Insights, "Statystyki", Icons.Default.Star),
        Triple(Screen.Settings, "Ustawienia", Icons.Default.Settings)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Hyperreal Journal",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                Spacer(modifier = Modifier.height(8.dp))

                drawerItems.forEach { (screen, title, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Show hamburger menu on main tabs only
                val showMenu = currentRoute in bottomItems.map { it.first.route } + drawerItems.map { it.first.route }
                if (showMenu) {
                    TopAppBar(
                        title = {
                            val title = when (currentRoute) {
                                Screen.Journal.route -> "Dziennik"
                                Screen.Substances.route -> "Substancje"
                                Screen.MixCalculator.route -> "Kalkulator Miksów"
                                Screen.MatrixExplorer.route -> "Tabela Miksów SIN"
                                Screen.Insights.route -> "Statystyki"
                                Screen.Settings.route -> "Ustawienia"
                                else -> "Hyperreal"
                            }
                            Text(title)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    bottomItems.forEach { (screen, title, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route ||
                                    (currentRoute?.startsWith("substance_detail") == true && screen == Screen.Substances),
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
                modifier = Modifier.padding(padding),
                enterTransition = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) +
                            androidx.compose.animation.slideInHorizontally(
                                animationSpec = androidx.compose.animation.core.tween(250),
                                initialOffsetX = { fullWidth -> fullWidth / 5 }
                            )
                },
                exitTransition = {
                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                            androidx.compose.animation.slideOutHorizontally(
                                animationSpec = androidx.compose.animation.core.tween(250),
                                targetOffsetX = { fullWidth -> -fullWidth / 5 }
                            )
                },
                popEnterTransition = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) +
                            androidx.compose.animation.slideInHorizontally(
                                animationSpec = androidx.compose.animation.core.tween(250),
                                initialOffsetX = { fullWidth -> -fullWidth / 5 }
                            )
                },
                popExitTransition = {
                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                            androidx.compose.animation.slideOutHorizontally(
                                animationSpec = androidx.compose.animation.core.tween(250),
                                targetOffsetX = { fullWidth -> fullWidth / 5 }
                            )
                }
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
                        val substance by viewModel.selectedSubstance.collectAsState()
                        val toleranceStatus by viewModel.toleranceStatus.collectAsState()
                        roa?.let {
                            EnterDoseScreen(
                                substance = substance,
                                roa = it,
                                toleranceStatus = toleranceStatus,
                                onDoseEntered = { dose, note ->
                                    viewModel.setDoseAmount(dose, note)
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
                composable(Screen.MatrixExplorer.route) { MatrixExplorerScreen() }
                composable(Screen.Insights.route) { info.hyperreal.journal.ui.insights.InsightsScreen() }
                composable(Screen.Settings.route) { info.hyperreal.journal.ui.settings.SettingsScreen() }
            }
        }
    }
}
