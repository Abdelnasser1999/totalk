package com.callberry.callingapp.util.contact

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ContactWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}