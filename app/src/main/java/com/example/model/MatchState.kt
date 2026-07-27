package com.example.model

enum class MatchState {
    LIVE, UPCOMING, COMPLETED;

    companion object {
        fun from(rawState: String, status: String, score1: String, score2: String): MatchState {
            val lowerState = rawState.lowercase()
            if (lowerState == "in progress" || lowerState == "live") {
                return LIVE
            } else if (lowerState == "complete" || lowerState == "completed") {
                return COMPLETED
            } else if (lowerState == "preview" || lowerState == "upcoming") {
                return UPCOMING
            }
            
            val lowerStatus = status.lowercase()
            if (score1.isEmpty() && score2.isEmpty()) {
                return UPCOMING
            } else if (lowerStatus.contains("won by") || lowerStatus.contains("abandoned") || lowerStatus.contains("drawn") || 
                       lowerStatus.contains("tied") || lowerStatus.contains("no result") || lowerStatus.contains("complete")) {
                return COMPLETED
            } else {
                return LIVE
            }
        }
    }
}
