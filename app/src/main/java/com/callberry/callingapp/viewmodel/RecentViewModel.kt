package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.database.dao.RecentDao
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.model.RecentContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentViewModel(app: Application) : AndroidViewModel(app) {

    private val recentDao: RecentDao = AppDatabase(app).recentDao()
    private val recentContacts = MutableLiveData<List<RecentContact>>()
    private val recentLogs = MutableLiveData<List<Recent>>()

    fun recentContacts(): LiveData<List<RecentContact>> = recentContacts

    fun recentLogs(): LiveData<List<Recent>> = recentLogs

    fun getRecentContacts() {
        CoroutineScope(IO).launch {
            val recent = recentDao.getRecentContacts()
            recentContacts.postValue(
                if (recent.isNotEmpty()) recent
                else null
            )
        }
    }

    fun getRecentLogs(phoneNo: String) {
        CoroutineScope(IO).launch {
            val callLogs = recentDao.getRecentByPhoneNo(phoneNo)
            recentLogs.postValue(
                if (callLogs.isNotEmpty()) callLogs
                else null
            )
        }
    }

    fun addRecent(recent: Recent, callback: () -> Unit){
        CoroutineScope(IO).launch {
            recentDao.insert(recent)
            withContext(Main){
                callback.invoke()
            }
        }
    }

    companion object {
        fun getInstance(activity: Activity): RecentViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(RecentViewModel::class.java)
        }
    }
}