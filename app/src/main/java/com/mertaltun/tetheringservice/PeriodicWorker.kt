package com.mertaltun.tetheringservice

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log
import androidx.core.app.NotificationCompat

class PeriodicWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    @SuppressLint("Wakelock")
    override fun doWork(): Result {
        AccessibilityServiceHelper.clearServiceState(applicationContext, "AirplaneMode")
        AccessibilityServiceHelper.clearServiceState(applicationContext, "HttpInjector")

        Log.d("PeriodicWorker", "...............Periodic Worker is running...............")
        //showNotification()

        wakeUp(applicationContext)
        return Result.success()
    }

    private fun wakeUp(context: Context)
    {
        // PowerManager ile ekran durumu kontrolü
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive // Ekranın açık olup olmadığını kontrol et

        if (!isScreenOn) {
            // Ekran kapalıysa WakeLock al ve ekranı aç
            val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:MyWakeLock")
            wakeLock.acquire(15 * 1000L) // 10 saniye boyunca ekran açık kalacak

            Handler(Looper.getMainLooper()).postDelayed({

                //httpInjector a gidecek
                AccessibilityServiceHelper.launchApp(context, "com.evozi.injector","PeriodicWorker")

                if (wakeLock.isHeld) {
                    wakeLock.release() // WakeLock'u bırak
                }
            }, 15 * 1000L) // 10 saniye sonra işlemi tamamla
        }
        else
        {
            //httpInjector a gidecek
            AccessibilityServiceHelper.launchApp(context, "com.evozi.injector","PeriodicWorker")
        }
    }

    private fun showNotification()
    {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(applicationContext, "my_channel_id")
            .setContentTitle("Uyarı")
            .setContentText("Worker tetiklendi!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
