package com.matchplan.coach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.navigation.AppNavHost
import com.matchplan.coach.ui.theme.MatchPlanCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val app = application as MatchPlanApplication

        setContent {
            MatchPlanCoachTheme {
                val viewModel: AppViewModel = viewModel(
                    factory = AppViewModel.Factory(app.appRepository, app.footballRepository)
                )
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
