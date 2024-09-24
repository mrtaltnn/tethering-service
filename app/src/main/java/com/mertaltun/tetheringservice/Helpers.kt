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
import androidx.core.app.NotificationCompat
import android.os.Build


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

    fun launchApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(context, it, null)
        }
    }

    fun launchAirplaneModeSettings(context: Context) {
        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(context, intent, null)
    }

    fun launchTetherSettings(context: Context) {
        val tetherIntent = Intent()
        tetherIntent.setClassName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
        tetherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(context, tetherIntent, null)
    }

    fun scheduleJob(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
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

    fun findButtonNode(text: String, node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.text != null && node.text.toString().contains(text)) {
            return node
        }

        for (i in 0 until node.childCount) {
            val foundNode = findButtonNode(text, node.getChild(i))
            if (foundNode != null) {
                return foundNode
            }
        }
        return null
    }
}

// ACS lerin arkaplanda da çalışabilmesi için

object ForegroundServiceHelper {

    private const val NOTIFICATION_CHANNEL_ID = "accessibility_service_channel"
    private const val NOTIFICATION_ID = 1

    fun startForegroundService(context: Context) {
        // Android 8.0 (API 26) ve üzeri için Notification Channel oluşturma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Accessibility Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Foreground Service için bir Notification oluşturma
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Foreground Service Running")
            .setContentText("Accessibility Service is running in the background.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        // Foreground Service başlatma
        if (context is AccessibilityService) {
            context.startForeground(NOTIFICATION_ID, notification)
        }
    }

    fun stopForegroundService(context: Context) {
        // Foreground Service'i durdurma
        if (context is AccessibilityService) {
            context.stopForeground(true)
        }
    }
}
