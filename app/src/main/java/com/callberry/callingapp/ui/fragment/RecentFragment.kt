package com.callberry.callingapp.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.RecentAdapter
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.ui.activity.CallHistoryActivity
import com.callberry.callingapp.util.exchangeView
import com.callberry.callingapp.viewmodel.RecentViewModel
import kotlinx.android.synthetic.main.fragment_recent.*

class RecentFragment : Fragment(R.layout.fragment_recent), RecentAdapter.RecentClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRecentContacts()

    }

    fun loadRecentContacts() {
        Log.e("recents", "loadRecentContacts")
        activity?.let {
            val viewModel = RecentViewModel.getInstance(it)
            viewModel.getRecentContacts()
            viewModel.recentContacts().observe(this, Observer {
                if (it == null) {
                    exchangeView(layoutNoRecents, layoutProgress)
                    return@Observer
                }

                val adapter = RecentAdapter(context!!, ArrayList(it), this)
                listRecentCalls.layoutManager = LinearLayoutManager(context!!)
                listRecentCalls.adapter = adapter
                exchangeView(listRecentCalls, arrayListOf(layoutNoRecents as View, layoutProgress as View)
                )
            })
        }
    }

    override fun onCallHistory(contact: Contact) {
        CallHistoryActivity.run(context!!, contact)
    }

    override fun onInitiateCall(contact: Contact) {
        InitCallFragment.init(childFragmentManager, contact)
    }
}
