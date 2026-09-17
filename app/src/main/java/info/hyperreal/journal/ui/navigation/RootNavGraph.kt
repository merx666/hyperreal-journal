package info.hyperreal.journal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import info.hyperreal.journal.ui.MainScreen
import info.hyperreal.journal.ui.onboarding.OnboardingScreen
import info.hyperreal.journal.ui.onboarding.OnboardingViewModel

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val hasAcceptedDisclaimer by onboardingViewModel.hasAcceptedDisclaimer.collectAsState(initial = null)

    if (hasAcceptedDisclaimer == null) {
        // Loading state, could show a splash screen here
        return
    }

    val startDestination = if (hasAcceptedDisclaimer == true) {
        Screen.Main.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onAccept = {
                    onboardingViewModel.acceptDisclaimer()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
