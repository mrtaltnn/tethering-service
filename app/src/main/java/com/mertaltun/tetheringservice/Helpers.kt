package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat.startActivity
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.app.NotificationCompat
import android.os.Build
import android.util.Log


object AccessibilityServiceHelper {
    private const val PREF_NAME = "AccessibilityServicePreferences"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setServiceActive(context: Context, serviceName: String, isActive: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(serviceName, isActive)
        editor.apply()
    }

    fun isServiceActive(context: Context, serviceName: String): Boolean {
        return getPreferences(context).getBoolean(serviceName, false)
    }

    fun clearServiceState(context: Context, serviceName: String) {
        val editor = getPreferences(context).edit()
        editor.remove(serviceName)
        editor.apply()
    }

    fun clearCallerState(context: Context, caller: String) {
        val editor = getPreferences(context).edit()
        editor.remove("$caller-caller")
        editor.apply()
    }

    fun clearAllCallerState(context: Context) {
        clearServiceState(context, "BootReceiver-caller")
        clearServiceState(context, "UsbConnectionReceiver-caller")
        clearServiceState(context, "HttpInjector-caller")
        clearServiceState(context, "DeviceSettings-caller")
        clearServiceState(context, "PeriodicWorker-caller")
        clearServiceState(context, "ProxyServer-caller")
        clearServiceState(context, "MainActivity-caller")
    }

    fun launchApp(context: Context, packageName: String,caller: String) {
        clearAllCallerState(context)
        setServiceActive(context,"$caller-caller", true)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, it, null)
        }
    }

    fun launchAirplaneModeSettings(context: Context,caller: String) {
        clearAllCallerState(context)
        setServiceActive(context,"$caller-caller", true)
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(context, intent, null)
    }

    fun launchTetherSettings(context: Context, caller: String) {
        clearAllCallerState(context)
        setServiceActive(context,"$caller-caller", true)
        val tetherIntent = Intent()
        tetherIntent.setClassName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
        tetherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(context, tetherIntent, null)
    }

    fun launchMobileDataSettings(context: Context, caller: String) {
        clearAllCallerState(context)
        setServiceActive(context,"$caller-caller", true)
        val intent = Intent()
        intent.setClassName("com.android.phone", "com.android.phone.MSimMobileNetworkSettings")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(context,intent,null)
    }

    fun scheduleJob(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .build()

        val request = PeriodicWorkRequestBuilder<PeriodicWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "MyUniqueWorkTag",
        ExistingPeriodicWorkPolicy.KEEP,
        request
        )
    }

    fun isCellularAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            return networkCapabilities != null && (
//                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    )
    }

    fun checkCellularConnection(context: Context): Boolean {
        var attempts = 0
        val maxAttempts = 5
        var isCellularAvailable = false
        while (attempts < maxAttempts) {
            attempts++
            if (isCellularAvailable(context)) {
                Log.d("CellularCheck", "Hücresel veri aktif!")
                isCellularAvailable= true
                break
            } else {
                Log.d("CellularCheck", "Hücresel veri aktif değil, tekrar deneme yapılıyor...")
            }

            Thread.sleep(2000) // 2 saniye bekle
        }

        if (attempts == maxAttempts) {
            Log.d("CellularCheck", "Hücresel veri aktif değil, maksimum deneme sayısına ulaşıldı.")
        }
        return isCellularAvailable
    }

    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    fun checkUsbConnection(context: Context): Boolean {
        val intentFilter = IntentFilter(Intent.ACTION_UMS_CONNECTED)
        val usbStateIntent = context.registerReceiver(null, intentFilter)
        return usbStateIntent != null
    }

    fun checkUsbConnection2(context: Context): Boolean {
        val intent = context.registerReceiver(null, IntentFilter("android.hardware.usb.action.USB_STATE"))
        return intent?.getBooleanExtra("connected", false) ?: false
    }
}

//findAndClickSwitches helper
object UiHelper {
    fun findSwitchesRecursively(node: AccessibilityNodeInfo, switches: MutableList<AccessibilityNodeInfo>) {
        if (node.childCount == 0) {
            if (node.className == "android.widget.Switch") {
                switches.add(node)
            }
        } else {
            for (i in 0 until node.childCount) {
                findSwitchesRecursively(node.getChild(i), switches)
            }
        }
    }

    fun findSwitchAtIndex(node: AccessibilityNodeInfo, index: Int,expectedCount: Int): AccessibilityNodeInfo? {
        val switches = mutableListOf<AccessibilityNodeInfo>()
        findSwitchesRecursively(node, switches)
        return if (switches.size == expectedCount && index in switches.indices) {
            switches[index]
        } else {
            null
        }
    }

    fun findButtonNode(node: AccessibilityNodeInfo,name:String): AccessibilityNodeInfo? {
        return node.findAccessibilityNodeInfosByText(name)?.firstOrNull()
    }

    fun getAllTextViewsText(rootNode: AccessibilityNodeInfo?): List<String> {
        val texts = mutableListOf<String>()
        rootNode?.let {
            collectTextViewsText(it, texts)
        }
        return texts
    }

    private fun collectTextViewsText(node: AccessibilityNodeInfo, texts: MutableList<String>) {
        if (node.className == "android.widget.TextView" && !node.text.isNullOrEmpty() && node.text.toString().startsWith("[")) {
            texts.add(node.text.toString())
        }

        // Alt node'larda arama yap
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                collectTextViewsText(childNode, texts)
            }
        }
    }

    fun refreshCurrentScreen(context: Context) {
        val intent = Intent(context, HttpInjectorACS::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

// ACS lerin arkaplanda da çalışabilmesi için

object ForegroundServiceHelper {
    private const val NOTIFICATION_CHANNEL_ID = "accessibility_service_channel"
    private const val NOTIFICATION_ID = 1

    fun startForegroundService(context: Context) {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            "Accessibility Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Tethering Service is running")
            .setContentText("All operations are working...")
            .setSmallIcon(android.R.drawable.presence_online)
            .build()

        if (context is AccessibilityService) {
            context.startForeground(NOTIFICATION_ID, notification)
        }
    }

    fun stopForegroundService(context: Context) {
        if (context is AccessibilityService) {
            context.stopForeground(true)
        }
    }
}
