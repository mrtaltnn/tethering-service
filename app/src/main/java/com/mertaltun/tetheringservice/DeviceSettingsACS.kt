package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log


class DeviceSettingsACS : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        ForegroundServiceHelper.startForegroundService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val rootNode = rootInActiveWindow
            val isUsbTetheringActive = AccessibilityServiceHelper.isServiceActive(this, "UsbTethering")
            val isAirplaneModeActive = AccessibilityServiceHelper.isServiceActive(this, "AirplaneMode")
            Log.d("DeviceSettingsACS", "onAccessibilityEvent: UsbTetheringActive: $isUsbTetheringActive, AirplaneModeActive: $isAirplaneModeActive")

            rootNode?.let {
                if (!isUsbTetheringActive) {
                    handleUsbTethering(it)
                }
                else {
                    handleAirplaneMode(it)
                }
            }
        }
    }

    private fun handleUsbTethering(node: AccessibilityNodeInfo) {
        Log.d("AccessibilityService", "handleUsbTethering")

        val usbTetheringSwitch = UiHelper.findSwitchAtIndex(node, 1,2)
//        val usbTetheringSwitch = UiHelper.findSwitchAtIndex(node, 0,2)

        usbTetheringSwitch?.let {
            Log.d("AccessibilityService", "Switch found: ${usbTetheringSwitch.isChecked} ${usbTetheringSwitch.isClickable}")
            if (usbTetheringSwitch.isClickable && !usbTetheringSwitch.isChecked) {
                usbTetheringSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(2000)
            }
            else
            {
                Log.d("AccessibilityService", "Switch is not clickable or already checked")
            }

            AccessibilityServiceHelper.setServiceActive(this, "UsbTethering", true)
            //start proxy app
            AccessibilityServiceHelper.launchApp(this, "com.gorillasoftware.everyproxy","DeviceSettings")
        }
    }

    private fun handleAirplaneMode(node: AccessibilityNodeInfo) {
            val airplaneModeSwitch = UiHelper.findSwitchAtIndex(node, 0,1)
            airplaneModeSwitch?.let {
                if (it.isChecked) {
                    // Uçak modu açıksa kapat
                    Thread.sleep(2000)
                    it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Thread.sleep(3000)
                    AccessibilityServiceHelper.launchApp(this, "com.evozi.injector","DeviceSettings")
                }
                else {
                    // Uçak modu kapalıysa aç, 3 saniye bekle ve tekrar kapat
                    it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Handler(Looper.getMainLooper()).postDelayed({
                        it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(3000)
                        AccessibilityServiceHelper.launchApp(this, "com.evozi.injector","DeviceSettings")
                    }, 3000)
                }
            }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "onInterrupt")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        ForegroundServiceHelper.stopForegroundService(this)
    }
}
