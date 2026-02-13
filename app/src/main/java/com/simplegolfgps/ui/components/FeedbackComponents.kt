package com.simplegolfgps.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

fun clubAbbreviation(fullName: String): String = when {
    fullName == "Driver" -> "D"
    fullName == "Putter" -> "P"
    fullName.endsWith("-Wood") -> fullName.substringBefore("-") + "w"
    fullName.endsWith("-Hybrid") -> fullName.substringBefore("-") + "h"
    fullName.endsWith("-Iron") -> fullName.substringBefore("-") + "i"
    else -> fullName
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T> ChipSelector(
    label: String,
    options: List<T>,
    selected: T?,
    displayName: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    wrap: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (wrap) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(if (selected == option) null else option) },
                        label = { Text(displayName(option), style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(28.dp),
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(options) { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(if (selected == option) null else option) },
                        label = { Text(displayName(option), style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun WindDirectionGrid(
    selected: com.simplegolfgps.data.WindDirection?,
    onSelect: (com.simplegolfgps.data.WindDirection?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = listOf(
        listOf(
            com.simplegolfgps.data.WindDirection.NW,
            com.simplegolfgps.data.WindDirection.N,
            com.simplegolfgps.data.WindDirection.NE,
        ),
        listOf(
            com.simplegolfgps.data.WindDirection.W,
            null,
            com.simplegolfgps.data.WindDirection.E,
        ),
        listOf(
            com.simplegolfgps.data.WindDirection.SW,
            com.simplegolfgps.data.WindDirection.S,
            com.simplegolfgps.data.WindDirection.SE,
        ),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Wind Direction",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            grid.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.weight(1f))
                    row.forEach { dir ->
                        if (dir != null) {
                            val isSelected = selected == dir
                            FilledTonalButton(
                                onClick = { onSelect(if (isSelected) null else dir) },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (isSelected) ButtonDefaults.buttonColors()
                                else ButtonDefaults.filledTonalButtonColors(),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    dir.arrow,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⛳", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
