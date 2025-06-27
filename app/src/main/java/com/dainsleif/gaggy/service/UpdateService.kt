package com.dainsleif.gaggy.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dainsleif.gaggy.model.VersionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class UpdateService {
    companion object {
        private const val VERSION_URL = "https://jonvicbarcenas.github.io/GAGgy/version.json"
        
        suspend fun checkForUpdates(context: Context): Result<VersionData> {
            return withContext(Dispatchers.IO) {
                try {
                    val response = URL(VERSION_URL).readText()
                    val jsonObject = JSONObject(response)
                    val versionData = VersionData(
                        version = jsonObject.getString("version"),
                        url = jsonObject.getString("url")
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
    }
} 