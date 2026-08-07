package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class OnboardingManager(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PREFERRED_TEAMS = stringSetPreferencesKey("preferred_teams")
        val PREFERRED_PLAYERS = stringSetPreferencesKey("preferred_players")
        val PIP_HINT_SHOWN = booleanPreferencesKey("pip_hint_shown")
        val IDOL_NAME = stringPreferencesKey("idol_name")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val APP_MODE = stringPreferencesKey("app_mode")
        val FUNDING_DISMISSED = booleanPreferencesKey("funding_dismissed")
        val APP_OPENS_COUNT = intPreferencesKey("app_opens_count")
        val FEEDBACK_DISMISSED = booleanPreferencesKey("feedback_dismissed")
        val WIDGET_PINNED_MATCH_ID = stringPreferencesKey("widget_pinned_match_id")
        val WIDGET_PINNED_TEAM1 = stringPreferencesKey("widget_pinned_team1")
        val WIDGET_PINNED_SCORE1 = stringPreferencesKey("widget_pinned_score1")
        val WIDGET_PINNED_OVERS1 = stringPreferencesKey("widget_pinned_overs1")
        val WIDGET_PINNED_TEAM2 = stringPreferencesKey("widget_pinned_team2")
        val WIDGET_PINNED_SCORE2 = stringPreferencesKey("widget_pinned_score2")
        val WIDGET_PINNED_OVERS2 = stringPreferencesKey("widget_pinned_overs2")
        val WIDGET_PINNED_STATUS = stringPreferencesKey("widget_pinned_status")
        val NOTIFIED_MATCHES = stringSetPreferencesKey("notified_matches")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val notifiedMatches: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[NOTIFIED_MATCHES] ?: emptySet()
    }

    val preferredTeams: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PREFERRED_TEAMS] ?: emptySet()
    }
    val preferredPlayers: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PREFERRED_PLAYERS] ?: emptySet()
    }

    val pipHintShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PIP_HINT_SHOWN] ?: false
    }
    
    val idolName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[IDOL_NAME] ?: ""
    }

    val wallpaperUri: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WALLPAPER_URI] ?: ""
    }
    val appMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_MODE] ?: "Fan Mode"
    }

    val fundingDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FUNDING_DISMISSED] ?: false
    }
    val appOpensCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[APP_OPENS_COUNT] ?: 0
    }
    val feedbackDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FEEDBACK_DISMISSED] ?: false
    }


    val widgetPinnedMatchId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIDGET_PINNED_MATCH_ID] ?: ""
    }

    val widgetPinnedStatus: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_STATUS] ?: "" }
    val widgetPinnedTeam1: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_TEAM1] ?: "" }
    val widgetPinnedScore1: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_SCORE1] ?: "" }
    val widgetPinnedOvers1: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_OVERS1] ?: "" }
    val widgetPinnedTeam2: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_TEAM2] ?: "" }
    val widgetPinnedScore2: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_SCORE2] ?: "" }
    val widgetPinnedOvers2: Flow<String> = context.dataStore.data.map { preferences -> preferences[WIDGET_PINNED_OVERS2] ?: "" }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun savePreferredTeams(teams: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_TEAMS] = teams
        }
    }

    suspend fun savePreferredPlayers(players: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_PLAYERS] = players
        }
    }

    suspend fun savePipHintShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PIP_HINT_SHOWN] = shown
        }
    }
    
    suspend fun saveIdolName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[IDOL_NAME] = name
        }
    }

    suspend fun saveWallpaperUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[WALLPAPER_URI] = uri
        }
    }

    suspend fun saveAppMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_MODE] = mode
        }
    }
    
    suspend fun saveFundingDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FUNDING_DISMISSED] = dismissed
        }
    }
    
    suspend fun incrementAppOpens() {
        context.dataStore.edit { preferences ->
            val current = preferences[APP_OPENS_COUNT] ?: 0
            preferences[APP_OPENS_COUNT] = current + 1
        }
    }

    suspend fun saveFeedbackDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FEEDBACK_DISMISSED] = dismissed
        }
    }

    suspend fun saveWidgetPinnedMatchId(matchId: String) {
        context.dataStore.edit { preferences ->
            preferences[WIDGET_PINNED_MATCH_ID] = matchId
        }
    }

    suspend fun saveWidgetPinnedMatchDetails(
        team1: String, score1: String, overs1: String,
        team2: String, score2: String, overs2: String,
        status: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[WIDGET_PINNED_TEAM1] = team1
            preferences[WIDGET_PINNED_SCORE1] = score1
            preferences[WIDGET_PINNED_OVERS1] = overs1
            preferences[WIDGET_PINNED_TEAM2] = team2
            preferences[WIDGET_PINNED_SCORE2] = score2
            preferences[WIDGET_PINNED_OVERS2] = overs2
            preferences[WIDGET_PINNED_STATUS] = status
        }
    }

    suspend fun addNotifiedMatch(matchKey: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[NOTIFIED_MATCHES] ?: emptySet()
            preferences[NOTIFIED_MATCHES] = current + matchKey
        }
    }
}
