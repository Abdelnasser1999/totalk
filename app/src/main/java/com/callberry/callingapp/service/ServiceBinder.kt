package com.callberry.callingapp.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.callberry.callingapp.util.App
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ServiceBinder {

    companion object {
        @JvmStatic
        fun bind(context: Context, callback: (binder: OutgoingCallService?) -> Unit) {
            var outgoingCallService: OutgoingCallService? = null
            val intent = Intent(context, OutgoingCallService::class.java)
            val conn = object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName?) {
                    callback.invoke(outgoingCallService)
                }

                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    outgoingCallService = service as OutgoingCallService
                    callback.invoke(outgoingCallService)

                }
            }
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
        }

        @JvmStatic
        suspend fun bindService(): OutgoingCallService? {
            val remoteService = Intent(App.context(), OutgoingCallService::class.java)
            val binderService =
                bindServiceAndWait(App.context(), remoteService, Context.BIND_AUTO_CREATE)
            return binderService.service as OutgoingCallService
        }

        private suspend fun bindServiceAndWait(context: Context, intent: Intent, flags: Int) =
            suspendCoroutine<BoundService> { continuation ->
                val conn = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        continuation.resume(BoundService(context, name, service, this))
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {

                    }

                }
                context.bindService(intent, conn, flags)
            }
    }

    class BoundService(
        private val context: Context,
        val name: ComponentName?,
        val service: IBinder?,
        val conn: ServiceConnection?
    ) {
        fun unbind() {
            context.unbindService(conn!!)
        }
    }

    public interface ServiceListener {
        fun onServiceConnected(isBound: Boolean, serviceBinder: IBinder?)
    }
}