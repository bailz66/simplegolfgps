package com.simplegolfgps.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shots",
    foreignKeys = [
        ForeignKey(
            entity = Round::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["roundId"])]
)
data class Shot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val holeNumber: Int,
    val clubUsed: String? = null,
    val distance: Double? = null,
    val carryDistance: Double? = null,
    val carryElevationChange: Double? = null,
    val elevationChange: Double? = null,
    val windDirection: WindDirection? = null,
    val windStrength: WindStrength? = null,
    val lie: Lie? = null,
    val shotType: ShotType? = null,
    val strike: Strike? = null,
    val clubDirection: ClubDirection? = null,
    val ballDirection: BallDirection? = null,
    val lieDirection: LieDirection? = null,
    val mentalState: MentalState? = null,
    val mentalStateNote: String? = null,
    val ballFlight: BallFlight? = null,
    val directionToTarget: DirectionToTarget? = null,
    val distanceToTarget: DistanceToTarget? = null,
    val customNote: String? = null,
    val shotNumber: Int = 1,
    val ignoreForAnalytics: Boolean = false,
    val powerPct: Int? = null,
    val fairwayHit: FairwayHit? = null,
    val greenInRegulation: GreenInRegulation? = null,
    val targetDistance: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
