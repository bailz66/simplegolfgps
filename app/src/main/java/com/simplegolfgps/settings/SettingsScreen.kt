package com.simplegolfgps.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onSetUseImperial: (Boolean) -> Unit,
    onToggleClub: (String) -> Unit,
    onMoveClub: (Int, Int) -> Unit,
    onSetShowWind: (Boolean) -> Unit,
    onSetShowLie: (Boolean) -> Unit,
    onSetShowLieDirection: (Boolean) -> Unit,
    onSetShowShotType: (Boolean) -> Unit,
    onSetShowStrike: (Boolean) -> Unit,
    onSetShowBallFlight: (Boolean) -> Unit,
    onSetShowClubDirection: (Boolean) -> Unit,
    onSetShowBallDirection: (Boolean) -> Unit,
    onSetShowDirectionToTarget: (Boolean) -> Unit,
    onSetShowDistanceToTarget: (Boolean) -> Unit,
    onSetShowMentalState: (Boolean) -> Unit,
    onSetShowCarryDistance: (Boolean) -> Unit,
    onSetShowFairwayHit: (Boolean) -> Unit,
    onSetShowGreenInRegulation: (Boolean) -> Unit,
    onSetShowTargetDistance: (Boolean) -> Unit,
    onSetShowPowerPct: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Units section
            item {
                SectionHeader("Units")
            }
            item {
                ListItem(
                    headlineContent = { Text("Use Imperial Units") },
                    supportingContent = {
                        Text(if (state.useImperial) "Yards / °F" else "Metres / °C")
                    },
                    trailingContent = {
                        Switch(
                            checked = state.useImperial,
                            onCheckedChange = onSetUseImperial,
                        )
                    }
                )
            }

            // Measurement section
            item {
                SectionHeader("Measurement")
            }
            item {
                FeedbackToggle("Show Carry Distance", state.showCarryDistance, onSetShowCarryDistance)
            }
            item {
                Text(
                    "When enabled, separately record where the ball lands (carry) and where it stops (total).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // Feedback categories section
            item {
                SectionHeader("Feedback Categories")
            }
            item {
                Text(
                    "Toggle which feedback fields appear on the shot recording form.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            item {
                FeedbackToggle("Wind Direction & Strength", state.showWind, onSetShowWind)
            }
            item {
                FeedbackToggle("Lie", state.showLie, onSetShowLie)
            }
            item {
                FeedbackToggle("Lie Direction", state.showLieDirection, onSetShowLieDirection)
            }
            item {
                FeedbackToggle("Shot Type", state.showShotType, onSetShowShotType)
            }
            item {
                FeedbackToggle("Strike", state.showStrike, onSetShowStrike)
            }
            item {
                FeedbackToggle("Ball Flight", state.showBallFlight, onSetShowBallFlight)
            }
            item {
                FeedbackToggle("Club Direction", state.showClubDirection, onSetShowClubDirection)
            }
            item {
                FeedbackToggle("Ball Direction", state.showBallDirection, onSetShowBallDirection)
            }
            item {
                FeedbackToggle("Direction to Target", state.showDirectionToTarget, onSetShowDirectionToTarget)
            }
            item {
                FeedbackToggle("Distance to Target", state.showDistanceToTarget, onSetShowDistanceToTarget)
            }
            item {
                FeedbackToggle("Mental State", state.showMentalState, onSetShowMentalState)
            }
            item {
                FeedbackToggle("Fairway Hit", state.showFairwayHit, onSetShowFairwayHit)
            }
            item {
                FeedbackToggle("Green in Regulation", state.showGreenInRegulation, onSetShowGreenInRegulation)
            }

            item {
                FeedbackToggle("Power %", state.showPowerPct, onSetShowPowerPct)
            }
            item {
                FeedbackToggle("Target Distance", state.showTargetDistance, onSetShowTargetDistance)
            }

            // Club configuration section
            item {
                SectionHeader("Club Configuration")
            }
            item {
                Text(
                    "Toggle clubs to include in the shot form. Tap arrows to reorder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            itemsIndexed(state.clubOrder) { index, club ->
                val isEnabled = club in state.enabledClubs
                ListItem(
                    headlineContent = {
                        Text(
                            club,
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    },
                    leadingContent = {
                        Row {
                            IconButton(
                                onClick = { if (index > 0) onMoveClub(index, index - 1) },
                                enabled = index > 0,
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move up",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            IconButton(
                                onClick = { if (index < state.clubOrder.size - 1) onMoveClub(index, index + 1) },
                                enabled = index < state.clubOrder.size - 1,
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move down",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { onToggleClub(club) },
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun FeedbackToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}
