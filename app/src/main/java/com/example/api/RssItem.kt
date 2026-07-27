package com.example.api

data class RssItem(
    val title: String, 
    val link: String, 
    val rawLiveStats: String = "",
    val seriesName: String = "",
    val matchTiming: String = "",
    val rawState: String = "",
    val team1: String = "",
    val team2: String = "",
    val score1: String = "",
    val score2: String = "",
    val overs1: String = "",
    val overs2: String = "",
    val matchStatus: String = "",
    val source: String = "RSS"
)
