package com.callberry.callingapp.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.CallHistoryAdapter
import com.callberry.callingapp.admob.AdHelper
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.ui.fragment.InitCallFragment
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.UIUtil
import com.callberry.callingapp.viewmodel.RecentViewModel
import com.google.android.gms.ads.AdSize
import kotlinx.android.synthetic.main.activity_call_history.*
import kotlin.collections.ArrayList

class CallHistoryActivity : AppCompatActivity() {

    private lateinit var contact: Contact
    private lateinit var viewModel: RecentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_history)

        contact =
            Gson().fromJson(intent.getStringExtra(Constants.CONTACT), Contact::class.java)
        viewModel = RecentViewModel.getInstance(this)

        initViews()

        AdHelper.loadBannerAd(layoutAdsContainer, AdSize.BANNER)

    }

    override fun onResume() {
        super.onResume()
        getRecentCallLogs()
    }

    private fun getRecentCallLogs() {
        viewModel.getRecentLogs(contact.phoneNo!!)
        viewModel.recentLogs().observe(this, Observer {
            if (it == null) {
                return@Observer
            }

            val adapter = CallHistoryAdapter(this, ArrayList(it))
            listRecents.layoutManager = LinearLayoutManager(this)
            listRecents.adapter = adapter

        })
    }

    private fun initViews() {
        textViewName.text = contact.name
        textViewPhoneNo.text = contact.phoneNo
        UIUtil.setIcon(this@CallHistoryActivity, textViewIcon, contact.name!!, contact.theme!!)
        btnCall.setOnClickListener { InitCallFragment.init(supportFragmentManager, contact) }
        btnBack.setOnClickListener { onBackPressed() }

    }

    companion object {
        @JvmStatic
        fun run(context: Context, contact: Contact) {
            val intent = Intent(context, CallHistoryActivity::class.java)
            intent.putExtra(Constants.CONTACT, Gson().toJson(contact))
            context.startActivity(intent)
        }
    }

}
