package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
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
fun SettingsBottomSheet(
    state: CricketUiState.Success,
    onDismiss: () -> Unit,
    onSaveIdol: (String) -> Unit,
    onSaveWallpaper: (String) -> Unit,
    onEditPreferences: () -> Unit,
    onSaveMode: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        var idolName by remember { mutableStateOf(state.idolName) }
        var showFaq by remember { mutableStateOf(false) }
        var showAbout by remember { mutableStateOf(false) }
        var appMode by remember { mutableStateOf(state.appMode) }
        
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                onSaveWallpaper(uri.toString())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Settings & Personalization", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("App Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = appMode == "Standard", onClick = { appMode = "Standard"; onSaveMode("Standard") })
                    Text("Standard", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = appMode == "Fan Mode", onClick = { appMode = "Fan Mode"; onSaveMode("Fan Mode") })
                    Text("Fan Mode", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            if (appMode == "Fan Mode") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = idolName,
                    onValueChange = { idolName = it; onSaveIdol(it) },
                    label = { Text("Custom Tracking Player (e.g., Virat Kohli)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Select Idol Wallpaper")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            val context = LocalContext.current
            Button(
                onClick = { 
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Enable Reliable Notifications", fontWeight = FontWeight.Bold)
            }
            Text("Fixes delayed notifications by ignoring battery limits.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onEditPreferences, 
                modifier = Modifier.fillMaxWidth(), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Edit Teams & Players", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = { showFaq = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("App FAQ & Limitations", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showAbout = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("About & Open Source", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { /* Open Play Store or similar */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Rate App / Give Feedback", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        if (showAbout) {
            AlertDialog(
                onDismissRequest = { showAbout = false },
                title = { Text("About CricZen", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Version 1.2 (F-Droid Ready)", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("CricZen is a fully open-source, privacy-respecting cricket score app built for fans.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Privacy Policy:", fontWeight = FontWeight.Bold)
                        Text("We do NOT collect, store, or share any personal data. All data fetching is done directly from public RSS feeds to your device. Your favorite players and settings are stored locally on your device.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("License: MIT License", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAbout = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showFaq) {
            AlertDialog(
                onDismissRequest = { showFaq = false },
                title = { Text("App FAQ & Limitations", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Q: Why does the widget sometimes not update instantly?", fontWeight = FontWeight.Bold)
                        Text("A: To save battery, Android restricts how often widgets can fetch data in the background (usually every 15 mins). However, when you 'Pin' a match, we use an atomic cache to show it instantly, and a background worker will periodically refresh it.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Q: Where do the scores come from?", fontWeight = FontWeight.Bold)
                        Text("A: We parse public RSS feeds. This means the data is free but might be slightly delayed compared to live TV. It focuses mainly on live matches.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Q: How does Custom Player/Fan Mode work?", fontWeight = FontWeight.Bold)
                        Text("A: Fan mode scans recent news articles related to your chosen player to fetch updates. It may not always provide real-time stats if they aren't covered in recent news.", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFaq = false }) {
                        Text("I Understand")
                    }
                }
            )
        }
    }
}

