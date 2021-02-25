package com.callberry.callingapp.plivo

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.plivo.endpoint.Incoming
import java.util.*

object UtilsPlivo {
    // endpoint username & password
    const val USERNAME = "totalkphlo58108905093193709"
    const val PASSWORD = "appexes"
    const val HH_MM_SS = "%02d:%02d:%02d"
    const val MM_SS = "%02d:%02d"
    private lateinit var mSharedPreferences: SharedPreferences
    private var context: Context? = null
    private var listener: PlivoBackEnd.BackendListener? = null
    var incoming: Incoming? = null
    var loggedinStatus = false
    var options: HashMap<String?, Any?> = object : HashMap<String?, Any?>() {
        init {
            put("enableTracking", true)
        }
    }
    var deviceToken: String?
        get() {
            context = options["sharedContext"] as Context?
            mSharedPreferences = context!!.getSharedPreferences("plivo_refs", Context.MODE_PRIVATE)
            return mSharedPreferences.getString("token", "")
        }
        set(newDeviceToken) {
            context = options["sharedContext"] as Context?
            mSharedPreferences = context!!.getSharedPreferences("plivo_refs", Context.MODE_PRIVATE)
            mSharedPreferences.edit().putString("token", newDeviceToken).apply()
        }
    var backendListener: PlivoBackEnd.BackendListener?
        get() = options["listener"] as PlivoBackEnd.BackendListener?
        set(backendListener) {
            listener = backendListener
            options["listener"] = listener
        }

    fun from(fromContact: String?, fromSip: String?): String {
        val from =
            if (TextUtils.isEmpty(fromContact)) if (TextUtils.isEmpty(fromSip)) "" else fromSip!! else fromContact!!
        return if (from.contains("\"")) from.substring(
            from.indexOf("\"") + 1,
            from.lastIndexOf("\"")
        ) else from
    }

    fun to(toSip: String): String {
        return if (TextUtils.isEmpty(toSip)) "" else toSip.substring(
            toSip.indexOf(":") + 1,
            toSip.indexOf("@")
        )
    }
}