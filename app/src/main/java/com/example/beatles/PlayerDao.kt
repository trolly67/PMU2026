package com.example.beatles.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: PlayerEntity): Long

    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): PlayerEntity?

    @Query("SELECT * FROM players ORDER BY name ASC")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Long): PlayerEntity?

    @Query("UPDATE players SET difficulty = :difficulty WHERE id = :playerId")
    suspend fun updateDifficulty(playerId: Long, difficulty: Int)
}