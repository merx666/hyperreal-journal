package info.hyperreal.journal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import info.hyperreal.journal.ui.MainScreen
import info.hyperreal.journal.ui.onboarding.OnboardingScreen
import info.hyperreal.journal.ui.onboarding.OnboardingViewModel
import info.hyperreal.journal.ui.security.BiometricGuardScreen

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val userPreferences by onboardingViewModel.userPreferences.collectAsState()

    var isSessionUnlocked by rememberSaveable { mutableStateOf(false) }

    val prefs = userPreferences ?: return

    val startDestination = when {
        !prefs.hasAcceptedDisclaimer -> Screen.Onboarding.route
        prefs.isSecurityLockEnabled && !isSessionUnlocked -> Screen.SecurityLock.route
        else -> Screen.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onAccept = {
                    onboardingViewModel.acceptDisclaimer()
                    val nextRoute = if (prefs.isSecurityLockEnabled && !isSessionUnlocked) {
                        Screen.SecurityLock.route
                    } else {
                        Screen.Main.route
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SecurityLock.route) {
            BiometricGuardScreen(
                userPreferencesRepository = onboardingViewModel.userPreferencesRepository,
                expectedPinHash = prefs.pinHash,
                onUnlocked = {
                    isSessionUnlocked = true
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SecurityLock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
