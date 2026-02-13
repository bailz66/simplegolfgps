package com.simplegolfgps.analytics

import com.simplegolfgps.data.*
import kotlin.math.abs
import kotlin.math.sqrt

object AnalyticsComputer {

    // --- Filtering ---

    fun filterShots(
        allShots: List<Shot>,
        rounds: List<Round>,
        filters: FilterState,
    ): List<Shot> {
        var shots = allShots
        filters.dateFrom?.let { from -> shots = shots.filter { it.timestamp >= from } }
        filters.dateTo?.let { to -> shots = shots.filter { it.timestamp <= to } }
        filters.club?.let { club -> shots = shots.filter { it.clubUsed == club } }
        filters.shotType?.let { st -> shots = shots.filter { it.shotType == st } }
        filters.lie?.let { lie -> shots = shots.filter { it.lie == lie } }
        filters.lieDirection?.let { ld -> shots = shots.filter { it.lieDirection == ld } }
        filters.strike?.let { s -> shots = shots.filter { it.strike == s } }
        filters.windStrength?.let { ws -> shots = shots.filter { it.windStrength == ws } }
        filters.windDirection?.let { wd -> shots = shots.filter { it.windDirection == wd } }
        filters.mentalState?.let { ms -> shots = shots.filter { it.mentalState == ms } }
        filters.ballFlight?.let { bf -> shots = shots.filter { it.ballFlight == bf } }
        if (filters.weatherType != null) {
            val roundIds = rounds.filter { it.weatherType == filters.weatherType }.map { it.id }.toSet()
            shots = shots.filter { it.roundId in roundIds }
        }
        if (filters.courseName != null) {
            val roundIds = rounds.filter { it.courseName == filters.courseName }.map { it.id }.toSet()
            shots = shots.filter { it.roundId in roundIds }
        }
        return shots
    }

    // --- Dashboard (unfiltered) ---

    fun computeDashboard(
        allShots: List<Shot>,
        rounds: List<Round>,
    ): DashboardState {
        if (allShots.isEmpty()) {
            return DashboardState(isLoading = false)
        }

        val clubGroups = allShots.groupBy { it.clubUsed ?: "Unknown" }
        val clubUsageCounts = clubGroups
            .map { (club, shots) -> ClubUsage(club, shots.size, shots.size.toFloat() / allShots.size) }
            .sortedByDescending { it.count }

        val mostUsed = clubUsageCounts.firstOrNull()?.club

        // Performance scores
        val shotsWithStrike = allShots.filter { it.strike != null }
        val pureStrikeRate = if (shotsWithStrike.isNotEmpty()) {
            shotsWithStrike.count { it.strike == Strike.Pure }.toFloat() / shotsWithStrike.size
        } else null

        val shotsWithDirection = allShots.filter { it.directionToTarget != null }
        val straightRate = if (shotsWithDirection.isNotEmpty()) {
            shotsWithDirection.count { it.directionToTarget == DirectionToTarget.Straight }.toFloat() / shotsWithDirection.size
        } else null

        val shotsWithDistTarget = allShots.filter { it.distanceToTarget != null }
        val onPinRate = if (shotsWithDistTarget.isNotEmpty()) {
            shotsWithDistTarget.count { it.distanceToTarget == DistanceToTarget.OnPin }.toFloat() / shotsWithDistTarget.size
        } else null

        val shotsWithFairway = allShots.filter { it.fairwayHit != null }
        val fairwayHitRate = if (shotsWithFairway.isNotEmpty()) {
            shotsWithFairway.count { it.fairwayHit == FairwayHit.Yes }.toFloat() / shotsWithFairway.size
        } else null

        val shotsWithGir = allShots.filter { it.greenInRegulation != null }
        val girRate = if (shotsWithGir.isNotEmpty()) {
            shotsWithGir.count { it.greenInRegulation == GreenInRegulation.Yes }.toFloat() / shotsWithGir.size
        } else null

        // Club distance overview
        val clubDistanceOverview = computeClubDistanceSummaries(allShots)

        // Trends
        val recentTrend = computeTrends(allShots, rounds)

        // Weaknesses
        val weaknesses = identifyWeaknesses(allShots)

        return DashboardState(
            totalShots = allShots.size,
            totalRounds = rounds.size,
            uniqueCourses = rounds.map { it.courseName }.distinct().size,
            mostUsedClub = mostUsed,
            clubUsageCounts = clubUsageCounts,
            overallPureStrikeRate = pureStrikeRate,
            overallStraightRate = straightRate,
            overallOnPinRate = onPinRate,
            fairwayHitRate = fairwayHitRate,
            greenInRegulationRate = girRate,
            recentTrend = recentTrend,
            weaknesses = weaknesses,
            clubDistanceOverview = clubDistanceOverview,
            isLoading = false,
        )
    }

