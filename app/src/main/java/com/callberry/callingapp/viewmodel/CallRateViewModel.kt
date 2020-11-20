package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.callberry.callingapp.model.remote.callrates.CallRate
import com.callberry.callingapp.model.remote.callrates.CallRates
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallRateViewModel(val app: Application) : AndroidViewModel(app) {

    private val apiService = ApiHelper.apiService()
    private val callRates = MutableLiveData<List<CallRate>>()
    private val callRateByDialCode = MutableLiveData<CallRate>()

    fun callRates(): LiveData<List<CallRate>> {
        return callRates
    }

    fun callRatesByCode(): LiveData<CallRate> {
        return callRateByDialCode
    }

    fun getCallRates() {
        apiService.getCallRates(Utils.getToken()).enqueue(object : Callback<CallRates> {
            override fun onFailure(call: Call<CallRates>, t: Throwable) {
                Log.d(Constants.CALL_RATES_LOGS, "getCallRates:onFailure")
                callRates.postValue(null)
            }

            override fun onResponse(call: Call<CallRates>, response: Response<CallRates>) {
                if (!response.isSuccessful) {
                    Log.d(Constants.CALL_RATES_LOGS, "getCallRates:onResponse -> Not Successful")
                    callRates.postValue(null)
                    return
                }

                val remoteCallRates = response.body()
                Log.d(
                    Constants.CALL_RATES_LOGS,
                    "getCallRates:onResponse -> ${remoteCallRates?.callRates}"
                )
                if (remoteCallRates?.callRates != null) {
                    val rates = remoteCallRates.callRates
                    callRates.postValue(rates)
                } else {

                    callRates.postValue(null)
                }
            }
        })
    }

    fun getCallRates(diaCode: String) {
        var code = diaCode
        if (!code.contains("+")) {
            code = "+$diaCode"
        }
        apiService.getCallRateByCode(code, Utils.getToken())
            .enqueue(object : Callback<CallRates> {
                override fun onFailure(call: Call<CallRates>, t: Throwable) {
                    callRateByDialCode.postValue(null)
                }

                override fun onResponse(call: Call<CallRates>, response: Response<CallRates>) {
                    if (!response.isSuccessful) {
                        callRateByDialCode.postValue(null)
                        return
                    }

                    val res = response.body()
                    if (res?.callRates != null) {
                        val callRates = res.callRates
                        if (callRates != null && callRates.isNotEmpty()) {
                            callRateByDialCode.postValue(callRates[0])
                        } else {
                            callRateByDialCode.postValue(null)
                        }
                    } else {
                        callRateByDialCode.postValue(null)
                    }
                }
            })
    }

    companion object {
        fun getInstance(activity: Activity): CallRateViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(CallRateViewModel::class.java)
        }
    }

}