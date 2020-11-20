package com.callberry.callingapp.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.CallRatesAdapter
import com.callberry.callingapp.util.UIUtil
import com.callberry.callingapp.util.exchangeView
import com.callberry.callingapp.viewmodel.CallRateViewModel
import kotlinx.android.synthetic.main.activity_call_rates.*

class CallRateFragment : Fragment(R.layout.fragment_call_rate) {

    private lateinit var viewModel: CallRateViewModel
    private lateinit var adapter: CallRatesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        getCallRates()
    }

    private fun initViews() {
        viewModel = CallRateViewModel.getInstance(activity!!)
        viewModel.getCallRates()
        editTextSearch.addTextChangedListener(textChangeListener)
    }

    private fun getCallRates() {
        exchangeView(layoutProgress, listCallRates)
        viewModel.callRates().observe(this, Observer {
            if (it == null) {
                exchangeView(listCallRates, layoutProgress)
                UIUtil.popError(
                    activity!!,
                    getString(R.string.oops),
                    getString(R.string.network_error)
                ) {
                    if (it) getCallRates()
                }
                return@Observer
            }

            adapter = CallRatesAdapter(activity!!, ArrayList(it))
            listCallRates.layoutManager = LinearLayoutManager(activity!!)
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
