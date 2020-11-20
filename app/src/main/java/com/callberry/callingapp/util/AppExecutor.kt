package com.callberry.callingapp.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppExecutor private constructor() {
    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val threadPoolExecutor: ExecutorService
    private val mainThread: Executor

    init {
        mainThread = MainThreadExecutor()
        threadPoolExecutor = Executors.newFixedThreadPool(3)
    }

    fun singleThreadExecutor(): ExecutorService {
        return singleThreadExecutor
    }

    fun threadPoolExecutor(): ExecutorService {
        return threadPoolExecutor
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

    companion object {
//        private var appExecutor: AppExecutor? = null
//        val instance: AppExecutor?
//            get() {
//                if (appExecutor == null) {
//                    appExecutor = AppExecutor()
//                }
//                return appExecutor
//            }

        @Volatile
        private var instance: AppExecutor? = null
        private val LOCK = Any()

        operator fun invoke() = instance
            ?: synchronized(LOCK) {
                instance ?: getInstance().also {
                    instance = it
                }
            }

        private fun getInstance(): AppExecutor {
            return AppExecutor()
        }

    }

}