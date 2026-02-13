package com.simplegolfgps.analytics

import com.simplegolfgps.data.*
import org.junit.Assert.*
import org.junit.Test

class AnalyticsComputationTest {

    private fun makeShot(
        roundId: Long = 1L,
        clubUsed: String? = "7 Iron",
        distance: Double? = 150.0,
        elevationChange: Double? = null,
        lie: Lie? = Lie.Fairway,
        lieDirection: LieDirection? = null,
        shotType: ShotType? = ShotType.Full,
        windStrength: WindStrength? = WindStrength.Calm,
        windDirection: WindDirection? = null,
        mentalState: MentalState? = MentalState.Calm,
        ballDirection: BallDirection? = BallDirection.Straight,
        strike: Strike? = Strike.Pure,
        clubDirection: ClubDirection? = ClubDirection.Straight,
        ballFlight: BallFlight? = BallFlight.Medium,
        directionToTarget: DirectionToTarget? = DirectionToTarget.Straight,
        distanceToTarget: DistanceToTarget? = DistanceToTarget.OnPin,
        timestamp: Long = System.currentTimeMillis(),
    ) = Shot(
        roundId = roundId,
        holeNumber = 1,
        clubUsed = clubUsed,
        distance = distance,
        elevationChange = elevationChange,
        lie = lie,
        lieDirection = lieDirection,
        shotType = shotType,
        windStrength = windStrength,
        windDirection = windDirection,
        mentalState = mentalState,
        ballDirection = ballDirection,
        strike = strike,
        clubDirection = clubDirection,
        ballFlight = ballFlight,
        directionToTarget = directionToTarget,
        distanceToTarget = distanceToTarget,
        timestamp = timestamp,
    )

    private fun makeRound(
        id: Long = 1L,
        weatherType: WeatherType = WeatherType.Sunny,
        courseName: String = "Test Course",
    ) = Round(
        id = id,
        courseName = courseName,
        weatherType = weatherType,
    )

    private val noFilters = FilterState()

    // =====================================================
    // Legacy computeAnalytics-compatible tests via new API
    // =====================================================

    private fun computeViaFilter(shots: List<Shot>, rounds: List<Round>, filters: FilterState): Pair<List<Shot>, DashboardState> {
        val filtered = AnalyticsComputer.filterShots(shots, rounds, filters)
        val dashboard = AnalyticsComputer.computeDashboard(filtered, rounds)
        return filtered to dashboard
    }

    @Test
    fun `empty shots list returns zero stats`() {
        val dashboard = AnalyticsComputer.computeDashboard(emptyList(), emptyList())
        assertEquals(0, dashboard.totalShots)
        assertEquals(0, dashboard.totalRounds)
        assertNull(dashboard.mostUsedClub)
        assertTrue(dashboard.clubDistanceOverview.isEmpty())
        assertFalse(dashboard.isLoading)
    }

    @Test
    fun `single shot produces correct dashboard stats`() {
        val shots = listOf(makeShot(distance = 150.0))
        val rounds = listOf(makeRound())
        val dashboard = AnalyticsComputer.computeDashboard(shots, rounds)

        assertEquals(1, dashboard.totalShots)
        assertEquals(1, dashboard.totalRounds)
        assertEquals("7 Iron", dashboard.mostUsedClub)
    }

