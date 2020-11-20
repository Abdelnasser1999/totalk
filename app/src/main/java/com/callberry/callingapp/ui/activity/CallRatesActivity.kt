package com.callberry.callingapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.CallRatesAdapter
import com.callberry.callingapp.util.UIUtil
import com.callberry.callingapp.util.exchangeView
import com.callberry.callingapp.viewmodel.CallRateViewModel
import kotlinx.android.synthetic.main.activity_call_rates.*

class CallRatesActivity : AppCompatActivity() {

    private lateinit var viewModel: CallRateViewModel
    private lateinit var adapter: CallRatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_rates)

        init()

        getCallRates()

    }

    private fun init() {
        viewModel = CallRateViewModel.getInstance(this)
        viewModel.getCallRates()
        editTextSearch.addTextChangedListener(textChangeListener)
        btnBack.setOnClickListener { onBackPressed() }
    }

    private fun getCallRates() {
        exchangeView(layoutProgress, listCallRates)
        viewModel.callRates().observe(this, Observer {
            if (it == null) {
                exchangeView(listCallRates, layoutProgress)
                UIUtil.popError(this, getString(R.string.oops), getString(R.string.network_error)) {
                    if (it) getCallRates()
                }
                return@Observer
            }

            adapter = CallRatesAdapter(this, ArrayList(it))
            listCallRates.layoutManager = LinearLayoutManager(this)
            listCallRates.adapter = adapter
            exchangeView(listCallRates, layoutProgress)
            editTextSearch.isEnabled = true
        })
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            adapter.getFilter()!!.filter(s.toString())
        }
    }


}
