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
    val matchUrl: String = ""
) {
    val matchState: String
        get() {
            val lowerStatus = status.lowercase()
            if (lowerStatus.contains("stump") || lowerStatus.contains("day ") || lowerStatus.contains("tea") || 
                lowerStatus.contains("lunch") || lowerStatus.contains("innings break") || lowerStatus.contains("rain") || 
                lowerStatus.contains("wet outfield") || lowerStatus.contains("live") || lowerStatus.contains("opt to") || 
                lowerStatus.contains("trail by") || lowerStatus.contains("lead by") || status.contains("*") ||
                lowerStatus.contains("toss") || lowerStatus.contains("in progress")) {
                return "LIVE"
            } else if (lowerStatus.contains("won by") || lowerStatus.contains("abandoned") || lowerStatus.contains("drawn") || 
                       lowerStatus.contains("tied") || lowerStatus.contains("no result") || lowerStatus.contains("complete")) {
                return "COMPLETE"
            } else if (lowerStatus.contains("starts") || lowerStatus.contains("to begin") || lowerStatus.contains("delayed") || lowerStatus.contains("upcoming")) {
                return "UPCOMING"
            } else {
                if (score1.isNotEmpty() || score2.isNotEmpty()) {
                    return "COMPLETE"
                } else {
                    return "UPCOMING"
                }
            }
        }
}

data class Commentary(
    val over: String,
    val text: String,
    val isWicket: Boolean = false,
    val isBoundary: Boolean = false
)
