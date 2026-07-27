package com.example.model

data class Match(
    val id: String,
    val team1: String,
    val team2: String,
    val score1: String,
    val score2: String,
    val overs1: String,
    val overs2: String,
    val status: String,
    val seriesName: String = "",
    val matchTiming: String = "",
    val currentBatter: String = "",
    val currentBowler: String = "",
    val squad1: List<String> = emptyList(),
    val squad2: List<String> = emptyList(),
    val notablePerformances: String = "",
    val liveCommentary: List<Commentary> = emptyList(),
    val matchUrl: String = "",
    val rawState: String = ""
) {
    val matchState: String
        get() {
            return MatchState.from(rawState, status, score1, score2).name
        }
}


data class Commentary(
    val over: String,
    val text: String,
    val isWicket: Boolean = false,
    val isBoundary: Boolean = false
)