    // --- Shot Analysis (filtered) ---

    fun computeShotAnalysis(
        allShots: List<Shot>,
        filteredShots: List<Shot>,
        filters: FilterState,
    ): ShotAnalysisState {
        if (filteredShots.isEmpty()) {
            return ShotAnalysisState(
                totalShotCount = allShots.size,
                isLoading = false,
            )
        }

        val distanceStats = computeDistanceStats(filteredShots)
        val elevationStats = computeElevationStats(filteredShots)

        return ShotAnalysisState(
            filteredShotCount = filteredShots.size,
            totalShotCount = allShots.size,
            distanceStats = distanceStats,
            elevationStats = elevationStats,
            strikeDistribution = computeEnumDistribution(filteredShots) { it.strike },
            ballFlightDistribution = computeEnumDistribution(filteredShots) { it.ballFlight },
            clubDirectionDistribution = computeEnumDistribution(filteredShots) { it.clubDirection },
            ballDirectionDistribution = computeEnumDistribution(filteredShots) { it.ballDirection },
            directionToTargetDistribution = computeEnumDistribution(filteredShots) { it.directionToTarget },
            distanceToTargetDistribution = computeEnumDistribution(filteredShots) { it.distanceToTarget },
            lieDistribution = if (filters.lie == null) computeEnumDistribution(filteredShots) { it.lie } else null,
            lieDirectionDistribution = if (filters.lieDirection == null) computeEnumDistribution(filteredShots) { it.lieDirection } else null,
            windStrengthDistribution = if (filters.windStrength == null) computeEnumDistribution(filteredShots) { it.windStrength } else null,
            windDirectionDistribution = if (filters.windDirection == null) computeEnumDistribution(filteredShots) { it.windDirection } else null,
            mentalStateDistribution = if (filters.mentalState == null) computeEnumDistribution(filteredShots) { it.mentalState } else null,
            shotTypeDistribution = if (filters.shotType == null) computeEnumDistribution(filteredShots) { it.shotType } else null,
            clubStats = if (filters.club == null) computeClubDistanceSummaries(filteredShots) else emptyList(),
            isLoading = false,
        )
    }

    // --- Enum distribution ---

    fun <T : Any> computeEnumDistribution(
        shots: List<Shot>,
        selector: (Shot) -> T?,
    ): EnumDistribution<T>? {
        val values = shots.mapNotNull(selector)
        if (values.isEmpty()) return null

        val total = values.size
        val entries = values
            .groupingBy { it }
            .eachCount()
            .map { (value, count) -> EnumCount(value, count, count.toFloat() / total) }
            .sortedByDescending { it.count }

        return EnumDistribution(entries, total)
    }

    // --- Distance stats ---

    fun computeDistanceStats(shots: List<Shot>): DistanceStats? {
        val distances = shots.mapNotNull { it.distance }.sorted()
        if (distances.isEmpty()) return null

        val avg = distances.average()
        val median = if (distances.size % 2 == 0) {
            (distances[distances.size / 2 - 1] + distances[distances.size / 2]) / 2.0
        } else {
            distances[distances.size / 2]
        }
        val stdDev = if (distances.size > 1) {
            val variance = distances.map { (it - avg) * (it - avg) }.average()
            sqrt(variance)
        } else 0.0

        val histogram = computeHistogram(distances)

        return DistanceStats(
            average = avg,
            median = median,
            min = distances.first(),
            max = distances.last(),
            stdDeviation = stdDev,
            shotCount = distances.size,
            histogram = histogram,
        )
    }

    // --- Elevation stats ---

    fun computeElevationStats(shots: List<Shot>): ElevationStats? {
        val shotsWithElevation = shots.filter { it.elevationChange != null }
        if (shotsWithElevation.isEmpty()) return null

        val elevations = shotsWithElevation.map { it.elevationChange!! }
        val avgChange = elevations.average()
        val maxUphill = elevations.maxOrNull() ?: 0.0
        val maxDownhill = elevations.minOrNull() ?: 0.0

        // Threshold: flat = within +-2m
        val uphillShots = shotsWithElevation.filter { it.elevationChange!! > 2.0 }
        val flatShots = shotsWithElevation.filter { abs(it.elevationChange!!) <= 2.0 }
        val downhillShots = shotsWithElevation.filter { it.elevationChange!! < -2.0 }

        val uphillAvg = uphillShots.mapNotNull { it.distance }.takeIf { it.isNotEmpty() }?.average()
        val flatAvg = flatShots.mapNotNull { it.distance }.takeIf { it.isNotEmpty() }?.average()
        val downhillAvg = downhillShots.mapNotNull { it.distance }.takeIf { it.isNotEmpty() }?.average()

        return ElevationStats(
            avgElevationChange = avgChange,
            maxUphill = maxUphill,
            maxDownhill = maxDownhill,
            uphillAvgDistance = uphillAvg,
            flatAvgDistance = flatAvg,
            downhillAvgDistance = downhillAvg,
        )
    }

