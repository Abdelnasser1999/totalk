package com.callberry.callingapp

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.callberry.callingapp.ui.LandingActivity
import com.callberry.callingapp.ui.activity.MainActivity
import com.callberry.callingapp.util.*


class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_launcher)

        Log.d("localName: ", localClassName)
        routeApp()

    }

    private fun routeApp() {
        Handler().postDelayed({
            if (PrefUtils.getBoolean(Constants.IS_SETUP_COMPLETE, false)) {
                route(MainActivity::class)
            } else {
                route(LandingActivity::class)
            }
            finish()
        }, 500)
    }

}
