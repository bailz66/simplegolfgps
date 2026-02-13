package com.simplegolfgps.settings

import org.junit.Assert.*
import org.junit.Test

class UnitConverterTest {

    @Test
    fun `metresToDisplay metric returns same value`() {
        assertEquals(100.0, UnitConverter.metresToDisplay(100.0, imperial = false), 0.001)
    }

    @Test
    fun `metresToDisplay imperial converts to yards`() {
        // 100m = 109.361 yards
        assertEquals(109.361, UnitConverter.metresToDisplay(100.0, imperial = true), 0.001)
    }

    @Test
    fun `displayToMetres metric returns same value`() {
        assertEquals(100.0, UnitConverter.displayToMetres(100.0, imperial = false), 0.001)
    }

    @Test
    fun `displayToMetres imperial converts yards to metres`() {
        // 109.361 yards = 100m
        assertEquals(100.0, UnitConverter.displayToMetres(109.361, imperial = true), 0.001)
    }

    @Test
    fun `round trip metres to yards and back`() {
        val original = 150.0
        val yards = UnitConverter.metresToDisplay(original, imperial = true)
        val backToMetres = UnitConverter.displayToMetres(yards, imperial = true)
        assertEquals(original, backToMetres, 0.001)
    }

    @Test
    fun `celsiusToFahrenheit freezing point`() {
        assertEquals(32, UnitConverter.celsiusToFahrenheit(0))
    }

    @Test
    fun `celsiusToFahrenheit boiling point`() {
        assertEquals(212, UnitConverter.celsiusToFahrenheit(100))
    }

    @Test
    fun `celsiusToFahrenheit typical golf weather`() {
        // 20C = 68F
        assertEquals(68, UnitConverter.celsiusToFahrenheit(20))
    }

    @Test
    fun `fahrenheitToCelsius freezing point`() {
        assertEquals(0, UnitConverter.fahrenheitToCelsius(32))
    }

    @Test
    fun `fahrenheitToCelsius boiling point`() {
        assertEquals(100, UnitConverter.fahrenheitToCelsius(212))
    }

    @Test
    fun `temperatureToDisplay metric returns celsius`() {
        assertEquals(25, UnitConverter.temperatureToDisplay(25, imperial = false))
    }

    @Test
    fun `temperatureToDisplay imperial converts to fahrenheit`() {
        assertEquals(77, UnitConverter.temperatureToDisplay(25, imperial = true))
    }

    @Test
    fun `displayToCelsius metric returns same value`() {
        assertEquals(25, UnitConverter.displayToCelsius(25, imperial = false))
    }

    @Test
    fun `displayToCelsius imperial converts from fahrenheit`() {
        assertEquals(25, UnitConverter.displayToCelsius(77, imperial = true))
    }

    @Test
    fun `distanceUnit returns correct strings`() {
        assertEquals("m", UnitConverter.distanceUnit(imperial = false))
        assertEquals("yd", UnitConverter.distanceUnit(imperial = true))
    }

    @Test
    fun `temperatureUnit returns correct strings`() {
        assertEquals("°C", UnitConverter.temperatureUnit(imperial = false))
        assertEquals("°F", UnitConverter.temperatureUnit(imperial = true))
    }

    @Test
    fun `zero metres converts to zero yards`() {
        assertEquals(0.0, UnitConverter.metresToDisplay(0.0, imperial = true), 0.001)
    }

    @Test
    fun `negative distance converts correctly`() {
        val result = UnitConverter.metresToDisplay(-50.0, imperial = true)
        assertTrue(result < 0)
        assertEquals(-50.0, UnitConverter.displayToMetres(result, imperial = true), 0.001)
    }

    @Test
    fun `large distance converts correctly`() {
        val result = UnitConverter.metresToDisplay(100000.0, imperial = true)
        assertEquals(109361.0, result, 1.0)
    }

    @Test
    fun `zero celsius converts correctly`() {
        assertEquals(32, UnitConverter.celsiusToFahrenheit(0))
        assertEquals(0, UnitConverter.fahrenheitToCelsius(32))
    }

    @Test
    fun `negative celsius converts correctly`() {
        // -40 is the same in both scales
        assertEquals(-40, UnitConverter.celsiusToFahrenheit(-40))
    }
}
