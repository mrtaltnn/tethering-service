package com.mertaltun.tetheringservice

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

class PeriodicWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        AccessibilityServiceHelper.clearServiceState(applicationContext, "AirplaneMode")
        AccessibilityServiceHelper.clearServiceState(applicationContext, "HttpInjector")

        // Burada çalışmasını istediğin işlemi gerçekleştir.
        Log.d("PeriodicWorker", "Servis çalıştı")

        //launch tether settings
        AccessibilityServiceHelper.launchTetherSettings(applicationContext)

        return Result.success()
    }
}
