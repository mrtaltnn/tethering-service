package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class HttpInjectorACS : AccessibilityService() {

    private var rootNode: AccessibilityNodeInfo? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ForegroundServiceHelper.startForegroundService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
            event?.let {
                    rootNode = rootInActiveWindow
                    rootNode?.let {
                        checkAndRunHttpInjector(it)
                    }
                }
            }

    private fun checkAndRunHttpInjector(node: AccessibilityNodeInfo) {
        Log.d("HttpInjectorACS", "checkAndRunHttpInjector operations")

        val lastLogs = UiHelper.getAllTextViewsText(node).takeLast(2)
        Log.d("HttpInjectorACS", "Last logs: $lastLogs")
        val isVpnConnectionPresent = lastLogs.any { it.contains("[VPN] Bağlandı") }

        if (isVpnConnectionPresent) {
            Log.d("HttpInjectorACS", "VPN connected, no problem")
            AccessibilityServiceHelper.setServiceActive(this,"HttpInjector", true)
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
        else
        {
            Log.d("HttpInjectorACS", "VPN not connected, trying reconnection")
            AccessibilityServiceHelper.setServiceActive(this,"HttpInjector", false)
            activateHttpInjector(node)
        }
    }

    private fun activateHttpInjector(node: AccessibilityNodeInfo) {
        Log.d("HttpInjectorACS", "HttpInjector operations")
        val isPeriodicWorkerCaller = AccessibilityServiceHelper.isServiceActive(this, "PeriodicWorker-caller")
        if(isPeriodicWorkerCaller){
            AccessibilityServiceHelper.clearAllCallerState(this)
            goToMain()
        }

        //Çalışıyosa önce bi durduralım
        stopHttpInjector(node)

        //uçak modundan gelmemişsek uçak moduna gidip aç kapa yapalım, yoksa başlatalım
        val isDeviceSettingsCaller = AccessibilityServiceHelper.isServiceActive(this, "DeviceSettings-caller")
        val isAirplaneModeon = AccessibilityServiceHelper.isAirplaneModeOn(this)

        if(!isDeviceSettingsCaller || isAirplaneModeon)
        {
            Thread.sleep(2000)
            AccessibilityServiceHelper.launchAirplaneModeSettings(this, "HttpInjector")
        }
        else
        {
            val isCellularAvailable = AccessibilityServiceHelper.checkCellularConnection(this)
            if(!isCellularAvailable)
            {
                //TODO bunun ACS si yok
                AccessibilityServiceHelper.launchMobileDataSettings(this, "HttpInjector")
            }
            AccessibilityServiceHelper.setServiceActive(this,"DeviceSettings-caller", false)
            startHttpInjector(node)
            Thread.sleep(2000)
            goToMain()
//            checkAndRunHttpInjector(node)
//            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun goToMain() {
        Thread.sleep(3000)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("HttpInjector", true)
        startActivity(intent)
    }

    private fun startHttpInjector(node: AccessibilityNodeInfo) {
        val startButton = UiHelper.findButtonNode(node, "Başlat")
        if (startButton != null && startButton.isClickable) {
            startButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Thread.sleep(2000)
            AccessibilityServiceHelper.setServiceActive(this, "HttpInjector", true)
            Log.d("HttpInjectorACS", "HttpInjector started")
        }
    }

    private fun stopHttpInjector(node: AccessibilityNodeInfo) {
        val stopButton = UiHelper.findButtonNode(node, "Dur")
        if (stopButton != null && stopButton.isClickable) {
            stopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Thread.sleep(2000)
            AccessibilityServiceHelper.setServiceActive(this, "HttpInjector", false)
            Log.d("HttpInjectorACS", "HttpInjector stopped")
        }
    }

    override fun onInterrupt() {
        Log.d("HttpInjectorACS", "onInterrupt")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        ForegroundServiceHelper.stopForegroundService(this)
    }
}
