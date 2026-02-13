package com.simplegolfgps.analytics

import com.simplegolfgps.data.*

enum class AnalyticsTab(val label: String) {
    Dashboard("Dashboard"),
    ShotAnalysis("Shot Analysis"),
}

data class FilterState(
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val club: String? = null,
    val shotType: ShotType? = null,
    val lie: Lie? = null,
    val lieDirection: LieDirection? = null,
    val strike: Strike? = null,
    val weatherType: WeatherType? = null,
    val windStrength: WindStrength? = null,
    val windDirection: WindDirection? = null,
    val mentalState: MentalState? = null,
    val ballFlight: BallFlight? = null,
    val courseName: String? = null,
) {
    val hasActiveFilters: Boolean
        get() = activeFilterCount > 0

    val activeFilterCount: Int
        get() = listOfNotNull(
            dateFrom, dateTo, club, shotType, lie, lieDirection, strike,
            weatherType, windStrength, windDirection, mentalState, ballFlight, courseName,
        ).size
}

data class DashboardState(
    val totalShots: Int = 0,
    val totalRounds: Int = 0,
    val uniqueCourses: Int = 0,
    val mostUsedClub: String? = null,
    val clubUsageCounts: List<ClubUsage> = emptyList(),
    val overallPureStrikeRate: Float? = null,
    val overallStraightRate: Float? = null,
    val overallOnPinRate: Float? = null,
    val fairwayHitRate: Float? = null,
    val greenInRegulationRate: Float? = null,
    val recentTrend: TrendData? = null,
    val weaknesses: List<WeaknessItem> = emptyList(),
    val clubDistanceOverview: List<ClubDistanceSummary> = emptyList(),
    val isLoading: Boolean = true,
)

data class ShotAnalysisState(
    val filteredShotCount: Int = 0,
    val totalShotCount: Int = 0,
    val distanceStats: DistanceStats? = null,
    val elevationStats: ElevationStats? = null,
    val strikeDistribution: EnumDistribution<Strike>? = null,
    val ballFlightDistribution: EnumDistribution<BallFlight>? = null,
    val clubDirectionDistribution: EnumDistribution<ClubDirection>? = null,
    val ballDirectionDistribution: EnumDistribution<BallDirection>? = null,
    val directionToTargetDistribution: EnumDistribution<DirectionToTarget>? = null,
    val distanceToTargetDistribution: EnumDistribution<DistanceToTarget>? = null,
    val lieDistribution: EnumDistribution<Lie>? = null,
    val lieDirectionDistribution: EnumDistribution<LieDirection>? = null,
    val windStrengthDistribution: EnumDistribution<WindStrength>? = null,
    val windDirectionDistribution: EnumDistribution<WindDirection>? = null,
    val mentalStateDistribution: EnumDistribution<MentalState>? = null,
    val shotTypeDistribution: EnumDistribution<ShotType>? = null,
    val clubStats: List<ClubDistanceSummary> = emptyList(),
    val isLoading: Boolean = true,
)

data class DistanceStats(
    val average: Double,
    val median: Double,
    val min: Double,
    val max: Double,
    val stdDeviation: Double,
    val shotCount: Int,
    val histogram: List<HistogramBucket>,
)

data class ElevationStats(
    val avgElevationChange: Double,
    val maxUphill: Double,
    val maxDownhill: Double,
    val uphillAvgDistance: Double?,
    val flatAvgDistance: Double?,
    val downhillAvgDistance: Double?,
)

data class EnumDistribution<T>(
    val entries: List<EnumCount<T>>,
    val total: Int,
)

data class EnumCount<T>(
    val value: T,
    val count: Int,
    val percentage: Float,
)

data class TrendData(
    val recentPureStrikeRate: Float?,
    val priorPureStrikeRate: Float?,
    val recentStraightRate: Float?,
    val priorStraightRate: Float?,
    val recentAvgDistance: Double?,
    val priorAvgDistance: Double?,
    val comparisonClub: String?,
    val hasEnoughData: Boolean,
)

data class WeaknessItem(
    val label: String,
    val detail: String,
    val severity: Float, // 0..1
)

data class ClubDistanceSummary(
    val club: String,
    val avgDistance: Double,
    val min: Double,
    val max: Double,
    val shotCount: Int,
    val stdDeviation: Double,
)

data class ClubUsage(
    val club: String,
    val count: Int,
    val percentage: Float,
)

data class HistogramBucket(
    val rangeStart: Double,
    val rangeEnd: Double,
    val count: Int,
    val label: String,
)
