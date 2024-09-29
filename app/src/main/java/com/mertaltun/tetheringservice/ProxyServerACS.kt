package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class ProxyServerACS : AccessibilityService() {

    private val targetPackageName = "com.gorillasoftware.everyproxy"
    private var rootNode: AccessibilityNodeInfo? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ForegroundServiceHelper.startForegroundService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val isProxyActive = AccessibilityServiceHelper.isServiceActive(this, "ProxyServer")
        if (isProxyActive) {
            AccessibilityServiceHelper.launchAirplaneModeSettings(this, "ProxyServer")
        }
        else
        {
            event?.let {
                if (event.packageName == targetPackageName) {
                    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                        rootNode = rootInActiveWindow
                    }

                    rootNode?.let {
                        activateSocksSwitch(it)
                    }
                }
            }
        }
    }

    private fun activateSocksSwitch(node: AccessibilityNodeInfo) {
        val switches = mutableListOf<AccessibilityNodeInfo>()
        UiHelper.findSwitchesRecursively(node, switches)
        Log.d("ProxyServerACS", "Switches found: ${node.childCount} ${switches.size}")

        if (switches.size == 3 && node.childCount==1) {
            val socksProxySwitch = switches[1]
            Log.d("ProxyServerACS", "Switch found: ${socksProxySwitch.isChecked} ${socksProxySwitch.isClickable}")
            if (socksProxySwitch.isClickable && !socksProxySwitch.isChecked)
            {
                socksProxySwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(4000)
            }

            Log.d("ProxyServerACS", "Switch is not clickable or already checked")
            AccessibilityServiceHelper.setServiceActive(this, "ProxyServer", true)

            //launch injector
            AccessibilityServiceHelper.launchApp(this, "com.evozi.injector","ProxyServer")
        }
    }

    override fun onInterrupt() {
        Log.d("ProxyServerACS", "onInterrupt")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        ForegroundServiceHelper.stopForegroundService(this)
    }
}
