package com.simplegolfgps.settings

object UnitConverter {
    private const val METRES_TO_YARDS = 1.09361

    fun metresToDisplay(metres: Double, imperial: Boolean): Double {
        return if (imperial) metres * METRES_TO_YARDS else metres
    }

    fun displayToMetres(display: Double, imperial: Boolean): Double {
        return if (imperial) display / METRES_TO_YARDS else display
    }

    fun celsiusToFahrenheit(celsius: Int): Int {
        return (celsius * 9.0 / 5.0 + 32).toInt()
    }

    fun fahrenheitToCelsius(fahrenheit: Int): Int {
        return ((fahrenheit - 32) * 5.0 / 9.0).toInt()
    }

    fun distanceUnit(imperial: Boolean): String = if (imperial) "yd" else "m"

    fun temperatureUnit(imperial: Boolean): String = if (imperial) "°F" else "°C"

    fun temperatureToDisplay(celsius: Int, imperial: Boolean): Int {
        return if (imperial) celsiusToFahrenheit(celsius) else celsius
    }

    fun displayToCelsius(display: Int, imperial: Boolean): Int {
        return if (imperial) fahrenheitToCelsius(display) else display
    }
}
