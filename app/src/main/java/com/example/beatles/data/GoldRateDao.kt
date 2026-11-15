package com.example.beatles.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldRateDao {
    @Query("SELECT * FROM gold_rate WHERE id = 1")
    fun observe(): Flow<GoldRateEntity?>

    @Query("SELECT * FROM gold_rate WHERE id = 1")
    suspend fun get(): GoldRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GoldRateEntity)
}