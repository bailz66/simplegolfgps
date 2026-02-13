# SimpleGolfGPS

A simple Android app for tracking golf shot distances and recording detailed feedback. Not a course GPS — it's a shot distance tracker with analytics.

## Features

- **Round Management** — Create rounds with course name, weather, temperature, wind conditions
- **GPS Shot Measurement** — Measure shot distances using GPS (tap to start, walk, tap to finish)
- **Manual Distance Entry** — Override or manually enter distances
- **Detailed Shot Feedback** — Record club, wind, lie, strike, ball flight, direction, mental state, and more
- **Configurable Feedback** — Toggle which feedback categories appear on the shot form
- **Club Configuration** — Enable/disable clubs and reorder them
- **Analytics Dashboard** — Per-club stats, miss analysis, strike analysis with filters
- **Metric/Imperial Units** — Toggle between metres/yards, °C/°F

## Tech Stack

- **Platform:** Android (Min SDK 26 / Android 8.0)
- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3
- **Database:** Room (local SQLite)
- **GPS:** Google Play Location Services
- **Navigation:** Jetpack Navigation Compose
- **Settings:** DataStore Preferences

## Building

Open the project in Android Studio and run on a device or emulator.

## License

MIT
