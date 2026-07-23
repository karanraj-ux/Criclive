package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Match
import com.example.viewmodel.CricketUiState
import com.example.viewmodel.CricketViewModel
import kotlin.math.abs

import com.example.ui.screens.*
import com.example.ui.components.*

import androidx.compose.ui.platform.LocalContext
import com.example.util.UpdateManager
import com.example.util.AppUpdate
import com.example.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricketApp(
    viewModel: CricketViewModel,
    isPipMode: Boolean = false,
    onEnterPip: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState(initial = false)
    val pipHintShown by viewModel.pipHintShown.collectAsState(initial = false)
    val suggestedPlayers by viewModel.suggestedPlayers.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showFanModeDialog by remember { mutableStateOf(false) }
    var forceOnboarding by remember { mutableStateOf(false) }
    var appUpdate by remember { mutableStateOf<AppUpdate?>(null) }

    LaunchedEffect(Unit) {
        // In a real open-source app, replace getMockUpdate() with checkForUpdate()
        val update = UpdateManager.getMockUpdate()
        if (update != null && update.isUpdateAvailable && update.version != BuildConfig.VERSION_NAME) {
            appUpdate = update
        }
    }

    if (appUpdate != null && !isPipMode) {
        AlertDialog(
            onDismissRequest = { appUpdate = null },
            title = { Text("Update Available", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Version ${appUpdate?.version} is now available.", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(appUpdate?.releaseNotes ?: "", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { 
                    appUpdate?.downloadUrl?.let { url ->
                        UpdateManager.downloadAndInstallUpdate(context, url)
                    }
                    appUpdate = null 
                }) {
                    Text("Download & Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { appUpdate = null }) {
                    Text("Later")
                }
            }
        )
    }

    if (!isOnboardingCompleted || forceOnboarding) {
        val initialMode = if (uiState is CricketUiState.Success) (uiState as CricketUiState.Success).appMode else "Standard"
        OnboardingScreen(
            onComplete = { teams, players, mode ->
                viewModel.updateAppMode(mode)
                viewModel.completeOnboarding(teams, players)
                forceOnboarding = false
            },
            suggestedPlayers = suggestedPlayers,
            onTeamsSelected = { teams ->
                viewModel.fetchSuggestedPlayers(teams)
            },
            initialMode = initialMode
        )
    } else {
        when (val state = uiState) {
            is CricketUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is CricketUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is CricketUiState.Success -> {
                if (state.selectedMatchId != null) {
                    val match = state.matches.find { it.id == state.selectedMatchId }
                    if (match != null) {
                        if (isPipMode) {
                            PipScoreCard(match = match)
                        } else {
                            MatchDetailScreen(
                                match = match,
                                isPreferred = state.preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) },
                                pipHintShown = pipHintShown,
                                onDismissPipHint = { viewModel.setPipHintShown(true) },
                                onBack = { viewModel.selectMatch(null) },
                                onEnterPip = onEnterPip
                            )
                        }
                    } else {
                        viewModel.selectMatch(null)
                    }
                } else {
                    MatchListScreen(
                        state = state,
                        onMatchClick = { viewModel.selectMatch(it.id) },
                        onRefresh = { viewModel.refresh() },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onSettingsClick = { showSettings = true },
                        onFanModeClick = { showFanModeDialog = true }
                    )
                }
                
                if (showFanModeDialog) {
                    FanModeBottomSheet(
                        state = state,
                        onDismiss = { showFanModeDialog = false },
                        onSaveIdol = { viewModel.updateIdolName(it) },
                        onSaveWallpaper = { viewModel.updateWallpaperUri(it) },
                        onEnableFanMode = { viewModel.updateAppMode("Fan Mode") }
                    )
                }

                if (showSettings) {
                    SettingsBottomSheet(
                        state = state,
                        onDismiss = { showSettings = false },
                        onSaveIdol = { viewModel.updateIdolName(it) },
                        onSaveWallpaper = { viewModel.updateWallpaperUri(it) },
                        onEditPreferences = { 
                            showSettings = false
                            forceOnboarding = true 
                        },
                        onSaveMode = { viewModel.updateAppMode(it) }
                    )
                }
            }
        }
    }
}

