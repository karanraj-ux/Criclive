package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.api.RssParser
import kotlinx.coroutines.flow.first
import com.example.model.Match

class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private suspend fun renderWidget(
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        matchId: String,
        team1: String, score1: String, overs1: String,
        team2: String, score2: String, overs2: String,
        status: String
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout)
            val intent = Intent(applicationContext, MainActivity::class.java)
            if (matchId.isNotEmpty()) {
                intent.putExtra("MATCH_ID", matchId)
            }

            views.setTextViewText(R.id.widget_team1, if (team1.isNotEmpty()) team1.take(3).uppercase() else "--")
            views.setTextViewText(R.id.widget_score1, score1.ifEmpty { if (team1.isNotEmpty()) "0/0" else "-" })
            views.setTextViewText(R.id.widget_overs1, overs1.ifEmpty { if (team1.isNotEmpty()) "(0.0)" else "" }.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it })
            
            views.setTextViewText(R.id.widget_team2, if (team2.isNotEmpty()) team2.take(3).uppercase() else "--")
            views.setTextViewText(R.id.widget_score2, score2.ifEmpty { if (team2.isNotEmpty()) "0/0" else "-" })
            views.setTextViewText(R.id.widget_overs2, overs2.ifEmpty { if (team2.isNotEmpty()) "(0.0)" else "" }.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it })
            
            views.setTextViewText(R.id.widget_status, status.ifEmpty { "NO LIVE MATCHES" })

            val pendingIntent = PendingIntent.getActivity(applicationContext, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val refreshIntent = Intent(applicationContext, MatchWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                applicationContext, appWidgetId, refreshIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val componentName = ComponentName(applicationContext, MatchWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) {
            return Result.success()
        }

        try {
            val onboardingManager = com.example.data.OnboardingManager(applicationContext)
            val preferredTeams = onboardingManager.preferredTeams.first()
            val pinnedMatchId = onboardingManager.widgetPinnedMatchId.first()

            // 1. Initial Instant Render from Cache (Prevents macrosecond blinking)
            if (pinnedMatchId.isNotEmpty()) {
                val t1 = onboardingManager.widgetPinnedTeam1.first()
                val s1 = onboardingManager.widgetPinnedScore1.first()
                val o1 = onboardingManager.widgetPinnedOvers1.first()
                val t2 = onboardingManager.widgetPinnedTeam2.first()
                val s2 = onboardingManager.widgetPinnedScore2.first()
                val o2 = onboardingManager.widgetPinnedOvers2.first()
                val st = onboardingManager.widgetPinnedStatus.first()
                if (t1.isNotEmpty() || t2.isNotEmpty()) {
                    renderWidget(appWidgetManager, appWidgetIds, pinnedMatchId, t1, s1, o1, t2, s2, o2, st)
                }
            }

            // 2. Fetch fresh live matches
            val rawItems = RssParser.fetchLiveMatches()
            if (rawItems.isEmpty()) {
                // If fetch fails, retain the cached render without showing errors
                return Result.success()
            }
            val parsedMatches = rawItems.map { com.example.data.CricketRepository.mapItemToMatch(it) }

            // 3. Find target match
            val preferredMatch = if (pinnedMatchId.isNotEmpty()) {
                parsedMatches.firstOrNull { it.id == pinnedMatchId }
            } else {
                parsedMatches.firstOrNull { match -> 
                    preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) } && match.matchState == "LIVE"
                } ?: parsedMatches.firstOrNull { it.matchState == "LIVE" }
                  ?: parsedMatches.firstOrNull()
            }

            // 4. Save and Render
            if (preferredMatch != null) {
                if (pinnedMatchId.isNotEmpty() && preferredMatch.id == pinnedMatchId) {
                    onboardingManager.saveWidgetPinnedMatchDetails(
                        preferredMatch.team1, preferredMatch.score1, preferredMatch.overs1,
                        preferredMatch.team2, preferredMatch.score2, preferredMatch.overs2,
                        preferredMatch.matchState
                    )
                }
                renderWidget(
                    appWidgetManager, appWidgetIds, preferredMatch.id,
                    preferredMatch.team1, preferredMatch.score1, preferredMatch.overs1,
                    preferredMatch.team2, preferredMatch.score2, preferredMatch.overs2,
                    preferredMatch.matchState
                )
            } else if (pinnedMatchId.isEmpty()) {
                renderWidget(appWidgetManager, appWidgetIds, "", "", "", "", "", "", "", "NO LIVE MATCHES")
            }
            
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
