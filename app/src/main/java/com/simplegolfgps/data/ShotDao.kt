package com.simplegolfgps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotDao {
    @Insert
    suspend fun insert(shot: Shot): Long

    @Update
    suspend fun update(shot: Shot)

    @Delete
    suspend fun delete(shot: Shot)

    @Query("SELECT * FROM shots WHERE roundId = :roundId ORDER BY timestamp ASC")
    fun getShotsByRoundId(roundId: Long): Flow<List<Shot>>

    @Query("SELECT * FROM shots WHERE ignoreForAnalytics = 0")
    fun getShotsForAnalytics(): Flow<List<Shot>>

    @Query("SELECT * FROM shots WHERE id = :id")
    suspend fun getById(id: Long): Shot?

    @Query("SELECT COUNT(*) FROM shots WHERE roundId = :roundId")
    fun getShotCountByRoundId(roundId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM shots WHERE roundId = :roundId")
    suspend fun getShotCountByRoundIdOnce(roundId: Long): Int

    @Query("SELECT * FROM shots WHERE roundId = :roundId AND holeNumber = :holeNumber AND shotNumber = :shotNumber LIMIT 1")
    suspend fun getByRoundHoleShot(roundId: Long, holeNumber: Int, shotNumber: Int): Shot?

    @Query("SELECT COUNT(*) FROM shots WHERE roundId = :roundId AND holeNumber = :holeNumber")
    suspend fun getShotCountByHole(roundId: Long, holeNumber: Int): Int
}
