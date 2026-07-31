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
import com.example.data.CricketConstants
import kotlin.math.abs
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.screens.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (Set<String>, Set<String>, String) -> Unit,
    suggestedPlayers: List<String>,
    onTeamsSelected: (Set<String>) -> Unit,
    initialMode: String
) {
    var step by remember { mutableStateOf(1) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    
    val internationalTeams = CricketConstants.INTERNATIONAL_TEAMS
    val t20Leagues = CricketConstants.T20_LEAGUES
    
    val selectedTeams = remember { mutableStateListOf<String>() }
    val selectedPlayers = remember { mutableStateListOf<String>() }
    
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        if (step == 1) {
            Text(
                text = "Choose Your Experience",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "How do you want to follow the game?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMode = "Standard" },
                colors = CardDefaults.cardColors(containerColor = if (selectedMode == "Standard") MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                border = BorderStroke(1.dp, if (selectedMode == "Standard") MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMode == "Standard", onClick = { selectedMode = "Standard" })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Standard Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
                        Text("Just the matches, clean and simple.", color = Color(0xFF374151), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedMode = "Fan Mode" },
                colors = CardDefaults.cardColors(containerColor = if (selectedMode == "Fan Mode") MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                border = BorderStroke(1.dp, if (selectedMode == "Fan Mode") MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMode == "Fan Mode", onClick = { selectedMode = "Fan Mode" })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Fan Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
                        Text("Follow specific players, add your Idol wallpaper, and immerse yourself.", color = Color(0xFF374151), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { step = 2 },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
        } else if (step == 2) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { step = 1 }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Pick Your Teams",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select teams for personalized 'My Teams' feed.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("International", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Leagues", fontWeight = FontWeight.Bold) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val currentList: List<String> = if (selectedTab == 0) internationalTeams else t20Leagues
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = currentList, key = { it }) { team: String ->
                    val isSelected = selectedTeams.contains(team)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) selectedTeams.remove(team) else selectedTeams.add(team)
                            },
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = team,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF111827),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onTeamsSelected(selectedTeams.toSet())
                    step = 3
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Next: Favorite Players", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else if (step == 3) {
            var playerSearchQuery by remember { mutableStateOf("") }
            var customPlayerInput by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { step = 2 }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Pick Favorite Players",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track your favorite players across all matches.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = playerSearchQuery,
                onValueChange = { playerSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search players...", color = Color(0xFF6B7280)) },
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
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customPlayerInput,
                    onValueChange = { customPlayerInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add custom player...", color = Color(0xFF6B7280)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val newPlayer = customPlayerInput.trim()
                        if (newPlayer.isNotBlank() && !selectedPlayers.contains(newPlayer)) {
                            selectedPlayers.add(newPlayer)
                        }
                        customPlayerInput = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (suggestedPlayers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val filteredPlayers = suggestedPlayers.filter { it.contains(playerSearchQuery, ignoreCase = true) }
                
                val globalTopPlayers = CricketConstants.GLOBAL_STARS
                
                val pinned = filteredPlayers.filter { selectedPlayers.contains(it) }
                val tops = filteredPlayers.filter { globalTopPlayers.contains(it) && !selectedPlayers.contains(it) }
                val others = filteredPlayers.filter { !selectedPlayers.contains(it) && !globalTopPlayers.contains(it) }.sorted()
                
                val finalList = pinned + tops + others

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(finalList, key = { it }) { player ->
                        val isSelected = selectedPlayers.contains(player)
                        val isTop = globalTopPlayers.contains(player) && !isSelected
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedPlayers.remove(player) else selectedPlayers.add(player)
                                },
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF3F4F6)),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF111827),
                                    modifier = Modifier.weight(1f)
                                )
                                if (isTop) {
                                    Icon(Icons.Default.Star, contentDescription = "Top Player", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { step = 4 },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // Step 4 - PiP & Finish
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { step = 3 }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Did you know?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Picture-in-Picture (PiP)", 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "You can minimize any match into a floating widget while you use other apps! Just click 'Minimize to Floating Player' inside any match.", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onComplete(selectedTeams.toSet(), selectedPlayers.toSet(), selectedMode) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Finish Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

