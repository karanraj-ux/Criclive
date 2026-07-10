package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.api.RssParser
import com.example.model.Match
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Can handle custom actions like manual refresh here
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Show loading state first
            views.setTextViewText(R.id.widget_status, "LOADING...")
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Fetch latest data
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val rawTitles = RssParser.fetchLiveMatches()
                    val matchTitle = rawTitles.firstOrNull { it.contains(" * ") || it.contains(" v ") }
                    
                    withContext(Dispatchers.Main) {
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
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_status, "ERROR")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }
    }
}
