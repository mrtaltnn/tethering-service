package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.concurrent.thread

class HttpInjectorACS : AccessibilityService() {

    private val targetPackageName = "com.evozi.injector"
    private var rootNode: AccessibilityNodeInfo? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ForegroundServiceHelper.startForegroundService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val isInjectorActive = AccessibilityServiceHelper.isServiceActive(this, "HttpInjector")
        if (isInjectorActive) {
            return
        }
        event?.let {
            if (event.packageName == targetPackageName) {
                if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    rootNode = rootInActiveWindow
                }

                rootNode?.let {
                    findAndClickButtons(it)
                }
            }
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

    private fun findAndClickButtons(node: AccessibilityNodeInfo) {
            val startButton=findButtonNode("Başlat")
            val stopButton=findButtonNode("Dur")

            if (startButton != null && startButton.isClickable) {
                startButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(2000)
                AccessibilityServiceHelper.setServiceActive(this, "HttpInjector", true)
            }
            else if (stopButton != null && stopButton.isClickable) {
                stopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(2000)
                AccessibilityServiceHelper.setServiceActive(this, "HttpInjector", false)
            }
            else {
                Log.d("HttpInjectorACS", "Button is not clickable")
            }
    }

    private fun findButtonsRecursively(node: AccessibilityNodeInfo, buttons: MutableList<AccessibilityNodeInfo>) {
        if (node.className == "android.widget.Button") {
            buttons.add(node)
        } else {
            for (i in 0 until node.childCount) {
                findButtonsRecursively(node.getChild(i), buttons)
            }
        }
    }

    private fun findButtonNode(name:String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return rootNode.findAccessibilityNodeInfosByText(name)?.firstOrNull()
    }
}
