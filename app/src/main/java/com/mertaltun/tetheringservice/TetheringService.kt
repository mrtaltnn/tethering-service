package com.mertaltun.tetheringservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class TetheringService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        enableUsbTethering()
        startForeground(1, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun enableUsbTethering() {
        try {
//            val usbTethering = Settings.Global.getInt(contentResolver, "tethering_on")
//            if (usbTethering == 0) {
//                Settings.Global.putInt(contentResolver, "tethering_on", 1)
//                Log.d("mert UsbTetheringService", "USB Tethering enabled")
//            }
            val tetherIntent = Intent()
            tetherIntent.setClassName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
            tetherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(tetherIntent)
            Log.d("TetheringService", "USB Tethering enabled")
        } catch (e: Exception) {
            Log.e("etheringService", "Error enabling USB Tethering", e)
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "TetheringServiceChannel"

        val channel = NotificationChannel(
            notificationChannelId,
            "Tethering Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("USB Tethering Service")
            .setContentText("Automatically enabling USB tethering...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }
}
