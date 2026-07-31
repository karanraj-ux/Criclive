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
            title = { Text("App Modes", fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Fan Mode:", fontWeight = FontWeight.Bold)
                    Text("Get a personalized layout with your favorite player's wallpaper, quick access to top stats, and customized themes.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Standard Mode:", fontWeight = FontWeight.Bold)
                    Text("A clean, traditional list of all cricket matches without extra visuals or wallpapers.", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeTooltip = false }) {
                    Text("Got it")
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
                title = { Text("CricLive", fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)) },
                actions = {
                    IconButton(onClick = { showModeTooltip = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "Mode Info", tint = Color(0xFF6B7280))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Mode Toggle Switch
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (state.appMode == "Fan Mode") 
                                    Brush.horizontalGradient(listOf(Color(0xFFFF007A), Color(0xFF7A00FF)))
                                else 
                                    Brush.horizontalGradient(listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB)))
                            )
                            .clickable { onToggleMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (state.appMode == "Fan Mode") "Fan Mode" else "Standard", 
                            color = if (state.appMode == "Fan Mode") Color.White else Color(0xFF374151), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF111827))
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF111827))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
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
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else Color(0xFF374151)
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
                placeholder = { Text("Search matches, teams...", color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF374151)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827)
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Color(0xFF9333EA), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Widgets, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Add the Home Screen Widget!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                                    Text("Get live scores directly on your home screen without opening the app.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF581C87))
                                }
                                IconButton(onClick = { showWidgetBanner = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF6B21A8))
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
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFF6B7280))
                                    ) {
                                        Text(
                                            text = player,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
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
                            color = if (state.isOffline) Color(0xFFB91C1C) else Color(0xFF047857)
                        )
                        Text(
                            text = "Updated: ${state.lastUpdated}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                    }
                }

                if (matchesToShow.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No matches found.", color = Color(0xFF374151), fontWeight = FontWeight.Medium)
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

