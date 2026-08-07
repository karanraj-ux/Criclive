package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import com.example.worker.MatchUpdateWorker
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.ui.theme.*

class MatchGlanceWidget : GlanceAppWidget() {
    
    companion object {
        val TEAM1_KEY = stringPreferencesKey("team1")
        val SCORE1_KEY = stringPreferencesKey("score1")
        val OVERS1_KEY = stringPreferencesKey("overs1")
        val TEAM2_KEY = stringPreferencesKey("team2")
        val SCORE2_KEY = stringPreferencesKey("score2")
        val OVERS2_KEY = stringPreferencesKey("overs2")
        val STATUS_KEY = stringPreferencesKey("status")
        val MATCH_ID_KEY = stringPreferencesKey("matchId")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val team1 = prefs[TEAM1_KEY] ?: "--"
            val score1 = prefs[SCORE1_KEY] ?: "-"
            val overs1 = prefs[OVERS1_KEY] ?: ""
            val team2 = prefs[TEAM2_KEY] ?: "--"
            val score2 = prefs[SCORE2_KEY] ?: "-"
            val overs2 = prefs[OVERS2_KEY] ?: ""
            val status = prefs[STATUS_KEY] ?: "NO LIVE MATCHES"
            val matchId = prefs[MATCH_ID_KEY] ?: ""
            
            val isLive = status.contains("LIVE", true) || status.contains("IN PROGRESS", true)

            // Workaround for creating intent dynamically
            val intent = Intent(context, MainActivity::class.java).apply {
                if (matchId.isNotEmpty()) {
                    putExtra("MATCH_ID", matchId)
                }
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(androidx.glance.color.ColorProvider(day = PremiumSurface, night = DeepCharcoal))
                    .padding(12.dp)
                    .clickable(actionStartActivity(intent))
            ) {
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (isLive) {
                        Spacer(modifier = GlanceModifier.size(6.dp).background(Color.Red))
                    }
                    Text(
                        text = status,
                        modifier = GlanceModifier.padding(start = 6.dp).defaultWeight(),
                        style = TextStyle(
                            color = androidx.glance.color.ColorProvider(day = if (isLive) Color.Red else PremiumTextDark, night = if (isLive) Color.Red else Color.White),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = GlanceModifier
                            .background(Color(0xFFE0E0E0)) // Light gray background
                            .cornerRadius(12.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable(actionRunCallback<RefreshAction>()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(android.R.drawable.ic_popup_sync),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier.size(12.dp)
                        )
                        Text(
                            text = "Tap to refresh",
                            modifier = GlanceModifier.padding(start = 4.dp),
                            style = TextStyle(
                                color = androidx.glance.color.ColorProvider(day = Color.Black, night = Color.Black),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(text = team1, style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextDark, night = Color.White), fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        Text(text = score1, style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextDark, night = Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                        Text(text = overs1, style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextMedium, night = Color(0xFFAEAEC0)), fontSize = 10.sp))
                    }
                    
                    Text(text = "VS", modifier = GlanceModifier.padding(horizontal = 8.dp), style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextMedium, night = Color(0xFFAEAEC0)), fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    
                    Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.End) {
                        Text(text = team2, style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextDark, night = Color.White), fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        Text(text = score2, style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextDark, night = Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                        Text(text = overs2, style = TextStyle(color = androidx.glance.color.ColorProvider(day = PremiumTextMedium, night = Color(0xFFAEAEC0)), fontSize = 10.sp))
                    }
                }
            }
        }
    }
}



class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<MatchUpdateWorker>().build()
        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork("WidgetUpdate", androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
    }
}
