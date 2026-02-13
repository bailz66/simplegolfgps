package com.simplegolfgps.ui.shots

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.simplegolfgps.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

data class MeasurementState(
    val isMeasuring: Boolean = false,
    val startLocation: Location? = null,
    val startLocked: Boolean = false,
    val carryLocked: Boolean = false,
    val currentAccuracy: Float? = null,
    val liveDistance: Double? = null,
    val liveElevation: Double? = null,
    val measuredDistance: Double? = null,
)

data class ShotFormState(
    val holeNumber: Int = 1,
    val shotNumber: Int = 1,
    val clubUsed: String? = null,
    val distance: String = "",
    val carryDistance: String = "",
    val carryElevationChange: Double = 0.0,
    val elevationChange: Double = 0.0,
    val windDirection: WindDirection? = null,
    val windStrength: WindStrength? = null,
    val lie: Lie? = null,
    val shotType: ShotType? = null,
    val strike: Strike? = null,
    val clubDirection: ClubDirection? = null,
    val ballDirection: BallDirection? = null,
    val lieDirection: LieDirection? = null,
    val mentalState: MentalState? = null,
    val mentalStateNote: String = "",
    val ballFlight: BallFlight? = null,
    val directionToTarget: DirectionToTarget? = null,
    val distanceToTarget: DistanceToTarget? = null,
    val customNote: String = "",
    val ignoreForAnalytics: Boolean = false,
    val fairwayHit: FairwayHit? = null,
    val greenInRegulation: GreenInRegulation? = null,
    val targetDistance: String = "",
)

class ShotViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val shotDao = db.shotDao()
    private val roundDao = db.roundDao()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _measurementState = MutableStateFlow(MeasurementState())
    val measurementState: StateFlow<MeasurementState> = _measurementState.asStateFlow()

    private val _formState = MutableStateFlow(ShotFormState())
    val formState: StateFlow<ShotFormState> = _formState.asStateFlow()

    private val _roundId = MutableStateFlow(0L)

    private val _round = MutableStateFlow<Round?>(null)
    val round: StateFlow<Round?> = _round.asStateFlow()

    val shots: StateFlow<List<Shot>> = _roundId.flatMapLatest { id ->
        if (id > 0) shotDao.getShotsByRoundId(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var locationCallback: LocationCallback? = null
    private var lockJob: Job? = null

    fun loadRound(roundId: Long) {
        _roundId.value = roundId
        viewModelScope.launch {
            val round = roundDao.getById(roundId)
            _round.value = round
            if (round != null) {
                val hole = round.startingHole
                val shotCount = shotDao.getShotCountByHole(roundId, hole)
                _formState.update { it.copy(holeNumber = hole, shotNumber = shotCount + 1) }
            }
        }
    }

    fun updateForm(update: ShotFormState.() -> ShotFormState) {
        _formState.update { it.update() }
    }

    fun onHoleChanged(newHole: Int) {
        val roundId = _roundId.value
        if (roundId <= 0) return
        viewModelScope.launch {
            val shotCount = shotDao.getShotCountByHole(roundId, newHole)
            _formState.value = ShotFormState(holeNumber = newHole, shotNumber = shotCount + 1)
            _measurementState.value = MeasurementState()
        }
    }

    fun onShotNumberChanged(newShotNumber: Int) {
        val roundId = _roundId.value
        if (roundId <= 0 || newShotNumber < 1) return
        val currentHole = _formState.value.holeNumber
        viewModelScope.launch {
            val existing = shotDao.getByRoundHoleShot(roundId, currentHole, newShotNumber)
            if (existing != null) {
                _formState.value = ShotFormState(
                    holeNumber = existing.holeNumber,
                    shotNumber = existing.shotNumber,
                    clubUsed = existing.clubUsed,
                    distance = existing.distance?.let { "%.1f".format(it) } ?: "",
                    carryDistance = existing.carryDistance?.let { "%.1f".format(it) } ?: "",
                    carryElevationChange = existing.carryElevationChange ?: 0.0,
                    elevationChange = existing.elevationChange ?: 0.0,
                    windDirection = existing.windDirection,
                    windStrength = existing.windStrength,
                    lie = existing.lie,
                    shotType = existing.shotType,
                    strike = existing.strike,
                    clubDirection = existing.clubDirection,
                    ballDirection = existing.ballDirection,
                    lieDirection = existing.lieDirection,
                    mentalState = existing.mentalState,
                    mentalStateNote = existing.mentalStateNote ?: "",
                    ballFlight = existing.ballFlight,
                    directionToTarget = existing.directionToTarget,
                    distanceToTarget = existing.distanceToTarget,
                    customNote = existing.customNote ?: "",
                    ignoreForAnalytics = existing.ignoreForAnalytics,
                    fairwayHit = existing.fairwayHit,
                    greenInRegulation = existing.greenInRegulation,
                    targetDistance = existing.targetDistance?.let { "%.1f".format(it) } ?: "",
                )
            } else {
                _formState.value = ShotFormState(holeNumber = currentHole, shotNumber = newShotNumber)
            }
            _measurementState.value = MeasurementState()
        }
    }

    fun hasLocationPermission(): Boolean {
        val context = getApplication<Application>()
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startMeasurement() {
        if (!hasLocationPermission()) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val current = _measurementState.value

                if (!current.startLocked) {
                    // Still positioning — keep the best accuracy location
                    val isBetter = current.startLocation == null ||
                            location.accuracy < (current.startLocation?.accuracy ?: Float.MAX_VALUE)
                    _measurementState.update {
                        it.copy(
                            startLocation = if (isBetter) location else it.startLocation,
                            currentAccuracy = location.accuracy,
                        )
                    }
                } else {
                    // Start is locked — calculate live distance and elevation from start
                    val start = current.startLocation ?: return
                    val distance = haversineDistance(
                        start.latitude, start.longitude,
                        location.latitude, location.longitude,
                    )
                    val elevation = location.altitude - start.altitude
                    _measurementState.update {
                        it.copy(
                            currentAccuracy = location.accuracy,
                            liveDistance = distance,
                            liveElevation = elevation,
                        )
                    }
                }
            }
        }

        _measurementState.update { MeasurementState(isMeasuring = true) }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback!!, null
        )

        // Wait for first GPS fix, then 3 seconds of positioning, then lock
        lockJob = viewModelScope.launch {
            _measurementState.first { it.startLocation != null }
            delay(3000L)
            _measurementState.update { it.copy(startLocked = true) }
        }
    }

    fun finishMeasurement() {
        val current = _measurementState.value
        val distance = current.liveDistance

        if (distance != null) {
            _measurementState.update {
                it.copy(isMeasuring = false, measuredDistance = distance)
            }
            _formState.update {
                it.copy(
                    distance = "%.1f".format(distance),
                    elevationChange = current.liveElevation ?: 0.0,
                )
            }
        } else {
            _measurementState.update { it.copy(isMeasuring = false) }
        }

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    fun lockCarryDistance() {
        val current = _measurementState.value
        val distance = current.liveDistance ?: return
        _formState.update {
            it.copy(
                carryDistance = "%.1f".format(distance),
                carryElevationChange = current.liveElevation ?: 0.0,
            )
        }
        _measurementState.update { it.copy(carryLocked = true) }
    }

    fun cancelMeasurement() {
        lockJob?.cancel()
        lockJob = null
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        _measurementState.update { MeasurementState() }
    }

    fun saveShot(onSaved: () -> Unit) {
        val form = _formState.value
        val roundId = _roundId.value
        if (roundId <= 0) return

        val distanceValue = form.distance.toDoubleOrNull()
        val carryValue = form.carryDistance.toDoubleOrNull()
        val targetDistValue = form.targetDistance.toDoubleOrNull()

        viewModelScope.launch {
            val existing = shotDao.getByRoundHoleShot(roundId, form.holeNumber, form.shotNumber)
            val shotData = Shot(
                id = existing?.id ?: 0,
                roundId = roundId,
                holeNumber = form.holeNumber,
                shotNumber = form.shotNumber,
                clubUsed = form.clubUsed,
                distance = distanceValue,
                carryDistance = carryValue,
                carryElevationChange = if (form.carryElevationChange != 0.0) form.carryElevationChange else null,
                elevationChange = if (form.elevationChange != 0.0) form.elevationChange else null,
                windDirection = form.windDirection,
                windStrength = form.windStrength,
                lie = form.lie,
                shotType = form.shotType,
                strike = form.strike,
                clubDirection = form.clubDirection,
                ballDirection = form.ballDirection,
                lieDirection = form.lieDirection,
                mentalState = form.mentalState,
                mentalStateNote = form.mentalStateNote.ifBlank { null },
                ballFlight = form.ballFlight,
                directionToTarget = form.directionToTarget,
                distanceToTarget = form.distanceToTarget,
                customNote = form.customNote.ifBlank { null },
                ignoreForAnalytics = form.ignoreForAnalytics,
                fairwayHit = form.fairwayHit,
                greenInRegulation = form.greenInRegulation,
                targetDistance = targetDistValue,
                timestamp = existing?.timestamp ?: System.currentTimeMillis(),
            )
            if (existing != null) {
                shotDao.update(shotData)
            } else {
                shotDao.insert(shotData)
            }
            // Advance to next shot number
            val currentHole = form.holeNumber
            val nextShot = form.shotNumber + 1
            _formState.value = ShotFormState(holeNumber = currentHole, shotNumber = nextShot)
            _measurementState.value = MeasurementState()
            onSaved()
        }
    }

    fun loadShotForEditing(shotId: Long) {
        viewModelScope.launch {
            val shot = shotDao.getById(shotId) ?: return@launch
            _roundId.value = shot.roundId
            _formState.value = ShotFormState(
                holeNumber = shot.holeNumber,
                shotNumber = shot.shotNumber,
                clubUsed = shot.clubUsed,
                distance = shot.distance?.let { "%.1f".format(it) } ?: "",
                carryDistance = shot.carryDistance?.let { "%.1f".format(it) } ?: "",
                carryElevationChange = shot.carryElevationChange ?: 0.0,
                elevationChange = shot.elevationChange ?: 0.0,
                windDirection = shot.windDirection,
                windStrength = shot.windStrength,
                lie = shot.lie,
                shotType = shot.shotType,
                strike = shot.strike,
                clubDirection = shot.clubDirection,
                ballDirection = shot.ballDirection,
                lieDirection = shot.lieDirection,
                mentalState = shot.mentalState,
                mentalStateNote = shot.mentalStateNote ?: "",
                ballFlight = shot.ballFlight,
                directionToTarget = shot.directionToTarget,
                distanceToTarget = shot.distanceToTarget,
                customNote = shot.customNote ?: "",
                ignoreForAnalytics = shot.ignoreForAnalytics,
                fairwayHit = shot.fairwayHit,
                greenInRegulation = shot.greenInRegulation,
                targetDistance = shot.targetDistance?.let { "%.1f".format(it) } ?: "",
            )
        }
    }

    fun updateShot(shotId: Long, onUpdated: () -> Unit) {
        val form = _formState.value
        val roundId = _roundId.value
        if (roundId <= 0) return

        viewModelScope.launch {
            val existing = shotDao.getById(shotId) ?: return@launch
            shotDao.update(
                existing.copy(
                    holeNumber = form.holeNumber,
                    shotNumber = form.shotNumber,
                    clubUsed = form.clubUsed,
                    distance = form.distance.toDoubleOrNull(),
                    carryDistance = form.carryDistance.toDoubleOrNull(),
                    carryElevationChange = if (form.carryElevationChange != 0.0) form.carryElevationChange else null,
                    elevationChange = if (form.elevationChange != 0.0) form.elevationChange else null,
                    windDirection = form.windDirection,
                    windStrength = form.windStrength,
                    lie = form.lie,
                    shotType = form.shotType,
                    strike = form.strike,
                    clubDirection = form.clubDirection,
                    ballDirection = form.ballDirection,
                    lieDirection = form.lieDirection,
                    mentalState = form.mentalState,
                    mentalStateNote = form.mentalStateNote.ifBlank { null },
                    ballFlight = form.ballFlight,
                    directionToTarget = form.directionToTarget,
                    distanceToTarget = form.distanceToTarget,
                    customNote = form.customNote.ifBlank { null },
                    ignoreForAnalytics = form.ignoreForAnalytics,
                    fairwayHit = form.fairwayHit,
                    greenInRegulation = form.greenInRegulation,
                    targetDistance = form.targetDistance.toDoubleOrNull(),
                )
            )
            onUpdated()
        }
    }

    fun deleteShot(shot: Shot) {
        viewModelScope.launch { shotDao.delete(shot) }
    }

    override fun onCleared() {
        super.onCleared()
        lockJob?.cancel()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    companion object {
        fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371000.0 // Earth's radius in metres
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }
}
