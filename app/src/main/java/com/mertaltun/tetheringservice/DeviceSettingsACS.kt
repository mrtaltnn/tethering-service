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

    private fun airplaneModeToggle(node: AccessibilityNodeInfo) {
        Log.d("AccessibilityService", "airplaneModeToggle operations")

        val switches = mutableListOf<AccessibilityNodeInfo>()
        UiHelper.findSwitchesRecursively(node, switches)
        Log.d("AccessibilityService", "Switches found: ${node.childCount} ${switches.size}")

        if (switches.size == 1) {
            val airplaneModeSwitch = switches[0]
            Log.d("AccessibilityService", "Switch found: ${airplaneModeSwitch.isChecked} ${airplaneModeSwitch.isClickable}")

//            if (!AccessibilityServiceHelper.isAirplaneModeOn(this)) {
            if (airplaneModeSwitch.isClickable && !airplaneModeSwitch.isChecked) {
                airplaneModeSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(3000)
            }

            if(AccessibilityServiceHelper.isAirplaneModeOn(this))
            {
                airplaneModeSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(5000)
            }

            val isHttpInjectorCaller = AccessibilityServiceHelper.isServiceActive(this, "HttpInjector-caller")
            val isCellularAvailable = AccessibilityServiceHelper.checkCellularConnection(this)

            if(isCellularAvailable)
            {
                //start http injector app
                AccessibilityServiceHelper.launchApp(this, "com.evozi.injector","DeviceSettings")
            }
            else
            {
                Log.d("AccessibilityService", "HttpInjectorCaller: $isHttpInjectorCaller, CellularAvailable: $isCellularAvailable")
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    private fun handleUsbTethering2(node: AccessibilityNodeInfo) {
        Log.d("AccessibilityService", "handleUsbTethering")
        val switches = mutableListOf<AccessibilityNodeInfo>()
        UiHelper.findSwitchesRecursively(node, switches)
        Log.d("AccessibilityService", "Switches found: ${node.childCount} ${switches.size}")

        if (switches.size == 2 && node.childCount==1) {
//            val usbTetheringSwitch = switches[1]
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
            AccessibilityServiceHelper.launchApp(this, "com.gorillasoftware.everyproxy","DeviceSettings")
        }
    }

    private fun handleUsbTethering(node: AccessibilityNodeInfo) {
        Log.d("AccessibilityService", "handleUsbTethering")

//        val usbTetheringSwitch = UiHelper.findSwitchAtIndex(node, 1,2)
        val usbTetheringSwitch = UiHelper.findSwitchAtIndex(node, 0,2)

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