    // --- Histogram ---

    fun computeHistogram(sortedDistances: List<Double>, bucketCount: Int = 8): List<HistogramBucket> {
        if (sortedDistances.isEmpty()) return emptyList()

        val min = sortedDistances.first()
        val max = sortedDistances.last()
        if (min == max) {
            return listOf(HistogramBucket(min, max, sortedDistances.size, "%.0f".format(min)))
        }

        val range = max - min
        val bucketWidth = range / bucketCount

        return (0 until bucketCount).map { i ->
            val start = min + i * bucketWidth
            val end = if (i == bucketCount - 1) max else start + bucketWidth
            val count = sortedDistances.count { d ->
                if (i == bucketCount - 1) d >= start && d <= end
                else d >= start && d < end
            }
            HistogramBucket(
                rangeStart = start,
                rangeEnd = end,
                count = count,
                label = "%.0f".format(start),
            )
        }
    }

    // --- Club distance summaries ---

    fun computeClubDistanceSummaries(shots: List<Shot>): List<ClubDistanceSummary> {
        return shots
            .filter { it.clubUsed != null }
            .groupBy { it.clubUsed!! }
            .mapNotNull { (club, clubShots) ->
                val distances = clubShots.mapNotNull { it.distance }
                if (distances.size < 2) return@mapNotNull null
                val avg = distances.average()
                val stdDev = sqrt(distances.map { (it - avg) * (it - avg) }.average())
                ClubDistanceSummary(
                    club = club,
                    avgDistance = avg,
                    min = distances.min(),
                    max = distances.max(),
                    shotCount = distances.size,
                    stdDeviation = stdDev,
                )
            }
            .sortedByDescending { it.avgDistance }
    }

    // --- Trends ---

    private fun computeTrends(allShots: List<Shot>, rounds: List<Round>): TrendData {
        val sortedRounds = rounds.sortedByDescending { it.dateCreated }
        if (sortedRounds.size < 10) {
            return TrendData(null, null, null, null, null, null, null, hasEnoughData = false)
        }

        val recent5 = sortedRounds.take(5).map { it.id }.toSet()
        val prior5 = sortedRounds.drop(5).take(5).map { it.id }.toSet()

        val recentShots = allShots.filter { it.roundId in recent5 }
        val priorShots = allShots.filter { it.roundId in prior5 }

        fun strikeRate(shots: List<Shot>): Float? {
            val withStrike = shots.filter { it.strike != null }
            if (withStrike.isEmpty()) return null
            return withStrike.count { it.strike == Strike.Pure }.toFloat() / withStrike.size
        }

        fun straightRate(shots: List<Shot>): Float? {
            val withDir = shots.filter { it.directionToTarget != null }
            if (withDir.isEmpty()) return null
            return withDir.count { it.directionToTarget == DirectionToTarget.Straight }.toFloat() / withDir.size
        }

        // Find most used club for distance comparison
        val mostUsedClub = allShots
            .filter { it.clubUsed != null && it.distance != null }
            .groupBy { it.clubUsed!! }
            .maxByOrNull { it.value.size }
            ?.key

        val recentAvgDist = mostUsedClub?.let { club ->
            recentShots.filter { it.clubUsed == club }.mapNotNull { it.distance }
                .takeIf { it.isNotEmpty() }?.average()
        }
        val priorAvgDist = mostUsedClub?.let { club ->
            priorShots.filter { it.clubUsed == club }.mapNotNull { it.distance }
                .takeIf { it.isNotEmpty() }?.average()
        }

        return TrendData(
            recentPureStrikeRate = strikeRate(recentShots),
            priorPureStrikeRate = strikeRate(priorShots),
            recentStraightRate = straightRate(recentShots),
            priorStraightRate = straightRate(priorShots),
            recentAvgDistance = recentAvgDist,
            priorAvgDistance = priorAvgDist,
            comparisonClub = mostUsedClub,
            hasEnoughData = true,
        )
    }

    // --- Weaknesses ---

