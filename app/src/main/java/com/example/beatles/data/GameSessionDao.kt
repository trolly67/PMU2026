package com.example.beatles.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Insert
    suspend fun insert(session: GameSessionEntity): Long

    @Query(
        "SELECT p.name AS playerName, gs.score, gs.difficulty, " +
                "gs.hits, gs.misses " +
                "FROM game_sessions gs JOIN players p ON gs.playerId = p.id " +
                "JOIN (SELECT playerId, MAX(score) as maxScore FROM game_sessions GROUP BY playerId) best " +
                "ON gs.playerId = best.playerId AND gs.score = best.maxScore " +
                "GROUP BY p.name " +
                "ORDER BY gs.score DESC, gs.difficulty DESC, gs.finishedAtEpochMillis DESC"
    )
    fun observeAllRecords(): Flow<List<RecordRow>>

    @Query(
        "SELECT p.name AS playerName, gs.score, gs.difficulty, " +
                "gs.hits, gs.misses " +
                "FROM game_sessions gs JOIN players p ON gs.playerId = p.id " +
                "JOIN (SELECT playerId, MAX(score) as maxScore FROM game_sessions WHERE difficulty = :difficulty GROUP BY playerId) best " +
                "ON gs.playerId = best.playerId AND gs.score = best.maxScore " +
                "WHERE gs.difficulty = :difficulty " +
                "GROUP BY p.name " +
                "ORDER BY gs.score DESC, gs.finishedAtEpochMillis DESC"
    )
    fun observeRecordsByDifficulty(difficulty: Int): Flow<List<RecordRow>>
}