package com.example.ui
import android.net.Uri

import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.MatchUpdateWorker
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.browser.customtabs.CustomTabsIntent
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
    val fundingDismissed by viewModel.fundingDismissed.collectAsState(initial = false)
    val appOpensCount by viewModel.appOpensCount.collectAsState(initial = 0)
    val feedbackDismissed by viewModel.feedbackDismissed.collectAsState(initial = false)
    val suggestedPlayers by viewModel.suggestedPlayers.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showFanModeDialog by remember { mutableStateOf(false) }
    var forceOnboarding by remember { mutableStateOf(false) }
    var appUpdate by remember { mutableStateOf<AppUpdate?>(null) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        viewModel.incrementAppOpens()

        // Checking for a real update from GitHub Releases
        val update = UpdateManager.checkForUpdate()
        if (update != null && update.isUpdateAvailable && update.version != BuildConfig.VERSION_NAME) {
            appUpdate = update
        }
    }

    // Feedback Dialog Logic
    if (appOpensCount >= 2 && !feedbackDismissed && !isPipMode) {
        var showFeedbackDialog by remember { mutableStateOf(true) }
        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showFeedbackDialog = false 
                    viewModel.dismissFeedback()
                },
                title = { Text("Next Phase Plan: Local Scorer", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Are you enjoying CricZen? We are planning to add a manual easy scorer for local tournaments (like gully cricket) with sharing options.", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Would you find this feature useful?", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        showFeedbackDialog = false
                        viewModel.dismissFeedback()
                        // Optional: trigger some analytics or open play store if positive
                    }) {
                        Text("Yes, I'd love it!")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showFeedbackDialog = false
                        viewModel.dismissFeedback()
                    }) {
                        Text("No, keep it simple")
                    }
                }
            )
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

    Box(modifier = Modifier.fillMaxSize()) {
        val showOnboarding = !isOnboardingCompleted || forceOnboarding
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showOnboarding) Modifier.blur(12.dp) else Modifier)
        ) {
            when (val state = uiState) {
                is CricketUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is CricketUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
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
                    if (state.selectedNewsUrl != null) {
                        // Launch Chrome Custom Tabs instantly and clear the selection
                        val context = LocalContext.current
                        androidx.compose.runtime.LaunchedEffect(state.selectedNewsUrl) {
                            try {
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                customTabsIntent.launchUrl(context, Uri.parse(state.selectedNewsUrl))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            viewModel.updateSelectedNewsUrl(null)
                        }
                        
                        // Show a temporary loading or empty state while Chrome opens over it
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (state.selectedMatchId != null) {
                        val match = state.matches.find { it.id == state.selectedMatchId }
                        if (match != null) {
                            if (isPipMode) {
                                PipScoreCard(match = match)
                            } else {
                                MatchDetailScreen(
                                    match = match,
                                    isPreferred = state.preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) },
                                    pipHintShown = pipHintShown,
                                    pinnedMatchId = state.pinnedMatchId,
                                    playerNews = state.playerNews,
                                    onDismissPipHint = { viewModel.setPipHintShown(true) },
                                    onBack = { viewModel.selectMatch(null) },
                                    onEnterPip = onEnterPip,
                                    onPinToWidget = {
                                        val newPinnedId = if (state.pinnedMatchId == match.id) "" else match.id
                                        viewModel.pinMatchToWidget(newPinnedId, match, context)
                                    },
                                    onNewsClick = { viewModel.updateSelectedNewsUrl(it) }
                                )
                            }
                        } else {
                            viewModel.selectMatch(null)
                        }
                    } else {
                        MatchListScreen(
                            state = state,
                            onMatchClick = { viewModel.selectMatch(it.id) },
                            onPinClick = { match -> 
                                val newPinnedId = if (state.pinnedMatchId == match.id) "" else match.id
                                viewModel.pinMatchToWidget(newPinnedId, match, context)
                            },
                            onRefresh = { viewModel.refresh() },
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onSettingsClick = { showSettings = true },
                            onFanModeClick = { showFanModeDialog = true },
                            onToggleMode = { viewModel.updateAppMode(if (state.appMode == "Fan Mode") "Standard" else "Fan Mode") }
                        )
                    }
                    
                    if (showFanModeDialog) {
                        FanModeBottomSheet(
                            state = state,
                            onDismiss = { showFanModeDialog = false },
                            onSaveIdol = { viewModel.updateIdolName(it) },
                            onSaveWallpaper = { viewModel.updateWallpaperUri(it, context) },
                            onEnableFanMode = { viewModel.updateAppMode("Fan Mode") }
                        )
                    }
                    if (showSettings) {
                        SettingsBottomSheet(
                            state = state,
                            onDismiss = { showSettings = false },
                            onSaveIdol = { viewModel.updateIdolName(it) },
                            onSaveWallpaper = { viewModel.updateWallpaperUri(it, context) },
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
        
        if (showOnboarding) {
            val initialMode = if (uiState is CricketUiState.Success) (uiState as CricketUiState.Success).appMode else "Fan Mode"
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
        }
    }
}

