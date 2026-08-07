package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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

@Composable
fun MatchCard(match: Match, isPreferred: Boolean, isPinned: Boolean = false, onPinClick: (() -> Unit)? = null, onClick: () -> Unit) {
    var showPinInfo by remember { mutableStateOf(false) }

    if (showPinInfo) {
        AlertDialog(
            onDismissRequest = { showPinInfo = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    Icons.Default.PushPin, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Widget Pinning", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
            text = { 
                Text(
                    "Pin this match to display its live score directly on your home screen widget.\n\nOnly one match can be pinned at a time.",
                    fontSize = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPinInfo = false },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val isLive = match.matchState == "LIVE"
    
    // India matches get the Tricolor border, otherwise just a thick primary border for preferred teams
    val isIndiaMatch = match.team1.contains("India", true) || match.team2.contains("India", true)
    
    val borderStroke = if (isIndiaMatch && isPreferred) {
        BorderStroke(3.dp, Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
    } else if (isPreferred) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant) // Even darker gray for standard border
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = borderStroke
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // League / Tournament & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // League / Tournament Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Tournament",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (match.seriesName.isNotBlank()) match.seriesName else "Cricket Championship",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onPinClick != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPinned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable { onPinClick() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                    contentDescription = "Pin to Widget",
                                    tint = if (isPinned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPinned) "Pinned" else "Pin",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPinned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { showPinInfo = true },
                            modifier = Modifier.size(32.dp).padding(end = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Pin Info",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isLive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = match.matchState,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Match Timing / Stage info
            val displayTiming = match.matchTiming.trim()
            val shouldShowTiming = displayTiming.isNotBlank() && 
                !displayTiming.equals("In Progress", ignoreCase = true) &&
                !displayTiming.equals("Match Update", ignoreCase = true)
                
            if (shouldShowTiming) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Timing",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = displayTiming,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TeamScoreRow(teamName = match.team1, score = match.score1, overs = match.overs1, seriesName = match.seriesName)
            Spacer(modifier = Modifier.height(12.dp))
            TeamScoreRow(teamName = match.team2, score = match.score2, overs = match.overs2, seriesName = match.seriesName)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = match.status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Favorite Player Update / Notable Performances
            if (match.notablePerformances.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val isFavUpdate = match.notablePerformances.contains("★")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFavUpdate) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    border = if (isFavUpdate) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFavUpdate) Icons.Default.Star else Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Fav Player Update",
                            tint = if (isFavUpdate) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            if (isFavUpdate) {
                                Text(
                                    text = "FAV PLAYER UPDATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = match.notablePerformances,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFavUpdate) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            

            
            Spacer(modifier = Modifier.height(12.dp))
            }
    }
}




fun getFlagUrl(teamName: String): String? {
    val lowerName = teamName.lowercase()
    val words = lowerName.split(" ", "(", ")", "-").filter { it.isNotBlank() }
    
    if (words.contains("csk") || lowerName.contains("super kings")) return "https://upload.wikimedia.org/wikipedia/en/thumb/2/2b/Chennai_Super_Kings_Logo.svg/100px-Chennai_Super_Kings_Logo.svg.png"
    if (words.contains("mi") || lowerName.contains("mumbai indians")) return "https://upload.wikimedia.org/wikipedia/en/thumb/c/cd/Mumbai_Indians_Logo.svg/100px-Mumbai_Indians_Logo.svg.png"
    if (words.contains("rcb") || lowerName.contains("royal challengers")) return "https://upload.wikimedia.org/wikipedia/en/thumb/2/2a/Royal_Challengers_Bengaluru_Logo.svg/100px-Royal_Challengers_Bengaluru_Logo.svg.png"
    
    val map = mapOf(
        "india" to "in", "australia" to "au", "england" to "gb-eng", "pakistan" to "pk",
        "south africa" to "za", "new zealand" to "nz", "sri lanka" to "lk", "bangladesh" to "bd",
        "afghanistan" to "af", "nepal" to "np", "ireland" to "ie", "zimbabwe" to "zw",
        "netherlands" to "nl", "scotland" to "gb-sct", "usa" to "us", "oman" to "om",
        "uae" to "ae", "namibia" to "na", "uganda" to "ug", "west indies" to "jm"
    )
    
    for ((country, code) in map) {
        // match word boundaries or use the split words
        val isMatch = words.any { it == country } || lowerName.matches(Regex(".*\\b$country\\b.*"))
        if (isMatch) {
            return "https://flagcdn.com/w80/$code.png"
        }
    }
    return null
}

@Composable
fun getTeamColor(teamName: String): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary
    )
    val hash = teamName.hashCode()
    return colors[Math.abs(hash) % colors.size]
}

