package com.simplegolfgps.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplegolfgps.settings.UnitConverter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun CircularScoreIndicator(
    label: String,
    score: Float?, // 0..1 or null
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp),
        ) {
            if (score != null) {
                val arcColor = when {
                    score < 0.33f -> colorScheme.error
                    score < 0.66f -> colorScheme.tertiary
                    else -> colorScheme.primary
                }
                val trackColor = colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = score * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
                Text(
                    "${"%.0f".format(score * 100)}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                val trackColor = colorScheme.surfaceVariant
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                Text(
                    "-",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun <T> DistributionCard(
    title: String,
    distribution: EnumDistribution<T>?,
    displayName: (T) -> String,
    modifier: Modifier = Modifier,
) {
    if (distribution != null) {
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.inversePrimary,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.primaryContainer,
        )

        ElevatedCard(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Stacked horizontal bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                ) {
                    distribution.entries.forEachIndexed { index, entry ->
                        if (entry.percentage > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(entry.percentage)
                                    .fillMaxHeight()
                                    .background(colors[index % colors.size]),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Legend
                distribution.entries.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(colors[index % colors.size], CircleShape),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            displayName(entry.value),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "${entry.count} (${"%.0f".format(entry.percentage * 100)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DistanceHistogram(
    histogram: List<HistogramBucket>,
    useImperial: Boolean,
    modifier: Modifier = Modifier,
) {
    if (histogram.isNotEmpty()) {
        val maxCount = histogram.maxOf { it.count }
        val barColor = MaterialTheme.colorScheme.primary
        val unit = UnitConverter.distanceUnit(useImperial)

        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                histogram.forEach { bucket ->
                    val fraction = if (maxCount > 0) bucket.count.toFloat() / maxCount else 0f
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            bucket.count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                                .background(barColor, MaterialTheme.shapes.extraSmall),
                        )
                    }
                }
            }
            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                histogram.forEach { bucket ->
                    val displayVal = UnitConverter.metresToDisplay(bucket.rangeStart, useImperial)
                    Text(
                        "%.0f".format(displayVal),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
            Text(
                "Distance ($unit)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun TrendRow(
    label: String,
    recentValue: String?,
    priorValue: String?,
    improving: Boolean?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            priorValue ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End,
        )
        Icon(
            when (improving) {
                true -> Icons.AutoMirrored.Filled.TrendingUp
                false -> Icons.AutoMirrored.Filled.TrendingDown
                null -> Icons.AutoMirrored.Filled.TrendingFlat
            },
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 4.dp).size(16.dp),
            tint = when (improving) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.error
                null -> MaterialTheme.colorScheme.outline
            },
        )
        Text(
            recentValue ?: "-",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    dateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            if (dateMillis != null) dateFormat.format(Date(dateMillis)) else label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(pickerState.selectedDateMillis)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    showPicker = false
                }) { Text("Clear") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun <T> EnumFilterDropdown(
    label: String,
    selected: T?,
    entries: List<T>,
    displayName: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(selected?.let(displayName) ?: label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = { onSelect(null); expanded = false },
            )
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(displayName(entry)) },
                    onClick = { onSelect(entry); expanded = false },
                )
            }
        }
    }
}

@Composable
fun StringFilterDropdown(
    label: String,
    selected: String?,
    entries: List<String>,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(selected ?: label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = { onSelect(null); expanded = false },
            )
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = { onSelect(entry); expanded = false },
                )
            }
        }
    }
}

@Composable
fun EmptyAnalyticsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No shot data yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Text(
                "Record some shots to see analytics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }
}
