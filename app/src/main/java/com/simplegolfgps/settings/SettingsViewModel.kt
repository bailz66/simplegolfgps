package com.simplegolfgps.settings

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Stable
data class SettingsState(
    val useImperial: Boolean = false,
    val enabledClubs: List<String> = SettingsDataStore.DEFAULT_CLUBS,
    val clubOrder: List<String> = SettingsDataStore.DEFAULT_CLUBS,
    val showWind: Boolean = false,
    val showLie: Boolean = true,
    val showLieDirection: Boolean = false,
    val showShotType: Boolean = true,
    val showStrike: Boolean = false,
    val showBallFlight: Boolean = false,
    val showClubDirection: Boolean = true,
    val showBallDirection: Boolean = true,
    val showDirectionToTarget: Boolean = true,
    val showDistanceToTarget: Boolean = true,
    val showMentalState: Boolean = false,
    val showCarryDistance: Boolean = false,
    val showFairwayHit: Boolean = false,
    val showGreenInRegulation: Boolean = false,
    val showTargetDistance: Boolean = false,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    val state: StateFlow<SettingsState> = combine(
        dataStore.useImperial,
        dataStore.enabledClubs,
        dataStore.clubOrder,
        dataStore.showWind,
        dataStore.showLie,
    ) { imperial, clubs, order, wind, lie ->
        SettingsState(
            useImperial = imperial,
            enabledClubs = clubs,
            clubOrder = order,
            showWind = wind,
            showLie = lie,
        )
    }.combine(
        combine(
            dataStore.showLieDirection,
            dataStore.showShotType,
            dataStore.showStrike,
            dataStore.showBallFlight,
            dataStore.showClubDirection,
        ) { lieDir, shotType, strike, ballFlight, clubDir ->
            listOf(lieDir, shotType, strike, ballFlight, clubDir)
        }
    ) { partial, extras ->
        partial.copy(
            showLieDirection = extras[0],
            showShotType = extras[1],
            showStrike = extras[2],
            showBallFlight = extras[3],
            showClubDirection = extras[4],
        )
    }.combine(
        combine(
            dataStore.showBallDirection,
            dataStore.showDirectionToTarget,
            dataStore.showDistanceToTarget,
            dataStore.showMentalState,
            dataStore.showCarryDistance,
        ) { ballDir, dirTarget, distTarget, mental, carry ->
            listOf(ballDir, dirTarget, distTarget, mental, carry)
        }
    ) { partial, extras ->
        partial.copy(
            showBallDirection = extras[0],
            showDirectionToTarget = extras[1],
            showDistanceToTarget = extras[2],
            showMentalState = extras[3],
            showCarryDistance = extras[4],
        )
    }.combine(
        combine(
            dataStore.showFairwayHit,
            dataStore.showGreenInRegulation,
            dataStore.showTargetDistance,
        ) { fairway, gir, target -> listOf(fairway, gir, target) }
    ) { partial, extras ->
        partial.copy(
            showFairwayHit = extras[0],
            showGreenInRegulation = extras[1],
            showTargetDistance = extras[2],
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun setUseImperial(value: Boolean) = viewModelScope.launch { dataStore.setUseImperial(value) }
    fun setEnabledClubs(clubs: List<String>) = viewModelScope.launch { dataStore.setEnabledClubs(clubs) }
    fun setClubOrder(clubs: List<String>) = viewModelScope.launch { dataStore.setClubOrder(clubs) }
    fun setShowWind(value: Boolean) = viewModelScope.launch { dataStore.setShowWind(value) }
    fun setShowLie(value: Boolean) = viewModelScope.launch { dataStore.setShowLie(value) }
    fun setShowLieDirection(value: Boolean) = viewModelScope.launch { dataStore.setShowLieDirection(value) }
    fun setShowShotType(value: Boolean) = viewModelScope.launch { dataStore.setShowShotType(value) }
    fun setShowStrike(value: Boolean) = viewModelScope.launch { dataStore.setShowStrike(value) }
    fun setShowBallFlight(value: Boolean) = viewModelScope.launch { dataStore.setShowBallFlight(value) }
    fun setShowClubDirection(value: Boolean) = viewModelScope.launch { dataStore.setShowClubDirection(value) }
    fun setShowBallDirection(value: Boolean) = viewModelScope.launch { dataStore.setShowBallDirection(value) }
    fun setShowDirectionToTarget(value: Boolean) = viewModelScope.launch { dataStore.setShowDirectionToTarget(value) }
    fun setShowDistanceToTarget(value: Boolean) = viewModelScope.launch { dataStore.setShowDistanceToTarget(value) }
    fun setShowMentalState(value: Boolean) = viewModelScope.launch { dataStore.setShowMentalState(value) }
    fun setShowCarryDistance(value: Boolean) = viewModelScope.launch { dataStore.setShowCarryDistance(value) }
    fun setShowFairwayHit(value: Boolean) = viewModelScope.launch { dataStore.setShowFairwayHit(value) }
    fun setShowGreenInRegulation(value: Boolean) = viewModelScope.launch { dataStore.setShowGreenInRegulation(value) }
    fun setShowTargetDistance(value: Boolean) = viewModelScope.launch { dataStore.setShowTargetDistance(value) }

    fun toggleClub(club: String) {
        val current = state.value.enabledClubs.toMutableList()
        if (current.contains(club)) current.remove(club) else current.add(club)
        setEnabledClubs(current)
    }

    fun moveClub(from: Int, to: Int) {
        val current = state.value.clubOrder.toMutableList()
        if (from in current.indices && to in current.indices) {
            val item = current.removeAt(from)
            current.add(to, item)
            setClubOrder(current)
        }
    }
}
