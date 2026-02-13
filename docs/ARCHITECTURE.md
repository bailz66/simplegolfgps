# SimpleGolfGPS - Architecture

## Data Flow

```
Room DB (SQLite)
  ↓ Flow<List<T>>
DAOs (ShotDao, RoundDao)
  ↓ Flow
ViewModels (combine, map, stateIn → StateFlow)
  ↓ collectAsState()
Composables (Jetpack Compose)
```

All state flows downward. Mutations go through ViewModel functions which call DAO suspend functions.

## ViewModels

### RoundsViewModel
- Exposes `rounds: StateFlow<List<RoundWithShotCount>>` from `roundDao.getAllWithShotCount()`
- CRUD operations: `createRound()`, `updateRound()`, `deleteRound()`

### ShotViewModel (GPS State Machine)
- **Form state**: `ShotFormState` — 20 fields covering all shot metadata
- **Measurement state**: `MeasurementState` — GPS tracking state machine
- **GPS flow**: `startMeasurement()` → 3s positioning → `lockStart` → live tracking → `lockCarryDistance()` (optional) → `finishMeasurement()`
- **Haversine**: Pure function calculating great-circle distance between two GPS coordinates
- **Shot lifecycle**: `saveShot()` creates/updates via `getByRoundHoleShot()` upsert pattern, then advances shot number

### SettingsViewModel
- **SettingsState** data class: 15 fields (1 unit pref, 2 club lists, 12 feedback toggles)
- Built via 3-stage `combine` chain from DataStore flows (Kotlin `combine` supports max 5 flows)
- Annotated `@Stable` for Compose compiler optimization

### AnalyticsViewModel
- **dashboardState**: `combine(allShots, allRounds)` → `AnalyticsComputer.computeDashboard()`
- **shotAnalysisState**: `combine(allShots, allRounds, filterState)` → filter → compute
- **courseNames**: `allRounds.map { distinct course names }`
- All use `SharingStarted.WhileSubscribed(5000)` — upstream flows only run while collected

## Unit Conversion Boundary

```
Storage: metres, Celsius (always)
    ↓ UnitConverter.metresToDisplay(metres, useImperial)
Display: yards/metres depending on useImperial setting
    ↑ UnitConverter.displayToMetres(display, useImperial)
User Input: converted back to metres before storing
```

`UnitConverter` provides:
- `metresToDisplay(metres, useImperial)` → Double (yards or metres)
- `displayToMetres(display, useImperial)` → Double (always metres)
- `distanceUnit(useImperial)` → "yd" or "m"
- Temperature: `celsiusToDisplay(c, useImperial)` / `displayToCelsius(d, useImperial)`

## GPS Measurement State Machine

```
Idle → [Start] → Positioning (3s, best accuracy GPS fix)
  → StartLocked → Live Tracking (distance + elevation updating)
    → [Carry] → carryDistance snapshotted, carryLocked=true, GPS continues
    → [Total/Finish] → totalDistance captured, GPS stops
    → [Cancel] → discard, return to Idle
```

Key fields in `MeasurementState`:
- `isMeasuring`, `startLocation`, `startLocked`, `carryLocked`
- `currentAccuracy`, `liveDistance`, `liveElevation`, `measuredDistance`

## Settings Pipeline

```
DataStore (15 keys)
  ↓ 15 individual Flow<T>
combine #1: imperial, clubs, order, wind, lie → partial SettingsState
  ↓
combine #2: + lieDir, shotType, strike, ballFlight, clubDir → partial
  ↓
combine #3: + ballDir, dirTarget, distTarget, mental, carry → complete SettingsState
  ↓ stateIn(WhileSubscribed)
settingsViewModel.state: StateFlow<SettingsState>
  ↓ collectAsState() in AppNavigation (top-level)
Passed as parameter to all screens
```

## Analytics Pipeline

```
shotDao.getShotsForAnalytics()  →  Flow<List<Shot>>  (ignoreForAnalytics=false)
roundDao.getAll()               →  Flow<List<Round>>
                ↓ combine
AnalyticsComputer.computeDashboard(shots, rounds) → DashboardState
AnalyticsComputer.filterShots(shots, rounds, filters) → filtered
AnalyticsComputer.computeShotAnalysis(all, filtered, filters) → ShotAnalysisState
```

### AnalyticsComputer (pure functions)
- **filterShots**: 13 conditions applied sequentially via `?.let` null-checks
- **computeDashboard**: Club usage, performance rates (strike/direction/distance), trends (10+ rounds), weaknesses
- **computeShotAnalysis**: Distance stats (avg/median/min/max/stddev/histogram), elevation stats, 12 enum distributions
- **computeEnumDistribution<T>**: Generic — groups non-null values, counts, calculates percentages

### Analytics UI Structure
```
AnalyticsScreen (Scaffold + SegmentedButton tabs)
  ├── DashboardTab (LazyColumn)
  │     ├── StatCards (shots, rounds, courses)
  │     ├── PerformanceScoresCard (3 circular indicators)
  │     ├── TrendsCard (last 5 vs prior 5 rounds)
  │     ├── ClubDistanceChartCard (whisker plot per club)
  │     ├── ClubUsageCard (donut chart)
  │     └── WeaknessesCard (severity bars)
  └── ShotAnalysisTab (LazyColumn)
        ├── DateRangeSection (LazyRow quick-select chips)
        ├── PrimaryFiltersRow (LazyRow: Club, ShotType, Lie)
        ├── MoreFiltersSection (2 LazyRows: 8 additional filters)
        ├── ResultsHeader (FlowRow of active filter chips)
        ├── DistanceAnalysisCard (stats + histogram)
        ├── ElevationAnalysisCard (elevation metrics)
        ├── DistributionCards (Strike, BallFlight, Direction, etc.)
        ├── Conditions section (dynamic enum distributions)
        └── PerClubStatsTable (table)
```

## Files Using Lazy Layouts
| File | Component | Type |
|------|-----------|------|
| RoundsListScreen.kt | Round cards list | LazyColumn |
| ShotAnalysisTab.kt | Date chips, filter rows | LazyRow (x4) |
| FeedbackComponents.kt | ChipSelector (wrap=false) | LazyRow |
| SettingsScreen.kt | Settings list | LazyColumn |
| CreateRoundScreen.kt | Weather type selector | LazyRow |
| DashboardTab.kt | Dashboard content | LazyColumn |

## Navigation
7 routes defined in `Navigation.kt` as sealed class `Screen`. NavHost in `MainActivity.kt` with argument passing via `NavType.LongType` for entity IDs.

```
RoundsList (start)
  ├── → CreateRound → (pop back, navigate to ShotRecording)
  ├── → ShotRecording/{roundId}
  │       └── → EditShot/{shotId}
  │       └── → EditRound/{roundId}
  ├── → Settings
  └── → Analytics
```
