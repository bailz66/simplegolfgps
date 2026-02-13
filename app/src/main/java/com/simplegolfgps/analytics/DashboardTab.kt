package com.simplegolfgps.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplegolfgps.settings.UnitConverter

@Composable
fun DashboardTab(
    state: DashboardState,
    useImperial: Boolean,
    onDispersionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Overview cards
        item(key = "overview") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard("Total Shots", state.totalShots.toString(), modifier = Modifier.weight(1f))
                StatCard("Rounds", state.totalRounds.toString(), modifier = Modifier.weight(1f))
                StatCard("Courses", state.uniqueCourses.toString(), modifier = Modifier.weight(1f))
            }
        }

        // Performance scores
        item(key = "performance") {
            PerformanceScoresCard(state)
        }

        // Shot Dispersion
        item(key = "dispersion") {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDispersionClick() },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Shot Dispersion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "5x5 target showing where shots land",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }

        // Trends
        item(key = "trends") {
            TrendsCard(state, useImperial)
        }

        // Club distance chart
        if (state.clubDistanceOverview.isNotEmpty()) {
            item(key = "club_distances") {
                ClubDistanceChartCard(state.clubDistanceOverview, useImperial)
            }
        }

        // Club usage
        if (state.clubUsageCounts.isNotEmpty()) {
            item(key = "club_usage") {
                ClubUsageCard(state.clubUsageCounts)
            }
        }

        // Weaknesses
        if (state.weaknesses.isNotEmpty()) {
            item(key = "weaknesses") {
                WeaknessesCard(state.weaknesses)
            }
        }

        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun PerformanceScoresCard(state: DashboardState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Performance")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CircularScoreIndicator(
                    label = "Pure Strike",
                    score = state.overallPureStrikeRate,
                    modifier = Modifier.weight(1f),
                )
                CircularScoreIndicator(
                    label = "On Target",
                    score = state.overallStraightRate,
                    modifier = Modifier.weight(1f),
                )
                CircularScoreIndicator(
                    label = "Good Length",
                    score = state.overallOnPinRate,
                    modifier = Modifier.weight(1f),
                )
            }
            if (state.fairwayHitRate != null || state.greenInRegulationRate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CircularScoreIndicator(
                        label = "FIR %",
                        score = state.fairwayHitRate,
                        modifier = Modifier.weight(1f),
                    )
                    CircularScoreIndicator(
                        label = "GIR %",
                        score = state.greenInRegulationRate,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendsCard(state: DashboardState, useImperial: Boolean) {
    val trend = state.recentTrend
    if (trend != null) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader("Trends: Last 5 vs Prior 5 Rounds")
                Spacer(modifier = Modifier.height(4.dp))

                if (!trend.hasEnoughData) {
                    Text(
                        "Need at least 10 rounds for trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                } else {
                    // Column headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "Prior",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.width(56.dp),
                            textAlign = TextAlign.End,
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            "Recent",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(56.dp),
                            textAlign = TextAlign.End,
                        )
                    }

                    TrendRow(
                        label = "Pure Strike %",
                        recentValue = trend.recentPureStrikeRate?.let { "${"%.0f".format(it * 100)}%" },
                        priorValue = trend.priorPureStrikeRate?.let { "${"%.0f".format(it * 100)}%" },
                        improving = if (trend.recentPureStrikeRate != null && trend.priorPureStrikeRate != null) {
                            trend.recentPureStrikeRate > trend.priorPureStrikeRate
                        } else null,
                    )

                    TrendRow(
                        label = "On Target %",
                        recentValue = trend.recentStraightRate?.let { "${"%.0f".format(it * 100)}%" },
                        priorValue = trend.priorStraightRate?.let { "${"%.0f".format(it * 100)}%" },
                        improving = if (trend.recentStraightRate != null && trend.priorStraightRate != null) {
                            trend.recentStraightRate > trend.priorStraightRate
                        } else null,
                    )

                    val unit = UnitConverter.distanceUnit(useImperial)
                    val clubLabel = trend.comparisonClub?.let { "Avg ($it)" } ?: "Avg Distance"
                    TrendRow(
                        label = clubLabel,
                        recentValue = trend.recentAvgDistance?.let {
                            "${"%.0f".format(UnitConverter.metresToDisplay(it, useImperial))}$unit"
                        },
                        priorValue = trend.priorAvgDistance?.let {
                            "${"%.0f".format(UnitConverter.metresToDisplay(it, useImperial))}$unit"
                        },
                        improving = if (trend.recentAvgDistance != null && trend.priorAvgDistance != null) {
                            trend.recentAvgDistance > trend.priorAvgDistance
                        } else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClubDistanceChartCard(clubs: List<ClubDistanceSummary>, useImperial: Boolean) {
    val maxDist = clubs.maxOf { it.max }
    val unit = UnitConverter.distanceUnit(useImperial)

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Club Distances")
            Spacer(modifier = Modifier.height(8.dp))

            clubs.forEach { club ->
                val avgDisplay = UnitConverter.metresToDisplay(club.avgDistance, useImperial)
                val minDisplay = UnitConverter.metresToDisplay(club.min, useImperial)
                val maxDisplay = UnitConverter.metresToDisplay(club.max, useImperial)
                val maxBarDisplay = UnitConverter.metresToDisplay(maxDist, useImperial)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        club.club,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(64.dp),
                    )

                    // Bar with spread whiskers
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp),
                    ) {
                        val primary = MaterialTheme.colorScheme.primary
                        val outline = MaterialTheme.colorScheme.outline

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val totalWidth = size.width
                            val barHeight = size.height

                            val minFraction = (minDisplay / maxBarDisplay).toFloat().coerceIn(0f, 1f)
                            val avgFraction = (avgDisplay / maxBarDisplay).toFloat().coerceIn(0f, 1f)
                            val maxFraction = (maxDisplay / maxBarDisplay).toFloat().coerceIn(0f, 1f)

                            // Spread line (min to max)
                            val y = barHeight / 2
                            drawLine(
                                color = outline,
                                start = Offset(minFraction * totalWidth, y),
                                end = Offset(maxFraction * totalWidth, y),
                                strokeWidth = 2.dp.toPx(),
                            )
                            // Whisker caps
                            drawLine(
                                color = outline,
                                start = Offset(minFraction * totalWidth, barHeight * 0.25f),
                                end = Offset(minFraction * totalWidth, barHeight * 0.75f),
                                strokeWidth = 2.dp.toPx(),
                            )
                            drawLine(
                                color = outline,
                                start = Offset(maxFraction * totalWidth, barHeight * 0.25f),
                                end = Offset(maxFraction * totalWidth, barHeight * 0.75f),
                                strokeWidth = 2.dp.toPx(),
                            )
                            // Avg marker
                            drawCircle(
                                color = primary,
                                radius = 5.dp.toPx(),
                                center = Offset(avgFraction * totalWidth, y),
                            )
                        }
                    }

                    Text(
                        "${"%.0f".format(avgDisplay)}$unit",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(52.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClubUsageCard(usages: List<ClubUsage>) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.inversePrimary,
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.primaryContainer,
    )

    // Show top 7, rest grouped as "Other"
    val topN = 7
    val topClubs = usages.take(topN)
    val otherCount = usages.drop(topN).sumOf { it.count }
    val totalCount = usages.sumOf { it.count }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Club Usage")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Donut chart
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        var currentAngle = -90f

                        topClubs.forEachIndexed { index, usage ->
                            val sweep = usage.percentage * 360f
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                            )
                            currentAngle += sweep
                        }
                        if (otherCount > 0) {
                            val sweep = (otherCount.toFloat() / totalCount) * 360f
                            drawArc(
                                color = colors.last(),
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                            )
                        }
                    }
                }

                // Legend
                Column(modifier = Modifier.weight(1f)) {
                    topClubs.forEachIndexed { index, usage ->
                        Row(
                            modifier = Modifier.padding(vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(colors[index % colors.size], androidx.compose.foundation.shape.CircleShape),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                usage.club,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${usage.count}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                    if (otherCount > 0) {
                        Row(
                            modifier = Modifier.padding(vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(colors.last(), androidx.compose.foundation.shape.CircleShape),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Other",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "$otherCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeaknessesCard(weaknesses: List<WeaknessItem>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("Areas to Improve")
            Spacer(modifier = Modifier.height(4.dp))

            weaknesses.forEach { weakness ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            weakness.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            weakness.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Severity bar
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small,
                            ),
                    ) {
                        val color = when {
                            weakness.severity > 0.6f -> MaterialTheme.colorScheme.error
                            weakness.severity > 0.3f -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(weakness.severity.coerceIn(0.05f, 1f))
                                .background(color, MaterialTheme.shapes.small),
                        )
                    }
                }
            }
        }
    }
}
