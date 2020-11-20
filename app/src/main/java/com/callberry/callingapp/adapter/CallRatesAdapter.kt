package com.callberry.callingapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.callberry.callingapp.R
import com.callberry.callingapp.model.remote.callrates.CallRate
import com.callberry.callingapp.util.UIUtil

class CallRatesAdapter(val context: Context, val callRates: ArrayList<CallRate>) :
    RecyclerView.Adapter<CallRatesAdapter.ViewHolder>() {

    private val callRateFullList = ArrayList<CallRate>()

    init {
        callRateFullList.addAll(callRates)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewFlag: TextView = view.findViewById(R.id.textViewFlag)
        val textViewCountry: TextView = view.findViewById(R.id.textViewName)
        val textViewCredits: TextView = view.findViewById(R.id.textViewCredits)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_call_rates, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return callRates.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callRate = callRates[position]
        holder.textViewFlag.text = callRate.flag?.let { UIUtil.flag(it) }
        holder.textViewCountry.text = "${callRate.name} (${callRate.dialcode})"
        holder.textViewCredits.text = "$${callRate.callRate}"
    }

    fun getFilter(): Filter? {
        return searchCallRate
    }

    private val searchCallRate: Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val searchCallRates = ArrayList<CallRate>()
            if (charSequence.isEmpty()) {
                searchCallRates.clear()
                searchCallRates.addAll(callRateFullList)
            } else {
                val filterPattern =
                    charSequence.toString().toLowerCase()
                for (contact in callRateFullList) {
                    if (contact.name!!.toLowerCase()
                            .contains(filterPattern) || contact.dialcode!!.toLowerCase()
                            .contains(filterPattern)
                    ) {
                        searchCallRates.add(contact)
                    }
                }
            }
            val results = FilterResults()
            results.values = searchCallRates
            return results
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            callRates.clear()
            callRates.addAll(filterResults.values as Collection<CallRate>)
            notifyDataSetChanged()
        }
    }

}