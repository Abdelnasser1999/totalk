package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.database.dao.CreditDao
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.remote.credits.RemoteCredit
import com.callberry.callingapp.model.remote.credits.RemoteCredits
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreditViewModel(val app: Application) : AndroidViewModel(app) {

    private val apiService = ApiHelper.apiService()
    private var creditDao: CreditDao = AppDatabase(app).creditDao()
    private val creditHistory = MutableLiveData<List<Credit>>()
    private val remoteCreditsModel = MutableLiveData<List<RemoteCredit>>()
    private val localCredits = MutableLiveData<List<RemoteCredit>>()
    private val currentBalance = MutableLiveData<Float>()

    fun creditHistory(): LiveData<List<Credit>> = creditHistory

    fun remoteCredits(): LiveData<List<RemoteCredit>> = remoteCreditsModel

    fun localCredits(): LiveData<List<RemoteCredit>> = localCredits

    fun getCreditHistory() {
        CoroutineScope(IO).launch {
            if (creditDao.getCredits().isEmpty()) {
                creditHistory.postValue(null)
            } else {
                creditHistory.postValue(creditDao.getCredits())
            }
        }
    }

    fun getCreditHistoryByName(name: String, callback: (credit: Credit?) -> Unit) {
        CoroutineScope(IO).launch {
            val credit = creditDao.getLastCreditByName(name)
            withContext(Main) {
                callback.invoke(credit)
            }

        }
    }

    fun getLocalCredits(callback: (remote: List<RemoteCredit>?) -> Unit) {
        CoroutineScope(IO).launch {
            var localCredits = creditDao.getLocalCredits()
            if (localCredits.isEmpty()) {
                localCredits = Constants.defaultCreditsLimits
            }
            withContext(Default) {
                callback.invoke(localCredits)
            }
        }
    }


    fun updateCredits(name: String, credits: Float, type: String) {
        val credit = Credit()
        credit.name = name
        credit.credit = credits
        credit.type = type
        CoroutineScope(IO).launch {
            creditDao.insert(credit)
            Utils.updateCredit(credits, type)
            Log.d("Balance", "Credit Updated ${Utils.getBalance()}")
        }
    }

    fun getRemoteCredits() {
//        CoroutineScope(IO).launch {
//            val result = apiService.getRemoteCredits(Utils.getToken(app)).execute()
//            if (result.isSuccessful) {
//                val response = result.body()
//                Log.d("Login", "Response $response")
//                response?.let { res ->
//                    Log.d("Login", "Response ${res.remoteCredits!!.size}")
//                    res.remoteCredits?.let { remoteCredits ->
//                        CoroutineScope(IO).launch {
//                            remoteCredits.forEach {
//                                creditDao.insert(it)
//                            }
//                            remoteCreditsModel.postValue(remoteCredits)
//                        }
//                    }
//                }
//            }
//        }

        apiService.getRemoteCredits(Utils.getToken()).enqueue(object : Callback<RemoteCredits> {
            override fun onFailure(call: Call<RemoteCredits>, t: Throwable) {
                remoteCreditsModel.postValue(null)
            }

            override fun onResponse(call: Call<RemoteCredits>, response: Response<RemoteCredits>) {
                if (!response.isSuccessful) {
                    remoteCreditsModel.postValue(null)
                    return
                }
                val res = response.body()
                if (res?.remoteCredits != null) {
                    val remoteCredit = res.remoteCredits
                    if (remoteCredit != null && remoteCredit.isNotEmpty()) {
                        CoroutineScope(IO).launch {
                            remoteCredit.forEach {
                                creditDao.insert(it)
                            }
                            remoteCreditsModel.postValue(remoteCredit)
                        }
                    } else {
                        remoteCreditsModel.postValue(null)
                    }
                } else {
                    remoteCreditsModel.postValue(null)
                }
            }

        })
    }

    companion object {
        fun getInstance(activity: Activity): CreditViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(CreditViewModel::class.java)
        }
    }

}