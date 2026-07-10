package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.CricketApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CricketViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CricketViewModel by viewModels()
    private var isPipMode by mutableStateOf(false)
    private var isFirstLaunch by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = getSharedPreferences("CricLivePrefs", android.content.Context.MODE_PRIVATE)
        isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)
        
        setContent {
            MyApplicationTheme {
                CricketApp(
                    viewModel = viewModel,
                    isPipMode = isPipMode,
                    isFirstLaunch = isFirstLaunch,
                    onOnboardingComplete = {
                        prefs.edit().putBoolean("isFirstLaunch", false).apply()
                        isFirstLaunch = false
                    },
                    onEnterPip = { enterPip() }
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter PiP when leaving the app if viewing a match
        if (viewModel.selectedMatchId.value != null) {
            enterPip()
        }
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)
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
