package com.simplegolfgps.ui.shots

import com.simplegolfgps.ui.shots.ShotViewModel.Companion.haversineDistance
import org.junit.Assert.*
import org.junit.Test

class HaversineTest {

    @Test
    fun `same point returns zero distance`() {
        val distance = haversineDistance(51.5074, -0.1278, 51.5074, -0.1278)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `London to Paris approximately 343km`() {
        // London: 51.5074 N, 0.1278 W  Paris: 48.8566 N, 2.3522 E
        val distance = haversineDistance(51.5074, -0.1278, 48.8566, 2.3522)
        // Expected ~343km, allow 5km tolerance for coordinate precision
        assertEquals(343_000.0, distance, 5_000.0)
    }

    @Test
    fun `short golf course distance approximately 150m`() {
        // Two points approximately 150m apart on a golf course
        // Starting at (0, 0), moving ~150m north is about 0.00135 degrees latitude
        val distance = haversineDistance(0.0, 0.0, 0.00135, 0.0)
        assertEquals(150.0, distance, 2.0)
    }

    @Test
    fun `antipodal points approximately 20000km`() {
        // North Pole to South Pole
        val distance = haversineDistance(90.0, 0.0, -90.0, 0.0)
        // Half circumference of Earth ~20,015 km
        assertEquals(20_015_000.0, distance, 100_000.0)
    }

    @Test
    fun `result is in metres`() {
        // 1 degree of latitude at equator using r=6371000 is ~111,195 metres
        val distance = haversineDistance(0.0, 0.0, 1.0, 0.0)
        assertEquals(111_195.0, distance, 100.0)
    }

    @Test
    fun `symmetry - A to B equals B to A`() {
        val ab = haversineDistance(51.5074, -0.1278, 48.8566, 2.3522)
        val ba = haversineDistance(48.8566, 2.3522, 51.5074, -0.1278)
        assertEquals(ab, ba, 0.001)
    }

    @Test
    fun `typical golf drive distance 250m`() {
        // About 250m north from equator = ~0.00225 degrees
        val distance = haversineDistance(0.0, 0.0, 0.00225, 0.0)
        assertEquals(250.0, distance, 2.0)
    }
}
