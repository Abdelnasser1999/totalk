package com.callberry.callingapp.ui.fragment

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.ContactAdapter
import com.callberry.callingapp.materialdialog.MaterialProgressDialog
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.util.UIUtil
import com.callberry.callingapp.util.exchangeView
import com.callberry.callingapp.viewmodel.ContactViewModel
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext


class ContactFragment : Fragment(R.layout.fragment_contact), ContactAdapter.ContactSelectListener {

    private lateinit var progressDialog: MaterialProgressDialog
    private lateinit var viewModel: ContactViewModel
    private lateinit var adapter: ContactAdapter
    private var isContactImported = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

    }

    override fun onResume() {
        super.onResume()
        getContacts()
    }

    private fun getContacts() {
        viewModel.getContacts()
        viewModel.contacts().observe(this, Observer {
            if (it == null) {
                exchangeView(layoutNoContacts, layoutProgress)
                return@Observer
            }
            isContactImported = true
            adapter = ContactAdapter(context!!, ArrayList(it), this@ContactFragment)
            recycleViewContacts.layoutManager = LinearLayoutManager(context)
            recycleViewContacts.adapter = adapter
            exchangeView(
                recycleViewContacts,
                arrayListOf(layoutNoContacts as View, layoutProgress as View)
            )
        })
    }

    private val importContactListener = View.OnClickListener {
        Permissions.check(context, Manifest.permission.READ_CONTACTS, null,
            object : PermissionHandler() {
                override fun onGranted() {
                    showProgress(getString(R.string.importing_contacts))
                    viewModel.importContacts {
                        dismissProgress()
                        getContacts()
                    }
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    UIUtil.popPermissionDenied(activity!!) {
                        if (it) {
                            showProgress(getString(R.string.importing_contacts))
                            viewModel.importContacts {
                                dismissProgress()
                                getContacts()
                            }
                        }
                    }
                }
            })
    }

    private fun init() {
        viewModel = ContactViewModel.getInstance(activity!!)
        progressDialog = MaterialProgressDialog(activity!!)
        txtViewImportContacts.setOnClickListener(importContactListener)

    }

    private fun showProgress(msg: String) {
        progressDialog.setCancelable(false)
        progressDialog.setMessage(msg)
        progressDialog.show()
    }

    private fun dismissProgress() {
        progressDialog.dismiss()
    }

    fun searchContact(query: String) {
        if (isContactImported) {
            adapter.getFilter()!!.filter(query)
        }
    }

    override fun onContactSelect(contact: Contact) {
        val permissions =
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)
        Permissions.check(context!!, permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                InitCallFragment.init(childFragmentManager, contact)
            }
        })
    }

}
