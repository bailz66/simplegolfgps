package com.simplegolfgps.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val USE_IMPERIAL = booleanPreferencesKey("use_imperial")
        val ENABLED_CLUBS = stringPreferencesKey("enabled_clubs")
        val CLUB_ORDER = stringPreferencesKey("club_order")
        val SHOW_WIND = booleanPreferencesKey("show_wind")
        val SHOW_LIE = booleanPreferencesKey("show_lie")
        val SHOW_STRIKE = booleanPreferencesKey("show_strike")
        val SHOW_BALL_FLIGHT = booleanPreferencesKey("show_ball_flight")
        val SHOW_CLUB_DIRECTION = booleanPreferencesKey("show_club_direction")
        val SHOW_BALL_DIRECTION = booleanPreferencesKey("show_ball_direction")
        val SHOW_DIRECTION_TO_TARGET = booleanPreferencesKey("show_direction_to_target")
        val SHOW_DISTANCE_TO_TARGET = booleanPreferencesKey("show_distance_to_target")
        val SHOW_LIE_DIRECTION = booleanPreferencesKey("show_lie_direction")
        val SHOW_SHOT_TYPE = booleanPreferencesKey("show_shot_type")
        val SHOW_MENTAL_STATE = booleanPreferencesKey("show_mental_state")
        val SHOW_CARRY_DISTANCE = booleanPreferencesKey("show_carry_distance")
        val SHOW_FAIRWAY_HIT = booleanPreferencesKey("show_fairway_hit")
        val SHOW_GREEN_IN_REGULATION = booleanPreferencesKey("show_green_in_regulation")
        val SHOW_TARGET_DISTANCE = booleanPreferencesKey("show_target_distance")

        val DEFAULT_CLUBS = listOf(
            "Driver", "3-Wood", "5-Wood", "3-Hybrid", "4-Hybrid",
            "3-Iron", "4-Iron", "5-Iron", "6-Iron", "7-Iron", "8-Iron", "9-Iron",
            "PW", "GW", "SW", "LW", "Putter"
        )
    }

    val useImperial: Flow<Boolean> = context.dataStore.data.map { it[USE_IMPERIAL] ?: false }

    val enabledClubs: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[ENABLED_CLUBS]
        if (raw.isNullOrEmpty()) DEFAULT_CLUBS
        else raw.split("|").filter { it.isNotEmpty() }
    }

    val clubOrder: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[CLUB_ORDER]
        if (raw.isNullOrEmpty()) DEFAULT_CLUBS
        else raw.split("|").filter { it.isNotEmpty() }
    }

    val showBallDirection: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BALL_DIRECTION] ?: true }
    val showClubDirection: Flow<Boolean> = context.dataStore.data.map { it[SHOW_CLUB_DIRECTION] ?: true }
    val showDistanceToTarget: Flow<Boolean> = context.dataStore.data.map { it[SHOW_DISTANCE_TO_TARGET] ?: true }
    val showDirectionToTarget: Flow<Boolean> = context.dataStore.data.map { it[SHOW_DIRECTION_TO_TARGET] ?: true }
    val showWind: Flow<Boolean> = context.dataStore.data.map { it[SHOW_WIND] ?: false }
    val showLie: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LIE] ?: true }
    val showLieDirection: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LIE_DIRECTION] ?: false }
    val showShotType: Flow<Boolean> = context.dataStore.data.map { it[SHOW_SHOT_TYPE] ?: true }
    val showStrike: Flow<Boolean> = context.dataStore.data.map { it[SHOW_STRIKE] ?: false }
    val showBallFlight: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BALL_FLIGHT] ?: false }
    val showMentalState: Flow<Boolean> = context.dataStore.data.map { it[SHOW_MENTAL_STATE] ?: false }
    val showCarryDistance: Flow<Boolean> = context.dataStore.data.map { it[SHOW_CARRY_DISTANCE] ?: false }
    val showFairwayHit: Flow<Boolean> = context.dataStore.data.map { it[SHOW_FAIRWAY_HIT] ?: true }
    val showGreenInRegulation: Flow<Boolean> = context.dataStore.data.map { it[SHOW_GREEN_IN_REGULATION] ?: true }
    val showTargetDistance: Flow<Boolean> = context.dataStore.data.map { it[SHOW_TARGET_DISTANCE] ?: false }

    suspend fun setUseImperial(value: Boolean) {
        context.dataStore.edit { it[USE_IMPERIAL] = value }
    }

    suspend fun setEnabledClubs(clubs: List<String>) {
        context.dataStore.edit { it[ENABLED_CLUBS] = clubs.joinToString("|") }
    }

    suspend fun setClubOrder(clubs: List<String>) {
        context.dataStore.edit { it[CLUB_ORDER] = clubs.joinToString("|") }
    }

    suspend fun setShowWind(value: Boolean) { context.dataStore.edit { it[SHOW_WIND] = value } }
    suspend fun setShowLie(value: Boolean) { context.dataStore.edit { it[SHOW_LIE] = value } }
    suspend fun setShowLieDirection(value: Boolean) { context.dataStore.edit { it[SHOW_LIE_DIRECTION] = value } }
    suspend fun setShowShotType(value: Boolean) { context.dataStore.edit { it[SHOW_SHOT_TYPE] = value } }
    suspend fun setShowStrike(value: Boolean) { context.dataStore.edit { it[SHOW_STRIKE] = value } }
    suspend fun setShowBallFlight(value: Boolean) { context.dataStore.edit { it[SHOW_BALL_FLIGHT] = value } }
    suspend fun setShowClubDirection(value: Boolean) { context.dataStore.edit { it[SHOW_CLUB_DIRECTION] = value } }
    suspend fun setShowBallDirection(value: Boolean) { context.dataStore.edit { it[SHOW_BALL_DIRECTION] = value } }
    suspend fun setShowDirectionToTarget(value: Boolean) { context.dataStore.edit { it[SHOW_DIRECTION_TO_TARGET] = value } }
    suspend fun setShowDistanceToTarget(value: Boolean) { context.dataStore.edit { it[SHOW_DISTANCE_TO_TARGET] = value } }
    suspend fun setShowMentalState(value: Boolean) { context.dataStore.edit { it[SHOW_MENTAL_STATE] = value } }
    suspend fun setShowCarryDistance(value: Boolean) { context.dataStore.edit { it[SHOW_CARRY_DISTANCE] = value } }
    suspend fun setShowFairwayHit(value: Boolean) { context.dataStore.edit { it[SHOW_FAIRWAY_HIT] = value } }
    suspend fun setShowGreenInRegulation(value: Boolean) { context.dataStore.edit { it[SHOW_GREEN_IN_REGULATION] = value } }
    suspend fun setShowTargetDistance(value: Boolean) { context.dataStore.edit { it[SHOW_TARGET_DISTANCE] = value } }
}
