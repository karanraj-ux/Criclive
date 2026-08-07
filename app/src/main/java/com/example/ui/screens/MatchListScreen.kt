package com.example.ui.screens

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
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    state: CricketUiState.Success,
    onMatchClick: (Match) -> Unit,
    onPinClick: (Match) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onFanModeClick: () -> Unit,
    onToggleMode: () -> Unit
) {
    val tabs = listOf("All Matches", "My Teams")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showModeTooltip by remember { mutableStateOf(false) }

    if (showModeTooltip) {
        AlertDialog(
            onDismissRequest = { showModeTooltip = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    Icons.Default.Star, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { 
                Text("App Modes", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) 
            },
            text = { 
                Column {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Fan Mode", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("A premium, personalized layout with your idol's wallpaper, quick stat access, and custom themes.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Standard Mode", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("A clean, minimalist traditional list of all cricket matches. Pure focus, zero visual clutter.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showModeTooltip = false },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val matchesToShow = if (selectedTabIndex == 1 && state.preferredTeams.isNotEmpty()) {
        state.matches.filter { match ->
            state.preferredTeams.any { pref ->
                match.team1.contains(pref, ignoreCase = true) || match.team2.contains(pref, ignoreCase = true)
            }
        }
    } else {
        state.matches
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        androidx.compose.material3.Text("CricZen", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { showModeTooltip = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "Mode Info", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Mode Toggle Switch
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (state.appMode == "Fan Mode") 
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                else 
                                    Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outline))
                            )
                            .clickable { onToggleMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (state.appMode == "Fan Mode") "Fan Mode" else "Standard", 
                            color = if (state.appMode == "Fan Mode") MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( scrolledContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search matches, teams...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            var showWidgetBanner by remember { mutableStateOf(true) }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showWidgetBanner) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Add the Home Screen Widget!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text("Get live scores directly on your home screen without opening the app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                IconButton(onClick = { showWidgetBanner = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
                if (state.appMode == "Fan Mode") {
                    item {
                        IdolHeader(state.idolName, state.wallpaperUri, onClick = onFanModeClick)
                    }
                    

                    
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Text(
                                "FAN FAVORITES", 
                                style = MaterialTheme.typography.labelMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val topPlayers = if (state.preferredPlayers.isNotEmpty()) {
                                    state.preferredPlayers.toList()
                                } else {
                                    listOf("Virat Kohli", "Rohit Sharma", "MS Dhoni", "Jasprit Bumrah", "Suryakumar Yadav", "Hardik Pandya")
                                }
                                topPlayers.forEach { player ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                                    ) {
                                        Text(
                                            text = player,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isOffline) "OFFLINE (CACHED DATA)" else "LIVE UPDATES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (state.isOffline) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Updated: ${state.lastUpdated}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (matchesToShow.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No matches found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    items(matchesToShow, key = { it.id }) { match ->
                        val isPreferred = state.preferredTeams.any { match.team1.contains(it, true) || match.team2.contains(it, true) }
                        MatchCard(
                            match = match, 
                            isPreferred = isPreferred, 
                            isPinned = match.id == state.pinnedMatchId,
                            onPinClick = { onPinClick(match) },
                            onClick = { onMatchClick(match) }
                        )
                    }
                }
            }
        }
    }
}

