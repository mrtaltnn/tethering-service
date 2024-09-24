package com.mertaltun.tetheringservice

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class UsbConnectionReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        //clear service state
        AccessibilityServiceHelper.clearServiceState(context, "UsbTethering")
        AccessibilityServiceHelper.clearServiceState(context, "ProxyServer")
        AccessibilityServiceHelper.clearServiceState(context, "HttpInjector")

        val usbConnected = intent.getBooleanExtra("connected", false)

        if(usbConnected)
        {
            //launch tether settings
            AccessibilityServiceHelper.launchTetherSettings(context)
        }

//        if (usbConnected) {
//            val serviceIntent = Intent(context, TetheringService::class.java)
//            ContextCompat.startForegroundService(context, serviceIntent)
//
//        } else {
//            val stopIntent = Intent(context, TetheringService::class.java)
//            context.stopService(stopIntent)
//        }
    }
}
