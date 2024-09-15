package com.mertaltun.tetheringservice

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            val tetherSettingsIntent = Intent().apply {
                component = ComponentName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(tetherSettingsIntent)

            val serviceIntent = Intent(context, TetheringService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
