package com.mertaltun.tetheringservice

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {

//            val tetherSettingsIntent = Intent().apply {
//                component = ComponentName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            }
//            context.startActivity(tetherSettingsIntent)
//
//            val serviceIntent = Intent(context, TetheringService::class.java)
//            ContextCompat.startForegroundService(context, serviceIntent)

            // PeriodicWorker'ı başlat
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresCharging(true)
//                .build()
//            val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(15, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .build()
//            WorkManager.getInstance(context).enqueue(periodicWorkRequest)

            //launch tether settings
            AccessibilityServiceHelper.launchTetherSettings(context)
        }
    }
}
