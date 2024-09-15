package com.mertaltun.tetheringservice

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log


class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            //find and click switches in this event
            val rootNode = rootInActiveWindow
            rootNode?.let {
                val activeWindowClassName = event.className.toString()
                val activeWindowPackageName = event.packageName.toString()
                Log.d("AccessibilityService", "Active Window Class Name: $activeWindowClassName")
                Log.d("AccessibilityService", "Active Window Package Name: $activeWindowPackageName")
                findAndClickSwitches(it)
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "onInterrupt")
        stopSelf()
    }

    private fun performTetheringSwitchToggle() {
        Log.d("AccessibilityService", "performTetheringSwitchToggle")
        val rootNode = rootInActiveWindow
        rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        rootNode?.let {
            findAndClickSwitches(it)
        }

    }

    private fun findAndClickSwitches(node: AccessibilityNodeInfo) {
        val switches = mutableListOf<AccessibilityNodeInfo>()
        findSwitchesRecursively(node, switches)
        Log.d("AccessibilityService", "Switches found: ${node.childCount} ${switches.size}")

        if (switches.size == 2 && node.childCount==1) {
            val usbTetheringSwitch = switches[1]
            Log.d("AccessibilityService", "Switch found: ${usbTetheringSwitch.isChecked} ${usbTetheringSwitch.isClickable}")
            if (usbTetheringSwitch.isClickable && !usbTetheringSwitch.isChecked) {
                usbTetheringSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    private fun findSwitchesRecursively(node: AccessibilityNodeInfo, switches: MutableList<AccessibilityNodeInfo>) {
        if (node.className == "android.widget.Switch") {
            switches.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            child?.let {
                findSwitchesRecursively(it, switches)
            }
        }
    }
}
