package com.dainsleif.gaggy.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.model.VersionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateService {
    companion object {
        private const val VERSION_URL = "https://jonvicbarcenas.github.io/GAGgy/version.json"
        private const val UPDATE_NOTIFICATION_CHANNEL_ID = "update_notification_channel"
        private const val UPDATE_NOTIFICATION_ID = 2001
        private const val UPDATE_WORK_NAME = "update_check_work"
        
        suspend fun checkForUpdates(context: Context): Result<VersionData> {
            return withContext(Dispatchers.IO) {
                try {
                    val response = URL(VERSION_URL).readText()
                    val jsonObject = JSONObject(response)
                    
                    // Parse features array if it exists
                    val featuresArray = jsonObject.optJSONArray("features")
                    val features = mutableListOf<String>()
                    
                    if (featuresArray != null) {
                        for (i in 0 until featuresArray.length()) {
                            features.add(featuresArray.getString(i))
                        }
                    }
                    
                    val versionData = VersionData(
                        version = jsonObject.getString("version"),
                        url = jsonObject.getString("url"),
                        features = features
                    )
                    Result.success(versionData)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
        
        fun openUpdateUrl(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
        
        fun scheduleUpdateChecks(context: Context) {
            // Create notification channel for updates
            createUpdateNotificationChannel(context)
            
            // Schedule periodic work to check for updates every hour
            val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                1, TimeUnit.HOURS, // Run every hour
                15, TimeUnit.MINUTES // Flex interval
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateWorkRequest
            )
        }
        
        private fun createUpdateNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "App Updates"
                val descriptionText = "Notifications about new app versions"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(UPDATE_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
        
        fun showUpdateNotification(context: Context, versionData: VersionData) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionData.url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val builder = NotificationCompat.Builder(context, UPDATE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("GAGgy Update Available")
                .setContentText("New version ${versionData.version} is available!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            
            with(NotificationManagerCompat.from(context)) {
                try {
                    notify(UPDATE_NOTIFICATION_ID, builder.build())
                } catch (e: SecurityException) {
                    // Handle missing notification permission
                }
            }
        }
        
        private fun getCurrentAppVersion(context: Context): String {
            return try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                packageInfo.versionName
            } catch (e: Exception) {
                "0.0.0" // Default version if unable to get current version
            }
        }
        
        fun isNewVersionAvailable(context: Context, remoteVersion: String): Boolean {
            val currentVersion = getCurrentAppVersion(context)
            
            // Simple version comparison (assumes format like "2.1.3")
            return try {
                val remoteParts = remoteVersion.replace("v", "").split(".").map { it.toInt() }
                val currentParts = currentVersion.split(".").map { it.toInt() }
                
                for (i in 0 until minOf(remoteParts.size, currentParts.size)) {
                    if (remoteParts[i] > currentParts[i]) return true
                    if (remoteParts[i] < currentParts[i]) return false
                }
                
                // If we get here, the common parts are equal, so the longer one is newer
                remoteParts.size > currentParts.size
            } catch (e: Exception) {
                // If version parsing fails, assume no update needed
                false
            }
        }
    }
}

class UpdateCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val updateResult = UpdateService.checkForUpdates(applicationContext)
            
            if (updateResult.isSuccess) {
                val versionData = updateResult.getOrThrow()
                
                // Check if the remote version is newer than the current app version
                if (UpdateService.isNewVersionAvailable(applicationContext, versionData.version)) {
                    // Show notification for the new version
                    UpdateService.showUpdateNotification(applicationContext, versionData)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 