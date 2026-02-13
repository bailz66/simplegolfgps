# SimpleGolfGPS - Project Overview

## Purpose
Android Kotlin golf shot tracking app. Record shots with GPS distance measurement, 13 enum-based feedback fields, carry/total distance tracking, and analytics dashboard.

## Tech Stack
| Component | Version |
|-----------|---------|
| Kotlin | 1.9.22 |
| Android Gradle Plugin | 8.2.2 |
| Compose BOM | 2024.06.00 |
| Compose Compiler | 1.5.8 |
| Room | 2.6.1 |
| Navigation Compose | 2.7.6 |
| DataStore Preferences | 1.0.0 |
| Play Services Location | 21.1.0 |
| Coroutines | 1.7.3 |
| Min SDK | 26 |
| Target SDK | 34 |
| JDK | 17 |

## Package Structure

```
com.simplegolfgps/
  MainActivity.kt              (218 lines) — NavHost, ViewModel wiring
  SimpleGolfGpsApp.kt           (5 lines)  — Application subclass

  data/
    Shot.kt                     (46 lines)  — Room entity: 24 columns, 13 enum feedback fields
    Round.kt                    (15 lines)  — Room entity: course, weather, temp, wind, hole
    AppDatabase.kt              (53 lines)  — Room DB v6, migrations 3→4, 4→5, 5→6
    ShotDao.kt                  (37 lines)  — Queries (Flow + suspend)
    RoundDao.kt                 (25 lines)  — CRUD + queries
    Enums.kt                   (110 lines)  — 13 enum types with displayName
    Converters.kt               (44 lines)  — Room TypeConverters: enum ↔ String

  ui/
    navigation/Navigation.kt    (17 lines)  — 7 routes via sealed class Screen
    theme/Theme.kt              (80 lines)  — Material 3 light/dark, golf green primary
    rounds/
      RoundsListScreen.kt      (194 lines)  — LazyColumn of round cards
      CreateRoundScreen.kt     (245 lines)  — Create + EditRoundScreen
      RoundsViewModel.kt        (61 lines)  — Round CRUD
    shots/
      ShotRecordingScreen.kt   (905 lines)  — Main GPS shot UI, feedback, history
      EditShotScreen.kt        (341 lines)  — Manual shot editing
      ShotViewModel.kt         (391 lines)  — GPS state machine, form state, save/load
    components/
      FeedbackComponents.kt    (145 lines)  — ChipSelector, WindDirectionGrid, ElevationControl

  analytics/
    AnalyticsScreen.kt          (77 lines)  — Tab container (Dashboard / Shot Analysis)
    AnalyticsViewModel.kt       (51 lines)  — State management, combine chains
    AnalyticsComputer.kt       (454 lines)  — Pure computation: filter, group, stats
    AnalyticsModels.kt         (138 lines)  — Data classes for analytics state
    AnalyticsComponents.kt     (477 lines)  — Charts, histograms, distribution cards
    DashboardTab.kt            (440 lines)  — Unfiltered overview
    ShotAnalysisTab.kt         (654 lines)  — Filtered analysis with interactive filters

  settings/
    SettingsScreen.kt          (216 lines)  — Settings UI
    SettingsViewModel.kt       (112 lines)  — SettingsState + mutations
    SettingsDataStore.kt        (89 lines)  — 15 DataStore preferences
    UnitConverter.kt            (33 lines)  — metres↔yards, degC↔degF

test/
  AnalyticsComputationTest.kt  — Analytics business logic tests
  ConvertersTest.kt            — Room type converter tests
  EnumsTest.kt                 — Enum displayName tests
  ShotEntityTest.kt            — Shot entity field tests
  UnitConverterTest.kt         — Unit conversion tests
  HaversineTest.kt             — GPS distance formula tests
  ShotFormStateTest.kt         — Form state behavior tests
```

## Database Schema (Room v6)

### Table: `rounds`
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | autoGenerate |
| courseName | TEXT | not null |
| weatherType | TEXT | enum via converter |
| temperature | INTEGER | nullable, degrees C |
| windCondition | TEXT | nullable |
| startingHole | INTEGER | default 1 |
| dateCreated | INTEGER | timestamp millis |

