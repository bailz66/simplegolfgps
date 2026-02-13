package com.simplegolfgps.ui.shots

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simplegolfgps.data.*
import com.simplegolfgps.settings.SettingsState
import com.simplegolfgps.settings.UnitConverter
import com.simplegolfgps.ui.components.ChipSelector
import com.simplegolfgps.ui.components.WindDirectionGrid
import com.simplegolfgps.ui.components.clubAbbreviation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShotRecordingScreen(
    round: Round?,
    formState: ShotFormState,
    measurementState: MeasurementState,
    shots: List<Shot>,
    settings: SettingsState,
    onUpdateForm: (ShotFormState.() -> ShotFormState) -> Unit,
    onStartMeasurement: () -> Unit,
    onFinishMeasurement: () -> Unit,
    onLockCarryDistance: () -> Unit,
    onCancelMeasurement: () -> Unit,
    onSaveShot: () -> Unit,
    onShotClick: (Long) -> Unit,
    onDeleteShot: (Shot) -> Unit,
    onEditRound: () -> Unit,
    onHoleChanged: (Int) -> Unit,
    onShotNumberChanged: (Int) -> Unit,
    onBack: () -> Unit,
    hasLocationPermission: Boolean,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onStartMeasurement()
    }

    var shotToDelete by remember { mutableStateOf<Shot?>(null) }
    var showSaveConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        round?.courseName ?: "Round",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Hole selector (wraps 1–18)
                    CompactSelector(
                        label = "H",
                        value = formState.holeNumber,
                        onDecrement = {
                            val prev = if (formState.holeNumber <= 1) 18 else formState.holeNumber - 1
                            onHoleChanged(prev)
                        },
                        onIncrement = {
                            val next = if (formState.holeNumber >= 18) 1 else formState.holeNumber + 1
                            onHoleChanged(next)
                        },
                    )
                    // Shot selector
                    CompactSelector(
                        label = "S",
                        value = formState.shotNumber,
                        onDecrement = {
                            if (formState.shotNumber > 1) onShotNumberChanged(formState.shotNumber - 1)
                        },
                        onIncrement = { onShotNumberChanged(formState.shotNumber + 1) },
                        decrementEnabled = formState.shotNumber > 1,
                    )
                    IconButton(onClick = onEditRound) {
                        Icon(Icons.Default.GolfCourse, contentDescription = "Edit Round")
                    }
                    FilledIconButton(
                        onClick = { showSaveConfirmation = true },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Shot")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Hole number and measurement section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (measurementState.isMeasuring) {
                    // Measuring state
                    val accuracyGood = (measurementState.currentAccuracy ?: Float.MAX_VALUE) < 3f
                    val accuracyColor = if (accuracyGood) Color(0xFF2E7D32) else Color(0xFFE65100)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Status header with accuracy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    if (measurementState.startLocked) "Start locked" else "Positioning...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (measurementState.startLocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                measurementState.currentAccuracy?.let { acc ->
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                "±${"%.1f".format(acc)}m",
                                                color = accuracyColor,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (accuracyGood) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = accuracyColor,
                                            )
                                        }
                                    )
                                }
                            }

                            // Live distance display
                            if (measurementState.startLocked && measurementState.liveDistance != null) {
                                val distUnit = UnitConverter.distanceUnit(settings.useImperial)
                                val displayDist = UnitConverter.metresToDisplay(
                                    measurementState.liveDistance, settings.useImperial
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        "${"%.1f".format(displayDist)} $distUnit",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    measurementState.liveElevation?.let { elev ->
                                        val elevDisplay = UnitConverter.metresToDisplay(
                                            kotlin.math.abs(elev), settings.useImperial
                                        )
                                        val arrow = if (elev >= 0) "\u2191" else "\u2193"
                                        Text(
                                            "$arrow${"%.0f".format(elevDisplay)}$distUnit",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                                // Show carry distance + elevation after it's been locked
                                if (measurementState.carryLocked) {
                                    val carryMetres = formState.carryDistance.toDoubleOrNull()
                                    if (carryMetres != null) {
                                        val carryDisplay = UnitConverter.metresToDisplay(carryMetres, settings.useImperial)
                                        val carryElev = formState.carryElevationChange
                                        val carryElevText = if (carryElev != 0.0) {
                                            val elevVal = UnitConverter.metresToDisplay(kotlin.math.abs(carryElev), settings.useImperial)
                                            val arrow = if (carryElev >= 0) "\u2191" else "\u2193"
                                            " $arrow${"%.0f".format(elevVal)}$distUnit"
                                        } else ""
                                        Text(
                                            "Carry: ${"%.1f".format(carryDisplay)} $distUnit$carryElevText",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }

                            // Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (settings.showCarryDistance && measurementState.startLocked && !measurementState.carryLocked) {
                                    // Carry mode: show Carry (primary) + Total (outlined)
                                    Button(
                                        onClick = onLockCarryDistance,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Icon(Icons.Default.FlightLand, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Carry")
                                    }
                                    OutlinedButton(
                                        onClick = onFinishMeasurement,
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Text("Total")
                                    }
                                } else {
                                    // Default: single Total/Finish button
                                    Button(
                                        onClick = onFinishMeasurement,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = measurementState.startLocked,
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (settings.showCarryDistance) "Total" else "Finish")
                                    }
                                }
                                OutlinedButton(
                                    onClick = onCancelMeasurement,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    // Start measurement button
                    FilledTonalButton(
                        onClick = {
                            if (hasLocationPermission) onStartMeasurement()
                            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(14.dp),
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Measure Shot", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Distance fields + Elevation
                val distUnit = UnitConverter.distanceUnit(settings.useImperial)
                val showCarry = settings.showCarryDistance || formState.carryDistance.toDoubleOrNull() != null

                val displayDistance = if (settings.useImperial) {
                    val metres = formState.distance.toDoubleOrNull()
                    if (metres != null) {
                        "%.1f".format(UnitConverter.metresToDisplay(metres, true))
                    } else formState.distance
                } else formState.distance

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Carry field first (when setting enabled or has value)
                    if (showCarry) {
                        val displayCarry = if (settings.useImperial) {
                            val metres = formState.carryDistance.toDoubleOrNull()
                            if (metres != null) "%.1f".format(UnitConverter.metresToDisplay(metres, true))
                            else formState.carryDistance
                        } else formState.carryDistance

                        OutlinedTextField(
                            value = displayCarry,
                            onValueChange = { newVal ->
                                val cleaned = newVal.filter { c -> c.isDigit() || c == '.' }
                                if (settings.useImperial) {
                                    val yards = cleaned.toDoubleOrNull()
                                    if (yards != null) {
                                        val metres = UnitConverter.displayToMetres(yards, true)
                                        onUpdateForm { copy(carryDistance = "%.1f".format(metres)) }
                                    } else {
                                        onUpdateForm { copy(carryDistance = cleaned) }
                                    }
                                } else {
                                    onUpdateForm { copy(carryDistance = cleaned) }
                                }
                            },
                            label = { Text("Carry ($distUnit)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                        )
                    }

                    // Total distance field
                    OutlinedTextField(
                        value = displayDistance,
                        onValueChange = { newVal ->
                            val cleaned = newVal.filter { c -> c.isDigit() || c == '.' }
                            if (settings.useImperial) {
                                val yards = cleaned.toDoubleOrNull()
                                if (yards != null) {
                                    val metres = UnitConverter.displayToMetres(yards, true)
                                    onUpdateForm { copy(distance = "%.1f".format(metres)) }
                                } else {
                                    onUpdateForm { copy(distance = cleaned) }
                                }
                            } else {
                                onUpdateForm { copy(distance = cleaned) }
                            }
                        },
                        label = { Text(if (showCarry) "Total ($distUnit)" else "Distance ($distUnit)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    // Elevation control
                    ElevationControl(
                        elevationMetres = formState.elevationChange,
                        useImperial = settings.useImperial,
                        onAdjust = { delta ->
                            onUpdateForm { copy(elevationChange = elevationChange + delta) }
                        },
                    )
                }

                // Target Distance field
                val showTarget = settings.showTargetDistance || formState.targetDistance.toDoubleOrNull() != null
                if (showTarget) {
                    val displayTarget = if (settings.useImperial) {
                        val metres = formState.targetDistance.toDoubleOrNull()
                        if (metres != null) "%.1f".format(UnitConverter.metresToDisplay(metres, true))
                        else formState.targetDistance
                    } else formState.targetDistance

                    OutlinedTextField(
                        value = displayTarget,
                        onValueChange = { newVal ->
                            val cleaned = newVal.filter { c -> c.isDigit() || c == '.' }
                            if (settings.useImperial) {
                                val yards = cleaned.toDoubleOrNull()
                                if (yards != null) {
                                    val metres = UnitConverter.displayToMetres(yards, true)
                                    onUpdateForm { copy(targetDistance = "%.1f".format(metres)) }
                                } else {
                                    onUpdateForm { copy(targetDistance = cleaned) }
                                }
                            } else {
                                onUpdateForm { copy(targetDistance = cleaned) }
                            }
                        },
                        label = { Text("Target Distance ($distUnit)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )
                }
            }

            Divider()

            // Club selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ChipSelector(
                    label = "Club",
                    options = settings.enabledClubs,
                    selected = formState.clubUsed,
                    displayName = { clubAbbreviation(it) },
                    onSelect = { club ->
                        onUpdateForm {
                            val newLie = if (club == "Putter") Lie.Green else lie
                            val newShotType = if (club == "Putter") ShotType.Putt else shotType
                            copy(clubUsed = club, lie = newLie, shotType = newShotType)
                        }
                    },
                    wrap = true,
                )

                // Lie — default on, wrapping layout
                if (settings.showLie) {
                    ChipSelector(
                        label = "Lie",
                        options = Lie.entries.toList(),
                        selected = formState.lie,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(lie = it) } },
                        wrap = true,
                    )
                }

                // Shot Type — default on
                if (settings.showShotType) {
                    ChipSelector(
                        label = "Shot Type",
                        options = ShotType.entries.toList(),
                        selected = formState.shotType,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(shotType = it) } },
                        wrap = true,
                    )
                }

                // Fairway Hit and GIR
                if (settings.showFairwayHit) {
                    ChipSelector(
                        label = "Fairway Hit",
                        options = FairwayHit.entries.toList(),
                        selected = formState.fairwayHit,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(fairwayHit = it) } },
                    )
                }

                if (settings.showGreenInRegulation) {
                    ChipSelector(
                        label = "Green in Reg.",
                        options = GreenInRegulation.entries.toList(),
                        selected = formState.greenInRegulation,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(greenInRegulation = it) } },
                    )
                }

                // Primary feedback
                if (settings.showBallDirection) {
                    ChipSelector(
                        label = "Ball Direction",
                        options = BallDirection.entries.toList(),
                        selected = formState.ballDirection,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(ballDirection = it) } },
                    )
                }

                if (settings.showClubDirection) {
                    ChipSelector(
                        label = "Club Direction",
                        options = ClubDirection.entries.toList(),
                        selected = formState.clubDirection,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(clubDirection = it) } },
                    )
                }

                if (settings.showDistanceToTarget) {
                    ChipSelector(
                        label = "Distance to Target",
                        options = DistanceToTarget.entries.toList(),
                        selected = formState.distanceToTarget,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(distanceToTarget = it) } },
                    )
                }

                if (settings.showDirectionToTarget) {
                    ChipSelector(
                        label = "Direction to Target",
                        options = DirectionToTarget.entries.toList(),
                        selected = formState.directionToTarget,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(directionToTarget = it) } },
                    )
                }

                // Additional feedback — hidden by default
                if (settings.showLieDirection) {
                    ChipSelector(
                        label = "Lie Direction",
                        options = LieDirection.entries.toList(),
                        selected = formState.lieDirection,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(lieDirection = it) } },
                    )
                }

                if (settings.showWind) {
                    WindDirectionGrid(
                        selected = formState.windDirection,
                        onSelect = { onUpdateForm { copy(windDirection = it) } },
                    )
                    ChipSelector(
                        label = "Wind Strength",
                        options = WindStrength.entries.toList(),
                        selected = formState.windStrength,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(windStrength = it) } },
                    )
                }

                if (settings.showStrike) {
                    ChipSelector(
                        label = "Strike",
                        options = Strike.entries.toList(),
                        selected = formState.strike,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(strike = it) } },
                    )
                }

                if (settings.showBallFlight) {
                    ChipSelector(
                        label = "Ball Flight",
                        options = BallFlight.entries.toList(),
                        selected = formState.ballFlight,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(ballFlight = it) } },
                    )
                }

                if (settings.showMentalState) {
                    ChipSelector(
                        label = "Mental State",
                        options = MentalState.entries.toList(),
                        selected = formState.mentalState,
                        displayName = { it.displayName },
                        onSelect = { onUpdateForm { copy(mentalState = it) } },
                    )
                    OutlinedTextField(
                        value = formState.mentalStateNote,
                        onValueChange = { onUpdateForm { copy(mentalStateNote = it) } },
                        label = { Text("Mental state note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 1,
                        maxLines = 3,
                    )
                }

                // Custom notes — always visible
                OutlinedTextField(
                    value = formState.customNote,
                    onValueChange = { onUpdateForm { copy(customNote = it) } },
                    label = { Text("Custom Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1,
                    maxLines = 3,
                )

                // Save shot button
                Button(
                    onClick = { showSaveConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Shot", style = MaterialTheme.typography.titleMedium)
                }

                // Ignore for analytics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = formState.ignoreForAnalytics,
                        onCheckedChange = { onUpdateForm { copy(ignoreForAnalytics = it) } },
                    )
                    Text(
                        "Ignore for analytics",
                        modifier = Modifier.clickable {
                            onUpdateForm { copy(ignoreForAnalytics = !ignoreForAnalytics) }
                        },
                    )
                }

            }

            Divider()

            // Shot history for this round
            if (shots.isNotEmpty()) {
                Text(
                    "Shots in this round",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                )
                shots.forEach { shot ->
                    ShotListItem(
                        shot = shot,
                        settings = settings,
                        onClick = { onShotClick(shot.id) },
                        onLongClick = { shotToDelete = shot },
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    shotToDelete?.let { shot ->
        AlertDialog(
            onDismissRequest = { shotToDelete = null },
            title = { Text("Delete Shot") },
            text = { Text("Delete this shot?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteShot(shot)
                    shotToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { shotToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showSaveConfirmation) {
        ShotConfirmationDialog(
            formState = formState,
            settings = settings,
            onConfirm = {
                showSaveConfirmation = false
                onSaveShot()
            },
            onDismiss = { showSaveConfirmation = false },
        )
    }
}

@Composable
private fun ShotConfirmationDialog(
    formState: ShotFormState,
    settings: SettingsState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val distUnit = UnitConverter.distanceUnit(settings.useImperial)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Save Shot?", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                // Key shot info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Hole ${formState.holeNumber} \u2022 Shot ${formState.shotNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            formState.clubUsed?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        val distMetres = formState.distance.toDoubleOrNull()
                        if (distMetres != null) {
                            val display = UnitConverter.metresToDisplay(distMetres, settings.useImperial)
                            Text(
                                "${"%.1f".format(display)} $distUnit",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        val carryMetres = formState.carryDistance.toDoubleOrNull()
                        if (carryMetres != null) {
                            val carryDisplay = UnitConverter.metresToDisplay(carryMetres, settings.useImperial)
                            val carryElev = formState.carryElevationChange
                            val carryElevText = if (carryElev != 0.0) {
                                val elevVal = UnitConverter.metresToDisplay(kotlin.math.abs(carryElev), settings.useImperial)
                                val arrow = if (carryElev >= 0) "\u2191" else "\u2193"
                                " $arrow${"%.0f".format(elevVal)}$distUnit"
                            } else ""
                            Text(
                                "Carry: ${"%.1f".format(carryDisplay)} $distUnit$carryElevText",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                // Detail rows
                val details = buildList {
                    if (formState.elevationChange != 0.0) {
                        val elevDisplay = UnitConverter.metresToDisplay(
                            kotlin.math.abs(formState.elevationChange), settings.useImperial
                        )
                        val dir = if (formState.elevationChange > 0) "Uphill" else "Downhill"
                        add("Elevation" to "$dir ${"%.0f".format(elevDisplay)}$distUnit")
                    }
                    formState.windDirection?.let { add("Wind" to it.arrow) }
                    formState.windStrength?.let { add("Wind Strength" to it.displayName) }
                    formState.lie?.let { add("Lie" to it.displayName) }
                    formState.shotType?.let { add("Shot Type" to it.displayName) }
                    formState.lieDirection?.let { add("Lie Direction" to it.displayName) }
                    formState.strike?.let { add("Strike" to it.displayName) }
                    formState.ballFlight?.let { add("Ball Flight" to it.displayName) }
                    formState.clubDirection?.let { add("Club Path" to it.displayName) }
                    formState.ballDirection?.let { add("Ball Shape" to it.displayName) }
                    formState.directionToTarget?.let { add("Direction" to it.displayName) }
                    formState.distanceToTarget?.let { add("Dist. to Target" to it.displayName) }
                    formState.mentalState?.let { add("Mental" to it.displayName) }
                    formState.fairwayHit?.let { add("Fairway Hit" to it.displayName) }
                    formState.greenInRegulation?.let { add("GIR" to it.displayName) }
                    formState.targetDistance.toDoubleOrNull()?.let {
                        val display = UnitConverter.metresToDisplay(it, settings.useImperial)
                        add("Target Dist." to "${"%.1f".format(display)} $distUnit")
                    }
                    if (formState.mentalStateNote.isNotBlank()) add("Mental Note" to formState.mentalStateNote)
                    if (formState.customNote.isNotBlank()) add("Note" to formState.customNote)
                    if (formState.ignoreForAnalytics) add("Analytics" to "Ignored")
                }

                if (details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    details.forEach { (label, value) ->
                        ShotDetailRow(label, value)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun ShotDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShotListItem(
    shot: Shot,
    settings: SettingsState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("H${shot.holeNumber} S${shot.shotNumber}", fontWeight = FontWeight.Bold)
                shot.clubUsed?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
        },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                shot.distance?.let {
                    val display = UnitConverter.metresToDisplay(it, settings.useImperial)
                    val unit = UnitConverter.distanceUnit(settings.useImperial)
                    Text("${"%.0f".format(display)}$unit")
                }
                shot.carryDistance?.let {
                    val display = UnitConverter.metresToDisplay(it, settings.useImperial)
                    val unit = UnitConverter.distanceUnit(settings.useImperial)
                    Text("(carry ${"%.0f".format(display)}$unit)")
                }
                shot.strike?.let { Text(it.displayName) }
                shot.ballDirection?.let { Text(it.displayName) }
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
        trailingContent = {
            if (shot.ignoreForAnalytics) {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = "Ignored for analytics",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }
    )
}

@Composable
private fun CompactSelector(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decrementEnabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp),
            enabled = decrementEnabled,
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Previous $label",
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            "$value",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Next $label",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun ElevationControl(
    elevationMetres: Double,
    useImperial: Boolean,
    onAdjust: (Double) -> Unit,
) {
    val distUnit = UnitConverter.distanceUnit(useImperial)
    val displayVal = UnitConverter.metresToDisplay(kotlin.math.abs(elevationMetres), useImperial)
    val arrow = if (elevationMetres >= 0) "\u2191" else "\u2193"
    val elevColor = if (elevationMetres > 0) Color(0xFF2E7D32)
        else if (elevationMetres < 0) Color(0xFFC62828)
        else MaterialTheme.colorScheme.onSurfaceVariant
    // Adjust by 1 display unit (1m or 1yd)
    val step = if (useImperial) UnitConverter.displayToMetres(1.0, true) else 1.0

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            IconButton(
                onClick = { onAdjust(step) },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Uphill",
                    modifier = Modifier.size(20.dp),
                )
            }
            Icon(
                Icons.Default.Terrain,
                contentDescription = "Elevation",
                modifier = Modifier.size(16.dp),
                tint = elevColor,
            )
            Text(
                "$arrow${"%.0f".format(displayVal)}$distUnit",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = elevColor,
            )
            IconButton(
                onClick = { onAdjust(-step) },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Downhill",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
