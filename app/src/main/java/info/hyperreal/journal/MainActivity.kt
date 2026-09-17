package info.hyperreal.journal

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import info.hyperreal.journal.data.local.datastore.UserPreferencesRepository
import info.hyperreal.journal.ui.theme.HyperrealTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val prefs by userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = info.hyperreal.journal.data.local.datastore.UserPreferences()
            )
            HyperrealTheme(isDarkMode = prefs.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    info.hyperreal.journal.ui.navigation.RootNavGraph()
                }
            }
        }
    }
}
