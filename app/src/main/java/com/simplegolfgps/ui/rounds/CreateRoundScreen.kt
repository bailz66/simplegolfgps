package com.simplegolfgps.ui.rounds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simplegolfgps.data.WeatherType
import com.simplegolfgps.settings.SettingsState
import com.simplegolfgps.settings.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoundScreen(
    settings: SettingsState,
    onSave: (String, WeatherType, Int?, String?, Int) -> Unit,
    onBack: () -> Unit,
) {
    var courseName by remember { mutableStateOf("") }
    var weatherType by remember { mutableStateOf(WeatherType.Sunny) }
    var temperature by remember { mutableStateOf("") }
    var windCondition by remember { mutableStateOf("") }
    var startingHole by remember { mutableStateOf("1") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Round") },
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
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("Weather", style = MaterialTheme.typography.labelLarge)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(WeatherType.entries.toList()) { type ->
                    FilterChip(
                        selected = weatherType == type,
                        onClick = { weatherType = type },
                        label = { Text(type.displayName) },
                    )
                }
            }

            val tempUnit = UnitConverter.temperatureUnit(settings.useImperial)
            OutlinedTextField(
                value = temperature,
                onValueChange = { temperature = it.filter { c -> c.isDigit() || c == '-' } },
                label = { Text("Temperature ($tempUnit)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            OutlinedTextField(
                value = windCondition,
                onValueChange = { windCondition = it },
                label = { Text("Wind Condition") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = startingHole,
                onValueChange = { startingHole = it.filter { c -> c.isDigit() } },
                label = { Text("Starting Hole") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            Button(
                onClick = {
                    if (courseName.isNotBlank()) {
                        val tempInt = temperature.toIntOrNull()
                        val tempCelsius = if (tempInt != null && settings.useImperial)
                            UnitConverter.displayToCelsius(tempInt, true)
                        else tempInt
                        onSave(
                            courseName,
                            weatherType,
                            tempCelsius,
                            windCondition.ifBlank { null },
                            startingHole.toIntOrNull() ?: 1,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = courseName.isNotBlank(),
            ) {
                Text("Start Round")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoundScreen(
    round: com.simplegolfgps.data.Round?,
    settings: SettingsState,
    onSave: (com.simplegolfgps.data.Round) -> Unit,
    onBack: () -> Unit,
) {
    if (round != null) {
        var courseName by remember(round) { mutableStateOf(round.courseName) }
        var weatherType by remember(round) { mutableStateOf(round.weatherType) }
        var temperature by remember(round) {
            val display = if (round.temperature != null && settings.useImperial)
                UnitConverter.temperatureToDisplay(round.temperature, true)
            else round.temperature
            mutableStateOf(display?.toString() ?: "")
        }
        var windCondition by remember(round) { mutableStateOf(round.windCondition ?: "") }
        var startingHole by remember(round) { mutableStateOf(round.startingHole.toString()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Round") },
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
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Text("Weather", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(WeatherType.entries.toList()) { type ->
                        FilterChip(
                            selected = weatherType == type,
                            onClick = { weatherType = type },
                            label = { Text(type.displayName) },
                        )
                    }
                }

                val tempUnit = UnitConverter.temperatureUnit(settings.useImperial)
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("Temperature ($tempUnit)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = windCondition,
                    onValueChange = { windCondition = it },
                    label = { Text("Wind Condition") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = startingHole,
                    onValueChange = { startingHole = it.filter { c -> c.isDigit() } },
                    label = { Text("Starting Hole") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )

                Button(
                    onClick = {
                        if (courseName.isNotBlank()) {
                            val tempInt = temperature.toIntOrNull()
                            val tempCelsius = if (tempInt != null && settings.useImperial)
                                UnitConverter.displayToCelsius(tempInt, true)
                            else tempInt
                            onSave(
                                round.copy(
                                    courseName = courseName,
                                    weatherType = weatherType,
                                    temperature = tempCelsius,
                                    windCondition = windCondition.ifBlank { null },
                                    startingHole = startingHole.toIntOrNull() ?: 1,
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = courseName.isNotBlank(),
                ) {
                    Text("Save Changes")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
    }
}
