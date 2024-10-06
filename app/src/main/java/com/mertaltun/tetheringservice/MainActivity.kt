package com.mertaltun.tetheringservice

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TetheringService Main", "onCreate")

        // PeriodicWorker'ı başlat
        AccessibilityServiceHelper.scheduleJob(this)

        if (!isAccessibilityServiceEnabled()) {
            //clear service state
            AccessibilityServiceHelper.clearServiceState(this, "UsbTethering")
            AccessibilityServiceHelper.clearServiceState(this, "ProxyServer")
            AccessibilityServiceHelper.clearServiceState(this, "HttpInjector")
            AccessibilityServiceHelper.clearAllCallerState(this)

            // Erişilebilirlik ayarlarına yönlendir
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        requestPermissions()
        setContentView(R.layout.activity_main)

        checkIntent(intent)
    }

    // Handle permission request response
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TetheringService Main", "Permission granted")
            } else {
                Log.d("TetheringService Main", "Permission denied")
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.CHANGE_NETWORK_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        )

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // onNewIntent'de Intent'i kontrol et
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        intent?.let {
            // Intent'ten ekstra bilgi al
            val fromAccessibilityService = it.getBooleanExtra("HttpInjector", false)
            if (fromAccessibilityService) {
                // Log at
                Log.d("MainActivity", "Intent ile HttpInjector'den geldik.")
                AccessibilityServiceHelper.launchApp(this, "com.evozi.injector","MainActivity")
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/${HttpInjectorACS::class.java.canonicalName}"
        val accessibilityEnabled = try {
            Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)
        } catch (e: Settings.SettingNotFoundException) {
            0
        }

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue != null) {
                colonSplitter.setString(settingValue)
                while (colonSplitter.hasNext()) {
                    val componentName = colonSplitter.next()
                    if (componentName.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
