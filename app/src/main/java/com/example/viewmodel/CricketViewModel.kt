package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.RssParser
import com.example.model.Commentary
import com.example.model.Match
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CricketViewModel : ViewModel() {

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _trackedTeam = MutableStateFlow("")
    val trackedTeam: StateFlow<String> = _trackedTeam.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()
    
    private val _lastUpdated = MutableStateFlow("Just now")
    val lastUpdated: StateFlow<String> = _lastUpdated.asStateFlow()

    val filteredMatches: StateFlow<List<Match>> = combine(_matches, _searchQuery, _trackedTeam) { matchItems, query, tracked ->
        var list = matchItems
        
        if (tracked.isNotBlank()) {
            val trackedList = list.filter {
                it.team1.contains(tracked, ignoreCase = true) ||
                it.team2.contains(tracked, ignoreCase = true)
            }
            if (trackedList.isNotEmpty()) {
                list = trackedList
            }
        }
        
        if (query.isNotBlank()) {
            list = list.filter {
                it.team1.contains(query, ignoreCase = true) ||
                it.team2.contains(query, ignoreCase = true) ||
                it.status.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMatchId = MutableStateFlow<String?>(null)
    val selectedMatchId: StateFlow<String?> = _selectedMatchId.asStateFlow()

    init {
        startLiveApiFetching()
    }

    fun selectMatch(id: String?) {
        _selectedMatchId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTrackedTeam(team: String) {
        _trackedTeam.value = team
    }

    fun refresh() {
        _isLoading.value = true
        _isError.value = false
        // Triggered immediately in the loop below anyway, but could be explicit
    }

    private fun startLiveApiFetching() {
        viewModelScope.launch {
            while (true) {
                try {
                    val rawTitles = RssParser.fetchLiveMatches()
                    
                    val majorKeywords = listOf(
                        // Major International Teams
                        "India", "Australia", "England", "South Africa", "New Zealand",
                        "Pakistan", "Sri Lanka", "West Indies", "Bangladesh", "Afghanistan",
                        "Ireland", "Zimbabwe", "Netherlands", "Scotland", "Nepal", "USA",
                        "Oman", "UAE", "Namibia", "Uganda", "Papua New Guinea",
                        
                        // Major Franchise Keywords (IPL, BBL, PSL, CPL, SA20, MLC etc.)
                        "Chennai", "Mumbai", "Royal Challengers", "Kolkata", "Delhi", "Gujarat", 
                        "Rajasthan", "Sunrisers", "Lucknow", "Punjab", "Super Kings", "Capitals",
                        "Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide", "Hobart", "Renegades", "Scorchers",
                        "Lahore", "Karachi", "Islamabad", "Peshawar", "Quetta", "Multan",
                        "Trinbago", "Jamaica", "Barbados", "Guyana", "St Lucia", "St Kitts", "Antigua",
                        "Pretoria", "MI Cape Town", "Paarl", "Durban", "Joburg",
                        "Oval", "Trent", "Welsh", "Southern", "London", "Manchester", "Birmingham", "Northern",
                        "MLC", "Major League", "LPL", "Global T20", "BPL", "WPL", "Super Smash", "The Hundred"
                    )

                    // Strictly filter out minor/domestic leagues and focus on major/standard matches
                    val filteredTitles = rawTitles.filter { title ->
                        val currentSearch = _searchQuery.value
                        if (currentSearch.isNotBlank() && title.contains(currentSearch, ignoreCase = true)) {
                            return@filter true
                        }
                        
                        // Exclude specific unwanted types first
                        val isExcluded = listOf("Under-19", "U19", "County", "Shield", "Trophy", "2nd XI", "Club", "Warm-up", "Practice").any { 
                            title.contains(it, ignoreCase = true) 
                        }
                        
                        // Include if it has a major keyword
                        val isMajor = majorKeywords.any { keyword -> title.contains(keyword, ignoreCase = true) }
                        
                        !isExcluded && isMajor
                    }

                    // For each raw title, map to our Match structure
                    val newMatches = filteredTitles.map { mapTitleToMatch(it) }
                    
                    _matches.value = newMatches
                    _isLoading.value = false
                    _isError.value = false
                    
                    val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    _lastUpdated.value = format.format(java.util.Date())
                } catch (e: Exception) {
                    Log.e("CricketViewModel", "Error fetching RSS matches", e)
                    _isError.value = true
                    _isLoading.value = false
                }
                delay(5000) // Poll every 5 seconds
            }
        }
    }
    
    private fun mapTitleToMatch(title: String): Match {
        // Example titles: 
        // "Sri Lanka Under-19s 291/9 * v India Under-19s 290/8 "
        // "India 150/3 * v England"
        // "Team A v Team B at London, Jul 10, 10:00 GMT"
        val parts = title.split(" v ")
        var team1Full = parts.getOrNull(0)?.trim() ?: title
        var team2Full = parts.getOrNull(1)?.trim() ?: ""
        
        var status = "Live Match (via RSS)"
        
        // Check for upcoming match timing in team2Full
        if (team2Full.contains(" at ")) {
            val atParts = team2Full.split(" at ")
            team2Full = atParts[0].trim()
            val locationAndTime = atParts.drop(1).joinToString(" at ").trim()
            
            // If neither team has a score, it's likely an upcoming match
            if (!team1Full.any { it.isDigit() } && !team2Full.any { it.isDigit() }) {
                status = "Starts: $locationAndTime"
            }
        }

        // Extract basic info
        val (t1Name, t1Score, t1Overs) = extractNameAndScore(team1Full)
        val (t2Name, t2Score, t2Overs) = extractNameAndScore(team2Full)

        // Assign a stable ID (hash of title or random if needed, let's use a hash of the team names to keep it stable)
        val id = (t1Name + t2Name).hashCode().toString()
        
        return Match(
            id = id,
            team1 = t1Name,
            team2 = t2Name,
            score1 = t1Score,
            score2 = t2Score,
            overs1 = t1Overs,
            overs2 = t2Overs,
            status = status,
            liveCommentary = emptyList() // RSS doesn't give ball-by-ball
        )
    }

    private fun extractNameAndScore(fullStr: String): Triple<String, String, String> {
        // Very rough heuristic parsing for RSS format
        // Handles formats like "291/9 *", "150/3", "400/5 d"
        val scoreRegex = Regex("""(\d+(?:/\d+)?(?:\s*(?:d|\*))?)$""")
        val match = scoreRegex.find(fullStr.trim())
        
        if (match != null) {
            val scorePart = match.groupValues[1].trim()
            val namePart = fullStr.substring(0, match.range.first).trim()
            return Triple(namePart, scorePart, "")
        }
        
        return Triple(fullStr.trim(), "", "")
    }
}
