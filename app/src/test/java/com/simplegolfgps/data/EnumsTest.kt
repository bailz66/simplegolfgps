package com.simplegolfgps.data

import org.junit.Assert.*
import org.junit.Test

class EnumsTest {

    @Test
    fun `all WeatherType values have non-blank displayName`() {
        for (value in WeatherType.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all WindDirection values have non-blank displayName`() {
        for (value in WindDirection.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all WindDirection values have non-blank arrow`() {
        for (value in WindDirection.entries) {
            assertTrue("${value.name} has blank arrow", value.arrow.isNotBlank())
        }
    }

    @Test
    fun `all WindStrength values have non-blank displayName`() {
        for (value in WindStrength.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all Lie values have non-blank displayName`() {
        for (value in Lie.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all Strike values have non-blank displayName`() {
        for (value in Strike.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all ClubDirection values have non-blank displayName`() {
        for (value in ClubDirection.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all BallDirection values have non-blank displayName`() {
        for (value in BallDirection.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all LieDirection values have non-blank displayName`() {
        for (value in LieDirection.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all MentalState values have non-blank displayName`() {
        for (value in MentalState.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all BallFlight values have non-blank displayName`() {
        for (value in BallFlight.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all DirectionToTarget values have non-blank displayName`() {
        for (value in DirectionToTarget.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    @Test
    fun `all DistanceToTarget values have non-blank displayName`() {
        for (value in DistanceToTarget.entries) {
            assertTrue("${value.name} has blank displayName", value.displayName.isNotBlank())
        }
    }

    // Spot-check specific display names
    @Test
    fun `LightRough displays as Rough`() {
        assertEquals("Rough", Lie.LightRough.displayName)
    }

    @Test
    fun `WormBurner displays as Worm`() {
        assertEquals("Worm", BallFlight.WormBurner.displayName)
    }

    @Test
    fun `HeavyRain displays as Heavy Rain`() {
        assertEquals("Heavy Rain", WeatherType.HeavyRain.displayName)
    }

    @Test
    fun `VeryStrong wind displays as V-Strong`() {
        assertEquals("V-Strong", WindStrength.VeryStrong.displayName)
    }

    @Test
    fun `AboveFeet lie direction displays as Above`() {
        assertEquals("Above", LieDirection.AboveFeet.displayName)
    }

    @Test
    fun `FarLeft direction to target displays as F-Left`() {
        assertEquals("F-Left", DirectionToTarget.FarLeft.displayName)
    }

    @Test
    fun `WayLong distance to target displays as V-Long`() {
        assertEquals("V-Long", DistanceToTarget.WayLong.displayName)
    }

    // WindDirection arrows spot-checks
    @Test
    fun `North arrow is up arrow`() {
        assertEquals("↑", WindDirection.N.arrow)
    }

    @Test
    fun `South arrow is down arrow`() {
        assertEquals("↓", WindDirection.S.arrow)
    }

    @Test
    fun `East arrow is right arrow`() {
        assertEquals("→", WindDirection.E.arrow)
    }

    @Test
    fun `West arrow is left arrow`() {
        assertEquals("←", WindDirection.W.arrow)
    }

    @Test
    fun `WindDirection has exactly 8 directions`() {
        assertEquals(8, WindDirection.entries.size)
    }
}
