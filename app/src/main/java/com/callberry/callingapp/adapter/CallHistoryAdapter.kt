package com.callberry.callingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.callberry.callingapp.R
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.util.TimeUtil

class CallHistoryAdapter(val context: Context, val recentList: ArrayList<Recent>) :
        RecyclerView.Adapter<CallHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTimestamp: TextView = view.findViewById(R.id.textViewTimestamp)
        val textViewCreditUsed: TextView = view.findViewById(R.id.textViewCreditUsed)
        val textViewDuration: TextView = view.findViewById(R.id.textViewDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_call_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recent = recentList[position]
        holder.apply {
            textViewTimestamp.text = TimeUtil.timeAgo(recent.timestamp)
            textViewCreditUsed.text = "${context.getString(R.string.credit)}: $${recent.credit}"
            textViewDuration.text = recent.duration

        }
    }
}