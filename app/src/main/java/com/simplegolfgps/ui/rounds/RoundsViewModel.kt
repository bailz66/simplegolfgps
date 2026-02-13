package com.simplegolfgps.ui.rounds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simplegolfgps.data.AppDatabase
import com.simplegolfgps.data.Round
import com.simplegolfgps.data.WeatherType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoundWithShotCount(
    val round: Round,
    val shotCount: Int
)

class RoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val roundDao = db.roundDao()
    private val shotDao = db.shotDao()

    val rounds: StateFlow<List<RoundWithShotCount>> = roundDao.getAll()
        .map { rounds ->
            rounds.map { round ->
                RoundWithShotCount(round, shotDao.getShotCountByRoundIdOnce(round.id))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createRound(
        courseName: String,
        weatherType: WeatherType,
        temperature: Int?,
        windCondition: String?,
        startingHole: Int,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = roundDao.insert(
                Round(
                    courseName = courseName,
                    weatherType = weatherType,
                    temperature = temperature,
                    windCondition = windCondition,
                    startingHole = startingHole
                )
            )
            onCreated(id)
        }
    }

    fun updateRound(round: Round) {
        viewModelScope.launch { roundDao.update(round) }
    }

    fun deleteRound(round: Round) {
        viewModelScope.launch { roundDao.delete(round) }
    }

    suspend fun getRoundById(id: Long): Round? = roundDao.getById(id)
}
