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
        val appMode: String = "Standard"
    ) : CricketUiState
    data class Error(val message: String) : CricketUiState
}

class CricketViewModel(
    private val onboardingManager: OnboardingManager,
    private val repository: CricketRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    
    private val _suggestedPlayers = MutableStateFlow<List<String>>(emptyList())
    val suggestedPlayers: StateFlow<List<String>> = _suggestedPlayers.asStateFlow()
    
    private val _selectedMatchId = MutableStateFlow<String?>(null)
    
    private val _fetchResult = MutableStateFlow<FetchResult>(FetchResult.Loading)
    private val _lastUpdated = MutableStateFlow("Just now")

    val isOnboardingCompleted = onboardingManager.isOnboardingCompleted
    val pipHintShown = onboardingManager.pipHintShown

val uiState: StateFlow<CricketUiState> = combine(
        _fetchResult,
        _searchQuery,
        onboardingManager.preferredTeams,
        onboardingManager.preferredPlayers,
        combine(
            _selectedMatchId, 
            _lastUpdated, 
            onboardingManager.idolName,
            onboardingManager.wallpaperUri,
            onboardingManager.appMode
        ) { id, time, idol, wp, mode ->  
            FiveTuple(id, time, idol, wp, mode)
        }
    ) { fetchResult, query, preferredTeams, preferredPlayers, extra ->
        val selectedId = extra.a
        val lastUpdated = extra.b
        val idolName = extra.c
        val wallpaperUri = extra.d
        val appMode = extra.e
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
                    appMode = appMode
                )
            }
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CricketUiState.Loading)

    init {
        startLiveApiFetching()
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
}

data class FiveTuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
