package com.callberry.callingapp.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.callberry.callingapp.R
import com.callberry.callingapp.ui.activity.MainActivity
import com.callberry.callingapp.util.Constants

class BonusWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        createNotification(context)
        return Result.success()
    }

    private fun createNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var builder: NotificationCompat.Builder?
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel("ID", "Name", importance)
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(context, notificationChannel.id)
        } else {
            NotificationCompat.Builder(context, "alert")
        }
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.putExtra(Constants.NOTIFICATION_INTENT, true)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        builder = builder.setSmallIcon(R.drawable.ic_winner)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setContentTitle(context.getString(R.string.noti_bonus_title))
            .setContentText(context.getString(R.string.noti_bonus_desp))
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        notificationManager.notify(122, builder.build())
    }
}