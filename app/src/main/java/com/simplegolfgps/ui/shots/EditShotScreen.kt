package com.simplegolfgps.ui.shots

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simplegolfgps.data.*
import com.simplegolfgps.settings.SettingsState
import com.simplegolfgps.settings.UnitConverter
import com.simplegolfgps.ui.components.ChipSelector
import com.simplegolfgps.ui.components.WindDirectionGrid
import com.simplegolfgps.ui.components.clubAbbreviation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShotScreen(
    formState: ShotFormState,
    settings: SettingsState,
    onUpdateForm: (ShotFormState.() -> ShotFormState) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Shot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = formState.holeNumber.toString(),
                    onValueChange = {
                        val hole = it.filter { c -> c.isDigit() }.toIntOrNull() ?: return@OutlinedTextField
                        onUpdateForm { copy(holeNumber = hole) }
                    },
                    label = { Text("Hole Number") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = formState.shotNumber.toString(),
                    onValueChange = {
                        val shot = it.filter { c -> c.isDigit() }.toIntOrNull() ?: return@OutlinedTextField
                        onUpdateForm { copy(shotNumber = shot) }
                    },
                    label = { Text("Shot Number") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }

            // Distance + Carry + Elevation
            val distUnit = UnitConverter.distanceUnit(settings.useImperial)
            val showCarry = settings.showCarryDistance || formState.carryDistance.toDoubleOrNull() != null

            val displayDistance = if (settings.useImperial) {
                val metres = formState.distance.toDoubleOrNull()
                if (metres != null) "%.1f".format(UnitConverter.metresToDisplay(metres, true))
                else formState.distance
            } else formState.distance

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Carry field first (when enabled)
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
                                    if (metres <= MAX_DISTANCE_METRES) {
                                        onUpdateForm { copy(carryDistance = "%.1f".format(metres)) }
                                    }
                                } else {
                                    onUpdateForm { copy(carryDistance = cleaned) }
                                }
                            } else {
                                val m = cleaned.toDoubleOrNull()
                                if (m == null || m <= MAX_DISTANCE_METRES) {
                                    onUpdateForm { copy(carryDistance = cleaned) }
                                }
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
                                if (metres <= MAX_DISTANCE_METRES) {
                                    onUpdateForm { copy(distance = "%.1f".format(metres)) }
                                }
                            } else {
                                onUpdateForm { copy(distance = cleaned) }
                            }
                        } else {
                            val m = cleaned.toDoubleOrNull()
                            if (m == null || m <= MAX_DISTANCE_METRES) {
                                onUpdateForm { copy(distance = cleaned) }
                            }
                        }
                    },
                    label = { Text(if (showCarry) "Total ($distUnit)" else "Distance ($distUnit)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )

                ElevationControl(
                    elevationMetres = formState.elevationChange,
                    useImperial = settings.useImperial,
                    onAdjust = { delta ->
                        onUpdateForm {
                            copy(elevationChange = (elevationChange + delta).coerceIn(-MAX_ELEVATION_METRES, MAX_ELEVATION_METRES))
                        }
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
                                if (metres <= MAX_DISTANCE_METRES) {
                                    onUpdateForm { copy(targetDistance = "%.1f".format(metres)) }
                                }
                            } else {
                                onUpdateForm { copy(targetDistance = cleaned) }
                            }
                        } else {
                            val m = cleaned.toDoubleOrNull()
                            if (m == null || m <= MAX_DISTANCE_METRES) {
                                onUpdateForm { copy(targetDistance = cleaned) }
                            }
                        }
                    },
                    label = { Text("Target Distance ($distUnit)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
            }

            // Club
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

            // Lie — wrapping layout
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

            // Shot Type
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

            // Power %
            if (settings.showPowerPct) {
                PowerPctSelector(
                    value = formState.powerPct,
                    onValueChange = { onUpdateForm { copy(powerPct = it) } },
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

            OutlinedTextField(
                value = formState.customNote,
                onValueChange = { onUpdateForm { copy(customNote = it) } },
                label = { Text("Custom Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 3,
            )

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

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
