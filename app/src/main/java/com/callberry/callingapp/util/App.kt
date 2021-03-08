package com.callberry.callingapp.util

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.admob.AdHelper
import com.callberry.callingapp.worker.WorkerUtil
import io.paperdb.Paper


class App : Application() {

    override fun onCreate() {
        super.onCreate()

//        StrictMode.setVmPolicy(
//            StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build())

        setContext(this)
        NotificationUtil.createNotificationChannel(this)
        Paper.init(this)
        if (PrefUtils.getBoolean(Constants.IS_SETUP_COMPLETE, false)) {
            startSync()
        }

    }

    companion object {
        private lateinit var context: Context

        fun setContext(context: Context) {
            this.context = context
        }

        fun context(): Context {
            return context
        }

        @JvmStatic
        fun startSync() {
            WorkerUtil.syncIfNeeded(context)
            AdHelper.initAdMob(context, DefaultConfig.googleAdmob.appId)
                .setBannerId(DefaultConfig.googleAdmob.bannerId)
                .setInterstitialId(DefaultConfig.googleAdmob.inersitialId)
                .setNativeId(DefaultConfig.googleAdmob.nativeId)
                .setRewardedId(DefaultConfig.googleAdmob.rewardedId)
                .load()

        }
    }
}