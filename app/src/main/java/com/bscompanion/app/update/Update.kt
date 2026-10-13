package com.bscompanion.app.update

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.bscompanion.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// Jeden model stanu aktualizacji — prosto dla 1-osobowego projektu.
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
    val force: Boolean
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data object UpToDate : UpdateState
    data object Offline : UpdateState
    data class Error(val message: String) : UpdateState
}

fun isOnline(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (_: Exception) {
        // Fallback: VALIDATED bywa false na captive portalach — wtedy wystarczy INTERNET.
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }
}

/** Subtelny obserwator sieci — do małej kropki wifi-off w nagłówku. */
@Composable
fun rememberIsOnline(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    var online by remember { mutableStateOf(isOnline(context)) }
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { online = true }
            override fun onLost(network: Network) { online = isOnline(context) }
            override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities) {
                online = isOnline(context)
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(cb)
            } else {
                @Suppress("DEPRECATION")
                val req = android.net.NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                cm.registerNetworkCallback(req, cb)
            }
        } catch (_: Exception) { }
        onDispose {
            try { cm.unregisterNetworkCallback(cb) } catch (_: Exception) { }
        }
    }
    return online
}

private fun parseUpdate(json: String): UpdateInfo? {
    return try {
        val o = JSONObject(json)
        val code = o.optInt("versionCode", -1)
        val name = o.optString("versionName", "")
        val url = o.optString("apkUrl", "")
        if (code <= 0 || name.isEmpty() || url.isEmpty()) return null
        UpdateInfo(
            versionCode = code,
            versionName = name,
            apkUrl = url,
            changelog = o.optString("changelog", ""),
            force = o.optBoolean("force", false)
        )
    } catch (_: Exception) {
        null
    }
}

suspend fun checkForUpdate(context: Context): UpdateState = withContext(Dispatchers.IO) {
    if (!isOnline(context)) return@withContext UpdateState.Offline
    var conn: HttpURLConnection? = null
    try {
        val url = URL(BuildConfig.UPDATE_URL)
        conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "BSCompanion/${BuildConfig.VERSION_NAME}")
        }
        if (conn.responseCode !in 200..299) {
            return@withContext UpdateState.Error("HTTP ${conn.responseCode}")
        }
        val body = conn.inputStream.bufferedReader().use { it.readText() }.take(20_000)
        val info = parseUpdate(body) ?: return@withContext UpdateState.Error("zły version.json")
        if (info.versionCode > BuildConfig.VERSION_CODE) UpdateState.Available(info)
        else UpdateState.UpToDate
    } catch (e: Exception) {
        // Rozróżnij offline zgubiony w trakcie od reszty błędów.
        if (!isOnline(context)) UpdateState.Offline
        else UpdateState.Error(e.message?.take(80) ?: "błąd sieci")
    } finally {
        try { conn?.disconnect() } catch (_: Exception) { }
    }
}

/** Pobiera apk przez DownloadManager i otwiera instalator. Bez WorkManagera. */
fun downloadAndInstall(context: Context, info: UpdateInfo) {
    try {
        val fileName = "bs-companion-${info.versionName}.apk"
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates").apply { mkdirs() }
        val dest = File(dir, fileName)
        if (dest.exists()) {
            try { dest.delete() } catch (_: Exception) { }
        }
        val req = DownloadManager.Request(Uri.parse(info.apkUrl)).apply {
            setTitle("BS Companion ${info.versionName}")
            setDescription("Pobieranie aktualizacji")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(dest))
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
            setMimeType("application/vnd.android.package-archive")
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(req)
        android.widget.Toast.makeText(context, "Pobieranie ${info.versionName}…", android.widget.Toast.LENGTH_SHORT).show()
        // Instalacja: użytkownik tapnie powiadomienie DownloadManagera albo plik.
        // Bezpośredni install tu byłby przedwczesny (plik jeszcze się pobiera).
        // Otwieramy po pobraniu przez receiver systemowy — tu tylko podpowiedź:
        android.util.Log.i("Update", "enqueue ${info.apkUrl} -> $dest")
    } catch (e: Exception) {
        android.util.Log.w("Update", "download failed", e)
        // Fallback: otwórz URL w przeglądarce.
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "Nie można pobrać aktualizacji", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

/** Otwiera lokalnie pobrany apk (gdy użytkownik tapnie "Zainstaluj"). */
fun installApkFile(context: Context, versionName: String) {
    try {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
        val f = File(dir, "bs-companion-$versionName.apk")
        if (!f.exists()) {
            android.widget.Toast.makeText(context, "Plik jeszcze się pobiera", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", f)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            android.widget.Toast.makeText(context, "Brak instalatora pakietów", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        android.util.Log.w("Update", "install failed", e)
        android.widget.Toast.makeText(context, "Nie można otworzyć pliku", android.widget.Toast.LENGTH_SHORT).show()
    }
}
