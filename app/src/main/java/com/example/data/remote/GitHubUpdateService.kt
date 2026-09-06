package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GitHubUpdateInfo(
  val hasUpdate: Boolean,
  val latestVersion: String,
  val releaseTitle: String,
  val releaseNotes: String,
  val apkDownloadUrl: String?,
  val releasePageUrl: String,
  val latestCommitSha: String,
  val latestCommitMessage: String,
  val publishedDate: String
)

object GitHubUpdateService {
  private const val TAG = "GitHubUpdateService"
  private const val PREFS_NAME = "github_update_prefs"
  private const val KEY_REPO = "github_repo_slug"
  private const val KEY_LAST_NOTIFIED_SHA = "last_notified_sha"
  private const val KEY_LAST_NOTIFIED_TAG = "last_notified_tag"

  // Default fallback repository (can be changed by user in the app settings)
  const val DEFAULT_REPO = "yunusafifuddin/sukses-jaya-hris"

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  fun getConfiguredRepo(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_REPO, DEFAULT_REPO) ?: DEFAULT_REPO
  }

  fun saveConfiguredRepo(context: Context, repo: String) {
    val clean = repo.trim()
      .removePrefix("https://github.com/")
      .removePrefix("http://github.com/")
      .removeSuffix(".git")
      .trim('/')
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_REPO, clean)
      .apply()
  }

  fun recordNotified(context: Context, tag: String, sha: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_LAST_NOTIFIED_TAG, tag)
      .putString(KEY_LAST_NOTIFIED_SHA, sha)
      .apply()
  }

  fun getLastNotifiedSha(context: Context): String? {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_LAST_NOTIFIED_SHA, null)
  }

  fun getLastNotifiedTag(context: Context): String? {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .getString(KEY_LAST_NOTIFIED_TAG, null)
  }

  suspend fun checkForUpdates(
    context: Context,
    repoSlug: String = getConfiguredRepo(context)
  ): GitHubUpdateInfo? = withContext(Dispatchers.IO) {
    val cleanRepo = repoSlug.trim()
      .removePrefix("https://github.com/")
      .removePrefix("http://github.com/")
      .removeSuffix(".git")
      .trim('/')

    if (cleanRepo.isBlank() || !cleanRepo.contains("/")) {
      Log.w(TAG, "Invalid repository slug: '$repoSlug'")
      return@withContext null
    }

    // 1. Check GitHub Releases Latest
    var releaseTag = ""
    var releaseTitle = ""
    var releaseNotes = ""
    var releaseUrl = "https://github.com/$cleanRepo/releases"
    var apkDownloadUrl: String? = null
    var publishedDate = ""

    try {
      val releaseReq = Request.Builder()
        .url("https://api.github.com/repos/$cleanRepo/releases/latest")
        .header("User-Agent", "SuksesJaya-AppUpdate-Client/1.0")
        .header("Accept", "application/vnd.github.v3+json")
        .get()
        .build()

      client.newCall(releaseReq).execute().use { resp ->
        if (resp.isSuccessful) {
          val body = resp.body?.string()
          if (!body.isNullOrBlank()) {
            val root = JSONObject(body)
            releaseTag = root.optString("tag_name", "")
            releaseTitle = root.optString("name", releaseTag)
            releaseNotes = root.optString("body", "Pembaruan otomatis dari GitHub Actions.")
            releaseUrl = root.optString("html_url", releaseUrl)
            publishedDate = root.optString("published_at", "")

            val assets = root.optJSONArray("assets")
            if (assets != null) {
              for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                  apkDownloadUrl = asset.optString("browser_download_url")
                  break
                }
              }
            }
            // If direct asset not found, construct standard GitHub Release direct download URL
            if (apkDownloadUrl.isNullOrBlank() && releaseTag.isNotBlank()) {
              apkDownloadUrl = "https://github.com/$cleanRepo/releases/download/$releaseTag/app-debug.apk"
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Could not fetch latest release for $cleanRepo: ${e.message}")
    }

    // 2. Check latest commit on main branch (to detect code/file changes)
    var latestSha = ""
    var commitMsg = ""
    var commitDate = ""
    try {
      val commitReq = Request.Builder()
        .url("https://api.github.com/repos/$cleanRepo/commits/main")
        .header("User-Agent", "SuksesJaya-AppUpdate-Client/1.0")
        .header("Accept", "application/vnd.github.v3+json")
        .get()
        .build()

      client.newCall(commitReq).execute().use { resp ->
        if (resp.isSuccessful) {
          val body = resp.body?.string()
          if (!body.isNullOrBlank()) {
            val root = JSONObject(body)
            latestSha = root.optString("sha", "")
            val commitObj = root.optJSONObject("commit")
            if (commitObj != null) {
              commitMsg = commitObj.optString("message", "")
              val authorObj = commitObj.optJSONObject("author")
              commitDate = authorObj?.optString("date", "") ?: ""
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Could not fetch latest commit for $cleanRepo: ${e.message}")
    }

    // Determine if there is an update
    val hasRelease = releaseTag.isNotBlank()
    val hasCommit = latestSha.isNotBlank()

    if (!hasRelease && !hasCommit) {
      return@withContext null
    }

    val lastNotifiedSha = getLastNotifiedSha(context)
    val lastNotifiedTag = getLastNotifiedTag(context)

    // Check if new compared to last notified or current version
    val isNewTag = releaseTag.isNotBlank() && (releaseTag != lastNotifiedTag)
    val isNewSha = latestSha.isNotBlank() && (latestSha != lastNotifiedSha)

    val hasUpdate = isNewTag || isNewSha || (hasRelease && apkDownloadUrl != null)

    val displayVersion = when {
      releaseTag.isNotBlank() -> releaseTag
      latestSha.isNotBlank() -> "Commit " + latestSha.take(7)
      else -> "v1.0"
    }

    val displayTitle = when {
      releaseTitle.isNotBlank() -> releaseTitle
      latestSha.isNotBlank() -> "Pembaruan GitHub (${latestSha.take(7)})"
      else -> "Pembaruan Tersedia"
    }

    val displayNotes = when {
      releaseNotes.isNotBlank() -> releaseNotes
      commitMsg.isNotBlank() -> commitMsg
      else -> "Perubahan file terbaru telah diunggah ke repositori GitHub."
    }

    GitHubUpdateInfo(
      hasUpdate = hasUpdate,
      latestVersion = displayVersion,
      releaseTitle = displayTitle,
      releaseNotes = displayNotes,
      apkDownloadUrl = apkDownloadUrl ?: "https://github.com/$cleanRepo/releases",
      releasePageUrl = releaseUrl,
      latestCommitSha = latestSha,
      latestCommitMessage = commitMsg,
      publishedDate = if (publishedDate.isNotBlank()) publishedDate else commitDate
    )
  }
}
