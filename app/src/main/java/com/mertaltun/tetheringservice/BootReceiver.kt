package com.mertaltun.tetheringservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            val  usbConnected = AccessibilityServiceHelper.checkUsbConnection(context)
            Log.d("BootReceiver", "USB Connected: $usbConnected")

            if(usbConnected)
            {
                //launch tether settings
                AccessibilityServiceHelper.launchTetherSettings(context,"BootReceiver")
            }
            else
            {
                //launch injector
                AccessibilityServiceHelper.setServiceActive(context, "UsbTethering", true)
                AccessibilityServiceHelper.launchApp(context, "com.evozi.injector","BootReceiver")
            }

            // PeriodicWorker'ı başlat
            AccessibilityServiceHelper.scheduleJob(context)
            Log.d("BootReceiver", "PeriodicWorker tamamlandı")
        }
    }
}
