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

@Composable
fun MatchCard(match: Match, isPreferred: Boolean, onClick: () -> Unit) {
    val isLive = match.status.contains("Live", ignoreCase = true) || match.status.contains("*")
    
    // India matches get the Tricolor border, otherwise just a thick primary border for preferred teams
    val isIndiaMatch = match.team1.contains("India", true) || match.team2.contains("India", true)
    
    val borderStroke = if (isIndiaMatch && isPreferred) {
        BorderStroke(3.dp, Brush.horizontalGradient(listOf(Color(0xFFFF9933), Color(0xFF000080), Color(0xFF138808))))
    } else if (isPreferred) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, Color(0xFF6B7280)) // Even darker gray for standard border
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLive) Color(0xFFFEE2E2) else Color(0xFFF3F4F6)
                ) {
                    Text(
                        text = if (isLive) "LIVE" else if (match.status.contains("Starts", true)) "UPCOMING" else "COMPLETE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLive) Color(0xFFB91C1C) else Color(0xFF374151)
                    )
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
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = displayTiming,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4B5563),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TeamScoreRow(teamName = match.team1, score = match.score1, overs = match.overs1)
            Spacer(modifier = Modifier.height(12.dp))
            TeamScoreRow(teamName = match.team2, score = match.score2, overs = match.overs2)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = match.status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLive) MaterialTheme.colorScheme.primary else Color(0xFF1F2937),
                fontWeight = FontWeight.Bold
            )

            // Favorite Player Update / Notable Performances
            if (match.notablePerformances.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val isFavUpdate = match.notablePerformances.contains("★")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isFavUpdate) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                    border = if (isFavUpdate) BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFavUpdate) Icons.Default.Star else Icons.Default.TrendingUp,
                            contentDescription = "Fav Player Update",
                            tint = if (isFavUpdate) Color(0xFFD97706) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            if (isFavUpdate) {
                                Text(
                                    text = "FAV PLAYER UPDATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = match.notablePerformances,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFavUpdate) Color(0xFF78350F) else MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


fun getFlagUrl(teamName: String): String? {
    val lowerName = teamName.lowercase()
    
    // Franchise mappings (can use external images if we had them, returning null falls back to colored initials)
    if (lowerName.contains("super kings") || lowerName.contains("csk")) return "https://upload.wikimedia.org/wikipedia/en/thumb/2/2b/Chennai_Super_Kings_Logo.svg/100px-Chennai_Super_Kings_Logo.svg.png"
    if (lowerName.contains("mumbai indians") || lowerName.contains("mi")) return "https://upload.wikimedia.org/wikipedia/en/thumb/c/cd/Mumbai_Indians_Logo.svg/100px-Mumbai_Indians_Logo.svg.png"
    if (lowerName.contains("royal challengers") || lowerName.contains("rcb")) return "https://upload.wikimedia.org/wikipedia/en/thumb/2/2a/Royal_Challengers_Bengaluru_Logo.svg/100px-Royal_Challengers_Bengaluru_Logo.svg.png"
    
    // Country mappings
    val map = mapOf(
        "india" to "in",
        "australia" to "au",
        "england" to "gb-eng",
        "pakistan" to "pk",
        "south africa" to "za",
        "new zealand" to "nz",
        "sri lanka" to "lk",
        "bangladesh" to "bd",
        "afghanistan" to "af",
        "nepal" to "np",
        "ireland" to "ie",
        "zimbabwe" to "zw",
        "netherlands" to "nl",
        "scotland" to "gb-sct",
        "usa" to "us",
        "oman" to "om",
        "uae" to "ae",
        "namibia" to "na",
        "uganda" to "ug",
        "west indies" to "jm" // Using Jamaica as a representative Caribbean flag for display purposes if needed, or null to fallback
    )
    
    for ((country, code) in map) {
        if (lowerName.contains(country)) {
            return "https://flagcdn.com/w80/$code.png"
        }
    }
    return null
}

// Generate dynamic colors for team logos
fun getTeamColor(teamName: String): Color {
    val colors = listOf(
        Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF10B981), 
        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899),
        Color(0xFF14B8A6), Color(0xFFF97316), Color(0xFF6366F1)
    )
    val hash = abs(teamName.hashCode())
    return colors[hash % colors.size]
}

