package com.example.data

import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.example.api.RssParser
import com.example.api.RetrofitClient
import com.example.model.Match
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.net.URL
import org.json.JSONObject

sealed interface FetchResult {
    object Loading : FetchResult
    data class Success(val matches: List<Match>, val isOffline: Boolean) : FetchResult
    data class Error(val message: String) : FetchResult
}


class CricketRepository(private val context: android.content.Context) {
    private val dao = AppDatabase.getDatabase(context).matchDao()

    private val squadCache = mutableMapOf<String, List<String>>()


    
    fun getLiveMatchesFlow(): Flow<List<Match>> {
        return dao.getAllMatchesFlow().map { entities ->
            val allMatches = entities.map { it.toMatch() }
            val liveMatches = allMatches.filter { it.status.contains("Live", true) || it.status.contains("*", true) }
            val otherMatches = allMatches.filterNot { it.status.contains("Live", true) || it.status.contains("*", true) }
            
            // Limit older matches to keep UI snappy
            (liveMatches + otherMatches.take(30))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun syncMatches(preferredPlayers: Set<String>, preferredTeams: Set<String>) = withContext(Dispatchers.IO) {
        val rawItems = RssParser.fetchLiveMatches()
        if (rawItems.isEmpty()) return@withContext
        
        val parsedMatches = rawItems.filter { item ->
            val title = item.title
            val isExcluded = CricketConstants.EXCLUDED_KEYWORDS.any { title.contains(it, ignoreCase = true) }
            val isMajor = CricketConstants.MAJOR_KEYWORDS.any { keyword -> title.contains(keyword, ignoreCase = true) }
            val isPreferredTeam = preferredTeams.any { team -> title.contains(team, ignoreCase = true) }
            !isExcluded && (isMajor || isPreferredTeam)
        }.map { mapItemToMatch(it) }.map { match ->
            val highlightStats = mutableListOf<String>()
            if (match.notablePerformances.isNotEmpty()) {
                val statsArray = match.notablePerformances.split(" | ")
                for (stat in statsArray) {
                    for (fav in preferredPlayers) {
                        if (stat.contains(fav, ignoreCase = true)) {
                            highlightStats.add("★ $stat")
                            break
                        }
                    }
                }
            }
            val finalPerformances = if (highlightStats.isNotEmpty()) {
                highlightStats.joinToString(" • ")
            } else if (match.notablePerformances.isNotEmpty()) {
                match.notablePerformances
            } else {
                ""
            }
            match.copy(notablePerformances = finalPerformances)
        }
        
        val rssMatches = deduplicateMatches(parsedMatches)
        if (rssMatches.isNotEmpty()) {
            dao.insertMatches(rssMatches.map { it.toEntity() })
        }
        
        // Data Pruning - Keep DB clean and fast
        val threeDaysAgo = System.currentTimeMillis() - (3L * 24L * 60L * 60L * 1000L)
        dao.deleteOldMatches(threeDaysAgo)
    }


    companion object {
        private fun getStatusPriority(match: Match): Int {
            return when (match.matchState) {
                "LIVE" -> 3
                "COMPLETED" -> 2
                else -> 1
            }
        }

        fun deduplicateMatches(parsedMatches: List<Match>): List<Match> {
            val dedupedMap = mutableMapOf<String, Match>()
            for (match in parsedMatches) {
                val existing = dedupedMap[match.id]
                if (existing == null) {
                    dedupedMap[match.id] = match
                } else {
                    val newScore = getStatusPriority(match)
                    val oldScore = getStatusPriority(existing)
                    if (newScore > oldScore) {
                        dedupedMap[match.id] = match
                    } else if (newScore == oldScore) {
                        // Prefer Cricbuzz if available, otherwise prefer the one with scores
                        val baseMatch = if (match.matchUrl.contains("cricbuzz") && !existing.matchUrl.contains("cricbuzz")) {
                            match
                        } else if (existing.matchUrl.contains("cricbuzz") && !match.matchUrl.contains("cricbuzz")) {
                            existing
                        } else if (match.score1.isNotEmpty() && existing.score1.isEmpty()) {
                            match
                        } else {
                            existing
                        }
                        
                        val otherMatch = if (baseMatch === match) existing else match
                        
                        var updated = baseMatch
                        if (updated.score1.isBlank() && otherMatch.score1.isNotBlank()) {
                            updated = updated.copy(score1 = otherMatch.score1)
                        }
                        if (updated.score2.isBlank() && otherMatch.score2.isNotBlank()) {
                            updated = updated.copy(score2 = otherMatch.score2)
                        }
                        if (updated.overs1.isBlank() && otherMatch.overs1.isNotBlank()) {
                            updated = updated.copy(overs1 = otherMatch.overs1)
                        }
                        if (updated.overs2.isBlank() && otherMatch.overs2.isNotBlank()) {
                            updated = updated.copy(overs2 = otherMatch.overs2)
                        }
                        if (otherMatch.score1.contains("*") && !updated.score1.contains("*")) {
                            updated = updated.copy(score1 = updated.score1 + " *")
                        }
                        if (otherMatch.score2.contains("*") && !updated.score2.contains("*")) {
                            updated = updated.copy(score2 = updated.score2 + " *")
                        }
                        if ((updated.matchTiming == "Match Update" || updated.matchTiming == "In Progress") && 
                            otherMatch.matchTiming != "Match Update" && otherMatch.matchTiming != "In Progress" && otherMatch.matchTiming.isNotBlank()) {
                            updated = updated.copy(matchTiming = otherMatch.matchTiming)
                        }
                        if (updated.seriesName == "Cricket Series" && otherMatch.seriesName != "Cricket Series" && otherMatch.seriesName.isNotBlank()) {
                            updated = updated.copy(seriesName = otherMatch.seriesName)
                        }
                        if (updated.notablePerformances.isBlank() && otherMatch.notablePerformances.isNotBlank()) {
                            updated = updated.copy(notablePerformances = otherMatch.notablePerformances)
                        }
                        dedupedMap[match.id] = updated
                    }
                }
            }
            return dedupedMap.values.toList()
        }

        fun mapItemToMatch(item: com.example.api.RssItem): Match {
            if (item.source == "CRICBUZZ") {
                val t1 = item.team1.trim()
                val t2 = item.team2.trim()
                val sortedTeams = listOf(t1, t2).sorted()
                val id = (sortedTeams[0] + sortedTeams[1]).hashCode().toString()
                
                return Match(
                    id = id,
                    team1 = t1,
                    team2 = t2,
                    score1 = item.score1,
                    score2 = item.score2,
                    overs1 = item.overs1,
                    overs2 = item.overs2,
                    status = item.matchStatus,
                    seriesName = item.seriesName.ifBlank { "Cricket Series" },
                    matchTiming = item.matchTiming,
                    matchUrl = item.link,
                    notablePerformances = item.rawLiveStats,
                    rawState = item.rawState
                )
            }
            
            val rawTitle = item.title
            val link = item.link
            val liveStats = item.rawLiveStats
            val rawSeries = item.seriesName
            val rawTiming = item.matchTiming
            val rawState = item.rawState
            
            val title = rawTitle.replace("via rss", "", ignoreCase = true).trim()
            
            var team1Full = title
            var team2Full = ""
            var status = "Live Match"
            
            val commaSplit = title.split(",", limit = 2)
            var teamsPart = title
            if (commaSplit.size > 1) {
                teamsPart = commaSplit[0].trim()
                status = commaSplit[1].trim()
            }

            if (teamsPart.contains(" vs ", ignoreCase = true)) {
                val teamParts = teamsPart.split(Regex(" vs ", RegexOption.IGNORE_CASE), limit = 2)
                team1Full = teamParts[0].trim()
                team2Full = teamParts[1].trim()
            } else if (teamsPart.contains(" v ", ignoreCase = true)) {
                val teamParts = teamsPart.split(Regex(" v ", RegexOption.IGNORE_CASE), limit = 2)
                team1Full = teamParts[0].trim()
                team2Full = teamParts[1].trim()
            }

            if (team2Full.contains(" at ")) {
                val atParts = team2Full.split(" at ", limit = 2)
                team2Full = atParts[0].trim()
                val locationAndTime = atParts[1].trim()
                if (status.isEmpty() || status == "Live Match") {
                    status = "Starts: $locationAndTime"
                }
            }
            
            if (status.isEmpty()) {
                 status = "Live Match"
            }

            val (t1Name, t1Score, t1Overs) = extractNameAndScore(team1Full)
            val (t2Name, t2Score, t2Overs) = extractNameAndScore(team2Full)
            val sortedTeams = listOf(t1Name, t2Name).sorted()
            val id = (sortedTeams[0] + sortedTeams[1]).hashCode().toString()

            val derivedSeries = if (rawSeries.isNotBlank()) rawSeries else {
                val combinedText = "$title $status"
                when {
                    combinedText.contains("IPL", true) || combinedText.contains("Premier League", true) -> "Indian Premier League"
                    combinedText.contains("T20 World Cup", true) -> "ICC T20 World Cup"
                    combinedText.contains("World Cup", true) -> "ICC Cricket World Cup"
                    combinedText.contains("BBL", true) || combinedText.contains("Big Bash", true) -> "Big Bash League"
                    combinedText.contains("PSL", true) -> "Pakistan Super League"
                    combinedText.contains("CPL", true) -> "Caribbean Premier League"
                    combinedText.contains("The Hundred", true) -> "The Hundred"
                    combinedText.contains("WPL", true) -> "Women's Premier League"
                    combinedText.contains("Test", true) -> "Test Championship Series"
                    combinedText.contains("T20I", true) || combinedText.contains("T20", true) -> "T20 International Series"
                    combinedText.contains("ODI", true) -> "ODI International Series"
                    else -> "Cricket Series"
                }
            }

            val derivedTiming = if (rawTiming.isNotBlank()) rawTiming else {
                if (status.contains("Starts:", true)) {
                    status.replace("Starts:", "").trim()
                } else if (status.contains("Live", true) || status.contains("*")) {
                    "In Progress"
                } else {
                    "Match Update"
                }
            }

            return Match(
                id = id,
                team1 = t1Name,
                team2 = t2Name,
                score1 = t1Score,
                score2 = t2Score,
                overs1 = t1Overs,
                overs2 = t2Overs,
                status = status,
                seriesName = derivedSeries,
                matchTiming = derivedTiming,
                liveCommentary = emptyList(),
                matchUrl = link.replace("http://", "https://"),
                notablePerformances = liveStats,
                rawState = rawState
            )
        }

        private fun extractNameAndScore(fullStr: String): Triple<String, String, String> {
            var strToProcess = fullStr.trim()
            val hasStar = strToProcess.endsWith("*")
            if (hasStar) {
                strToProcess = strToProcess.dropLast(1).trim()
            }
            
            val overRegex = Regex("""\(([^)]+)\)$""")
            var overs = ""
            val overMatch = overRegex.find(strToProcess)
            if (overMatch != null) {
                val parsedOvers = overMatch.groupValues[1].trim()
                if (parsedOvers.contains("Women", ignoreCase = true) || parsedOvers.contains("Men", ignoreCase = true)) {
                    // Part of the team name
                } else {
                    overs = if (parsedOvers.isNotEmpty()) "($parsedOvers)" else ""
                    strToProcess = strToProcess.substring(0, overMatch.range.first).trim()
                }
            }
            
            val scoreRegex = Regex("""(\d+(?:/\d+)?(?:\s*(?:d|\*))?)$""")
            val match = scoreRegex.find(strToProcess)
            if (match != null) {
                var scorePart = match.groupValues[1].trim()
                if (hasStar && !scorePart.endsWith("*")) {
                    scorePart += " *"
                }
                val namePart = strToProcess.substring(0, match.range.first).trim()
                return Triple(namePart, scorePart, overs)
            }
            
            return Triple(strToProcess, if (hasStar) "*" else "", overs)
        }
    }
    private suspend fun checkMatchForPreferredPlayers(match: Match, preferredPlayers: Set<String>): Match = withContext(Dispatchers.IO) {
        if (preferredPlayers.isEmpty() || match.matchUrl.isEmpty()) {
            return@withContext match
        }
        try {
            val response = RetrofitClient.cricketService.getHtml(match.matchUrl)
            val html = response.string()
            if (html.isNotEmpty()) {
                
                // Silent HTML Extraction Engine
                // Strip tags to extract raw text
                val textOnly = html.replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
                
                val foundPlayers = preferredPlayers.filter { player ->
                    val names = player.split(" ")
                    val lastName = names.last()
                    val hasLastName = textOnly.contains(lastName, ignoreCase = true)
                    
                    textOnly.contains(player, ignoreCase = true) || 
                    html.contains(player, ignoreCase = true) ||
                    (names.size > 1 && hasLastName && html.contains(names.first(), ignoreCase = true))
                }
                
                if (foundPlayers.isNotEmpty()) {
                    val extractedStats = mutableListOf<String>()
                    val isPlayingXI = textOnly.contains("Playing XI", ignoreCase = true) || textOnly.contains("Squad", ignoreCase = true)
                    
                    for (player in foundPlayers) {
                        val lastName = player.split(" ").last()
                        val idx = textOnly.indexOf(lastName, ignoreCase = true)
                        
                        if (idx != -1) {
                            val contextEnd = (idx + 100).coerceAtMost(textOnly.length)
                            val context = textOnly.substring(idx, contextEnd)
                            
                            // Look for batting stats: "Kohli 45 (30)"
                            val batMatch = Regex("\\b(\\d{1,3})\\s*\\(\\s*(\\d{1,3})\\s*\\)").find(context)
                            
                            // Look for bowling stats: "Boland 2/24 (4.0)" or similar "2-24"
                            val bowlMatch = Regex("\\b(\\d{1,2})[/\\-](\\d{1,3})\\s*\\(\\s*(\\d{1,2}\\.\\d)\\s*\\)").find(context)
                            
                            if (batMatch != null) {
                                extractedStats.add("$lastName: ${batMatch.groupValues[1]}* (${batMatch.groupValues[2]}b)")
                            } else if (bowlMatch != null) {
                                extractedStats.add("$lastName: ${bowlMatch.groupValues[1]}/${bowlMatch.groupValues[2]} (${bowlMatch.groupValues[3]}ov)")
                            } else if (isPlayingXI) {
                                extractedStats.add("$lastName (In XI)")
                            } else {
                                extractedStats.add("$lastName (Playing)")
                            }
                        } else {
                            extractedStats.add(player)
                        }
                    }
                    
                    val performances = if (extractedStats.isNotEmpty()) {
                        "★ " + extractedStats.joinToString(" | ")
                    } else {
                        "Your favorite player(s) playing: " + foundPlayers.joinToString(", ")
                    }
                    
                    return@withContext match.copy(notablePerformances = performances)
                }
            }
        } catch (e: Exception) {
            Log.e("CricketRepository", "Error fetching HTML for player check", e)
        }
        
        // Fallback for UPCOMING matches or if HTML fetch failed:
        // Check if the favorite player is in the global squad for team1 or team2
        try {
            val t1Squad = squadCache[match.team1] ?: fetchDynamicPlayers(setOf(match.team1)).also { squadCache[match.team1] = it }
            val t2Squad = squadCache[match.team2] ?: fetchDynamicPlayers(setOf(match.team2)).also { squadCache[match.team2] = it }
            
            val likelyPlayers = preferredPlayers.filter { player -> 
                val playerLower = player.lowercase()
                t1Squad.any { it.lowercase() == playerLower } || t2Squad.any { it.lowercase() == playerLower } 
            }
            if (likelyPlayers.isNotEmpty()) {
                val performances = "★ In Squad (Upcoming): " + likelyPlayers.joinToString(", ")
                return@withContext match.copy(notablePerformances = performances)
            }
        } catch (e: Exception) {
            Log.e("CricketRepository", "Error in fallback squad check", e)
        }
        
        return@withContext match
    }

    suspend fun fetchDynamicPlayers(teams: Set<String>): List<String> = withContext(Dispatchers.IO) {
        val players = mutableSetOf<String>()
        
        for (team in teams) {
            CricketConstants.TOP_PLAYERS_FALLBACK[team]?.let { players.addAll(it) }
        }
        
        // Always add some global stars if list is too small
        if (players.size < 10) {
            players.addAll(CricketConstants.GLOBAL_STARS)
        }
        
        return@withContext players.toList().sorted()
    }

    suspend fun getPlayerNews(playerName: String): List<com.example.model.NewsArticle> {
        return RssParser.fetchPlayerNews(playerName)
    }
}