package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.callberry.callingapp.model.prefmodel.PreferenceModel
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.DefaultConfig
import com.callberry.callingapp.util.PreferenceUtil
import com.callberry.callingapp.util.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreferenceViewModel(val app: Application) : AndroidViewModel(app) {

    private val apiService = ApiHelper.apiService()
    private var prefLoaded = MutableLiveData<Boolean>()
    fun prefLoaded(): LiveData<Boolean> = prefLoaded

    fun loadPref() {
        apiService.getPreference(Utils.getToken()).enqueue(object : Callback<PreferenceModel> {
            override fun onFailure(call: Call<PreferenceModel>, t: Throwable) {
                prefLoaded.postValue(false)
            }

            override fun onResponse(
                call: Call<PreferenceModel>, response: Response<PreferenceModel>
            ) {
                if (!response.isSuccessful) {
                    prefLoaded.postValue(false)
                    return
                }

                var isPrefLoaded = false
                response.body()?.apply {
                    prefSettings?.run {
                        sinch?.let { PreferenceUtil.setSinchCred(it) }
                        googleBilling?.let { PreferenceUtil.setBillingCred(it) }
                        googleAdmob?.let { PreferenceUtil.setGoogleAdmob(it) }
                        facebookAd?.let { PreferenceUtil.setFacebookAd(it) }
                        remoteCredits?.let { PreferenceUtil.setRemoteCredits(it) }
                        remotePackages?.let { PreferenceUtil.setPackages(it) }
                        isPrefLoaded = true
                    }
                }

                prefLoaded.postValue(isPrefLoaded)
            }
        })
    }

    companion object {
        fun getInstance(activity: Activity): PreferenceViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(PreferenceViewModel::class.java)
        }
    }
}