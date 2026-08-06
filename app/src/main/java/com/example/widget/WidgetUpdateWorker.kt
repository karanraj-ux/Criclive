package com.example.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.api.RssParser
import com.example.util.toAbbreviation
import kotlinx.coroutines.flow.first

class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(MatchGlanceWidget::class.java)
            if (glanceIds.isEmpty()) return Result.success()

            val onboardingManager = com.example.data.OnboardingManager(applicationContext)
            val preferredTeams = onboardingManager.preferredTeams.first()
            val pinnedMatchId = onboardingManager.widgetPinnedMatchId.first()

            val rawItems = RssParser.fetchLiveMatches()
            if (rawItems.isEmpty()) {
                return Result.success()
            }
            val parsedMatches = rawItems.map { com.example.data.CricketRepository.mapItemToMatch(it) }
            
            val preferredMatch = if (pinnedMatchId.isNotEmpty()) {
                parsedMatches.firstOrNull { it.id == pinnedMatchId }
            } else {
                parsedMatches.firstOrNull { match -> 
                    preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) } && match.matchState == "LIVE"
                } ?: parsedMatches.firstOrNull { it.matchState == "LIVE" }
                  ?: parsedMatches.firstOrNull()
            }

            if (preferredMatch != null) {
                if (pinnedMatchId.isNotEmpty() && preferredMatch.id == pinnedMatchId) {
                    onboardingManager.saveWidgetPinnedMatchDetails(
                        preferredMatch.team1, preferredMatch.score1, preferredMatch.overs1,
                        preferredMatch.team2, preferredMatch.score2, preferredMatch.overs2,
                        preferredMatch.matchState
                    )
                }

                glanceIds.forEach { glanceId ->
                    var hasChanged = false
                    updateAppWidgetState(applicationContext, glanceId) { prefs ->
                        val newMatchId = preferredMatch.id
                        val newTeam1 = preferredMatch.team1.toAbbreviation()
                        val newScore1 = preferredMatch.score1.ifEmpty { if (preferredMatch.team1.isNotEmpty()) "0/0" else "-" }
                        val newOvers1 = preferredMatch.overs1.ifEmpty { if (preferredMatch.team1.isNotEmpty()) "(0.0)" else "" }.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it }
                        val newTeam2 = preferredMatch.team2.toAbbreviation()
                        val newScore2 = preferredMatch.score2.ifEmpty { if (preferredMatch.team2.isNotEmpty()) "0/0" else "-" }
                        val newOvers2 = preferredMatch.overs2.ifEmpty { if (preferredMatch.team2.isNotEmpty()) "(0.0)" else "" }.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it }
                        val newStatus = preferredMatch.matchState.ifEmpty { "NO LIVE MATCHES" }
                        
                        if (prefs[MatchGlanceWidget.MATCH_ID_KEY] != newMatchId ||
                            prefs[MatchGlanceWidget.TEAM1_KEY] != newTeam1 ||
                            prefs[MatchGlanceWidget.SCORE1_KEY] != newScore1 ||
                            prefs[MatchGlanceWidget.OVERS1_KEY] != newOvers1 ||
                            prefs[MatchGlanceWidget.TEAM2_KEY] != newTeam2 ||
                            prefs[MatchGlanceWidget.SCORE2_KEY] != newScore2 ||
                            prefs[MatchGlanceWidget.OVERS2_KEY] != newOvers2 ||
                            prefs[MatchGlanceWidget.STATUS_KEY] != newStatus) {
                            
                            hasChanged = true
                            prefs[MatchGlanceWidget.MATCH_ID_KEY] = newMatchId
                            prefs[MatchGlanceWidget.TEAM1_KEY] = newTeam1
                            prefs[MatchGlanceWidget.SCORE1_KEY] = newScore1
                            prefs[MatchGlanceWidget.OVERS1_KEY] = newOvers1
                            prefs[MatchGlanceWidget.TEAM2_KEY] = newTeam2
                            prefs[MatchGlanceWidget.SCORE2_KEY] = newScore2
                            prefs[MatchGlanceWidget.OVERS2_KEY] = newOvers2
                            prefs[MatchGlanceWidget.STATUS_KEY] = newStatus
                        }
                    }
                    if (hasChanged) {
                        MatchGlanceWidget().update(applicationContext, glanceId)
                    }
                }
            } else if (pinnedMatchId.isEmpty()) {
                glanceIds.forEach { glanceId ->
                    var hasChanged = false
                    updateAppWidgetState(applicationContext, glanceId) { prefs ->
                        if (prefs[MatchGlanceWidget.MATCH_ID_KEY] != "" ||
                            prefs[MatchGlanceWidget.TEAM1_KEY] != "--" ||
                            prefs[MatchGlanceWidget.SCORE1_KEY] != "-" ||
                            prefs[MatchGlanceWidget.STATUS_KEY] != "NO LIVE MATCHES") {
                            
                            hasChanged = true
                            prefs[MatchGlanceWidget.MATCH_ID_KEY] = ""
                            prefs[MatchGlanceWidget.TEAM1_KEY] = "--"
                            prefs[MatchGlanceWidget.SCORE1_KEY] = "-"
                            prefs[MatchGlanceWidget.OVERS1_KEY] = ""
                            prefs[MatchGlanceWidget.TEAM2_KEY] = "--"
                            prefs[MatchGlanceWidget.SCORE2_KEY] = "-"
                            prefs[MatchGlanceWidget.OVERS2_KEY] = ""
                            prefs[MatchGlanceWidget.STATUS_KEY] = "NO LIVE MATCHES"
                        }
                    }
                    if (hasChanged) {
                        MatchGlanceWidget().update(applicationContext, glanceId)
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
