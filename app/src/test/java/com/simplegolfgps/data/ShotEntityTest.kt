package com.simplegolfgps.data

import org.junit.Assert.*
import org.junit.Test

class ShotEntityTest {

    @Test
    fun `default shotNumber is 1`() {
        val shot = Shot(roundId = 1L, holeNumber = 1)
        assertEquals(1, shot.shotNumber)
    }

    @Test
    fun `can set shotNumber explicitly`() {
        val shot = Shot(roundId = 1L, holeNumber = 3, shotNumber = 5)
        assertEquals(5, shot.shotNumber)
        assertEquals(3, shot.holeNumber)
    }

    @Test
    fun `copy preserves shotNumber`() {
        val shot = Shot(roundId = 1L, holeNumber = 2, shotNumber = 3, clubUsed = "Driver")
        val copied = shot.copy(distance = 250.0)
        assertEquals(3, copied.shotNumber)
        assertEquals(2, copied.holeNumber)
        assertEquals("Driver", copied.clubUsed)
        assertEquals(250.0, copied.distance!!, 0.001)
    }

    @Test
    fun `copy can change shotNumber`() {
        val shot = Shot(roundId = 1L, holeNumber = 1, shotNumber = 1)
        val changed = shot.copy(shotNumber = 4)
        assertEquals(4, changed.shotNumber)
    }

    @Test
    fun `shot equality includes shotNumber`() {
        val a = Shot(id = 1, roundId = 1L, holeNumber = 1, shotNumber = 1)
        val b = Shot(id = 1, roundId = 1L, holeNumber = 1, shotNumber = 2)
        assertNotEquals(a, b)
    }

    @Test
    fun `shots with same fields are equal`() {
        val timestamp = System.currentTimeMillis()
        val a = Shot(id = 1, roundId = 1L, holeNumber = 5, shotNumber = 3, timestamp = timestamp)
        val b = Shot(id = 1, roundId = 1L, holeNumber = 5, shotNumber = 3, timestamp = timestamp)
        assertEquals(a, b)
    }

    @Test
    fun `default carryDistance is null`() {
        val shot = Shot(roundId = 1L, holeNumber = 1)
        assertNull(shot.carryDistance)
    }

    @Test
    fun `can set carryDistance`() {
        val shot = Shot(roundId = 1L, holeNumber = 1, distance = 250.0, carryDistance = 230.0)
        assertEquals(230.0, shot.carryDistance!!, 0.001)
        assertEquals(250.0, shot.distance!!, 0.001)
    }

    @Test
    fun `copy preserves carryDistance`() {
        val shot = Shot(roundId = 1L, holeNumber = 1, carryDistance = 180.0)
        val copied = shot.copy(distance = 200.0)
        assertEquals(180.0, copied.carryDistance!!, 0.001)
        assertEquals(200.0, copied.distance!!, 0.001)
    }

    @Test
    fun `copy can change carryDistance`() {
        val shot = Shot(roundId = 1L, holeNumber = 1, carryDistance = 180.0)
        val changed = shot.copy(carryDistance = 190.0)
        assertEquals(190.0, changed.carryDistance!!, 0.001)
    }

    @Test
    fun `default carryElevationChange is null`() {
        val shot = Shot(roundId = 1L, holeNumber = 1)
        assertNull(shot.carryElevationChange)
    }

    @Test
    fun `can set carryElevationChange`() {
        val shot = Shot(roundId = 1L, holeNumber = 1, carryDistance = 230.0, carryElevationChange = -3.5)
        assertEquals(-3.5, shot.carryElevationChange!!, 0.001)
    }

    @Test
    fun `copy preserves carryElevationChange`() {
        val shot = Shot(roundId = 1L, holeNumber = 1, carryElevationChange = 5.0)
        val copied = shot.copy(carryDistance = 200.0)
        assertEquals(5.0, copied.carryElevationChange!!, 0.001)
    }

    @Test
    fun `shot equality includes carryDistance`() {
        val timestamp = System.currentTimeMillis()
        val a = Shot(id = 1, roundId = 1L, holeNumber = 1, carryDistance = 100.0, timestamp = timestamp)
        val b = Shot(id = 1, roundId = 1L, holeNumber = 1, carryDistance = 200.0, timestamp = timestamp)
        assertNotEquals(a, b)
    }

    @Test
    fun `shot with all fields populated includes shotNumber`() {
        val shot = Shot(
            roundId = 1L,
            holeNumber = 7,
            shotNumber = 2,
            clubUsed = "7 Iron",
            distance = 150.0,
            carryDistance = 140.0,
            elevationChange = 5.0,
            windDirection = WindDirection.N,
            windStrength = WindStrength.Moderate,
            lie = Lie.Fairway,
            shotType = ShotType.Full,
            strike = Strike.Pure,
            clubDirection = ClubDirection.Straight,
            ballDirection = BallDirection.Straight,
            lieDirection = LieDirection.Flat,
            mentalState = MentalState.Calm,
            ballFlight = BallFlight.Medium,
            directionToTarget = DirectionToTarget.Straight,
            distanceToTarget = DistanceToTarget.OnPin,
        )
        assertEquals(7, shot.holeNumber)
        assertEquals(2, shot.shotNumber)
        assertEquals("7 Iron", shot.clubUsed)
    }
}
