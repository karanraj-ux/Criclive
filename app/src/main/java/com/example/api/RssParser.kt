package com.example.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.StringReader


object RssParser {
    private const val RSS_URL = "https://static.cricinfo.com/rss/livescores.xml"
    private const val CRICBUZZ_URL = "https://www.cricbuzz.com/cricket-match/live-scores"
    private const val CRICBUZZ_RECENT_URL = "https://www.cricbuzz.com/cricket-match/live-scores/recent-matches"
    
    private const val TAG = "RssParser"

    suspend fun fetchLiveMatches(): List<RssItem> = withContext(Dispatchers.IO) {
        val matches = mutableListOf<RssItem>()
        val titles = mutableSetOf<String>()

        fetchCricbuzzMatches(matches, titles)
        fetchRssMatches(matches, titles)

        matches
    }

    private suspend fun fetchRssMatches(matches: MutableList<RssItem>, titles: MutableSet<String>) {
        try {
            val response = RetrofitClient.cricketService.getRssFeed(RSS_URL)
            val xmlString = response.string()
            
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlString))
            
            var eventType = parser.eventType
            var insideItem = false
            var currentTitle = ""
            var currentLink = ""
            var currentDesc = ""
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when {
                            parser.name.equals("item", ignoreCase = true) -> {
                                insideItem = true
                                currentTitle = ""
                                currentLink = ""
                                currentDesc = ""
                            }
                            insideItem && parser.name.equals("title", ignoreCase = true) -> {
                                currentTitle = parser.nextText().trim()
                            }
                            insideItem && parser.name.equals("link", ignoreCase = true) -> {
                                currentLink = parser.nextText().trim()
                            }
                            insideItem && parser.name.equals("description", ignoreCase = true) -> {
                                currentDesc = parser.nextText().trim()
                            }
                            insideItem && parser.name.equals("guid", ignoreCase = true) -> {
                                if (currentLink.isEmpty()) {
                                    currentLink = parser.nextText().trim()
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("item", ignoreCase = true)) {
                            insideItem = false
                            if (currentTitle.isNotEmpty() && !currentTitle.contains("No Match in progress")) {
                                matches.add(RssItem(currentTitle, currentLink, currentDesc, rawState = ""))
                                titles.add(currentTitle)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching RSS matches", e)
        }
    }

    private suspend fun fetchCricbuzzMatches(matches: MutableList<RssItem>, titles: MutableSet<String>) {
        val urlsToTry = listOf(CRICBUZZ_URL, CRICBUZZ_RECENT_URL)
        
        for (cbUrl in urlsToTry) {
            try {
                val response = RetrofitClient.cricketService.getHtml(cbUrl)
                val html = response.string()
                parseCricbuzzHtml(html, cbUrl, matches, titles)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching Cricbuzz matches from $cbUrl", e)
            }
        }
    }

    private fun parseCricbuzzHtml(html: String, cbUrl: String, matches: MutableList<RssItem>, titles: MutableSet<String>) {
        var searchStr = "\"typeMatches\":["
        var idx = html.indexOf(searchStr)
        if (idx == -1) {
            searchStr = "\\\"typeMatches\\\":["
            idx = html.indexOf(searchStr)
        }
        if (idx == -1) return
        
        val startIdx = idx + searchStr.length - 1
        var brackets = 0
        var endIdx = -1
        var inString = false
        var escape = false
        
        for (i in startIdx until html.length) {
            val c = html[i]
            if (escape) {
                escape = false
                continue
            }
            if (c == '\\') {
                escape = true
                continue
            }
            if (c == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (c == '[') brackets++
                else if (c == ']') brackets--
                
                if (brackets == 0) {
                    endIdx = i
                    break
                }
            }
        }
        
        if (endIdx != -1) {
            var jsonStr = html.substring(startIdx, endIdx + 1)
            jsonStr = jsonStr.replace("\\\"", "\"").replace("\\\\", "\\")
            try {
                val dataArray = JSONArray(jsonStr)
                parseCricbuzzJsonArray(dataArray, cbUrl, matches, titles)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing Cricbuzz JSON", e)
            }
        }
    }

    private fun parseCricbuzzJsonArray(dataArray: JSONArray, cbUrl: String, matches: MutableList<RssItem>, titles: MutableSet<String>) {
        for (i in 0 until dataArray.length()) {
            val typeObj = dataArray.optJSONObject(i) ?: continue
            val seriesMatches = typeObj.optJSONArray("seriesMatches") ?: continue
            
            for (j in 0 until seriesMatches.length()) {
                val seriesObj = seriesMatches.optJSONObject(j) ?: continue
                val wrapper = seriesObj.optJSONObject("seriesAdWrapper") ?: continue
                val wrapperName = wrapper.optString("seriesName", "")
                val matchesArray = wrapper.optJSONArray("matches") ?: continue
                
                for (k in 0 until matchesArray.length()) {
                    val matchObj = matchesArray.optJSONObject(k) ?: continue
                    parseSingleCricbuzzMatch(matchObj, cbUrl, matches, titles, wrapperName)
                }
            }
        }
    }

    private fun parseSingleCricbuzzMatch(matchObj: JSONObject, cbUrl: String, matches: MutableList<RssItem>, titles: MutableSet<String>, fallbackSeries: String = "") {
        val matchInfo = matchObj.optJSONObject("matchInfo") ?: return
        val matchScore = matchObj.optJSONObject("matchScore")
        
        val team1Name = matchInfo.optJSONObject("team1")?.optString("teamName") ?: return
        val team2Name = matchInfo.optJSONObject("team2")?.optString("teamName") ?: return
        val status = matchInfo.optString("status")
        val matchDesc = matchInfo.optString("matchDesc", "")
        var seriesName = matchInfo.optString("seriesName", "").ifEmpty {
            matchInfo.optJSONObject("series")?.optString("seriesName", "").orEmpty()
        }.ifEmpty { 
            matchInfo.optJSONObject("series")?.optString("name", "").orEmpty()
        }.ifEmpty { fallbackSeries }
        if (seriesName.isBlank()) {
            seriesName = "Cricket Series"
        }
        
        val rawState = matchInfo.optString("state", "")
        
        val startDate = matchInfo.optString("startDate", "")
        
        var t1Runs = ""
        var t1Wickets = ""
        var t1Overs = ""
        var t2Runs = ""
        var t2Wickets = ""
        var t2Overs = ""
        
        if (matchScore != null) {
            val inngs1T1 = matchScore.optJSONObject("team1Score")?.optJSONObject("inngs1")
            if (inngs1T1 != null) {
                t1Runs = inngs1T1.opt("runs")?.toString() ?: ""
                t1Wickets = inngs1T1.opt("wickets")?.toString() ?: ""
                t1Overs = inngs1T1.opt("overs")?.toString() ?: ""
            }
            
            val inngs1T2 = matchScore.optJSONObject("team2Score")?.optJSONObject("inngs1")
            if (inngs1T2 != null) {
                t2Runs = inngs1T2.opt("runs")?.toString() ?: ""
                t2Wickets = inngs1T2.opt("wickets")?.toString() ?: ""
                t2Overs = inngs1T2.opt("overs")?.toString() ?: ""
            }
        }
        
                val t1Str = if (t1Runs.isNotEmpty()) {
            if (t1Overs.isNotEmpty()) "$team1Name $t1Runs/$t1Wickets ($t1Overs ov)"
            else "$team1Name $t1Runs/$t1Wickets"
        } else team1Name
        
        val t2Str = if (t2Runs.isNotEmpty()) {
            if (t2Overs.isNotEmpty()) "$team2Name $t2Runs/$t2Wickets ($t2Overs ov)"
            else "$team2Name $t2Runs/$t2Wickets"
        } else team2Name
        
        val displayDesc = if (matchDesc.isNotEmpty()) "$matchDesc - $status" else status
        val finalTitle = "$t1Str vs $t2Str, $displayDesc"
        
        var liveStatsStr = ""
        if (matchScore != null) {
            val combined = mutableListOf<String>()
            val batsmanArr = matchScore.optJSONArray("batsman")
            if (batsmanArr != null) {
                for (b in 0 until batsmanArr.length()) {
                    val batObj = batsmanArr.optJSONObject(b) ?: continue
                    val name = batObj.optString("batName", "")
                    val runs = batObj.optString("runs", "")
                    val balls = batObj.optString("balls", "")
                    if (name.isNotEmpty()) {
                        combined.add("$name $runs* ($balls)")
                    }
                }
            }
            
            val bowlerArr = matchScore.optJSONArray("bowler")
            if (bowlerArr != null) {
                for (b in 0 until bowlerArr.length()) {
                    val bowlObj = bowlerArr.optJSONObject(b) ?: continue
                    val name = bowlObj.optString("bowlName", "")
                    val wickets = bowlObj.optString("wickets", "")
                    val runs = bowlObj.optString("runs", "")
                    if (name.isNotEmpty()) {
                        combined.add("$name $wickets/$runs")
                    }
                }
            }
            liveStatsStr = combined.joinToString(" | ")
        }
        
        if (!titles.contains(finalTitle)) {
            val score1Str = if (t1Runs.isNotEmpty()) "$t1Runs/$t1Wickets" else ""
            val score2Str = if (t2Runs.isNotEmpty()) "$t2Runs/$t2Wickets" else ""
            
            val matchId = matchInfo.optInt("matchId", 0)
            val slug = "${team1Name.replace(" ", "-").lowercase()}-vs-${team2Name.replace(" ", "-").lowercase()}-${matchDesc.replace(" ", "-").lowercase()}-${seriesName.replace(" ", "-").lowercase()}"
            val matchLink = "https://www.cricbuzz.com/"

            matches.add(RssItem(
                title = finalTitle, 
                link = matchLink, 
                rawLiveStats = liveStatsStr, 
                seriesName = seriesName, 
                matchTiming = matchDesc,
                rawState = rawState,
                team1 = team1Name,
                team2 = team2Name,
                score1 = score1Str,
                score2 = score2Str,
                overs1 = t1Overs,
                overs2 = t2Overs,
                matchStatus = status,
                source = "CRICBUZZ"
            ))
            titles.add(finalTitle)
        }
    }

    suspend fun fetchPlayerNews(playerName: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = if (playerName.isNotBlank()) "$playerName cricket" else "cricket match updates"
            val encodedName = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://news.google.com/rss/search?q=$encodedName&hl=en-IN&gl=IN&ceid=IN:en"
            
            val response = RetrofitClient.cricketService.getRssFeed(url)
            val xmlString = response.string()
            
            val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(java.io.StringReader(xmlString))

            var eventType = parser.eventType
            var inItem = false

            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name
                when (eventType) {
                    org.xmlpull.v1.XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            inItem = true
                        } else if (inItem && tagName.equals("title", ignoreCase = true)) {
                            return@withContext parser.nextText()
                        }
                    }
                    org.xmlpull.v1.XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            inItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            android.util.Log.e("RssParser", "Error fetching news for player: $playerName", e)
        }
        null
    }
}