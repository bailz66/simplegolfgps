package com.simplegolfgps.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds")
data class Round(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseName: String,
    val weatherType: WeatherType,
    val temperature: Int? = null,
    val windCondition: String? = null,
    val startingHole: Int = 1,
    val dateCreated: Long = System.currentTimeMillis()
)