### Table: `shots`
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER PK | autoGenerate |
| roundId | LONG | FK → rounds.id (CASCADE) |
| holeNumber | INTEGER | 1-18 |
| shotNumber | INTEGER | default 1, added v3→v4 |
| clubUsed | TEXT | nullable |
| distance | REAL | nullable, total metres |
| carryDistance | REAL | nullable, carry metres, added v4→v5 |
| carryElevationChange | REAL | nullable, metres, added v5→v6 |
| elevationChange | REAL | nullable, total metres |
| windDirection | TEXT | enum: N, NE, E, SE, S, SW, W, NW |
| windStrength | TEXT | enum: VeryStrong, Strong, Moderate, Calm, None |
| lie | TEXT | enum: Tee, Fairway, Fringe, Green, LightRough, HeavyRough, Bunker |
| shotType | TEXT | enum: Full, Pitch, Punch, Flop, BumpAndRun, Chip, BunkerChip, Putt |
| strike | TEXT | enum: Pure, Fat, Thin, Shank, Toe |
| clubDirection | TEXT | enum: Straight, Pull, Push |
| ballDirection | TEXT | enum: Straight, Fade, Slice, Draw, Hook |
| lieDirection | TEXT | enum: Flat, Uphill, Downhill, AboveFeet, BelowFeet |
| mentalState | TEXT | enum: Calm, Rushed, Frustrating, Overthinking |
| mentalStateNote | TEXT | nullable |
| ballFlight | TEXT | enum: Sky, High, Medium, Low, WormBurner |
| directionToTarget | TEXT | enum: FarLeft, Left, Straight, Right, FarRight |
| distanceToTarget | TEXT | enum: WayLong, Long, OnPin, Short, WayShort |
| customNote | TEXT | nullable |
| ignoreForAnalytics | INTEGER | boolean, default false |
| timestamp | INTEGER | millis |

### Migrations
- **v3 → v4**: `ALTER TABLE shots ADD COLUMN shotNumber INTEGER NOT NULL DEFAULT 1`
- **v4 → v5**: `ALTER TABLE shots ADD COLUMN carryDistance REAL`
- **v5 → v6**: `ALTER TABLE shots ADD COLUMN carryElevationChange REAL`

## Navigation Routes (7)
| Route Pattern | Screen | Purpose |
|---------------|--------|---------|
| `rounds_list` | RoundsListScreen | Start destination, list all rounds |
| `create_round` | CreateRoundScreen | Create new round |
| `edit_round/{roundId}` | EditRoundScreen | Edit existing round |
| `shot_recording/{roundId}` | ShotRecordingScreen | GPS shot recording |
| `edit_shot/{shotId}` | EditShotScreen | Edit individual shot |
| `settings` | SettingsScreen | App settings |
| `analytics` | AnalyticsScreen | Analytics dashboard |

## Settings (15 DataStore Preferences)
| Key | Type | Default | Purpose |
|-----|------|---------|---------|
| use_imperial | Boolean | false | Yards/feet vs metres |
| enabled_clubs | String (JSON) | 14 clubs | Which clubs appear in selector |
| club_order | String (JSON) | 14 clubs | Club display order |
| show_wind | Boolean | false | Wind direction/strength fields |
| show_lie | Boolean | true | Lie field |
| show_lie_direction | Boolean | false | Lie direction field |
| show_shot_type | Boolean | true | Shot type field |
| show_strike | Boolean | false | Strike quality field |
| show_ball_flight | Boolean | false | Ball flight field |
| show_club_direction | Boolean | true | Club direction field |
| show_ball_direction | Boolean | true | Ball direction field |
| show_direction_to_target | Boolean | true | Direction to target field |
| show_distance_to_target | Boolean | true | Distance to target field |
| show_mental_state | Boolean | false | Mental state field |
| show_carry_distance | Boolean | false | Carry distance measurement |

## Enum Types (13)
All enums defined in `data/Enums.kt` with `displayName` property.

| Enum | Values |
|------|--------|
| WeatherType | Sunny, Cloudy, Overcast, LightRain, HeavyRain |
| WindDirection | N, NE, E, SE, S, SW, W, NW |
| WindStrength | VeryStrong, Strong, Moderate, Calm, None |
| Lie | Tee, Fairway, Fringe, Green, LightRough, HeavyRough, Bunker |
| ShotType | Full, Pitch, Punch, Flop, BumpAndRun, Chip, BunkerChip, Putt |
| Strike | Pure, Fat, Thin, Shank, Toe |
| ClubDirection | Straight, Pull, Push |
| BallDirection | Straight, Fade, Slice, Draw, Hook |
| LieDirection | Flat, Uphill, Downhill, AboveFeet, BelowFeet |
| MentalState | Calm, Rushed, Frustrating, Overthinking |
| BallFlight | Sky, High, Medium, Low, WormBurner |
| DirectionToTarget | FarLeft, Left, Straight, Right, FarRight |
| DistanceToTarget | WayLong, Long, OnPin, Short, WayShort |

## Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
