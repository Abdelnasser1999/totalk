package com.callberry.callingapp.util

import com.callberry.callingapp.model.prefmodel.FacebookAd
import com.callberry.callingapp.model.prefmodel.GoogleAdmob
import com.callberry.callingapp.model.prefmodel.GoogleBilling
import com.callberry.callingapp.model.prefmodel.Sinch
import com.callberry.callingapp.model.prefmodel.RemoteCredit
import com.callberry.callingapp.model.prefmodel.Package

class DefaultConfig {
    companion object {
        val billing = GoogleBilling(
                "0a24328c-92f4-4f3d-aa59-92aad1bb8798",
                "5uO1YVv1NEG35rq+XM/G8A==",
                "clientapi.sinch.com"
        )
        val sinch = Sinch(
                "21037c5c-ae88-4fc8-b48c-9fb08515de36",
                "0ACS/aSUPUaCkgeUY3h+gg==",
                "clientapi.sinch.com",
                "163350"
        )
        val googleAdmob = GoogleAdmob(
                "ca-app-pub-3940256099942544~3347511713",
                "ca-app-pub-3940256099942544/6300978111",
                "ca-app-pub-3940256099942544/2247696110",
                "ca-app-pub-3940256099942544/5224354917",
                "ca-app-pub-3940256099942544/1033173712"
        )
        val facebookAd = FacebookAd("", "", "")
        val creditsLimits = arrayListOf(
                RemoteCredit(Constants.BONUS_CREDIT, "0.1, 0.001"),
                RemoteCredit(Constants.CHECK_IN_CREDIT, "1, 0.01"),
                RemoteCredit(Constants.WATCH_VIDEOS_CREDIT, "2, 0.1")
        )
        val packages = arrayListOf(
                Package("Starter", 5),
                Package("Bronze", 10),
                Package("Silver", 30),
                Package("Golden", 100)
        )
    }
}