package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
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
                    performTetheringSwitchToggle(it)
                }
                else {
                    airplaneModeToggle(it)
                }
            }
        }
    }

    private fun airplaneModeToggle(node: AccessibilityNodeInfo) {
        Log.d("AccessibilityService", "airplaneModeToggle")
        //val isHaveAirplaneMode  = node.findAccessibilityNodeInfosByText("Uçak modu").firstOrNull()

        val switches = mutableListOf<AccessibilityNodeInfo>()
        //findSwitchesRecursively(node, switches)
        UiHelper.findSwitchesRecursively(node, switches)
        Log.d("AccessibilityService", "Switches found: ${node.childCount} ${switches.size}")

        if (switches.size == 1) {
            val airplaneModeSwitch = switches[0]
            Log.d("AccessibilityService", "Switch found: ${airplaneModeSwitch.isChecked} ${airplaneModeSwitch.isClickable}")
            if (airplaneModeSwitch.isClickable && !airplaneModeSwitch.isChecked) {
                //Uçak modu açık değilse aç
                airplaneModeSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                AccessibilityServiceHelper.setServiceActive(this, "AirplaneMode", true)
                Thread.sleep(2000)
            }

            if(airplaneModeSwitch.isClickable && !airplaneModeSwitch.isChecked)
            {
                //Uçak modu açıksa kapat
                airplaneModeSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                AccessibilityServiceHelper.setServiceActive(this, "AirplaneMode", false)
                Thread.sleep(2000)
            }

            //start http injector app
            AccessibilityServiceHelper.launchApp(this, "com.evozi.injector")
        }
    }

    private fun performTetheringSwitchToggle(node: AccessibilityNodeInfo) {
        Log.d("AccessibilityService", "performTetheringSwitchToggle")
        val switches = mutableListOf<AccessibilityNodeInfo>()
            //findSwitchesRecursively(node, switches)
        UiHelper.findSwitchesRecursively(node, switches)
        Log.d("AccessibilityService", "Switches found: ${node.childCount} ${switches.size}")

        if (switches.size == 2 && node.childCount==1) {
            //val usbTetheringSwitch = switches[1]
            val usbTetheringSwitch = switches[0]
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
            AccessibilityServiceHelper.launchApp(this, "com.gorillasoftware.everyproxy")
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
