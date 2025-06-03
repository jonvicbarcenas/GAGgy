package com.dainsleif.gaggy.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.dainsleif.gaggy.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AppUpdateChecker(private val context: Context) {
    
    companion object {
        private const val UPDATE_URL = "https://jonvicbarcenas.github.io/GAGgy/version.json"
    }
    
    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = URL(UPDATE_URL).readText()
            val updateInfo = JSONObject(jsonString)
            
            val latestVersion = updateInfo.getString("version")
            val downloadUrl = updateInfo.getString("url")
            
            val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            
            val hasUpdate = compareVersions(latestVersion, currentVersion) > 0
            
            UpdateResult(hasUpdate, latestVersion, currentVersion, downloadUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateResult(false, "", "", "")
        }
    }
    
    fun showUpdateDialog(updateResult: UpdateResult) {
        if (updateResult.hasUpdate) {
            AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle("Update Available")
                .setMessage("A new version (${updateResult.latestVersion}) is available. You are currently using version ${updateResult.currentVersion}. Would you like to update?")
                .setPositiveButton("Update") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateResult.downloadUrl))
                    context.startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        } else {
            AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle("No Updates")
                .setMessage("You are using the latest version (${updateResult.currentVersion}).")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    private fun compareVersions(version1: String, version2: String): Int {
        val v1 = version1.replace("v", "").split(".")
        val v2 = version2.replace("v", "").split(".")
        
        for (i in 0 until minOf(v1.size, v2.size)) {
            val num1 = v1[i].toIntOrNull() ?: 0
            val num2 = v2[i].toIntOrNull() ?: 0
            
            if (num1 != num2) {
                return num1 - num2
            }
        }
        
        return v1.size - v2.size
    }
    
    data class UpdateResult(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val currentVersion: String,
        val downloadUrl: String
    )
} 