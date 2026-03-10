package com.weatherapp.data.update

import com.weatherapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/matthemodest/WeatherApp/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val tagName = json.getString("tag_name").removePrefix("v")
            val assets = json.getJSONArray("assets")
            if (assets.length() == 0) return@withContext null

            val downloadUrl = assets.getJSONObject(0).getString("browser_download_url")

            if (tagName == BuildConfig.VERSION_NAME) null
            else UpdateInfo(latestVersion = tagName, downloadUrl = downloadUrl)
        } catch (e: Exception) {
            Timber.w(e, "UpdateChecker: failed to check for update")
            null
        }
    }
}
