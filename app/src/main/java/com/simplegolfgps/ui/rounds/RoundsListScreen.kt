package com.simplegolfgps.ui.rounds

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simplegolfgps.data.Round
import com.simplegolfgps.data.WeatherType
import com.simplegolfgps.settings.SettingsState
import com.simplegolfgps.settings.UnitConverter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundsListScreen(
    rounds: List<RoundWithShotCount>,
    settings: SettingsState,
    onCreateRound: () -> Unit,
    onRoundClick: (Long) -> Unit,
    onDeleteRound: (Round) -> Unit,
    onSettingsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
) {
    var roundToDelete by remember { mutableStateOf<Round?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SimpleGolfGPS") },
                actions = {
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRound,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Round") },
            )
        }
    ) { padding ->
        if (rounds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.GolfCourse,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No rounds yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "Tap \"New Round\" to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(rounds, key = { it.round.id }) { item ->
                    RoundCard(
                        roundWithCount = item,
                        settings = settings,
                        onClick = { onRoundClick(item.round.id) },
                        onLongClick = { roundToDelete = item.round },
                    )
                }
            }
        }
    }

    roundToDelete?.let { round ->
        AlertDialog(
            onDismissRequest = { roundToDelete = null },
            title = { Text("Delete Round") },
            text = { Text("Delete the round at ${round.courseName}? All shots in this round will also be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteRound(round)
                    roundToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { roundToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RoundCard(
    roundWithCount: RoundWithShotCount,
    settings: SettingsState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val round = roundWithCount.round
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = round.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = weatherIcon(round.weatherType),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = dateFormat.format(Date(round.dateCreated)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = "${roundWithCount.shotCount} shots",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            if (round.temperature != null) {
                val tempDisplay = if (settings.useImperial)
                    UnitConverter.temperatureToDisplay(round.temperature, true)
                else round.temperature
                val unit = UnitConverter.temperatureUnit(settings.useImperial)
                Text(
                    text = "$tempDisplay$unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

private fun weatherIcon(type: WeatherType): String = when (type) {
    WeatherType.Sunny -> "☀️"
    WeatherType.Cloudy -> "⛅"
    WeatherType.Overcast -> "☁️"
    WeatherType.LightRain -> "🌦️"
    WeatherType.HeavyRain -> "🌧️"
}
