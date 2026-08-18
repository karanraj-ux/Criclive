package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Match

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val team1: String,
    val team2: String,
    val score1: String,
    val score2: String,
    val overs1: String,
    val overs2: String,
    val status: String,
    val seriesName: String = "",
    val matchTiming: String = "",
    val matchUrl: String,
    val notablePerformances: String = "",
    val rawState: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMatch(): Match {
        return Match(
            id = id,
            team1 = team1,
            team2 = team2,
            score1 = score1,
            score2 = score2,
            overs1 = overs1,
            overs2 = overs2,
            status = status,
            seriesName = seriesName,
            matchTiming = matchTiming,
            matchUrl = matchUrl,
            notablePerformances = notablePerformances,
            rawState = rawState
        )
    }
}

fun Match.toEntity(): MatchEntity {
    return MatchEntity(
        id = id,
        team1 = team1,
        team2 = team2,
        score1 = score1,
        score2 = score2,
        overs1 = overs1,
        overs2 = overs2,
        status = status,
        seriesName = seriesName,
        matchTiming = matchTiming,
        matchUrl = matchUrl,
            notablePerformances = notablePerformances,
        rawState = rawState
    )
}
