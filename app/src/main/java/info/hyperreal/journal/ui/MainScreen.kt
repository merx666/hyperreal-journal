package info.hyperreal.journal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import info.hyperreal.journal.R
import info.hyperreal.journal.ui.addingestion.*
import info.hyperreal.journal.ui.components.EmergencyHelpBottomSheet
import info.hyperreal.journal.ui.components.EmergencyHelpTopBarButton
import info.hyperreal.journal.ui.journal.JournalScreen
import info.hyperreal.journal.ui.matrix.MatrixExplorerScreen
import info.hyperreal.journal.ui.mixcalculator.MixCalculatorScreen
import info.hyperreal.journal.ui.navigation.Screen
import info.hyperreal.journal.ui.onboarding.OnboardingScreen
import info.hyperreal.journal.ui.substances.SubstanceDetailScreen
import info.hyperreal.journal.ui.substances.SubstancesListScreen
import info.hyperreal.journal.ui.theme.HyperrealTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showEmergencyHelpSheet by remember { mutableStateOf(false) }

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
        Triple(Screen.Settings, "Ustawienia", Icons.Default.Settings),
        Triple(Screen.Onboarding, "Ekran powitalny & Oświadczenie", Icons.Default.Info)
    )

    if (showEmergencyHelpSheet) {
        EmergencyHelpBottomSheet(
            onDismissRequest = { showEmergencyHelpSheet = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = HyperrealTokens.SurfaceDark,
                drawerContentColor = HyperrealTokens.TextPrimary
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            "Hyperreal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = HyperrealTokens.BrandGreen
                        )
                        Text(
                            "Harm Reduction 24H",
                            style = MaterialTheme.typography.bodySmall,
                            color = HyperrealTokens.TextSecondary
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = HyperrealTokens.BorderSubtle
                )

                drawerItems.forEach { (screen, title, icon) ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                icon,
                                contentDescription = title,
                                tint = if (currentRoute == screen.route) HyperrealTokens.BrandGreen else HyperrealTokens.TextSecondary
                            )
                        },
                        label = {
                            Text(
                                title,
                                fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentRoute == screen.route) Color.White else HyperrealTokens.TextPrimary
                            )
                        },
                        selected = currentRoute == screen.route,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = HyperrealTokens.SurfaceRaised,
                            unselectedContainerColor = Color.Transparent
                        ),
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
            containerColor = HyperrealTokens.Canvas,
            topBar = {
                // Show hamburger menu on main tabs only
                val showMenu = currentRoute in bottomItems.map { it.first.route } + drawerItems.map { it.first.route }
                if (showMenu) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                val title = when (currentRoute) {
                                    Screen.Journal.route -> "Dziennik"
                                    Screen.Substances.route -> "Substancje"
                                    Screen.MixCalculator.route -> "Kalkulator Miksów"
                                    Screen.MatrixExplorer.route -> "Tabela Miksów SIN"
                                    Screen.Insights.route -> "Statystyki"
                                    Screen.Settings.route -> "Ustawienia"
                                    Screen.Onboarding.route -> "Oświadczenie"
                                    else -> "Hyperreal"
                                }
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        actions = {
                            EmergencyHelpTopBarButton(
                                onClick = { showEmergencyHelpSheet = true }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = HyperrealTokens.SurfaceDark,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = HyperrealTokens.SurfaceDark,
                    contentColor = HyperrealTokens.TextPrimary
                ) {
                    bottomItems.forEach { (screen, title, icon) ->
                        val isSelected = currentRoute == screen.route ||
                                (currentRoute?.startsWith("substance_detail") == true && screen == Screen.Substances)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HyperrealTokens.Canvas,
                                selectedTextColor = HyperrealTokens.BrandGreen,
                                indicatorColor = HyperrealTokens.BrandGreen,
                                unselectedIconColor = HyperrealTokens.TextSecondary,
                                unselectedTextColor = HyperrealTokens.TextSecondary
                            )
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
                            },
                            onAddCustomSubstance = { name, roaName, duration ->
                                viewModel.addCustomSubstance(name, roaName, duration) {
                                    navController.navigate(Screen.EnterDose.route)
                                }
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
                    SubstanceDetailScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.MixCalculator.route) { MixCalculatorScreen() }
                composable(Screen.MatrixExplorer.route) { MatrixExplorerScreen() }
                composable(Screen.Insights.route) { info.hyperreal.journal.ui.insights.InsightsScreen() }
                composable(Screen.Settings.route) { info.hyperreal.journal.ui.settings.SettingsScreen() }
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onAccept = {
                            navController.navigate(Screen.Journal.route) {
                                popUpTo(Screen.Journal.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
