package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.model.remote.packages.RemotePackage
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.DefaultConfig
import com.callberry.callingapp.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class PackageViewModel(val app: Application) : AndroidViewModel(app) {

    private val apiService = ApiHelper.apiService()
    private val packages = MutableLiveData<List<RemotePackage>>()
    private val packageDao = AppDatabase(app).packageDao()

    fun packages(): LiveData<List<RemotePackage>> = packages

    fun getLocalPackages() {
//        CoroutineScope(IO).launch {
//            if (packageDao.getPackages().isEmpty()) {
//                packages.postValue(DefaultConfig.packages)
//            } else {
//                packages.postValue(packageDao.getPackages())
//            }
//
//        }
    }

    fun getRemotePackages() {
        CoroutineScope(IO).launch {
            val result = apiService.getPackages(Utils.getToken()).execute()
            if (result.isSuccessful) {
                val response = result.body()
                response?.let { res ->
                    res.packages?.let { remotePack ->
                        CoroutineScope(IO).launch {
                            remotePack.forEach {
                                packageDao.insert(it)
                            }
                        }
                    }
                }
            }
        }

//        apiService.getPackages(Utils.getToken(app)).enqueue(object : Callback<RemotePackages> {
//            override fun onFailure(call: Call<RemotePackages>, t: Throwable) {
//                packages.postValue(null)
//            }
//
//            override fun onResponse(
//                call: Call<RemotePackages>,
//                response: Response<RemotePackages>
//            ) {
//                if (!response.isSuccessful) {
//                    packages.postValue(null)
//                    return
//                }
//
//                val res = response.body()
//                if (res?.packages != null) {
//                    val pkgs = res.packages
//                    if (pkgs != null) {
//                        CoroutineScope(IO).launch {
//                            pkgs.forEach {
//                                packageDao.insert(it)
//                            }
//                            packages.postValue(pkgs)
//                        }
//                    } else {
//                        packages.postValue(null)
//                    }
//                } else {
//                    packages.postValue(null)
//                }
//            }
//
//        })
    }

    companion object {
        fun getInstance(activity: Activity): PackageViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(PackageViewModel::class.java)
        }
    }
}