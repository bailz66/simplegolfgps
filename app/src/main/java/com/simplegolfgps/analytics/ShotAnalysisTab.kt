package com.simplegolfgps.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplegolfgps.data.*
import com.simplegolfgps.settings.SettingsState
import com.simplegolfgps.settings.UnitConverter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShotAnalysisTab(
    state: ShotAnalysisState,
    filterState: FilterState,
    settings: SettingsState,
    courseNames: List<String>,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMoreFilters by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.isLoading) {
            item(key = "loading") {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            return@LazyColumn
        }

        // Date range quick-select
        item(key = "date_range") {
            DateRangeSection(filterState, onUpdateFilter)
        }

        // Primary filters
        item(key = "primary_filters") {
            PrimaryFiltersRow(filterState, settings, onUpdateFilter)
        }

        // More filters toggle
        item(key = "more_filters_toggle") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showMoreFilters = !showMoreFilters }) {
                    Icon(
                        if (showMoreFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showMoreFilters) "Less Filters" else "More Filters")
                }
            }
        }

        // Expandable more filters
        item(key = "more_filters") {
            AnimatedVisibility(visible = showMoreFilters) {
                MoreFiltersSection(filterState, settings, courseNames, onUpdateFilter)
            }
        }

        // Results header with active filter chips
        item(key = "results_header") {
            ResultsHeader(state, filterState, onUpdateFilter, onClearFilters)
        }

        // If no filtered shots
        if (state.filteredShotCount == 0 && state.totalShotCount > 0) {
            item(key = "no_results") {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "No shots match the current filters",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        if (filterState.hasActiveFilters) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onClearFilters) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }
        }

        if (state.totalShotCount == 0) {
            item(key = "empty_state") {
                EmptyAnalyticsState()
            }
        }

        // Distance analysis
        state.distanceStats?.let { stats ->
            item(key = "distance_stats") {
                DistanceAnalysisCard(stats, settings.useImperial)
            }
        }

        // Elevation analysis
        state.elevationStats?.let { stats ->
            item(key = "elevation_stats") {
                ElevationAnalysisCard(stats, settings.useImperial)
            }
        }

        // Distribution cards — Strike & Flight
        if (state.strikeDistribution != null || state.ballFlightDistribution != null) {
            item(key = "strike_flight_header") {
                SectionHeader("Strike & Flight")
            }
            state.strikeDistribution?.let {
                item(key = "strike_dist") {
                    DistributionCard("Strike", it, displayName = { s -> s.displayName })
                }
            }
            state.ballFlightDistribution?.let {
                item(key = "ball_flight_dist") {
                    DistributionCard("Ball Flight", it, displayName = { s -> s.displayName })
                }
            }
        }

        // Direction distributions
        if (state.clubDirectionDistribution != null || state.ballDirectionDistribution != null || state.directionToTargetDistribution != null) {
            item(key = "direction_header") {
                SectionHeader("Direction")
            }
            state.clubDirectionDistribution?.let {
                item(key = "club_direction_dist") {
                    DistributionCard("Club Direction", it, displayName = { s -> s.displayName })
                }
            }
            state.ballDirectionDistribution?.let {
                item(key = "ball_direction_dist") {
                    DistributionCard("Ball Direction", it, displayName = { s -> s.displayName })
                }
            }
            state.directionToTargetDistribution?.let {
                item(key = "dir_to_target_dist") {
                    DistributionCard("Direction to Target", it, displayName = { s -> s.displayName })
                }
            }
        }

        // Distance control
        state.distanceToTargetDistribution?.let {
            item(key = "dist_control_header") {
                SectionHeader("Distance Control")
            }
            item(key = "dist_to_target_dist") {
                DistributionCard("Distance to Target", it, displayName = { s -> s.displayName })
            }
        }

        // Conditions (only shown if not already filtered)
        val conditionDists = listOfNotNull(
            state.lieDistribution?.let { "Lie" to it },
            state.lieDirectionDistribution?.let { "Lie Direction" to it },
            state.windStrengthDistribution?.let { "Wind Strength" to it },
            state.windDirectionDistribution?.let { "Wind Direction" to it },
            state.mentalStateDistribution?.let { "Mental State" to it },
            state.shotTypeDistribution?.let { "Shot Type" to it },
        )
        if (conditionDists.isNotEmpty()) {
            item(key = "conditions_header") {
                SectionHeader("Conditions")
            }
            conditionDists.forEach { (title, dist) ->
                item(key = "condition_$title") {
                    @Suppress("UNCHECKED_CAST")
                    DistributionCard(
                        title = title,
                        distribution = dist as EnumDistribution<Any>,
                        displayName = { value ->
                            when (value) {
                                is Lie -> value.displayName
                                is LieDirection -> value.displayName
                                is WindStrength -> value.displayName
                                is WindDirection -> value.displayName
                                is MentalState -> value.displayName
                                is ShotType -> value.displayName
                                else -> value.toString()
                            }
                        },
                    )
                }
            }
        }

        // Per-club stats table
        if (state.clubStats.isNotEmpty()) {
            item(key = "per_club_stats") {
                PerClubStatsTable(state.clubStats, settings.useImperial)
            }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun DateRangeSection(
    filterState: FilterState,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
) {
    Column {
        Text(
            "Date Range",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Quick-select chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val now = System.currentTimeMillis()
            val day = 86_400_000L

            data class QuickSelect(val label: String, val from: Long?, val to: Long?)
            val options = listOf(
                QuickSelect("All Time", null, null),
                QuickSelect("Last 30 Days", now - 30 * day, null),
                QuickSelect("Last 90 Days", now - 90 * day, null),
                QuickSelect("Last Year", now - 365 * day, null),
            )

            options.forEach { option ->
                item {
                    FilterChip(
                        selected = filterState.dateFrom == option.from && filterState.dateTo == option.to,
                        onClick = {
                            onUpdateFilter { copy(dateFrom = option.from, dateTo = option.to) }
                        },
                        label = { Text(option.label) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom date pickers
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DatePickerField(
                label = "From",
                dateMillis = filterState.dateFrom,
                onDateSelected = { onUpdateFilter { copy(dateFrom = it) } },
            )
            DatePickerField(
                label = "To",
                dateMillis = filterState.dateTo,
                onDateSelected = { onUpdateFilter { copy(dateTo = it) } },
            )
        }
    }
}

@Composable
private fun PrimaryFiltersRow(
    filterState: FilterState,
    settings: SettingsState,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            StringFilterDropdown(
                label = "Club",
                selected = filterState.club,
                entries = settings.enabledClubs,
                onSelect = { onUpdateFilter { copy(club = it) } },
            )
        }
        item {
            EnumFilterDropdown(
                label = "Shot Type",
                selected = filterState.shotType,
                entries = ShotType.entries,
                displayName = { it.displayName },
                onSelect = { onUpdateFilter { copy(shotType = it) } },
            )
        }
        item {
            EnumFilterDropdown(
                label = "Lie",
                selected = filterState.lie,
                entries = Lie.entries,
                displayName = { it.displayName },
                onSelect = { onUpdateFilter { copy(lie = it) } },
            )
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun MoreFiltersSection(
    filterState: FilterState,
    settings: SettingsState,
    courseNames: List<String>,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                EnumFilterDropdown(
                    label = "Weather",
                    selected = filterState.weatherType,
                    entries = WeatherType.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(weatherType = it) } },
                )
            }
            item {
                EnumFilterDropdown(
                    label = "Wind",
                    selected = filterState.windStrength,
                    entries = WindStrength.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(windStrength = it) } },
                )
            }
            item {
                EnumFilterDropdown(
                    label = "Wind Dir",
                    selected = filterState.windDirection,
                    entries = WindDirection.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(windDirection = it) } },
                )
            }
            item {
                EnumFilterDropdown(
                    label = "Lie Dir",
                    selected = filterState.lieDirection,
                    entries = LieDirection.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(lieDirection = it) } },
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                EnumFilterDropdown(
                    label = "Mental",
                    selected = filterState.mentalState,
                    entries = MentalState.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(mentalState = it) } },
                )
            }
            item {
                EnumFilterDropdown(
                    label = "Ball Flight",
                    selected = filterState.ballFlight,
                    entries = BallFlight.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(ballFlight = it) } },
                )
            }
            item {
                EnumFilterDropdown(
                    label = "Strike",
                    selected = filterState.strike,
                    entries = Strike.entries,
                    displayName = { it.displayName },
                    onSelect = { onUpdateFilter { copy(strike = it) } },
                )
            }
            if (courseNames.isNotEmpty()) {
                item {
                    StringFilterDropdown(
                        label = "Course",
                        selected = filterState.courseName,
                        entries = courseNames,
                        onSelect = { onUpdateFilter { copy(courseName = it) } },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultsHeader(
    state: ShotAnalysisState,
    filterState: FilterState,
    onUpdateFilter: (FilterState.() -> FilterState) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Showing ${state.filteredShotCount} of ${state.totalShotCount} shots",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (filterState.hasActiveFilters) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
        }

        // Active filter chips
        if (filterState.hasActiveFilters) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                filterState.dateFrom?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(dateFrom = null) } },
                        label = { Text("From: ${formatDate(it)}", style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.dateTo?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(dateTo = null) } },
                        label = { Text("To: ${formatDate(it)}", style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.club?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(club = null) } },
                        label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.shotType?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(shotType = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.lie?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(lie = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.lieDirection?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(lieDirection = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.strike?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(strike = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.weatherType?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(weatherType = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.windStrength?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(windStrength = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.windDirection?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(windDirection = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.mentalState?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(mentalState = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.ballFlight?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(ballFlight = null) } },
                        label = { Text(it.displayName, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
                filterState.courseName?.let {
                    InputChip(
                        selected = true,
                        onClick = { onUpdateFilter { copy(courseName = null) } },
                        label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceAnalysisCard(stats: DistanceStats, useImperial: Boolean) {
    val unit = UnitConverter.distanceUnit(useImperial)
    fun fmt(v: Double) = "${"%.0f".format(UnitConverter.metresToDisplay(v, useImperial))}$unit"

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Distance Analysis")
            Spacer(modifier = Modifier.height(4.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmt(stats.average), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Median", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmt(stats.median), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmt(stats.min), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmt(stats.max), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("StdDev", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmt(stats.stdDeviation), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (stats.histogram.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                DistanceHistogram(
                    histogram = stats.histogram,
                    useImperial = useImperial,
                )
            }
        }
    }
}

@Composable
private fun ElevationAnalysisCard(stats: ElevationStats, useImperial: Boolean) {
    val unit = UnitConverter.distanceUnit(useImperial)
    fun fmtDist(v: Double?) = v?.let { "${"%.0f".format(UnitConverter.metresToDisplay(it, useImperial))}$unit" } ?: "-"
    fun fmtElev(v: Double) = "${"%.1f".format(UnitConverter.metresToDisplay(v, useImperial))}$unit"

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Elevation Analysis")
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Avg Change", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmtElev(stats.avgElevationChange), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Max Uphill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmtElev(stats.maxUphill), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Max Downhill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmtElev(stats.maxDownhill), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Average Distance by Elevation", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Uphill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmtDist(stats.uphillAvgDistance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Flat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmtDist(stats.flatAvgDistance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Downhill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(fmtDist(stats.downhillAvgDistance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PerClubStatsTable(clubs: List<ClubDistanceSummary>, useImperial: Boolean) {
    fun fmt(v: Double) = "${"%.0f".format(UnitConverter.metresToDisplay(v, useImperial))}"

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Per-Club Statistics")
            Spacer(modifier = Modifier.height(4.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Text("Club", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Shots", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                Text("Avg", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                Text("Min", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                Text("Max", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                Text("SD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }
            HorizontalDivider()

            clubs.forEach { club ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(club.club, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(club.shotCount.toString(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    Text(fmt(club.avgDistance), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                    Text(fmt(club.min), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                    Text(fmt(club.max), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                    Text(fmt(club.stdDeviation), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}
