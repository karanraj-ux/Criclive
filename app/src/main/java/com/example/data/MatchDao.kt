package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatchesFlow(): kotlinx.coroutines.flow.Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    suspend fun getAllMatches(): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)
    
    @Query("DELETE FROM matches WHERE timestamp < :olderThan")
    suspend fun deleteOldMatches(olderThan: Long)
}
