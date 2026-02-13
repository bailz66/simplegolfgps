package com.simplegolfgps.ui.navigation

sealed class Screen(val route: String) {
    data object RoundsList : Screen("rounds_list")
    data object CreateRound : Screen("create_round")
    data object EditRound : Screen("edit_round/{roundId}") {
        fun createRoute(roundId: Long) = "edit_round/$roundId"
    }
    data object ShotRecording : Screen("shot_recording/{roundId}") {
        fun createRoute(roundId: Long) = "shot_recording/$roundId"
    }
    data object EditShot : Screen("edit_shot/{shotId}") {
        fun createRoute(shotId: Long) = "edit_shot/$shotId"
    }
    data object Settings : Screen("settings")
    data object Analytics : Screen("analytics")
    data object Dispersion : Screen("dispersion")
}
