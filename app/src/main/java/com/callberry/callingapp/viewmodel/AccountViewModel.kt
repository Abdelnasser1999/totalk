package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.database.dao.AccountDao
import com.callberry.callingapp.model.Account
import com.callberry.callingapp.model.remote.account.RemoteAccounts
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountViewModel(val app: Application) : AndroidViewModel(app) {

    private val apiService = ApiHelper.apiServiceLogin()
    private var setupAccount = MutableLiveData<Boolean>()

    fun setupAccount(): LiveData<Boolean> = setupAccount

    fun setupAccount(localAccount: Account) {
        val call =
            apiService.login(localAccount.phoneNo, localAccount.dialCode, localAccount.refCode)
        call.enqueue(object : Callback<RemoteAccounts> {
            override fun onFailure(call: Call<RemoteAccounts>, t: Throwable) {
                Log.d("response", "Response Failed")
                setupAccount.postValue(false)
            }

            override fun onResponse(call: Call<RemoteAccounts>, response: Response<RemoteAccounts>) {
                if (!response.isSuccessful || response.body() == null || response.body()?.remoteUser == null || response.body()?.remoteUser?.status != 200) {
                    setupAccount.postValue(false)
                    return
                }

                val remote = response.body()?.remoteUser!!
                remote.token?.let { Utils.setToken(it) }
                remote.account?.let {
                    Utils.createAccount(it)
                    if (remote.flag == 1) {
                            Utils.updateCredit(it.balance.toFloat(), Constants.CREDIT)
//                        Log.d("response", "Balance ${it.balance}")
                    }
                    setupAccount.postValue(true)
                }
            }
        })
    }

    companion object {
        fun getInstance(activity: Activity): AccountViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(AccountViewModel::class.java)
        }
    }


}