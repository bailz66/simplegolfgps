package com.simplegolfgps.data

enum class WeatherType(val displayName: String) {
    Sunny("Sunny"),
    Cloudy("Cloudy"),
    Overcast("Overcast"),
    LightRain("Light Rain"),
    HeavyRain("Heavy Rain")
}

enum class WindDirection(val displayName: String, val arrow: String) {
    N("N", "↑"),
    NE("NE", "↗"),
    E("E", "→"),
    SE("SE", "↘"),
    S("S", "↓"),
    SW("SW", "↙"),
    W("W", "←"),
    NW("NW", "↖")
}

enum class WindStrength(val displayName: String) {
    VeryStrong("V-Strong"),
    Strong("Strong"),
    Moderate("Moderate"),
    Calm("Calm"),
    None("None")
}

enum class Lie(val displayName: String) {
    Tee("Tee"),
    Fairway("Fairway"),
    Fringe("Fringe"),
    Green("Green"),
    LightRough("Light Rough"),
    HeavyRough("H-Rough"),
    Bunker("Bunker")
}

enum class ShotType(val displayName: String) {
    Full("Full"),
    Pitch("Pitch"),
    Punch("Punch"),
    Flop("Flop"),
    BumpAndRun("Bump & Run"),
    Chip("Chip"),
    BunkerChip("Bunker"),
    Putt("Putt")
}

enum class Strike(val displayName: String) {
    Pure("Pure"),
    Fat("Fat"),
    Thin("Thin"),
    Shank("Shank"),
    Toe("Toe")
}

enum class ClubDirection(val displayName: String) {
    Straight("Straight"),
    Pull("Pull"),
    Push("Push")
}

enum class BallDirection(val displayName: String) {
    Straight("Straight"),
    Fade("Fade"),
    Slice("Slice"),
    Draw("Draw"),
    Hook("Hook")
}

enum class LieDirection(val displayName: String) {
    Flat("Flat"),
    Uphill("Uphill"),
    Downhill("Downhill"),
    AboveFeet("Above"),
    BelowFeet("Below")
}

enum class MentalState(val displayName: String) {
    Calm("Calm"),
    Rushed("Rushed"),
    Frustrating("Frustrating"),
    Overthinking("Overthinking")
}

enum class BallFlight(val displayName: String) {
    Sky("Sky"),
    High("High"),
    Medium("Medium"),
    Low("Low"),
    WormBurner("Worm")
}

enum class DirectionToTarget(val displayName: String) {
    FarLeft("F-Left"),
    Left("Left"),
    Straight("Straight"),
    Right("Right"),
    FarRight("F-Right")
}

enum class DistanceToTarget(val displayName: String) {
    WayLong("V-Long"),
    Long("Long"),
    OnPin("Length"),
    Short("Short"),
    WayShort("V-Short")
}

enum class FairwayHit(val displayName: String) {
    Yes("Yes"), No("No")
}

enum class GreenInRegulation(val displayName: String) {
    Yes("Yes"), No("No")
}
