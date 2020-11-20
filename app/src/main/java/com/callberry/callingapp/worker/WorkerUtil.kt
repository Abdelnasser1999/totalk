package com.callberry.callingapp.worker

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.work.*
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.SharedPrefUtil
import java.util.*
import java.util.concurrent.TimeUnit

class WorkerUtil {
    companion object {
        fun startBonusAlarm(context: Context) {
            if (SharedPrefUtil.getBoolean(Constants.IS_BONUS_NOTIFICATION_ENABLED, true)) {
                val bonusWork = OneTimeWorkRequest.Builder(BonusWorker::class.java)
                    .setInitialDelay(1, TimeUnit.HOURS)
                    .build()
                val workManager = WorkManager.getInstance(context)
                workManager.enqueue(bonusWork)
            }
        }

        fun syncIfNeeded(context: Context) {
            Log.d("pref", "Sync if needed")
            val lastSync = SharedPrefUtil.getLong(Constants.LAST_SYNC)
            if (lastSync == null || !DateUtils.isToday(lastSync)) {
                runSyncJob(context)
            }
        }

        private fun runSyncJob(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val syncWorker = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(syncWorker)
        }
    }
}
