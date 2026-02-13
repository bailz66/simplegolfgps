package com.simplegolfgps.ui.shots

import org.junit.Assert.*
import org.junit.Test

class ShotFormStateTest {

    @Test
    fun `default shotNumber is 1`() {
        val state = ShotFormState()
        assertEquals(1, state.shotNumber)
    }

    @Test
    fun `default holeNumber is 1`() {
        val state = ShotFormState()
        assertEquals(1, state.holeNumber)
    }

    @Test
    fun `can set shotNumber via constructor`() {
        val state = ShotFormState(shotNumber = 5)
        assertEquals(5, state.shotNumber)
    }

    @Test
    fun `copy preserves shotNumber`() {
        val state = ShotFormState(holeNumber = 3, shotNumber = 4)
        val copied = state.copy(clubUsed = "Driver")
        assertEquals(4, copied.shotNumber)
        assertEquals(3, copied.holeNumber)
        assertEquals("Driver", copied.clubUsed)
    }

    @Test
    fun `copy can change shotNumber`() {
        val state = ShotFormState(shotNumber = 2)
        val changed = state.copy(shotNumber = 7)
        assertEquals(7, changed.shotNumber)
    }

    @Test
    fun `resetting form preserves hole and shot number`() {
        val state = ShotFormState(
            holeNumber = 5,
            shotNumber = 3,
            clubUsed = "7 Iron",
            distance = "150.0",
        )
        // Simulate the reset pattern used in saveShot: new ShotFormState with just hole/shot
        val reset = ShotFormState(holeNumber = state.holeNumber, shotNumber = state.shotNumber + 1)
        assertEquals(5, reset.holeNumber)
        assertEquals(4, reset.shotNumber)
        assertNull(reset.clubUsed)
        assertEquals("", reset.distance)
    }

    @Test
    fun `default carryDistance is empty string`() {
        val state = ShotFormState()
        assertEquals("", state.carryDistance)
    }

    @Test
    fun `can set carryDistance`() {
        val state = ShotFormState(carryDistance = "180.0")
        assertEquals("180.0", state.carryDistance)
    }

    @Test
    fun `copy preserves carryDistance`() {
        val state = ShotFormState(distance = "200.0", carryDistance = "180.0")
        val copied = state.copy(clubUsed = "7-Iron")
        assertEquals("180.0", copied.carryDistance)
        assertEquals("200.0", copied.distance)
    }

    @Test
    fun `resetting form clears carryDistance`() {
        val state = ShotFormState(holeNumber = 1, shotNumber = 1, carryDistance = "180.0")
        val reset = ShotFormState(holeNumber = state.holeNumber, shotNumber = state.shotNumber + 1)
        assertEquals("", reset.carryDistance)
    }

    @Test
    fun `default carryElevationChange is zero`() {
        val state = ShotFormState()
        assertEquals(0.0, state.carryElevationChange, 0.001)
    }

    @Test
    fun `can set carryElevationChange`() {
        val state = ShotFormState(carryDistance = "180.0", carryElevationChange = -3.5)
        assertEquals(-3.5, state.carryElevationChange, 0.001)
    }

    @Test
    fun `copy preserves carryElevationChange`() {
        val state = ShotFormState(carryElevationChange = 5.0)
        val copied = state.copy(carryDistance = "200.0")
        assertEquals(5.0, copied.carryElevationChange, 0.001)
    }

    @Test
    fun `resetting form clears carryElevationChange`() {
        val state = ShotFormState(holeNumber = 1, shotNumber = 1, carryElevationChange = 5.0)
        val reset = ShotFormState(holeNumber = state.holeNumber, shotNumber = state.shotNumber + 1)
        assertEquals(0.0, reset.carryElevationChange, 0.001)
    }

    @Test
    fun `default MeasurementState carryLocked is false`() {
        val state = MeasurementState()
        assertFalse(state.carryLocked)
    }

    @Test
    fun `MeasurementState copy can set carryLocked`() {
        val state = MeasurementState(isMeasuring = true, startLocked = true)
        val locked = state.copy(carryLocked = true)
        assertTrue(locked.carryLocked)
        assertTrue(locked.isMeasuring)
        assertTrue(locked.startLocked)
    }

    @Test
    fun `form state equality includes shotNumber`() {
        val a = ShotFormState(holeNumber = 1, shotNumber = 1)
        val b = ShotFormState(holeNumber = 1, shotNumber = 2)
        assertNotEquals(a, b)
    }

    @Test
    fun `form state equality with same shotNumber`() {
        val a = ShotFormState(holeNumber = 1, shotNumber = 3)
        val b = ShotFormState(holeNumber = 1, shotNumber = 3)
        assertEquals(a, b)
    }
}
