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
            
            val rawItems = RssParser.fetchLiveMatches()
            val parsedMatches = rawItems.map { com.example.data.CricketRepository.mapItemToMatch(it) }
            
            val preferredMatch = if (pinnedMatchId.isNotEmpty()) {
                parsedMatches.firstOrNull { it.id == pinnedMatchId }
            } else {
                parsedMatches.firstOrNull { match -> 
                    preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) } && match.matchState == "LIVE"
                } ?: parsedMatches.firstOrNull { it.matchState == "LIVE" }
                  ?: parsedMatches.firstOrNull()
            }

            if (preferredMatch != null && pinnedMatchId.isNotEmpty() && preferredMatch.id == pinnedMatchId) {
                onboardingManager.saveWidgetPinnedMatchDetails(
                    preferredMatch.team1, preferredMatch.score1, preferredMatch.overs1,
                    preferredMatch.team2, preferredMatch.score2, preferredMatch.overs2,
                    preferredMatch.matchState
                )
            }

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout)
                
                val intent = Intent(applicationContext, MainActivity::class.java)
                if (preferredMatch != null) {
                    intent.putExtra("MATCH_ID", preferredMatch.id)
                    
                    val displayStatus = preferredMatch.matchState
                    
                    views.setTextViewText(R.id.widget_team1, preferredMatch.team1.take(3).uppercase())
                    views.setTextViewText(R.id.widget_score1, preferredMatch.score1.ifEmpty { "0/0" })
                    views.setTextViewText(R.id.widget_overs1, preferredMatch.overs1.ifEmpty { "(0.0)" }.let { if (!it.startsWith("(")) "($it)" else it })
                    
                    views.setTextViewText(R.id.widget_team2, preferredMatch.team2.take(3).uppercase())
                    views.setTextViewText(R.id.widget_score2, preferredMatch.score2.ifEmpty { "0/0" })
                    views.setTextViewText(R.id.widget_overs2, preferredMatch.overs2.ifEmpty { "(0.0)" }.let { if (!it.startsWith("(")) "($it)" else it })
                    
                    views.setTextViewText(R.id.widget_status, displayStatus)
                } else if (pinnedMatchId.isNotEmpty()) {
                    intent.putExtra("MATCH_ID", pinnedMatchId)
                    val t1 = onboardingManager.widgetPinnedTeam1.first()
                    val s1 = onboardingManager.widgetPinnedScore1.first()
                    val o1 = onboardingManager.widgetPinnedOvers1.first()
                    val t2 = onboardingManager.widgetPinnedTeam2.first()
                    val s2 = onboardingManager.widgetPinnedScore2.first()
                    val o2 = onboardingManager.widgetPinnedOvers2.first()
                    val st = onboardingManager.widgetPinnedStatus.first()

                    if (t1.isNotEmpty() || t2.isNotEmpty()) {
                        views.setTextViewText(R.id.widget_team1, t1.take(3).uppercase())
                        views.setTextViewText(R.id.widget_score1, s1.ifEmpty { "0/0" })
                        views.setTextViewText(R.id.widget_overs1, o1.ifEmpty { "(0.0)" }.let { if (!it.startsWith("(")) "($it)" else it })
                        
                        views.setTextViewText(R.id.widget_team2, t2.take(3).uppercase())
                        views.setTextViewText(R.id.widget_score2, s2.ifEmpty { "0/0" })
                        views.setTextViewText(R.id.widget_overs2, o2.ifEmpty { "(0.0)" }.let { if (!it.startsWith("(")) "($it)" else it })
                        
                        views.setTextViewText(R.id.widget_status, st)
                    } else {
                        views.setTextViewText(R.id.widget_status, "NOT FOUND")
                        views.setTextViewText(R.id.widget_team1, "--")
                        views.setTextViewText(R.id.widget_score1, "-")
                        views.setTextViewText(R.id.widget_overs1, "")
                        views.setTextViewText(R.id.widget_team2, "--")
                        views.setTextViewText(R.id.widget_score2, "-")
                        views.setTextViewText(R.id.widget_overs2, "")
                    }
                } else {
                    if (rawItems.isEmpty()) {
                        // Keep previous state if network fails
                        continue
                    }
                    views.setTextViewText(R.id.widget_status, "NO LIVE MATCHES")
                    views.setTextViewText(R.id.widget_team1, "--")
                    views.setTextViewText(R.id.widget_score1, "-")
                    views.setTextViewText(R.id.widget_overs1, "")
                    views.setTextViewText(R.id.widget_team2, "--")
                    views.setTextViewText(R.id.widget_score2, "-")
                    views.setTextViewText(R.id.widget_overs2, "")
                }
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
            return Result.success()
        } catch (e: Exception) {
            // Do not clear the widget on exception to prevent flashing
            // Just return retry so WorkManager can try again later
            return Result.retry()
        }
    }
}
