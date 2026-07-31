package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun IdolHeader(idolName: String, wallpaperUri: String, onClick: () -> Unit) {
    val isConfigured = idolName.isNotBlank() || wallpaperUri.isNotBlank()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (!isConfigured) {
            // Cheap gray background for unconfigured state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddAPhoto, 
                        contentDescription = "Add Photo",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap to set up Fan Zone & add a photo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (wallpaperUri.isNotBlank()) {
                    AsyncImage(
                        model = wallpaperUri,
                        contentDescription = "Idol Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark premium gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC000000)),
                                    startY = 100f
                                )
                            )
                    )
                } else {
                    // Fallback just in case they have a name but no photo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95))
                                )
                            )
                    )
                    
                    Icon(
                        Icons.Default.Star, 
                        contentDescription = null,
                        tint = Color(0xFFFFD700).copy(alpha = 0.2f),
                        modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(x = 20.dp, y = -20.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (idolName.isNotBlank()) idolName else "My Idol",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Cursive,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Fan Zone",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFD700).copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}
