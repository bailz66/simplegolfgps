package com.simplegolfgps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Insert
    suspend fun insert(round: Round): Long

    @Update
    suspend fun update(round: Round)

    @Delete
    suspend fun delete(round: Round)

    @Query("SELECT * FROM rounds ORDER BY dateCreated DESC")
    fun getAll(): Flow<List<Round>>

    @Query("SELECT * FROM rounds WHERE id = :id")
    suspend fun getById(id: Long): Round?

    @Query("SELECT * FROM rounds WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Round?>
}
