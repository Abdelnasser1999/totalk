package com.callberry.callingapp.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.admob.AdHelper
import com.callberry.callingapp.plivo.PlivoBackEnd
import com.callberry.callingapp.plivo.UtilsPlivo
import com.callberry.callingapp.ui.activity.CallScreenActivity
import com.callberry.callingapp.worker.WorkerUtil
import io.paperdb.Paper


class App : Application() {

    lateinit var backend: PlivoBackEnd
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
        createNotificationChannel()
        Paper.init(this)
        if (SharedPrefUtil.getBoolean(Constants.IS_SETUP_COMPLETE, false)) {
            startSync()
        }
        initPlivo()


    }

    private fun initPlivo() {
        UtilsPlivo.options.put("context", applicationContext)
        UtilsPlivo.options.put("sharedContext", applicationContext)
        backend = PlivoBackEnd.newInstance()
        backend.init(true)
    }

    fun backend(): PlivoBackEnd? {
        return backend
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "outgoingCallChannelId"

        private lateinit var context: Context

        fun setContext(context: Context) {
            this.context = context
        }

        fun context(): Context {
            return context
        }

        fun createNotification(context: Context): Notification {
            val notificationIntent = Intent(context, CallScreenActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.outgoing_call))
                .build()
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