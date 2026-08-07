package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.CricketApp
import com.example.ui.theme.CricZenTheme
import com.example.viewmodel.CricketViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CricketViewModel by viewModel()
    private var isPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val matchId = intent.getStringExtra("MATCH_ID")
        if (matchId != null) {
            viewModel.selectMatch(matchId)
        }
        
        setContent {
            val isFirstLaunch by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = false)
            val pipHintShown by viewModel.pipHintShown.collectAsStateWithLifecycle(initialValue = false)
            
            CricZenTheme(darkTheme = false) {
                Box(modifier = Modifier.fillMaxSize().background(com.example.ui.theme.PremiumGradientLight)) {
                    CricketApp(
                    viewModel = viewModel,
                    isPipMode = isPipMode,
                    onEnterPip = { enterPip() }
                )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        val matchId = intent.getStringExtra("MATCH_ID")
        if (matchId != null) {
            viewModel.selectMatch(matchId)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter PiP when leaving the app if viewing a match
        val state = viewModel.uiState.value
        if (state is com.example.viewmodel.CricketUiState.Success && state.selectedMatchId != null) {
            enterPip()
        }
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(2, 1)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode = isInPictureInPictureMode
    }
}
