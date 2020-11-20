package com.callberry.callingapp.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.database.dao.ContactDao
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.util.Utils
import com.callberry.callingapp.util.contact.ContactLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(val app: Application) : AndroidViewModel(app) {

    private var contactDao: ContactDao = AppDatabase(app).contactDao()
    private var contacts = MutableLiveData<List<Contact>>()

    fun contacts(): LiveData<List<Contact>> = contacts

    fun getContactsByPhoneNo(
        phoneNo: String,
        dialCode: String,
        callback: (contact: Contact) -> Unit
    ) {
        CoroutineScope(IO).launch {
            var contact = contactDao.getContactByPhoneNo(phoneNo)
            withContext(Main) {
                if (contact == null) {
                    contact = Utils.createContact(app, phoneNo, dialCode)
                }
                callback.invoke(contact!!)
            }
        }
    }

    fun getContacts() {
        CoroutineScope(IO).launch {
            var contactsList: List<Contact>? = null
            if (contactDao.countContact() > 0) {
                val dbContacts = contactDao.getContacts()
                contactsList = dbContacts
            }
            withContext(Main) {
                contacts.postValue(contactsList)
            }
        }
    }

    fun importContacts(callback: () -> Unit) {
        CoroutineScope(IO).launch {
            ContactLoader.syncContact(app) { isComplete, msg ->
                if (isComplete) {
                    CoroutineScope(Main).launch {
                        callback.invoke()
                    }
                }
            }
        }
    }

    companion object {
        fun getInstance(activity: Activity): ContactViewModel {
            return ViewModelProvider.AndroidViewModelFactory(activity.application)
                .create(ContactViewModel::class.java)
        }
    }

}