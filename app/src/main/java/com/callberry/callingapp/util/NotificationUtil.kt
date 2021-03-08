package com.callberry.callingapp.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.callberry.callingapp.R
import com.callberry.callingapp.ui.activity.CallActivity

object NotificationUtil {
    val CHANNEL_ID = "outgoingCallChannelId"

    @JvmStatic
    fun createNotification(context: Context): Notification {
        val notificationIntent = Intent(context, CallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.drawable.ic_call)
        builder.setContentTitle(context.resources.getString(R.string.app_name))
        builder.setContentText(context.resources.getString(R.string.call_in_progress))
        builder.setContentIntent(pendingIntent)
        return builder.build();
    }

    @JvmStatic
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, context.resources.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}