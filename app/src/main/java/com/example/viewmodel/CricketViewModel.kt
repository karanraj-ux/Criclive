package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OnboardingManager
import com.example.data.CricketRepository
import com.example.data.FetchResult
import com.example.model.Match
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CricketUiState {
    object Loading : CricketUiState
    data class Success(
        val matches: List<Match>,
        val lastUpdated: String,
        val isOffline: Boolean,
        val searchQuery: String = "",
        val selectedMatchId: String? = null,
        val preferredTeams: Set<String> = emptySet(),
        val preferredPlayers: Set<String> = emptySet(),
        val idolName: String = "",
        val wallpaperUri: String = "",
        val appMode: String = "Fan Mode",
        val pinnedMatchId: String = "",
        val playerNews: String? = null
    ) : CricketUiState
    data class Error(val message: String) : CricketUiState
}

class CricketViewModel(
    private val onboardingManager: OnboardingManager,
    private val repository: CricketRepository
) : ViewModel() {
    
    
    private val _playerNews = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    
    private val _suggestedPlayers = MutableStateFlow<List<String>>(emptyList())
    val suggestedPlayers: StateFlow<List<String>> = _suggestedPlayers.asStateFlow()
    
    private val _selectedMatchId = MutableStateFlow<String?>(null)
    
    private val _fetchResult = MutableStateFlow<FetchResult>(FetchResult.Loading)
    private val _lastUpdated = MutableStateFlow("Just now")

    val isOnboardingCompleted = onboardingManager.isOnboardingCompleted
    val pipHintShown = onboardingManager.pipHintShown
    val fundingDismissed = onboardingManager.fundingDismissed
    val appOpensCount = onboardingManager.appOpensCount
    val feedbackDismissed = onboardingManager.feedbackDismissed

val uiState: StateFlow<CricketUiState> = combine(
        _fetchResult,
        combine(_searchQuery, _playerNews) { q, p -> Pair(q, p) },
        onboardingManager.preferredTeams,
        onboardingManager.preferredPlayers,
        combine(
            _selectedMatchId, 
            _lastUpdated, 
            onboardingManager.idolName,
            combine(
                onboardingManager.wallpaperUri,
                onboardingManager.appMode,
                onboardingManager.widgetPinnedMatchId
            ) { wp, mode, pinned ->
                Triple(wp, mode, pinned)
            }
        ) { id, time, idol, triple ->  
            SixTuple(id, time, idol, triple.first, triple.second, triple.third)
        }
    ) { fetchResult, queryAndNews, preferredTeams, preferredPlayers, extra ->
        val query = queryAndNews.first
        val playerNews = queryAndNews.second
        val selectedId = extra.a
        val lastUpdated = extra.b
        val idolName = extra.c
        val wallpaperUri = extra.d
        val appMode = extra.e
        val pinnedMatchId = extra.f
        
        when (fetchResult) {
            is FetchResult.Loading -> CricketUiState.Loading
            is FetchResult.Error -> CricketUiState.Error(fetchResult.message)
            is FetchResult.Success -> {
                var list = fetchResult.matches
                
                if (preferredTeams.isNotEmpty()) {
                    val preferredList = mutableListOf<Match>()
                    val otherList = mutableListOf<Match>()
                    for (match in list) {
                        val isPreferred = preferredTeams.any { pref ->
                            match.team1.contains(pref, ignoreCase = true) || match.team2.contains(pref, ignoreCase = true)
                        }
                        if (isPreferred) {
                            preferredList.add(match)
                        } else {
                            otherList.add(match)
                        }
                    }
                    list = preferredList + otherList
                }
                
                if (query.isNotBlank()) {
                    list = list.filter {
                        it.team1.contains(query, ignoreCase = true) ||
                        it.team2.contains(query, ignoreCase = true) ||
                        it.status.contains(query, ignoreCase = true)
                    }
                }
                CricketUiState.Success(
                    matches = list,
                    lastUpdated = lastUpdated,
                    isOffline = fetchResult.isOffline,
                    searchQuery = query,
                    selectedMatchId = selectedId,
                    preferredTeams = preferredTeams,
                    preferredPlayers = preferredPlayers,
                    idolName = idolName,
                    wallpaperUri = wallpaperUri,
                    appMode = appMode,
                    pinnedMatchId = pinnedMatchId,
                    playerNews = playerNews
                )
            }
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CricketUiState.Loading)

    init {
        startLiveApiFetching()
        viewModelScope.launch {
            onboardingManager.idolName.collectLatest { name ->
                fetchPlayerNews(name)
            }
        }
    }

    fun selectMatch(id: String?) {
        _selectedMatchId.value = id
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        _fetchResult.value = FetchResult.Loading
        startLiveApiFetching()
    }

    private var fetchJob: kotlinx.coroutines.Job? = null
    
    private fun startLiveApiFetching() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            repository.getLiveMatchesFlow(onboardingManager.preferredPlayers, onboardingManager.preferredTeams)
                .collect { result ->
                    _fetchResult.value = result
                    if (result is FetchResult.Success) {
                        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        _lastUpdated.value = format.format(java.util.Date())
                    }
                }
        }
    }

    fun completeOnboarding(selectedTeams: Set<String>, selectedPlayers: Set<String> = emptySet()) {
        viewModelScope.launch {
            onboardingManager.savePreferredTeams(selectedTeams)
            onboardingManager.savePreferredPlayers(selectedPlayers)
            onboardingManager.saveOnboardingCompleted(true)
        }
    }
    
    fun updatePreferredPlayers(players: Set<String>) {
        viewModelScope.launch {
            onboardingManager.savePreferredPlayers(players)
        }
    }
            
    fun updateIdolName(name: String) {
        viewModelScope.launch {
            onboardingManager.saveIdolName(name)
            fetchPlayerNews(name)
        }
    }
    
    fun fetchPlayerNews(name: String) {
        viewModelScope.launch {
            _playerNews.value = repository.getPlayerNews(name)
        }
    }
    
    fun updateWallpaperUri(uri: String) {
        viewModelScope.launch {
            onboardingManager.saveWallpaperUri(uri)
        }
    }

    fun updateAppMode(mode: String) {
        viewModelScope.launch {
            onboardingManager.saveAppMode(mode)
        }
    }

    fun dismissFunding() {
        viewModelScope.launch {
            onboardingManager.saveFundingDismissed(true)
        }
    }
    
    fun incrementAppOpens() {
        viewModelScope.launch {
            onboardingManager.incrementAppOpens()
        }
    }

    fun dismissFeedback() {
        viewModelScope.launch {
            onboardingManager.saveFeedbackDismissed(true)
        }
    }

    fun setPipHintShown(shown: Boolean) {
        viewModelScope.launch {
            onboardingManager.savePipHintShown(shown)
        }
    }

    fun fetchSuggestedPlayers(teams: Set<String>) {
        viewModelScope.launch {
            val players = repository.fetchDynamicPlayers(teams)
            _suggestedPlayers.value = players
        }
    }

    fun pinMatchToWidget(matchId: String, match: Match?, context: android.content.Context) {
        viewModelScope.launch {
            onboardingManager.saveWidgetPinnedMatchId(matchId)
            if (matchId.isNotEmpty() && match != null) {
                onboardingManager.saveWidgetPinnedMatchDetails(
                    match.team1, match.score1, match.overs1,
                    match.team2, match.score2, match.overs2,
                    match.matchState
                )
                
                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, com.example.widget.MatchWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (appWidgetId in appWidgetIds) {
                    val views = android.widget.RemoteViews(context.packageName, com.example.R.layout.widget_layout)
                    val intent = android.content.Intent(context, com.example.MainActivity::class.java)
                    intent.putExtra("MATCH_ID", matchId)
                    
                    views.setTextViewText(com.example.R.id.widget_team1, if (match.team1.isNotEmpty()) match.team1.take(3).uppercase() else "--")
                    views.setTextViewText(com.example.R.id.widget_score1, match.score1.ifEmpty { if (match.team1.isNotEmpty()) "0/0" else "-" })
                    views.setTextViewText(com.example.R.id.widget_overs1, match.overs1.ifEmpty { if (match.team1.isNotEmpty()) "(0.0)" else "" }.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it })
                    
                    views.setTextViewText(com.example.R.id.widget_team2, if (match.team2.isNotEmpty()) match.team2.take(3).uppercase() else "--")
                    views.setTextViewText(com.example.R.id.widget_score2, match.score2.ifEmpty { if (match.team2.isNotEmpty()) "0/0" else "-" })
                    views.setTextViewText(com.example.R.id.widget_overs2, match.overs2.ifEmpty { if (match.team2.isNotEmpty()) "(0.0)" else "" }.let { if (it.isNotEmpty() && !it.startsWith("(")) "($it)" else it })
                    
                    views.setTextViewText(com.example.R.id.widget_status, match.matchState.ifEmpty { "NO LIVE MATCHES" })
                    
                    val pendingIntent = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
                    views.setOnClickPendingIntent(com.example.R.id.widget_root, pendingIntent)
                    
                    val refreshIntent = android.content.Intent(context, com.example.widget.MatchWidgetProvider::class.java).apply {
                        action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                    }
                    val refreshPendingIntent = android.app.PendingIntent.getBroadcast(
                        context, appWidgetId, refreshIntent, 
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(com.example.R.id.widget_refresh, refreshPendingIntent)
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } else if (matchId.isEmpty()) {
                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, com.example.widget.MatchWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (appWidgetId in appWidgetIds) {
                    val views = android.widget.RemoteViews(context.packageName, com.example.R.layout.widget_layout)
                    val intent = android.content.Intent(context, com.example.MainActivity::class.java)
                    views.setTextViewText(com.example.R.id.widget_team1, "--")
                    views.setTextViewText(com.example.R.id.widget_score1, "-")
                    views.setTextViewText(com.example.R.id.widget_overs1, "")
                    views.setTextViewText(com.example.R.id.widget_team2, "--")
                    views.setTextViewText(com.example.R.id.widget_score2, "-")
                    views.setTextViewText(com.example.R.id.widget_overs2, "")
                    views.setTextViewText(com.example.R.id.widget_status, "LOADING...")
                    
                    val pendingIntent = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
                    views.setOnClickPendingIntent(com.example.R.id.widget_root, pendingIntent)
                    
                    val refreshIntent = android.content.Intent(context, com.example.widget.MatchWidgetProvider::class.java).apply {
                        action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                    }
                    val refreshPendingIntent = android.app.PendingIntent.getBroadcast(
                        context, appWidgetId, refreshIntent, 
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(com.example.R.id.widget_refresh, refreshPendingIntent)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.widget.WidgetUpdateWorker>().build()
            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork("WidgetUpdate", androidx.work.ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}

data class FiveTuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
data class SixTuple<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)