    fun identifyWeaknesses(shots: List<Shot>): List<WeaknessItem> {
        val weaknesses = mutableListOf<WeaknessItem>()

        // Check slice/hook tendency
        val withBallDir = shots.filter { it.ballDirection != null }
        if (withBallDir.size >= 10) {
            val sliceCount = withBallDir.count { it.ballDirection == BallDirection.Slice }
            val sliceRate = sliceCount.toFloat() / withBallDir.size
            if (sliceRate > 0.30f) {
                weaknesses.add(
                    WeaknessItem(
                        "Slice Tendency",
                        "${"%.0f".format(sliceRate * 100)}% of shots slice",
                        severity = (sliceRate - 0.30f).coerceIn(0f, 1f) / 0.50f,
                    )
                )
            }

            val hookCount = withBallDir.count { it.ballDirection == BallDirection.Hook }
            val hookRate = hookCount.toFloat() / withBallDir.size
            if (hookRate > 0.30f) {
                weaknesses.add(
                    WeaknessItem(
                        "Hook Tendency",
                        "${"%.0f".format(hookRate * 100)}% of shots hook",
                        severity = (hookRate - 0.30f).coerceIn(0f, 1f) / 0.50f,
                    )
                )
            }
        }

        // Check poor strike rate
        val withStrike = shots.filter { it.strike != null }
        if (withStrike.size >= 10) {
            val pureRate = withStrike.count { it.strike == Strike.Pure }.toFloat() / withStrike.size
            if (pureRate < 0.50f) {
                weaknesses.add(
                    WeaknessItem(
                        "Strike Quality",
                        "Only ${"%.0f".format(pureRate * 100)}% pure strikes",
                        severity = (0.50f - pureRate).coerceIn(0f, 1f) / 0.50f,
                    )
                )
            }

            val fatRate = withStrike.count { it.strike == Strike.Fat }.toFloat() / withStrike.size
            if (fatRate > 0.20f) {
                weaknesses.add(
                    WeaknessItem(
                        "Fat Shots",
                        "${"%.0f".format(fatRate * 100)}% fat strikes",
                        severity = (fatRate - 0.20f).coerceIn(0f, 1f) / 0.40f,
                    )
                )
            }
        }

        // Check direction to target
        val withDirTarget = shots.filter { it.directionToTarget != null }
        if (withDirTarget.size >= 10) {
            val offTarget = withDirTarget.count {
                it.directionToTarget == DirectionToTarget.FarLeft || it.directionToTarget == DirectionToTarget.FarRight
            }
            val offRate = offTarget.toFloat() / withDirTarget.size
            if (offRate > 0.20f) {
                weaknesses.add(
                    WeaknessItem(
                        "Accuracy",
                        "${"%.0f".format(offRate * 100)}% far off target",
                        severity = (offRate - 0.20f).coerceIn(0f, 1f) / 0.40f,
                    )
                )
            }
        }

        // Check distance control
        val withDistTarget = shots.filter { it.distanceToTarget != null }
        if (withDistTarget.size >= 10) {
            val badDist = withDistTarget.count {
                it.distanceToTarget == DistanceToTarget.WayLong || it.distanceToTarget == DistanceToTarget.WayShort
            }
            val badRate = badDist.toFloat() / withDistTarget.size
            if (badRate > 0.20f) {
                weaknesses.add(
                    WeaknessItem(
                        "Distance Control",
                        "${"%.0f".format(badRate * 100)}% way off distance",
                        severity = (badRate - 0.20f).coerceIn(0f, 1f) / 0.40f,
                    )
                )
            }
        }

        // Check mental state impact
        val withMental = shots.filter { it.mentalState != null }
        if (withMental.size >= 10) {
            val nonCalm = withMental.count { it.mentalState != MentalState.Calm }
            val nonCalmRate = nonCalm.toFloat() / withMental.size
            if (nonCalmRate > 0.40f) {
                weaknesses.add(
                    WeaknessItem(
                        "Mental Game",
                        "${"%.0f".format(nonCalmRate * 100)}% of shots not calm",
                        severity = (nonCalmRate - 0.40f).coerceIn(0f, 1f) / 0.40f,
                    )
                )
            }
        }

        return weaknesses
            .sortedByDescending { it.severity }
            .take(5)
    }

    // --- Legacy support for grouping enums by club ---

    fun <T> groupEnumByClub(
        shots: List<Shot>,
        selector: (Shot) -> T?,
    ): Map<String, Map<T, Int>> {
        return shots
            .filter { it.clubUsed != null && selector(it) != null }
            .groupBy { it.clubUsed!! }
            .mapValues { (_, clubShots) ->
                clubShots
                    .mapNotNull { selector(it) }
                    .groupingBy { it }
                    .eachCount()
            }
    }
}
