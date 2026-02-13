package com.simplegolfgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simplegolfgps.analytics.AnalyticsScreen
import com.simplegolfgps.analytics.AnalyticsViewModel
import com.simplegolfgps.settings.SettingsScreen
import com.simplegolfgps.settings.SettingsViewModel
import com.simplegolfgps.ui.navigation.Screen
import com.simplegolfgps.ui.rounds.CreateRoundScreen
import com.simplegolfgps.ui.rounds.EditRoundScreen
import com.simplegolfgps.ui.rounds.RoundsListScreen
import com.simplegolfgps.ui.rounds.RoundsViewModel
import com.simplegolfgps.ui.shots.EditShotScreen
import com.simplegolfgps.ui.shots.ShotRecordingScreen
import com.simplegolfgps.ui.shots.ShotViewModel
import com.simplegolfgps.ui.theme.SimpleGolfGPSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleGolfGPSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel()
    val roundsViewModel: RoundsViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()

    val settingsState by settingsViewModel.state.collectAsState()
    val rounds by roundsViewModel.rounds.collectAsState()

    NavHost(navController = navController, startDestination = Screen.RoundsList.route) {

        // Rounds List
        composable(Screen.RoundsList.route) {
            RoundsListScreen(
                rounds = rounds,
                settings = settingsState,
                onCreateRound = { navController.navigate(Screen.CreateRound.route) },
                onRoundClick = { id -> navController.navigate(Screen.ShotRecording.createRoute(id)) },
                onDeleteRound = { roundsViewModel.deleteRound(it) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onAnalyticsClick = { navController.navigate(Screen.Analytics.route) },
            )
        }

        // Create Round
        composable(Screen.CreateRound.route) {
            CreateRoundScreen(
                settings = settingsState,
                onSave = { name, weather, temp, wind, hole ->
                    roundsViewModel.createRound(name, weather, temp, wind, hole) { id ->
                        navController.popBackStack()
                        navController.navigate(Screen.ShotRecording.createRoute(id))
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // Edit Round
        composable(
            Screen.EditRound.route,
            arguments = listOf(navArgument("roundId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val roundId = backStackEntry.arguments?.getLong("roundId") ?: return@composable
            var round by remember { mutableStateOf<com.simplegolfgps.data.Round?>(null) }
            LaunchedEffect(roundId) {
                round = roundsViewModel.getRoundById(roundId)
            }
            EditRoundScreen(
                round = round,
                settings = settingsState,
                onSave = { updated ->
                    roundsViewModel.updateRound(updated)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }

        // Shot Recording
        composable(
            Screen.ShotRecording.route,
            arguments = listOf(navArgument("roundId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val roundId = backStackEntry.arguments?.getLong("roundId") ?: return@composable
            val shotViewModel: ShotViewModel = viewModel()

            LaunchedEffect(roundId) {
                shotViewModel.loadRound(roundId)
            }

            val round by shotViewModel.round.collectAsState()
            val formState by shotViewModel.formState.collectAsState()
            val measurementState by shotViewModel.measurementState.collectAsState()
            val shots by shotViewModel.shots.collectAsState()

            ShotRecordingScreen(
                round = round,
                formState = formState,
                measurementState = measurementState,
                shots = shots,
                settings = settingsState,
                onUpdateForm = { shotViewModel.updateForm(it) },
                onStartMeasurement = { shotViewModel.startMeasurement() },
                onFinishMeasurement = { shotViewModel.finishMeasurement() },
                onLockCarryDistance = { shotViewModel.lockCarryDistance() },
                onCancelMeasurement = { shotViewModel.cancelMeasurement() },
                onSaveShot = { shotViewModel.saveShot {} },
                onShotClick = { shotId ->
                    navController.navigate(Screen.EditShot.createRoute(shotId))
                },
                onDeleteShot = { shotViewModel.deleteShot(it) },
                onEditRound = { navController.navigate(Screen.EditRound.createRoute(roundId)) },
                onHoleChanged = { shotViewModel.onHoleChanged(it) },
                onShotNumberChanged = { shotViewModel.onShotNumberChanged(it) },
                onBack = { navController.popBackStack() },
                hasLocationPermission = shotViewModel.hasLocationPermission(),
            )
        }

        // Edit Shot
        composable(
            Screen.EditShot.route,
            arguments = listOf(navArgument("shotId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val shotId = backStackEntry.arguments?.getLong("shotId") ?: return@composable
            val shotViewModel: ShotViewModel = viewModel()

            LaunchedEffect(shotId) {
                shotViewModel.loadShotForEditing(shotId)
            }

            val formState by shotViewModel.formState.collectAsState()

            EditShotScreen(
                formState = formState,
                settings = settingsState,
                onUpdateForm = { shotViewModel.updateForm(it) },
                onSave = {
                    shotViewModel.updateShot(shotId) {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                state = settingsState,
                onSetUseImperial = { settingsViewModel.setUseImperial(it) },
                onToggleClub = { settingsViewModel.toggleClub(it) },
                onMoveClub = { from, to -> settingsViewModel.moveClub(from, to) },
                onSetShowWind = { settingsViewModel.setShowWind(it) },
                onSetShowLie = { settingsViewModel.setShowLie(it) },
                onSetShowLieDirection = { settingsViewModel.setShowLieDirection(it) },
                onSetShowShotType = { settingsViewModel.setShowShotType(it) },
                onSetShowStrike = { settingsViewModel.setShowStrike(it) },
                onSetShowBallFlight = { settingsViewModel.setShowBallFlight(it) },
                onSetShowClubDirection = { settingsViewModel.setShowClubDirection(it) },
                onSetShowBallDirection = { settingsViewModel.setShowBallDirection(it) },
                onSetShowDirectionToTarget = { settingsViewModel.setShowDirectionToTarget(it) },
                onSetShowDistanceToTarget = { settingsViewModel.setShowDistanceToTarget(it) },
                onSetShowMentalState = { settingsViewModel.setShowMentalState(it) },
                onSetShowCarryDistance = { settingsViewModel.setShowCarryDistance(it) },
                onSetShowFairwayHit = { settingsViewModel.setShowFairwayHit(it) },
                onSetShowGreenInRegulation = { settingsViewModel.setShowGreenInRegulation(it) },
                onSetShowTargetDistance = { settingsViewModel.setShowTargetDistance(it) },
                onBack = { navController.popBackStack() },
            )
        }

        // Analytics
        composable(Screen.Analytics.route) {
            val dashboardState by analyticsViewModel.dashboardState.collectAsState()
            val shotAnalysisState by analyticsViewModel.shotAnalysisState.collectAsState()
            val filterState by analyticsViewModel.filterState.collectAsState()
            val selectedTab by analyticsViewModel.selectedTab.collectAsState()
            val courseNames by analyticsViewModel.courseNames.collectAsState()

            AnalyticsScreen(
                dashboardState = dashboardState,
                shotAnalysisState = shotAnalysisState,
                filterState = filterState,
                selectedTab = selectedTab,
                settings = settingsState,
                courseNames = courseNames,
                onSelectTab = { analyticsViewModel.selectTab(it) },
                onUpdateFilter = { analyticsViewModel.updateFilter(it) },
                onClearFilters = { analyticsViewModel.clearFilters() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
