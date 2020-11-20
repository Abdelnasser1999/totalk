package com.callberry.callingapp.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.callberry.callingapp.model.prefmodel.PreferenceModel
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SyncWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val call = ApiHelper.apiService().getPreference(Utils.getToken())
        call.enqueue(object : Callback<PreferenceModel> {
            override fun onFailure(call: Call<PreferenceModel>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<PreferenceModel>, response: Response<PreferenceModel>
            ) {
                if (!response.isSuccessful) {
                    return
                }

                response.body()?.apply {
                    prefSettings?.run {
                        sinch?.let { PreferenceUtil.setSinchCred(it) }
                        googleBilling?.let { PreferenceUtil.setBillingCred(it) }
                        googleAdmob?.let { PreferenceUtil.setGoogleAdmob(it) }
                        facebookAd?.let { PreferenceUtil.setFacebookAd(it) }
                        remoteCredits?.let { PreferenceUtil.setRemoteCredits(it) }
                        remotePackages?.let { PreferenceUtil.setPackages(it) }
                        SharedPrefUtil.setLong(Constants.LAST_SYNC, System.currentTimeMillis())
                    }
                }
            }
        })
        return Result.success()
    }


}