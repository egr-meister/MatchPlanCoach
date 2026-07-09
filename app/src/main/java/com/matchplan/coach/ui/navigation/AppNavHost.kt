package com.matchplan.coach.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.screens.AddEditMatchScreen
import com.matchplan.coach.ui.screens.AddEditPlayerScreen
import com.matchplan.coach.ui.screens.HistoryScreen
import com.matchplan.coach.ui.screens.HomeScreen
import com.matchplan.coach.ui.screens.LineupScreen
import com.matchplan.coach.ui.screens.MatchDetailScreen
import com.matchplan.coach.ui.screens.NotesScreen
import com.matchplan.coach.ui.screens.OnboardingScreen
import com.matchplan.coach.ui.screens.PlayerListScreen
import com.matchplan.coach.ui.screens.ScheduleScreen
import com.matchplan.coach.ui.screens.ScheduleSettingsScreen
import com.matchplan.coach.ui.screens.SettingsScreen
import com.matchplan.coach.ui.screens.SquadScreen
import com.matchplan.coach.ui.screens.TasksScreen

@Composable
fun AppNavHost(viewModel: AppViewModel) {
    val ready by viewModel.ready.collectAsState()
    val data by viewModel.appData.collectAsState()
    val navController = rememberNavController()

    // Wait for the first real DataStore emission so returning users don't flash
    // the onboarding screen.
    if (!ready) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {}
        return
    }

    val startDestination = remember {
        if (data.settings.onboardingCompleted) Routes.HOME else Routes.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    viewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) { HomeScreen(viewModel, navController) }

        composable(Routes.ADD_MATCH) {
            AddEditMatchScreen(viewModel, navController, matchId = null)
        }

        composable(
            Routes.EDIT_MATCH,
            arguments = listOf(navArgument(Routes.ARG_MATCH_ID) { type = NavType.StringType })
        ) { entry ->
            val matchId = entry.arguments?.getString(Routes.ARG_MATCH_ID)
            AddEditMatchScreen(viewModel, navController, matchId = matchId)
        }

        composable(
            Routes.MATCH_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_MATCH_ID) { type = NavType.StringType })
        ) { entry ->
            val matchId = entry.arguments?.getString(Routes.ARG_MATCH_ID)
            MatchDetailScreen(viewModel, navController, matchId = matchId)
        }

        composable(Routes.PLAYER_LIST) { PlayerListScreen(viewModel, navController) }

        composable(Routes.ADD_PLAYER) {
            AddEditPlayerScreen(viewModel, navController, playerId = null)
        }

        composable(
            Routes.EDIT_PLAYER,
            arguments = listOf(navArgument(Routes.ARG_PLAYER_ID) { type = NavType.StringType })
        ) { entry ->
            val playerId = entry.arguments?.getString(Routes.ARG_PLAYER_ID)
            AddEditPlayerScreen(viewModel, navController, playerId = playerId)
        }

        composable(
            Routes.SQUAD,
            arguments = listOf(navArgument(Routes.ARG_MATCH_ID) { type = NavType.StringType })
        ) { entry ->
            SquadScreen(viewModel, navController, entry.arguments?.getString(Routes.ARG_MATCH_ID))
        }

        composable(
            Routes.LINEUP,
            arguments = listOf(navArgument(Routes.ARG_MATCH_ID) { type = NavType.StringType })
        ) { entry ->
            LineupScreen(viewModel, navController, entry.arguments?.getString(Routes.ARG_MATCH_ID))
        }

        composable(
            Routes.TASKS,
            arguments = listOf(navArgument(Routes.ARG_MATCH_ID) { type = NavType.StringType })
        ) { entry ->
            TasksScreen(viewModel, navController, entry.arguments?.getString(Routes.ARG_MATCH_ID))
        }

        composable(
            Routes.NOTES,
            arguments = listOf(navArgument(Routes.ARG_MATCH_ID) { type = NavType.StringType })
        ) { entry ->
            NotesScreen(viewModel, navController, entry.arguments?.getString(Routes.ARG_MATCH_ID))
        }

        composable(Routes.HISTORY) { HistoryScreen(viewModel, navController) }
        composable(Routes.SCHEDULE) { ScheduleScreen(viewModel, navController) }
        composable(Routes.SCHEDULE_SETTINGS) { ScheduleSettingsScreen(viewModel, navController) }
        composable(Routes.SETTINGS) { SettingsScreen(viewModel, navController) }
    }
}
