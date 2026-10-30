package app.sren.mobile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

// Corregido el 2026-10-29: con FlutterActivity no compilaba (registerForActivityResult
// necesita una ComponentActivity). FlutterFragmentActivity sí lo es.
class MainActivity : FlutterFragmentActivity() {
    private companion object {
        const val CHANNEL = "app.sren.mobile/platform"
        const val PREFS = "sren_prefs"
    }

    private var pendingResult: MethodChannel.Result? = null
    private var pendingRequest: String? = null

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            when (pendingRequest) {
                "notifications" -> resolveNotificationPermission(result)
                "media" -> resolveMediaPermission(result)
                else -> clearPending()
            }
        }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getState" -> result.success(getState())
                "saveState" -> {
                    saveState(call.arguments as? Map<*, *> ?: emptyMap<String, Any>())
                    result.success(null)
                }
                "requestNotificationPermission" -> requestNotificationPermission(result)
                "requestMediaPermission" -> requestMediaPermission(result)
                "openSystemSettings" -> {
                    openSystemSettings()
                    result.success(null)
                }
                "openExternalUrl" -> {
                    val url = call.argument<String>("url")
                    if (url.isNullOrBlank()) {
                        result.error("invalid_url", "Missing URL", null)
                    } else {
                        openExternalUrl(url)
                        result.success(null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun getState(): Map<String, Any> {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        return mapOf(
            "hasSeenOnboarding" to prefs.getBoolean("hasSeenOnboarding", false),
            "hasCompletedPermissionFlow" to prefs.getBoolean("hasCompletedPermissionFlow", false),
            "selectedLanguage" to (prefs.getString("selectedLanguage", "es") ?: "es"),
            "notificationPermission" to (prefs.getString("notificationPermission", "unknown")
                ?: "unknown"),
            "mediaPermission" to (prefs.getString("mediaPermission", "unknown") ?: "unknown"),
        )
    }

    private fun saveState(arguments: Map<*, *>) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit().apply {
            arguments["hasSeenOnboarding"]?.let {
                putBoolean("hasSeenOnboarding", it as Boolean)
            }
            arguments["hasCompletedPermissionFlow"]?.let {
                putBoolean("hasCompletedPermissionFlow", it as Boolean)
            }
            arguments["selectedLanguage"]?.let {
                putString("selectedLanguage", it as String)
            }
            arguments["notificationPermission"]?.let {
                putString("notificationPermission", it as String)
            }
            arguments["mediaPermission"]?.let {
                putString("mediaPermission", it as String)
            }
        }.apply()
    }

    private fun requestNotificationPermission(result: MethodChannel.Result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            savePermissionState("notificationPermission", "granted")
            result.success("granted")
            return
        }

        pendingResult = result
        pendingRequest = "notifications"
        permissionsLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
    }

    private fun requestMediaPermission(result: MethodChannel.Result) {
        pendingResult = result
        pendingRequest = "media"

        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        permissionsLauncher.launch(permissions)
    }

    private fun resolveNotificationPermission(result: Map<String, Boolean>) {
        val granted = result[Manifest.permission.POST_NOTIFICATIONS] == true
        val state = if (granted) "granted" else "denied"
        savePermissionState("notificationPermission", state)
        pendingResult?.success(state)
        clearPending()
    }

    private fun resolveMediaPermission(result: Map<String, Boolean>) {
        val state = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                result[Manifest.permission.READ_MEDIA_IMAGES] == true -> "full"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                result[Manifest.permission.READ_MEDIA_VIDEO] == true -> "full"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                result[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true -> "limited"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                result[Manifest.permission.READ_MEDIA_IMAGES] == true -> "full"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                result[Manifest.permission.READ_MEDIA_VIDEO] == true -> "full"
            result[Manifest.permission.READ_EXTERNAL_STORAGE] == true -> "full"
            else -> "denied"
        }

        savePermissionState("mediaPermission", state)
        pendingResult?.success(state)
        clearPending()
    }

    private fun savePermissionState(key: String, value: String) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(key, value).apply()
    }

    private fun openSystemSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun openExternalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun clearPending() {
        pendingRequest = null
        pendingResult = null
    }
}
