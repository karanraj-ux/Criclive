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
            val rawItems = RssParser.fetchLiveMatches()
            val matchTitle = rawItems.firstOrNull { it.title.contains(" * ") || it.title.contains(" v ") }?.title

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout)
                
                val intent = Intent(applicationContext, MainActivity::class.java)
                if (matchTitle != null) {
                    val parts = matchTitle.split(" v ")
                    val team1Full = parts.getOrNull(0)?.trim() ?: ""
                    val team2Full = parts.getOrNull(1)?.trim() ?: ""
                    val scoreRegex = Regex("""(\d+(?:/\d+)?(?:\s*(?:d|\*))?)$""")
                    
                    var t1Name = team1Full
                    var t1Score = ""
                    val m1 = scoreRegex.find(team1Full)
                    if (m1 != null) {
                        t1Score = m1.groupValues[1].trim()
                        t1Name = team1Full.substring(0, m1.range.first).trim()
                    }
                    
                    var t2Name = team2Full
                    var t2Score = ""
                    val m2 = scoreRegex.find(team2Full)
                    if (m2 != null) {
                        t2Score = m2.groupValues[1].trim()
                        t2Name = team2Full.substring(0, m2.range.first).trim()
                    }
                    if (t2Name.contains(" at ")) {
                        t2Name = t2Name.split(" at ")[0].trim()
                    }
                    
                    val id = (t1Name + t2Name).hashCode().toString()
                    intent.putExtra("MATCH_ID", id)
                    
                    views.setTextViewText(R.id.widget_team1, t1Name.take(3).uppercase())
                    views.setTextViewText(R.id.widget_score1, t1Score.ifEmpty { "0/0" })
                    views.setTextViewText(R.id.widget_overs1, "")
                    views.setTextViewText(R.id.widget_team2, t2Name.take(3).uppercase())
                    views.setTextViewText(R.id.widget_score2, t2Score.ifEmpty { "0/0" })
                    views.setTextViewText(R.id.widget_overs2, "")
                    views.setTextViewText(R.id.widget_status, "LIVE")
                } else {
                    views.setTextViewText(R.id.widget_status, "NO LIVE MATCHES")
                }
                
                val pendingIntent = PendingIntent.getActivity(applicationContext, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
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
