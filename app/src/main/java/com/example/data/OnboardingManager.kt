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
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
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
        preferences[APP_MODE] ?: "Standard"
    }


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
}