    @Test
    fun `most used club is the one with most shots`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver"),
            makeShot(clubUsed = "Driver"),
            makeShot(clubUsed = "Driver"),
            makeShot(clubUsed = "7 Iron"),
            makeShot(clubUsed = "Putter"),
        )
        val dashboard = AnalyticsComputer.computeDashboard(shots, listOf(makeRound()))
        assertEquals("Driver", dashboard.mostUsedClub)
    }

    @Test
    fun `filter by club`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver", distance = 250.0),
            makeShot(clubUsed = "7 Iron", distance = 150.0),
        )
        val filters = FilterState(club = "Driver")
        val filtered = AnalyticsComputer.filterShots(shots, listOf(makeRound()), filters)
        assertEquals(1, filtered.size)
        assertEquals("Driver", filtered[0].clubUsed)
    }

    @Test
    fun `filter by lie`() {
        val shots = listOf(
            makeShot(lie = Lie.Fairway),
            makeShot(lie = Lie.Bunker),
            makeShot(lie = Lie.Fairway),
        )
        val filters = FilterState(lie = Lie.Bunker)
        val filtered = AnalyticsComputer.filterShots(shots, listOf(makeRound()), filters)
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by wind strength`() {
        val shots = listOf(
            makeShot(windStrength = WindStrength.Calm),
            makeShot(windStrength = WindStrength.Strong),
        )
        val filters = FilterState(windStrength = WindStrength.Strong)
        val filtered = AnalyticsComputer.filterShots(shots, listOf(makeRound()), filters)
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by mental state`() {
        val shots = listOf(
            makeShot(mentalState = MentalState.Calm),
            makeShot(mentalState = MentalState.Rushed),
        )
        val filters = FilterState(mentalState = MentalState.Rushed)
        val filtered = AnalyticsComputer.filterShots(shots, listOf(makeRound()), filters)
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by weather type uses round data`() {
        val rounds = listOf(
            makeRound(id = 1L, weatherType = WeatherType.Sunny),
            makeRound(id = 2L, weatherType = WeatherType.HeavyRain),
        )
        val shots = listOf(
            makeShot(roundId = 1L),
            makeShot(roundId = 2L),
            makeShot(roundId = 2L),
        )
        val filters = FilterState(weatherType = WeatherType.HeavyRain)
        val filtered = AnalyticsComputer.filterShots(shots, rounds, filters)
        assertEquals(2, filtered.size)
    }

    @Test
    fun `groupEnumByClub counts ball directions per club`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver", ballDirection = BallDirection.Slice),
            makeShot(clubUsed = "Driver", ballDirection = BallDirection.Slice),
            makeShot(clubUsed = "Driver", ballDirection = BallDirection.Straight),
            makeShot(clubUsed = "7 Iron", ballDirection = BallDirection.Draw),
        )
        val result = AnalyticsComputer.groupEnumByClub(shots) { it.ballDirection }

        assertEquals(2, result.size)
        assertEquals(2, result["Driver"]!![BallDirection.Slice])
        assertEquals(1, result["Driver"]!![BallDirection.Straight])
        assertEquals(1, result["7 Iron"]!![BallDirection.Draw])
    }

    @Test
    fun `groupEnumByClub excludes null clubs`() {
        val shots = listOf(
            makeShot(clubUsed = null, ballDirection = BallDirection.Straight),
            makeShot(clubUsed = "Driver", ballDirection = BallDirection.Straight),
        )
        val result = AnalyticsComputer.groupEnumByClub(shots) { it.ballDirection }

        assertEquals(1, result.size)
        assertTrue(result.containsKey("Driver"))
    }

    @Test
    fun `groupEnumByClub excludes null enum values`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver", ballDirection = null),
            makeShot(clubUsed = "Driver", ballDirection = BallDirection.Straight),
        )
        val result = AnalyticsComputer.groupEnumByClub(shots) { it.ballDirection }

        assertEquals(1, result["Driver"]!!.size)
        assertEquals(1, result["Driver"]!![BallDirection.Straight])
    }

    // =====================================================
    // New: FilterState tests
    // =====================================================

    @Test
    fun `FilterState hasActiveFilters is false when empty`() {
        assertFalse(FilterState().hasActiveFilters)
        assertEquals(0, FilterState().activeFilterCount)
    }

    @Test
    fun `FilterState hasActiveFilters is true with one filter`() {
        assertTrue(FilterState(club = "Driver").hasActiveFilters)
        assertEquals(1, FilterState(club = "Driver").activeFilterCount)
    }

    @Test
    fun `FilterState activeFilterCount counts multiple filters`() {
        val fs = FilterState(club = "Driver", lie = Lie.Fairway, dateFrom = 1000L)
        assertEquals(3, fs.activeFilterCount)
    }

    // =====================================================
    // New: Date filtering
    // =====================================================

    @Test
    fun `filter by dateFrom excludes older shots`() {
        val shots = listOf(
            makeShot(timestamp = 1000L),
            makeShot(timestamp = 2000L),
            makeShot(timestamp = 3000L),
        )
        val filters = FilterState(dateFrom = 2000L)
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), filters)
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filter by dateTo excludes newer shots`() {
        val shots = listOf(
            makeShot(timestamp = 1000L),
            makeShot(timestamp = 2000L),
            makeShot(timestamp = 3000L),
        )
        val filters = FilterState(dateTo = 2000L)
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), filters)
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filter by date range`() {
        val shots = listOf(
            makeShot(timestamp = 1000L),
            makeShot(timestamp = 2000L),
            makeShot(timestamp = 3000L),
            makeShot(timestamp = 4000L),
        )
        val filters = FilterState(dateFrom = 2000L, dateTo = 3000L)
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), filters)
        assertEquals(2, filtered.size)
    }

    // =====================================================
    // New: Filter by shotType, lieDirection, windDirection, ballFlight, courseName
    // =====================================================

    @Test
    fun `filter by shotType`() {
        val shots = listOf(
            makeShot(shotType = ShotType.Full),
            makeShot(shotType = ShotType.Chip),
        )
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), FilterState(shotType = ShotType.Chip))
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by lieDirection`() {
        val shots = listOf(
            makeShot(lieDirection = LieDirection.Flat),
            makeShot(lieDirection = LieDirection.Uphill),
        )
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), FilterState(lieDirection = LieDirection.Uphill))
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by windDirection`() {
        val shots = listOf(
            makeShot(windDirection = WindDirection.N),
            makeShot(windDirection = WindDirection.S),
        )
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), FilterState(windDirection = WindDirection.S))
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by ballFlight`() {
        val shots = listOf(
            makeShot(ballFlight = BallFlight.High),
            makeShot(ballFlight = BallFlight.Low),
        )
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), FilterState(ballFlight = BallFlight.Low))
        assertEquals(1, filtered.size)
    }

    @Test
    fun `filter by courseName`() {
        val rounds = listOf(
            makeRound(id = 1L, courseName = "Links"),
            makeRound(id = 2L, courseName = "Parkland"),
        )
        val shots = listOf(
            makeShot(roundId = 1L),
            makeShot(roundId = 2L),
            makeShot(roundId = 2L),
        )
        val filtered = AnalyticsComputer.filterShots(shots, rounds, FilterState(courseName = "Parkland"))
        assertEquals(2, filtered.size)
    }

    @Test
    fun `combined filters narrow results`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver", lie = Lie.Tee, shotType = ShotType.Full),
            makeShot(clubUsed = "Driver", lie = Lie.Fairway, shotType = ShotType.Full),
            makeShot(clubUsed = "7 Iron", lie = Lie.Tee, shotType = ShotType.Full),
            makeShot(clubUsed = "7 Iron", lie = Lie.Fairway, shotType = ShotType.Chip),
        )
        val filters = FilterState(club = "Driver", lie = Lie.Tee)
        val filtered = AnalyticsComputer.filterShots(shots, emptyList(), filters)
        assertEquals(1, filtered.size)
    }

    // =====================================================
    // New: computeEnumDistribution
    // =====================================================

    @Test
    fun `computeEnumDistribution returns null for no data`() {
        val result = AnalyticsComputer.computeEnumDistribution(emptyList<Shot>()) { it.strike }
        assertNull(result)
    }

    @Test
    fun `computeEnumDistribution returns null when all values null`() {
        val shots = listOf(makeShot(strike = null), makeShot(strike = null))
        val result = AnalyticsComputer.computeEnumDistribution(shots) { it.strike }
        assertNull(result)
    }

    @Test
    fun `computeEnumDistribution counts correctly`() {
        val shots = listOf(
            makeShot(strike = Strike.Pure),
            makeShot(strike = Strike.Pure),
            makeShot(strike = Strike.Fat),
            makeShot(strike = Strike.Thin),
        )
        val result = AnalyticsComputer.computeEnumDistribution(shots) { it.strike }!!

        assertEquals(4, result.total)
        assertEquals(3, result.entries.size)
        assertEquals(Strike.Pure, result.entries[0].value)
        assertEquals(2, result.entries[0].count)
        assertEquals(0.5f, result.entries[0].percentage, 0.001f)
    }

    @Test
    fun `computeEnumDistribution sorted by count descending`() {
        val shots = listOf(
            makeShot(ballDirection = BallDirection.Slice),
            makeShot(ballDirection = BallDirection.Slice),
            makeShot(ballDirection = BallDirection.Slice),
            makeShot(ballDirection = BallDirection.Straight),
            makeShot(ballDirection = BallDirection.Draw),
        )
        val result = AnalyticsComputer.computeEnumDistribution(shots) { it.ballDirection }!!

        assertEquals(BallDirection.Slice, result.entries[0].value)
        assertEquals(BallDirection.Straight, result.entries[1].value)
        assertEquals(BallDirection.Draw, result.entries[2].value)
    }

    // =====================================================
    // New: computeDistanceStats
    // =====================================================

    @Test
    fun `computeDistanceStats returns null for no distances`() {
        val shots = listOf(makeShot(distance = null))
        assertNull(AnalyticsComputer.computeDistanceStats(shots))
    }

    @Test
    fun `computeDistanceStats single distance`() {
        val shots = listOf(makeShot(distance = 150.0))
        val stats = AnalyticsComputer.computeDistanceStats(shots)!!

        assertEquals(150.0, stats.average, 0.001)
        assertEquals(150.0, stats.median, 0.001)
        assertEquals(150.0, stats.min, 0.001)
        assertEquals(150.0, stats.max, 0.001)
        assertEquals(0.0, stats.stdDeviation, 0.001)
        assertEquals(1, stats.shotCount)
    }

    @Test
    fun `computeDistanceStats multiple distances`() {
        val shots = listOf(
            makeShot(distance = 100.0),
            makeShot(distance = 200.0),
            makeShot(distance = 300.0),
        )
        val stats = AnalyticsComputer.computeDistanceStats(shots)!!

        assertEquals(200.0, stats.average, 0.001)
        assertEquals(200.0, stats.median, 0.001)
        assertEquals(100.0, stats.min, 0.001)
        assertEquals(300.0, stats.max, 0.001)
        assertEquals(3, stats.shotCount)
        assertTrue(stats.stdDeviation > 0)
    }

    @Test
    fun `computeDistanceStats even count median`() {
        val shots = listOf(
            makeShot(distance = 100.0),
            makeShot(distance = 200.0),
            makeShot(distance = 300.0),
            makeShot(distance = 400.0),
        )
        val stats = AnalyticsComputer.computeDistanceStats(shots)!!
        assertEquals(250.0, stats.median, 0.001)
    }

    // =====================================================
    // New: computeHistogram
    // =====================================================

    @Test
    fun `computeHistogram empty list`() {
        assertTrue(AnalyticsComputer.computeHistogram(emptyList()).isEmpty())
    }

    @Test
    fun `computeHistogram single value`() {
        val result = AnalyticsComputer.computeHistogram(listOf(100.0))
        assertEquals(1, result.size)
        assertEquals(1, result[0].count)
    }

    @Test
    fun `computeHistogram covers all values`() {
        val distances = (1..100).map { it.toDouble() }.sorted()
        val result = AnalyticsComputer.computeHistogram(distances, 8)
        assertEquals(8, result.size)
        assertEquals(100, result.sumOf { it.count })
    }

    @Test
    fun `computeHistogram bucket ranges span full range`() {
        val distances = listOf(0.0, 50.0, 100.0)
        val result = AnalyticsComputer.computeHistogram(distances, 4)
        assertEquals(4, result.size)
        assertEquals(0.0, result.first().rangeStart, 0.001)
        assertEquals(100.0, result.last().rangeEnd, 0.001)
    }

    // =====================================================
    // New: computeElevationStats
    // =====================================================

    @Test
    fun `computeElevationStats returns null when no elevation data`() {
        val shots = listOf(makeShot(elevationChange = null))
        assertNull(AnalyticsComputer.computeElevationStats(shots))
    }

    @Test
    fun `computeElevationStats basic computation`() {
        val shots = listOf(
            makeShot(elevationChange = 10.0, distance = 140.0),
            makeShot(elevationChange = -5.0, distance = 160.0),
            makeShot(elevationChange = 0.0, distance = 150.0),
        )
        val stats = AnalyticsComputer.computeElevationStats(shots)!!

        assertEquals(10.0, stats.maxUphill, 0.001)
        assertEquals(-5.0, stats.maxDownhill, 0.001)
        assertEquals(140.0, stats.uphillAvgDistance!!, 0.001)
        assertEquals(150.0, stats.flatAvgDistance!!, 0.001)
        assertEquals(160.0, stats.downhillAvgDistance!!, 0.001)
    }

    // =====================================================
    // New: computeClubDistanceSummaries
    // =====================================================

    @Test
    fun `computeClubDistanceSummaries requires at least 2 distances per club`() {
        val shots = listOf(makeShot(clubUsed = "Driver", distance = 250.0))
        val result = AnalyticsComputer.computeClubDistanceSummaries(shots)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `computeClubDistanceSummaries correct stats`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver", distance = 240.0),
            makeShot(clubUsed = "Driver", distance = 260.0),
        )
        val result = AnalyticsComputer.computeClubDistanceSummaries(shots)
        assertEquals(1, result.size)
        assertEquals("Driver", result[0].club)
        assertEquals(250.0, result[0].avgDistance, 0.001)
        assertEquals(240.0, result[0].min, 0.001)
        assertEquals(260.0, result[0].max, 0.001)
        assertEquals(2, result[0].shotCount)
        assertTrue(result[0].stdDeviation > 0)
    }

    @Test
    fun `computeClubDistanceSummaries sorted by avg distance descending`() {
        val shots = listOf(
            makeShot(clubUsed = "Driver", distance = 250.0),
            makeShot(clubUsed = "Driver", distance = 260.0),
            makeShot(clubUsed = "7 Iron", distance = 140.0),
            makeShot(clubUsed = "7 Iron", distance = 160.0),
        )
        val result = AnalyticsComputer.computeClubDistanceSummaries(shots)
        assertEquals("Driver", result[0].club)
        assertEquals("7 Iron", result[1].club)
    }

    // =====================================================
    // New: identifyWeaknesses
    // =====================================================

    @Test
    fun `identifyWeaknesses returns empty with too few shots`() {
        val shots = (1..5).map { makeShot(ballDirection = BallDirection.Slice) }
        assertTrue(AnalyticsComputer.identifyWeaknesses(shots).isEmpty())
    }

    @Test
    fun `identifyWeaknesses detects slice tendency`() {
        val shots = (1..10).map { makeShot(ballDirection = BallDirection.Slice) }
        val weaknesses = AnalyticsComputer.identifyWeaknesses(shots)
        assertTrue(weaknesses.any { it.label == "Slice Tendency" })
    }

    @Test
    fun `identifyWeaknesses detects poor strike rate`() {
        val shots = (1..10).map { makeShot(strike = Strike.Fat) }
        val weaknesses = AnalyticsComputer.identifyWeaknesses(shots)
        assertTrue(weaknesses.any { it.label == "Strike Quality" })
        assertTrue(weaknesses.any { it.label == "Fat Shots" })
    }

    @Test
    fun `identifyWeaknesses no weaknesses for perfect shots`() {
        val shots = (1..20).map {
            makeShot(
                ballDirection = BallDirection.Straight,
                strike = Strike.Pure,
                directionToTarget = DirectionToTarget.Straight,
                distanceToTarget = DistanceToTarget.OnPin,
                mentalState = MentalState.Calm,
            )
        }
        val weaknesses = AnalyticsComputer.identifyWeaknesses(shots)
        assertTrue(weaknesses.isEmpty())
    }

    @Test
    fun `identifyWeaknesses limited to 5`() {
        // Create shots that trigger many weaknesses
        val shots = (1..20).map {
            makeShot(
                ballDirection = BallDirection.Slice,
                strike = Strike.Fat,
                directionToTarget = DirectionToTarget.FarRight,
                distanceToTarget = DistanceToTarget.WayLong,
                mentalState = MentalState.Rushed,
            )
        }
        val weaknesses = AnalyticsComputer.identifyWeaknesses(shots)
        assertTrue(weaknesses.size <= 5)
    }

    // =====================================================
    // New: computeDashboard
    // =====================================================

    @Test
    fun `computeDashboard uniqueCourses counts distinct courses`() {
        val rounds = listOf(
            makeRound(id = 1, courseName = "Links"),
            makeRound(id = 2, courseName = "Links"),
            makeRound(id = 3, courseName = "Parkland"),
        )
        val dashboard = AnalyticsComputer.computeDashboard(listOf(makeShot()), rounds)
        assertEquals(2, dashboard.uniqueCourses)
    }

    @Test
    fun `computeDashboard performance rates with data`() {
        val shots = listOf(
            makeShot(strike = Strike.Pure, directionToTarget = DirectionToTarget.Straight, distanceToTarget = DistanceToTarget.OnPin),
            makeShot(strike = Strike.Fat, directionToTarget = DirectionToTarget.Left, distanceToTarget = DistanceToTarget.Long),
        )
        val dashboard = AnalyticsComputer.computeDashboard(shots, listOf(makeRound()))
        assertEquals(0.5f, dashboard.overallPureStrikeRate!!, 0.001f)
        assertEquals(0.5f, dashboard.overallStraightRate!!, 0.001f)
        assertEquals(0.5f, dashboard.overallOnPinRate!!, 0.001f)
    }

    @Test
    fun `computeDashboard performance rates null when no data`() {
        val shots = listOf(
            makeShot(strike = null, directionToTarget = null, distanceToTarget = null),
        )
        val dashboard = AnalyticsComputer.computeDashboard(shots, listOf(makeRound()))
        assertNull(dashboard.overallPureStrikeRate)
        assertNull(dashboard.overallStraightRate)
        assertNull(dashboard.overallOnPinRate)
    }

    // =====================================================
    // New: computeShotAnalysis
    // =====================================================

    @Test
    fun `computeShotAnalysis empty filtered returns counts only`() {
        val allShots = listOf(makeShot())
        val state = AnalyticsComputer.computeShotAnalysis(allShots, emptyList(), noFilters)
        assertEquals(0, state.filteredShotCount)
        assertEquals(1, state.totalShotCount)
        assertNull(state.distanceStats)
    }

    @Test
    fun `computeShotAnalysis excludes filtered-by distributions`() {
        val shots = listOf(makeShot(), makeShot())
        val filters = FilterState(lie = Lie.Fairway, shotType = ShotType.Full)
        val state = AnalyticsComputer.computeShotAnalysis(shots, shots, filters)

        // lie and shotType are actively filtered, so their distributions should be null
        assertNull(state.lieDistribution)
        assertNull(state.shotTypeDistribution)
        // Other distributions should be present
        assertNotNull(state.strikeDistribution)
    }

    @Test
    fun `computeShotAnalysis clubStats empty when club filter active`() {
        val shots = listOf(makeShot(distance = 150.0), makeShot(distance = 160.0))
        val filters = FilterState(club = "7 Iron")
        val state = AnalyticsComputer.computeShotAnalysis(shots, shots, filters)
        assertTrue(state.clubStats.isEmpty())
    }
}
