# SimpleGolfGPS — Complete Project Reference

## Overview
Android Kotlin app for tracking golf shot distances and detailed feedback. Jetpack Compose + Material 3 UI, Room DB persistence, GPS distance measurement via FusedLocationProviderClient.

**Package:** `com.simplegolfgps` | **Min SDK:** 26 | **Target SDK:** 34
**Build:** Gradle 8.5, AGP 8.2.2, Kotlin 1.9.22, KSP 1.9.22-1.0.17
**Compose BOM:** 2024.02.02 | **Compose Compiler:** 1.5.8 | **Room:** 2.6.1
**~3,930 lines main code | ~800 lines tests | 25 source files | 7 test files**

---

## File Map

| File | Lines | Purpose |
|------|-------|---------|
| **Data Layer** (`data/`) | | |
| `Enums.kt` | 118 | 14 enum classes for golf feedback (weather, wind, lie, strike, fairwayHit, GIR, etc.) |
| `Round.kt` | 15 | Room entity: id, courseName, weatherType, temperature(°C), windCondition, startingHole, dateCreated |
| `Shot.kt` | 50 | Room entity: 15 nullable enum/data fields + distance(m), elevation(m), clubUsed, holeNumber, shotNumber, powerPct, ignoreForAnalytics, customNote |
| `RoundDao.kt` | 25 | CRUD + getAll() Flow, getById/getByIdFlow |
| `ShotDao.kt` | 37 | CRUD + getShotsByRoundId, getShotsForAnalytics (excludes ignored), getShotCountByHole, getByRoundHoleShot |
| `Converters.kt` | 50 | Room TypeConverters: 15 enum↔String pairs |
| `AppDatabase.kt` | 62 | Room DB v8, singleton, MIGRATION_3_4 through MIGRATION_7_8 |
| **Settings** (`settings/`) | | |
| `SettingsDataStore.kt` | 96 | DataStore prefs: useImperial, enabledClubs, clubOrder, 14 feedback toggles |
| `SettingsViewModel.kt` | 108 | Exposes SettingsState StateFlow, toggle/reorder methods |
| `UnitConverter.kt` | 33 | Static: metres↔yards, °C↔°F, unit labels. Internal storage always metres/°C |
| `SettingsScreen.kt` | 199 | Units toggle, feedback toggles, club config with reorder |
| **Analytics** (`analytics/`) | | |
| `AnalyticsViewModel.kt` | 72 | FilterState (club/lie/weather/wind/mental), AnalyticsState, ClubStats |
| `AnalyticsComputer.kt` | 68 | Pure computation: filters shots, groups by club, computes avg/min/max, enum distributions |
| `AnalyticsScreen.kt` | 609 | Filters, summary cards, bar chart, stats table, miss analysis, strike analysis |
| **UI** (`ui/`) | | |
| `Navigation.kt` | 17 | Sealed class Screen: RoundsList, CreateRound, EditRound/{id}, ShotRecording/{id}, EditShot/{id}, Settings, Analytics |
| `MainActivity.kt` | 209 | NavHost with 7 routes, ViewModel instantiation, unit conversion at boundaries |
| `RoundsViewModel.kt` | 61 | RoundWithShotCount list, createRound, updateRound, deleteRound |
| `RoundsListScreen.kt` | 194 | Round cards with weather emoji, date, shot count, long-press delete |
| `CreateRoundScreen.kt` | 245 | Form: course name, weather chips, temperature, wind, starting hole. Also contains EditRoundScreen |
| `ShotViewModel.kt` | 367 | GPS measurement state machine, ShotFormState, save/load/edit shots, Haversine distance |
| `ShotRecordingScreen.kt` | 809 | Main UI: GPS measurement, distance/elevation input, club selector, all feedback chips, shot history table |
| `EditShotScreen.kt` | 307 | Manual shot editing (no GPS), same feedback fields |
| `FeedbackComponents.kt` | 145 | ChipSelector<T> (wrap/scroll), WindDirectionGrid (3×3 compass), clubAbbreviation() |
| `Theme.kt` | 80 | Material 3: GolfGreen (#2E7D32) primary, dark/light schemes |
| `SimpleGolfGpsApp.kt` | 5 | Empty Application subclass |

---

## Database Schema

### Round (v4)
| Column | Type | Notes |
|--------|------|-------|
| id | Long PK | autoGenerate |
| courseName | String | required |
| weatherType | WeatherType enum | stored as String |
| temperature | Int? | Celsius |
| windCondition | String? | free text |
| startingHole | Int | default 1 |
| dateCreated | Long | epoch millis |

### Shot (v8)
| Column | Type | Notes |
|--------|------|-------|
| id | Long PK | autoGenerate |
| roundId | Long FK | → Round.id |
| holeNumber | Int | |
| shotNumber | Int | default 1, multiple shots per hole |
| clubUsed | String? | |
| distance | Double? | metres |
| carryDistance | Double? | metres (v5) |
| carryElevationChange | Double? | metres (v6) |
| elevationChange | Double? | metres |
| windDirection | WindDirection? | enum |
| windStrength | WindStrength? | enum |
| lie | Lie? | enum |
| shotType | ShotType? | enum |
| strike | Strike? | enum |
| clubDirection | ClubDirection? | enum |
| ballDirection | BallDirection? | enum |
| lieDirection | LieDirection? | enum |
| mentalState | MentalState? | enum |
| mentalStateNote | String? | |
| ballFlight | BallFlight? | enum |
| directionToTarget | DirectionToTarget? | enum |
| distanceToTarget | DistanceToTarget? | enum |
| customNote | String? | |
| ignoreForAnalytics | Boolean | default false |
| fairwayHit | FairwayHit? | enum (v7) |
| greenInRegulation | GreenInRegulation? | enum (v7) |
| targetDistance | Double? | metres (v7) |
| powerPct | Int? | null = 100% (v8) |
| timestamp | Long | epoch millis |

---

## Enums (Enums.kt)

| Enum | Values |
|------|--------|
| WeatherType | Sunny, Cloudy, Overcast, LightRain, HeavyRain |
| WindDirection | N, NE, E, SE, S, SW, W, NW (each has arrow symbol) |
| WindStrength | VeryStrong, Strong, Moderate, Calm, None |
| Lie | Tee, Fairway, Fringe, Green, LightRough("Rough"), HeavyRough, Bunker |
| ShotType | Full, Pitch, Punch, Flop, BumpAndRun, Chip, BunkerChip, Putt |
| Strike | Pure, Fat, Thin, Shank, Toe |
| ClubDirection | Pull, Straight, Push |
| BallDirection | Slice, Fade, Straight, Draw, Hook |
| FairwayHit | Yes, No |
| GreenInRegulation | Yes, No |
| LieDirection | Flat, Uphill, Downhill, AboveFeet, BelowFeet |
| MentalState | Calm, Rushed, Frustrating, Overthinking |
| BallFlight | Sky, High, Medium, Low, WormBurner |
| DirectionToTarget | FarLeft, Left, Straight, Right, FarRight |
| DistanceToTarget | WayLong, Long, OnPin, Short, WayShort |

---

## Settings (DataStore Preferences)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| USE_IMPERIAL | Boolean | false | yards/°F vs metres/°C |
| ENABLED_CLUBS | String | 17 clubs pipe-separated | Which clubs appear in selector |
| CLUB_ORDER | String | 17 clubs pipe-separated | Display order of clubs |
| SHOW_WIND | Boolean | false | Wind direction + strength fields |
| SHOW_LIE | Boolean | true | Lie field |
| SHOW_STRIKE | Boolean | false | Strike quality field |
| SHOW_BALL_FLIGHT | Boolean | false | Ball flight field |
| SHOW_CLUB_DIRECTION | Boolean | true | Club face direction |
| SHOW_BALL_DIRECTION | Boolean | true | Ball curve direction |
| SHOW_DIRECTION_TO_TARGET | Boolean | true | Left/right of target |
| SHOW_DISTANCE_TO_TARGET | Boolean | true | Long/short of target |
| SHOW_LIE_DIRECTION | Boolean | false | Uphill/downhill lie |
| SHOW_SHOT_TYPE | Boolean | true | Full/pitch/chip/putt etc |
| SHOW_MENTAL_STATE | Boolean | false | Mental state + note |
| SHOW_CARRY_DISTANCE | Boolean | false | Separate carry distance field |
| SHOW_FAIRWAY_HIT | Boolean | false | Fairway hit Yes/No |
| SHOW_GREEN_IN_REGULATION | Boolean | false | Green in regulation Yes/No |
| SHOW_TARGET_DISTANCE | Boolean | false | Target distance field |

**Default clubs:** Driver, 3-Wood, 5-Wood, 3-Hybrid, 4-Hybrid, 3-Iron through 9-Iron, PW, GW, SW, LW, Putter

---

## GPS Measurement State Machine (ShotViewModel)

```
Idle → [startMeasurement()] → Requesting location
  → First GPS fix → startLocation captured, accuracy tracked
  → 3-second delay → startLocked = true
  → Subsequent fixes → liveDistance = Haversine(start, current), liveElevation = altitude delta
  → [finishMeasurement()] → measuredDistance captured → distance copied to form
  → [cancelMeasurement()] → reset to Idle
```

- High-accuracy requests: 1000ms interval, 500ms fastest
- Haversine formula in companion object (Earth radius 6,371,000m)
- Accuracy displayed as chip: green <3m, orange ≥3m

---

## Navigation Routes

| Route | Args | Screen |
|-------|------|--------|
| `rounds_list` | — | RoundsListScreen (start destination) |
| `create_round` | — | CreateRoundScreen |
| `edit_round/{roundId}` | Long | EditRoundScreen |
| `shot_recording/{roundId}` | Long | ShotRecordingScreen |
| `edit_shot/{shotId}` | Long | EditShotScreen |
| `settings` | — | SettingsScreen |
| `analytics` | — | AnalyticsScreen |

---

## Unit Conversion Strategy

**Rule:** Internal storage always in metres and Celsius. Conversion only at display layer.

| Direction | Distance | Temperature |
|-----------|----------|-------------|
| Input → Storage | yards → metres (÷1.09361) | °F → °C |
| Storage → Display | metres → yards (×1.09361) | °C → °F |
| Unit label | "yd" / "m" | "°F" / "°C" |

Conversion happens in:
- `MainActivity.kt` — temperature on CreateRound/EditRound save
- `ShotRecordingScreen.kt` — distance display/input
- `RoundsListScreen.kt` — temperature display on cards
- `AnalyticsScreen.kt` — distance stats display

---

## Analytics Pipeline

```
shotDao.getShotsForAnalytics() ─┐
roundDao.getAll() ──────────────┼─→ AnalyticsComputer.computeAnalytics() → AnalyticsState
_filterState ───────────────────┘
```

**FilterState:** club, lie, weatherType, windStrength, mentalState (all nullable)

**AnalyticsState contains:**
- totalShots, totalRounds, mostUsedClub
- clubStats: List<ClubStats> (club, count, avg/min/max distance)
- Per-club enum distributions: ballDirection, directionToTarget, distanceToTarget, clubDirection, strike, ballFlight

**AnalyticsScreen sections:** Filters → Summary Cards (4) → Bar Chart → Stats Table → Miss Analysis → Strike Analysis

---

## Key Behaviors

1. **Putter auto-sets:** Selecting Putter club auto-sets Lie=Green, ShotType=Putt
2. **Defaults on new shot:** ShotType=Full, PowerPct=100
3. **Shot advancement:** After saving, auto-increments to next shotNumber for same hole
3. **Hole/shot switching:** Changing hole resets shotNumber to count+1; changing shotNumber loads existing shot if one exists
4. **Delete cascade:** Deleting a round deletes all its shots
5. **Ignore for analytics:** Per-shot flag, filtered at DAO level (`getShotsForAnalytics`)
6. **Weather emojis:** Sunny→☀️, Cloudy→⛅, Overcast→☁️, LightRain→🌦️, HeavyRain→🌧️
7. **Club abbreviations:** Driver→D, 3-Wood→3w, 4-Hybrid→4h, 5-Iron→5i, PW/GW/SW/LW, Putter→P

---

## Test Coverage

| Test File | Tests | What It Covers |
|-----------|-------|----------------|
| HaversineTest | 7 | Distance calculation: same point, known distances, symmetry |
| UnitConverterTest | 15 | metres↔yards, °C↔°F, edge cases, round-trips |
| AnalyticsComputationTest | 16 | Filtering, aggregation, grouping, null handling |
| ConvertersTest | — | Room type converter round-trips |
| EnumsTest | — | Enum validation |
| ShotEntityTest | — | Room entity construction |
| ShotFormStateTest | — | Form state validation |

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2024.02.02 | UI framework |
| Material 3 | (BOM) | Design system |
| Material Icons Extended | (BOM) | Icon set |
| Navigation Compose | 2.7.6 | Screen navigation |
| Room | 2.6.1 | SQLite ORM |
| DataStore Preferences | 1.0.0 | Settings persistence |
| Play Services Location | 21.1.0 | GPS/FusedLocation |
| Coroutines Android | 1.7.3 | Async operations |
| Coroutines Play Services | 1.7.3 | Location coroutine support |

---

## Permissions
- `ACCESS_FINE_LOCATION` — GPS shot measurement
- `ACCESS_COARSE_LOCATION` — Fallback location
