package com.simplegolfgps.data

import org.junit.Assert.*
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    // WeatherType
    @Test
    fun `WeatherType round trip`() {
        for (value in WeatherType.entries) {
            val str = converters.fromWeatherType(value)
            assertEquals(value, converters.toWeatherType(str))
        }
    }

    @Test
    fun `WeatherType null handling`() {
        assertNull(converters.fromWeatherType(null))
        assertNull(converters.toWeatherType(null))
    }

    // WindDirection
    @Test
    fun `WindDirection round trip`() {
        for (value in WindDirection.entries) {
            val str = converters.fromWindDirection(value)
            assertEquals(value, converters.toWindDirection(str))
        }
    }

    @Test
    fun `WindDirection null handling`() {
        assertNull(converters.fromWindDirection(null))
        assertNull(converters.toWindDirection(null))
    }

    // WindStrength
    @Test
    fun `WindStrength round trip`() {
        for (value in WindStrength.entries) {
            val str = converters.fromWindStrength(value)
            assertEquals(value, converters.toWindStrength(str))
        }
    }

    @Test
    fun `WindStrength null handling`() {
        assertNull(converters.fromWindStrength(null))
        assertNull(converters.toWindStrength(null))
    }

    // Lie
    @Test
    fun `Lie round trip`() {
        for (value in Lie.entries) {
            val str = converters.fromLie(value)
            assertEquals(value, converters.toLie(str))
        }
    }

    @Test
    fun `Lie null handling`() {
        assertNull(converters.fromLie(null))
        assertNull(converters.toLie(null))
    }

    // Strike
    @Test
    fun `Strike round trip`() {
        for (value in Strike.entries) {
            val str = converters.fromStrike(value)
            assertEquals(value, converters.toStrike(str))
        }
    }

    @Test
    fun `Strike null handling`() {
        assertNull(converters.fromStrike(null))
        assertNull(converters.toStrike(null))
    }

    // ClubDirection
    @Test
    fun `ClubDirection round trip`() {
        for (value in ClubDirection.entries) {
            val str = converters.fromClubDirection(value)
            assertEquals(value, converters.toClubDirection(str))
        }
    }

    @Test
    fun `ClubDirection null handling`() {
        assertNull(converters.fromClubDirection(null))
        assertNull(converters.toClubDirection(null))
    }

    // BallDirection
    @Test
    fun `BallDirection round trip`() {
        for (value in BallDirection.entries) {
            val str = converters.fromBallDirection(value)
            assertEquals(value, converters.toBallDirection(str))
        }
    }

    @Test
    fun `BallDirection null handling`() {
        assertNull(converters.fromBallDirection(null))
        assertNull(converters.toBallDirection(null))
    }

    // LieDirection
    @Test
    fun `LieDirection round trip`() {
        for (value in LieDirection.entries) {
            val str = converters.fromLieDirection(value)
            assertEquals(value, converters.toLieDirection(str))
        }
    }

    @Test
    fun `LieDirection null handling`() {
        assertNull(converters.fromLieDirection(null))
        assertNull(converters.toLieDirection(null))
    }

    // MentalState
    @Test
    fun `MentalState round trip`() {
        for (value in MentalState.entries) {
            val str = converters.fromMentalState(value)
            assertEquals(value, converters.toMentalState(str))
        }
    }

    @Test
    fun `MentalState null handling`() {
        assertNull(converters.fromMentalState(null))
        assertNull(converters.toMentalState(null))
    }

    // BallFlight
    @Test
    fun `BallFlight round trip`() {
        for (value in BallFlight.entries) {
            val str = converters.fromBallFlight(value)
            assertEquals(value, converters.toBallFlight(str))
        }
    }

    @Test
    fun `BallFlight null handling`() {
        assertNull(converters.fromBallFlight(null))
        assertNull(converters.toBallFlight(null))
    }

    // DirectionToTarget
    @Test
    fun `DirectionToTarget round trip`() {
        for (value in DirectionToTarget.entries) {
            val str = converters.fromDirectionToTarget(value)
            assertEquals(value, converters.toDirectionToTarget(str))
        }
    }

    @Test
    fun `DirectionToTarget null handling`() {
        assertNull(converters.fromDirectionToTarget(null))
        assertNull(converters.toDirectionToTarget(null))
    }

    // DistanceToTarget
    @Test
    fun `DistanceToTarget round trip`() {
        for (value in DistanceToTarget.entries) {
            val str = converters.fromDistanceToTarget(value)
            assertEquals(value, converters.toDistanceToTarget(str))
        }
    }

    @Test
    fun `DistanceToTarget null handling`() {
        assertNull(converters.fromDistanceToTarget(null))
        assertNull(converters.toDistanceToTarget(null))
    }
}
