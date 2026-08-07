package com.example.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import com.example.BuildConfig

data class AppUpdate(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

object UpdateManager {

    private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/your-username/criclive/releases/latest"

    suspend fun checkForUpdate(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            // In a real app, you would fetch from GitHub Releases API:
            // val response = URL(GITHUB_RELEASES_URL).readText()
            // val json = JSONObject(response)
            // val remoteVersion = json.getString("tag_name").replace("v", "")
            // val downloadUrl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
            // val body = json.getString("body")
            
            // For now, let's simulate checking an API that always returns an update 
            // if we want to demonstrate the flow, or just return null normally.
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // For demonstration, a mock update
    fun getMockUpdate(): AppUpdate {
        return AppUpdate(
            version = "1.3.0",
            releaseNotes = "• Major performance improvements\n• New Fan Mode exclusive themes\n• Bug fixes for offline caching",
            downloadUrl = "https://example.com/criclive-v1.3.0.apk", // Dummy URL
            isUpdateAvailable = true
        )
    }

    fun downloadAndInstallUpdate(context: Context, downloadUrl: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(downloadUrl)
        val request = DownloadManager.Request(uri).apply {
            setTitle("CricZen Update")
            setDescription("Downloading latest version...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CricZen_update.apk")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(ctxt)
                    ctxt.unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(context: Context) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CricZen_update.apk")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

fun String.toAbbreviation(): String {
    if (this.isBlank()) return "--"
    val words = this.trim().split(Regex("\\s+"))
    return if (words.size > 1) {
        words.mapNotNull { it.firstOrNull() }.joinToString("").take(3).uppercase()
    } else {
        this.take(3).uppercase()
    }
}
