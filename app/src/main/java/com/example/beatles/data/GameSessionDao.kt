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
        "SELECT gs.id AS sessionId, p.name AS playerName, gs.score, gs.difficulty, " +
                "gs.hits, gs.misses, gs.finishedAtEpochMillis " +
                "FROM game_sessions gs JOIN players p ON gs.playerId = p.id " +
                "ORDER BY gs.difficulty DESC, gs.score DESC, gs.finishedAtEpochMillis DESC"
    )
    fun observeAllRecords(): Flow<List<RecordRow>>

    @Query(
        "SELECT gs.id AS sessionId, p.name AS playerName, gs.score, gs.difficulty, " +
                "gs.hits, gs.misses, gs.finishedAtEpochMillis " +
                "FROM game_sessions gs JOIN players p ON gs.playerId = p.id " +
                "WHERE gs.difficulty = :difficulty " +
                "ORDER BY gs.score DESC, gs.finishedAtEpochMillis DESC"
    )
    fun observeRecordsByDifficulty(difficulty: Int): Flow<List<RecordRow>>

    @Query(
        "SELECT gs.id AS sessionId, p.name AS playerName, gs.score, gs.difficulty, " +
                "gs.hits, gs.misses, gs.finishedAtEpochMillis " +
                "FROM game_sessions gs JOIN players p ON gs.playerId = p.id " +
                "ORDER BY gs.score DESC, gs.finishedAtEpochMillis DESC LIMIT 10"
    )
    fun observeTop10(): Flow<List<RecordRow>>

    @Query(
        "SELECT gs.id AS sessionId, p.name AS playerName, gs.score, gs.difficulty, " +
                "gs.hits, gs.misses, gs.finishedAtEpochMillis " +
                "FROM game_sessions gs JOIN players p ON gs.playerId = p.id " +
                "WHERE gs.playerId = :playerId " +
                "ORDER BY gs.score DESC, gs.finishedAtEpochMillis DESC"
    )
    fun observeByPlayer(playerId: Long): Flow<List<RecordRow>>
}