package com.example.beatles.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: PlayerEntity): Long

    @Query("SELECT * FROM players WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): PlayerEntity?

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Long): PlayerEntity?
}