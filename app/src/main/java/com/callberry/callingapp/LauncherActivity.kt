package com.callberry.callingapp

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.callberry.callingapp.ui.LandingActivity
import com.callberry.callingapp.ui.activity.MainActivity
import com.callberry.callingapp.ui.activity.OnBoarding
import com.callberry.callingapp.util.*


class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_launcher)

        routeApp()
    }

    private fun checkLogs() {
        val tag = "preferences"
        for (packages in PreferenceUtil.getPackages()) {
            Log.d(tag, "${packages.name}, ${packages.value}")
        }

        val sinch = PreferenceUtil.getSinchCred()
        Log.d(
            tag,
            "Sinch: ${sinch.appKey}, ${sinch.appSecret}, ${sinch.environment}, ${sinch.userId}"
        )

        for (credits in PreferenceUtil.getRemoteCredits()) {
            Log.d(tag, "${credits.name}, ${credits.value}")
        }

        val facebookAd = PreferenceUtil.getFacebookAd()
        Log.d(
            tag,
            "Facebook: ${facebookAd.fbBannerAd}, ${facebookAd.fbInterstitialAd}, ${facebookAd.fbRewardedAd}"
        )

        val admob = PreferenceUtil.getGoogleAdmob()
        Log.d(
            tag,
            "Admob: ${admob.appId}, ${admob.inersitialId}, ${admob.bannerId}, ${admob.rewardedId}, ${admob.bannerId}"
        )


    }

    private fun routeApp() {
        Handler().postDelayed({
            if (SharedPrefUtil.getBoolean(Constants.IS_SETUP_COMPLETE, false)) {
                route(OnBoarding::class)
                //route(MainActivity::class)
            } else {
                route(OnBoarding::class)
                //route(LandingActivity::class)
            }
            finish()
        }, 500)
    }
}
