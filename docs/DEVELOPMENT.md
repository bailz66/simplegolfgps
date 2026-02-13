# SimpleGolfGPS - Development Guide

## Prerequisites
- **Android SDK**: `F:\studio`
- **JDK 17**: `C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot` (configured in `gradle.properties`)
- **ADB**: `F:\studio\platform-tools\adb.exe`
- **Emulator or device** connected via ADB

## Build Commands

```bash
# Debug build (APK only)
./gradlew assembleDebug

# Build + install on connected device
./gradlew installDebug

# Clean build + install (use after dependency/schema changes)
./gradlew clean installDebug
```

## Run / Stop / Restart

```bash
# Start app
F:/studio/platform-tools/adb.exe shell am start -n com.simplegolfgps/.MainActivity

# Stop app
F:/studio/platform-tools/adb.exe shell am force-stop com.simplegolfgps

# Restart (stop + start)
F:/studio/platform-tools/adb.exe shell am force-stop com.simplegolfgps && F:/studio/platform-tools/adb.exe shell am start -n com.simplegolfgps/.MainActivity
```

## Testing

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.simplegolfgps.data.ShotEntityTest"

# Run with console output
./gradlew testDebugUnitTest --info
```

### Test Files
| Test File | Tests |
|-----------|-------|
| `AnalyticsComputationTest.kt` | Analytics business logic |
| `ConvertersTest.kt` | Room enum type converters |
| `EnumsTest.kt` | Enum displayName values |
| `ShotEntityTest.kt` | Shot entity fields, copy, equality |
| `UnitConverterTest.kt` | metres↔yards, C↔F conversions |
| `HaversineTest.kt` | GPS distance formula |
| `ShotFormStateTest.kt` | Form state defaults, copy, reset |

## Debugging

### Crash Logs
```bash
# View runtime errors (crash stack traces)
F:/studio/platform-tools/adb.exe logcat -d -s AndroidRuntime:E

# Full crash trace with context
F:/studio/platform-tools/adb.exe logcat -d | grep -A 50 "FATAL EXCEPTION"

# All logs from our process (while app is running)
F:/studio/platform-tools/adb.exe logcat -d | grep "simplegolfgps"

# Clear logcat buffer (do before reproducing a crash)
F:/studio/platform-tools/adb.exe logcat -c

# Continuous log stream (Ctrl+C to stop)
F:/studio/platform-tools/adb.exe logcat -s "com.simplegolfgps:*" "AndroidRuntime:E"
```

### Clear App Data
```bash
# Wipe database + preferences (resets everything)
F:/studio/platform-tools/adb.exe shell pm clear com.simplegolfgps
```

## Database Migrations

Room database is at version 6. When adding new columns:

1. Add field to entity (`Shot.kt` or `Round.kt`) with nullable type and default value
2. Increment version in `@Database(version = N)` in `AppDatabase.kt`
3. Add migration object:
```kotlin
private val MIGRATION_N_N1 = object : Migration(N, N+1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shots ADD COLUMN newField REAL")
    }
}
```
4. Chain in builder: `.addMigrations(..., MIGRATION_N_N1)`

## Key Conventions

- **Units**: Internal storage always in metres and Celsius. Convert at display layer via `UnitConverter`.
- **Enums**: All feedback fields use nullable enum types. Room persists via `Converters.kt` (enum name ↔ String).
- **Flows**: DAOs return `Flow<List<T>>` for reactive queries, `suspend fun` for one-shot lookups.
- **State**: ViewModels expose `StateFlow`, UI collects via `collectAsState()`.
- **Settings**: DataStore preferences, combined into `SettingsState` data class via 3-stage `combine` chain.
