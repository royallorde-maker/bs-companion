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

/** Jaki transport niesie sieć — żeby karta mówiła wprost, czego brakuje. */
enum class ConnectionKind { NONE, WIFI, CELLULAR, OTHER }

fun connectionKind(context: Context): ConnectionKind {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return ConnectionKind.NONE
        val caps = cm.getNetworkCapabilities(net) ?: return ConnectionKind.NONE
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionKind.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionKind.CELLULAR
            else -> ConnectionKind.OTHER
        }
    } catch (_: Exception) {
        ConnectionKind.NONE
    }
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
        // Twarda zasada: tylko https z domen GitHuba. DownloadManager podąża za
        // redirectem releases/download → objects.githubusercontent.com, więc obie
        // rodziny hostów muszą przejść. Android i tak odrzuci update spod innego
        // klucza, ale nie pobieramy w ogóle niczego spoza allowlisty.
        val uri = try { Uri.parse(url) } catch (_: Exception) { return null }
        if (uri.scheme != "https") return null
        val host = (uri.host ?: "").lowercase(java.util.Locale.ROOT)
        // Kropka obowiązkowa: bez niej evilgithubusercontent.com też by przeszło.
        val okHost = host == "github.com" || host.endsWith(".github.com") ||
            host.endsWith(".githubusercontent.com")
        if (!okHost) return null
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
            setRequestProperty("User-Agent", "Internauta/${BuildConfig.VERSION_NAME}")
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
        // Podwójny tap w Pobierz: jedno pobieranie już leci — nie dokładaj drugiego
        // pisarza do tego samego pliku.
        if (queryDownloadStatus(context, info.versionName).state == DlState.RUNNING) {
            android.widget.Toast.makeText(context, "Już się pobiera…", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val dest = apkFile(context, info.versionName)
        try { dest.parentFile?.mkdirs() } catch (_: Exception) { }
        if (dest.exists()) {
            try { dest.delete() } catch (_: Exception) { }
        }
        val req = DownloadManager.Request(Uri.parse(info.apkUrl)).apply {
            setTitle("Internauta ${info.versionName}")
            setDescription("Pobieranie aktualizacji")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // Dedykowane API zamiast setDestinationUri(Uri.fromFile) — poprawnie
            // przechodzi scoped storage; docelowo ten sam plik co dest powyżej.
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "updates/${dest.name}")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
            setMimeType("application/vnd.android.package-archive")
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // Nowe pobieranie unieważnia stare: anuluj w locie + posprzątaj obce pliki.
        // Dzięki temu v3 po v2 nie miesza rekordów ani nie zjada miejsca.
        try {
            val prefs = context.getSharedPreferences(PREFS_UPDATE, Context.MODE_PRIVATE)
            val oldId = prefs.getLong(KEY_DL_ID, -1L)
            if (oldId >= 0) {
                try { dm.remove(oldId) } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
        deleteStaleApks(context, keepVersion = info.versionName)
        // Zapamiętaj id — "Zainstaluj" pyta DownloadManager o STAN zamiast zgadywać po rozmiarze.
        val id = dm.enqueue(req)
        try {
            context.getSharedPreferences(PREFS_UPDATE, Context.MODE_PRIVATE).edit()
                .putLong(KEY_DL_ID, id)
                .putString(KEY_DL_VERSION, info.versionName)
                .apply()
        } catch (_: Exception) { }
        android.widget.Toast.makeText(context, "Pobieranie ${info.versionName}…", android.widget.Toast.LENGTH_SHORT).show()
        android.util.Log.i("Update", "enqueue ${info.apkUrl} -> $dest (id=$id)")
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

/** Stan lokalnego pobrania danej wersji — niezależny od version.json. */
enum class DlState { MISSING, RUNNING, PAUSED, SUCCESS, FAILED }

data class DownloadStatus(
    val state: DlState,
    val downloaded: Long = 0L,
    val total: Long = -1L
) {
    /** Ułamek 0..1 albo null, gdy rozmiar nieznany (pasek nieokreślony). */
    val fraction: Float?
        get() = if (total > 0) (downloaded.toFloat() / total).coerceIn(0f, 1f) else null
}

private const val PREFS_UPDATE = "bs_update"
private const val KEY_DL_ID = "dl_id"
private const val KEY_DL_VERSION = "dl_version"

private fun apkFile(context: Context, versionName: String): File {
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
    // Dezynfekcja nazwy z JSON: bez tego ../ w versionName wychodzi poza updates/.
    val safe = versionName.replace(Regex("[^A-Za-z0-9._-]"), "_").take(32)
    return File(dir, "bs-companion-$safe.apk")
}

/** Plik udaje skończone pobranie? Parsowanie pakietu odrzuca ucięte/obce pliki —
 *  te wracają jako MISSING i apka pobiera od nowa zamiast sypać instalatorem. */
private fun isIntactApk(context: Context, f: File): Boolean {
    return try {
        if (!f.exists() || f.length() <= 0) false
        else context.packageManager.getPackageArchiveInfo(f.absolutePath, 0) != null
    } catch (_: Exception) {
        false
    }
}

/** Pyta DownloadManager o stan NASZEGO pobrania; spójne z plikiem na dysku. */
fun queryDownloadStatus(context: Context, versionName: String): DownloadStatus {
    return try {
        val prefs = context.getSharedPreferences(PREFS_UPDATE, Context.MODE_PRIVATE)
        // Nowsza wersja w version.json unieważnia stare pobranie — bez kasowania pliku
        // (kasuje je dopiero start nowego pobierania, żeby "Zainstaluj" działało wstecz).
        if (prefs.getString(KEY_DL_VERSION, "") != versionName) {
            val f = apkFile(context, versionName)
            return if (isIntactApk(context, f)) DownloadStatus(DlState.SUCCESS, f.length(), f.length())
            else DownloadStatus(DlState.MISSING)
        }
        val id = prefs.getLong(KEY_DL_ID, -1L)
        if (id < 0) {
            val f = apkFile(context, versionName)
            return if (isIntactApk(context, f)) DownloadStatus(DlState.SUCCESS, f.length(), f.length())
            else DownloadStatus(DlState.MISSING)
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val c = dm.query(DownloadManager.Query().setFilterById(id))
        try {
            if (!c.moveToFirst()) {
                // Rekord zniknął z DownloadManagera (reboot/wyczyszczone dane):
                // plik na dysku nadal może być dobry — ale tylko gdy się parsuje.
                val f = apkFile(context, versionName)
                return if (isIntactApk(context, f)) DownloadStatus(DlState.SUCCESS, f.length(), f.length())
                else DownloadStatus(DlState.MISSING)
            }
            val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val f = apkFile(context, versionName)
                    // Sukces transferu ≠ poprawny APK (HTML z proxy albo ucięty plik):
                    // parsujemy pakiet jak w pozostałych gałęziach — inaczej
                    // „gotowe do instalacji” kończyło się błędem instalatora.
                    if (isIntactApk(context, f)) {
                        DownloadStatus(DlState.SUCCESS, f.length(), f.length())
                    } else {
                        DownloadStatus(DlState.MISSING)
                    }
                }
                DownloadManager.STATUS_FAILED -> DownloadStatus(DlState.FAILED)
                // Wstrzymane (np. roaming przy setAllowedOverRoaming(false) albo
                // brak sieci): to NIE jest postęp — pokazujemy wprost zamiast
                // wiecznego "Pobieranie…".
                DownloadManager.STATUS_PAUSED -> DownloadStatus(DlState.PAUSED)
                else -> {
                    val done = try {
                        c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    } catch (_: Exception) { 0L }
                    val total = try {
                        c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    } catch (_: Exception) { -1L }
                    DownloadStatus(DlState.RUNNING, done.coerceAtLeast(0L), total)
                }
            }
        } finally {
            try { c.close() } catch (_: Exception) { }
        }
    } catch (_: Exception) {
        DownloadStatus(DlState.MISSING)
    }
}

/** Anuluje bieżące pobieranie i sprząta po nim (rekord + pliki). */
fun cancelDownload(context: Context) {
    try {
        val prefs = context.getSharedPreferences(PREFS_UPDATE, Context.MODE_PRIVATE)
        val id = prefs.getLong(KEY_DL_ID, -1L)
        if (id >= 0) {
            try {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.remove(id)
            } catch (_: Exception) { }
        }
        try { prefs.edit().remove(KEY_DL_ID).remove(KEY_DL_VERSION).apply() } catch (_: Exception) { }
        deleteStaleApks(context, keepVersion = null)
    } catch (_: Exception) { }
}

/** Kasuje apk wszystkich wersji poza wskazaną (wołane przy starcie nowego pobierania). */
private fun deleteStaleApks(context: Context, keepVersion: String?) {
    try {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
        val keepName = keepVersion?.let { apkFile(context, it).name }
        dir.listFiles { f -> f.isFile && f.name.startsWith("bs-companion-") && f.name.endsWith(".apk") }
            ?.forEach {
                if (keepName == null || it.name != keepName) {
                    try { it.delete() } catch (_: Exception) { }
                }
            }
    } catch (_: Exception) { }
}

/** Prawda, gdy DownloadManager zgłasza SUCCESS dla naszego pobrania tej wersji. */
private fun downloadSucceeded(context: Context, versionName: String): Boolean {
    return queryDownloadStatus(context, versionName).state == DlState.SUCCESS
}

/** Otwiera lokalnie pobrany apk (gdy użytkownik tapnie "Zainstaluj"). */
fun installApkFile(context: Context, versionName: String) {
    try {
        val f = apkFile(context, versionName)
        // Najpierw STAN z DownloadManagera (zapisane id): rozmiar kłamie, bo
        // DownloadManager tworzy pusty plik docelowy już przy starcie.
        if (!downloadSucceeded(context, versionName)) {
            if (!f.exists()) {
                android.widget.Toast.makeText(context, "Plik zniknął — pobierz ponownie", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, "Plik jeszcze się pobiera", android.widget.Toast.LENGTH_SHORT).show()
            }
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
