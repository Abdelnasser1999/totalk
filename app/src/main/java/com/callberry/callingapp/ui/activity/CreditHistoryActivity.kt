package com.callberry.callingapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.CreditAdapter
import com.callberry.callingapp.util.exchangeView
import com.callberry.callingapp.viewmodel.CreditViewModel
import kotlinx.android.synthetic.main.activity_credit_history.*
import kotlinx.android.synthetic.main.layout_no_result.*

class CreditHistoryActivity : AppCompatActivity() {

    private lateinit var creditViewModel: CreditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_history)

        init()

        getCreditsHistory()
    }

    private fun getCreditsHistory() {
        creditViewModel.getCreditHistory()
        creditViewModel.creditHistory().observe(this, Observer {
            if (it == null) {
                exchangeView(layoutNoCreditHistory, layoutProgress)
                return@Observer
            }
            val adapter = CreditAdapter(this@CreditHistoryActivity, ArrayList(it))
            listCreditHistory.layoutManager = LinearLayoutManager(this@CreditHistoryActivity)
            listCreditHistory.adapter = adapter
            exchangeView(listCreditHistory, layoutProgress)
        })
    }

    private fun init() {
        creditViewModel = CreditViewModel.getInstance(this)
        txtViewMessage.text = getString(R.string.no_credit_history)
        btnBack.setOnClickListener { onBackPressed() }

    }
}
