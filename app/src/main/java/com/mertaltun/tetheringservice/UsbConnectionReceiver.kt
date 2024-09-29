package com.mertaltun.tetheringservice

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class UsbConnectionReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        //clear service state
        AccessibilityServiceHelper.clearServiceState(context, "UsbTethering")
        AccessibilityServiceHelper.clearServiceState(context, "ProxyServer")
        AccessibilityServiceHelper.clearServiceState(context, "HttpInjector")

        Thread.sleep(3000)
//        val usbConnected = intent.getBooleanExtra("connected", false)
        val isCellularAvailable = AccessibilityServiceHelper.checkCellularConnection(context)

        if(isCellularAvailable)
        {
            //launch tether settings
            AccessibilityServiceHelper.launchTetherSettings(context,"UsbConnectionReceiver")
        }
        else{
            //TODO bunun ACS si yok
            AccessibilityServiceHelper.launchMobileDataSettings(context, "UsbConnectionReceiver")
        }
    }
}
