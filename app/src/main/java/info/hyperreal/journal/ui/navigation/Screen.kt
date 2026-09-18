package info.hyperreal.journal.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object SecurityLock : Screen("security_lock")
    data object Main : Screen("main")
    data object Journal : Screen("journal")
    data object Substances : Screen("substances")
    data object MixCalculator : Screen("mix_calculator")
    data object MatrixExplorer : Screen("matrix_explorer")
    data object Insights : Screen("insights")
    data object Settings : Screen("settings")

    data object AddIngestionFlow : Screen("add_ingestion_flow")
    data object ChooseSubstance : Screen("choose_substance")
    data object ChooseRoa : Screen("choose_roa")
    data object EnterDose : Screen("enter_dose")
    data object ConfirmIngestion : Screen("confirm_ingestion")

    data object SubstanceDetail : Screen("substance_detail/{substanceId}") {
        fun createRoute(substanceId: String) = "substance_detail/$substanceId"
    }
}
