package com.mertaltun.tetheringservice

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //clear service state
        AccessibilityServiceHelper.clearServiceState(this, "UsbTethering")
        AccessibilityServiceHelper.clearServiceState(this, "ProxyServer")
        AccessibilityServiceHelper.clearServiceState(this, "HttpInjector")

        // PeriodicWorker'ı başlat
        AccessibilityServiceHelper.scheduleJob(this)
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .setRequiresCharging(true)
//            .build()
//
//        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(2, TimeUnit.MINUTES)
//            .setConstraints(constraints)
//            .build()
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "PeriodicWorker", // Unique iş ismi
//            ExistingPeriodicWorkPolicy.KEEP, // Var olan işi koru, yeni iş başlatma
//            periodicWorkRequest
//        )

        //launch accesibility settings
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//        val intent = Intent()
//        intent.setClassName("com.android.settings", "com.android.settings.SubSettings}")
        startActivity(intent)

        requestPermissions()
        setContentView(R.layout.activity_main)
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
}
