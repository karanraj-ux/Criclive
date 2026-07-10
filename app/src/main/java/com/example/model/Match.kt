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
    val currentBatter: String = "",
    val currentBowler: String = "",
    val liveCommentary: List<Commentary> = emptyList()
)

data class Commentary(
    val over: String,
    val text: String,
    val isWicket: Boolean = false,
    val isBoundary: Boolean = false
)
