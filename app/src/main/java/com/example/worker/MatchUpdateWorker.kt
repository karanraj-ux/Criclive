package com.example.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.widget.MatchGlanceWidget
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.api.RssParser
import com.example.util.toAbbreviation
import kotlinx.coroutines.flow.first

class MatchUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(MatchGlanceWidget::class.java)
            

            
            val onboardingManager = com.example.data.OnboardingManager(applicationContext)
            val preferredTeams = onboardingManager.preferredTeams.first()
            val preferredPlayers = onboardingManager.preferredPlayers.first()
            val pinnedMatchId = onboardingManager.widgetPinnedMatchId.first()
            
            val repository = com.example.data.CricketRepository(applicationContext)
            repository.syncMatches(preferredPlayers, preferredTeams)
            
            val dao = com.example.data.AppDatabase.getDatabase(applicationContext).matchDao()
            val parsedMatches = dao.getAllMatches().map { it.toMatch() }

            
            
            val notifiedMatches = onboardingManager.notifiedMatches.first()
            val preferredMatchesForNotification = parsedMatches.filter { match ->
                preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) }
            }
            
            for (match in preferredMatchesForNotification) {
                val stateKey = "${match.id}_${match.matchState}"
                if (!notifiedMatches.contains(stateKey)) {
                    // Send notification
                    if (match.matchState == "LIVE" || match.matchState == "COMPLETED" || (match.matchState == "UPCOMING" && match.matchTiming.isNotBlank())) {
                        sendNotification(applicationContext, match)
                        onboardingManager.addNotifiedMatch(stateKey)
                    }
                }
            }

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
                        val newOvers1 = preferredMatch.overs1.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it }
                        val newTeam2 = preferredMatch.team2.toAbbreviation()
                        val newScore2 = preferredMatch.score2.ifEmpty { if (preferredMatch.team2.isNotEmpty()) "0/0" else "-" }
                        val newOvers2 = preferredMatch.overs2.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it }
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

    private fun sendNotification(context: Context, match: com.example.model.Match) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }

        val channelId = "criczen_matches"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cricket Match Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for your preferred teams"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("MATCH_ID", match.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, match.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "${match.team1} vs ${match.team2}"
        val content = if (match.matchState == "LIVE") {
            if (match.score1.isNotBlank() && match.score2.isNotBlank()) {
                "${match.score1} ${match.overs1} vs ${match.score2} ${match.overs2} - ${match.status}"
            } else if (match.score1.isNotBlank()) {
                "${match.score1} ${match.overs1} - ${match.status}"
            } else {
                "Match is LIVE: ${match.status}"
            }
        } else if (match.matchState == "UPCOMING") {
            "Upcoming Match: ${match.matchTiming}"
        } else {
            match.status
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_sync) // We should use a better icon if available, maybe the launcher icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(match.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Permission denied
        }
    }
}
