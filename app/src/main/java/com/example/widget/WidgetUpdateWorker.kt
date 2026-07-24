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
            val parsedMatches = rawItems.map { com.example.data.CricketRepository.mapTitleToMatch(it.title, it.link, it.rawLiveStats, it.seriesName, it.matchTiming) }
            
            val preferredMatch = (if (pinnedMatchId.isNotEmpty()) parsedMatches.firstOrNull { it.id == pinnedMatchId } else null)
                ?: parsedMatches.firstOrNull { match -> 
                    preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) } && match.matchState == "LIVE"
                } ?: parsedMatches.firstOrNull { it.matchState == "LIVE" }
                  ?: parsedMatches.firstOrNull()

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
                } else {
                    views.setTextViewText(R.id.widget_status, "NO LIVE MATCHES")
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
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.widget_status, "ERROR")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            return Result.retry()
        }
    }
}
