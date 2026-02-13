package com.simplegolfgps.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromWeatherType(value: WeatherType?): String? = value?.name
    @TypeConverter fun toWeatherType(value: String?): WeatherType? = value?.let { WeatherType.valueOf(it) }

    @TypeConverter fun fromWindDirection(value: WindDirection?): String? = value?.name
    @TypeConverter fun toWindDirection(value: String?): WindDirection? = value?.let { WindDirection.valueOf(it) }

    @TypeConverter fun fromWindStrength(value: WindStrength?): String? = value?.name
    @TypeConverter fun toWindStrength(value: String?): WindStrength? = value?.let { WindStrength.valueOf(it) }

    @TypeConverter fun fromLie(value: Lie?): String? = value?.name
    @TypeConverter fun toLie(value: String?): Lie? = value?.let { Lie.valueOf(it) }

    @TypeConverter fun fromShotType(value: ShotType?): String? = value?.name
    @TypeConverter fun toShotType(value: String?): ShotType? = value?.let { ShotType.valueOf(it) }

    @TypeConverter fun fromStrike(value: Strike?): String? = value?.name
    @TypeConverter fun toStrike(value: String?): Strike? = value?.let { Strike.valueOf(it) }

    @TypeConverter fun fromClubDirection(value: ClubDirection?): String? = value?.name
    @TypeConverter fun toClubDirection(value: String?): ClubDirection? = value?.let { ClubDirection.valueOf(it) }

    @TypeConverter fun fromBallDirection(value: BallDirection?): String? = value?.name
    @TypeConverter fun toBallDirection(value: String?): BallDirection? = value?.let { BallDirection.valueOf(it) }

    @TypeConverter fun fromLieDirection(value: LieDirection?): String? = value?.name
    @TypeConverter fun toLieDirection(value: String?): LieDirection? = value?.let { LieDirection.valueOf(it) }

    @TypeConverter fun fromMentalState(value: MentalState?): String? = value?.name
    @TypeConverter fun toMentalState(value: String?): MentalState? = value?.let { MentalState.valueOf(it) }

    @TypeConverter fun fromBallFlight(value: BallFlight?): String? = value?.name
    @TypeConverter fun toBallFlight(value: String?): BallFlight? = value?.let { BallFlight.valueOf(it) }

    @TypeConverter fun fromDirectionToTarget(value: DirectionToTarget?): String? = value?.name
    @TypeConverter fun toDirectionToTarget(value: String?): DirectionToTarget? = value?.let { DirectionToTarget.valueOf(it) }

    @TypeConverter fun fromDistanceToTarget(value: DistanceToTarget?): String? = value?.name
    @TypeConverter fun toDistanceToTarget(value: String?): DistanceToTarget? = value?.let { DistanceToTarget.valueOf(it) }

    @TypeConverter fun fromFairwayHit(value: FairwayHit?): String? = value?.name
    @TypeConverter fun toFairwayHit(value: String?): FairwayHit? = value?.let { FairwayHit.valueOf(it) }

    @TypeConverter fun fromGreenInRegulation(value: GreenInRegulation?): String? = value?.name
    @TypeConverter fun toGreenInRegulation(value: String?): GreenInRegulation? = value?.let { GreenInRegulation.valueOf(it) }
}
