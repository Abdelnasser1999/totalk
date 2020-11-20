package com.callberry.callingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.callberry.callingapp.R
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.TimeUtil
import com.callberry.callingapp.util.UIUtil

class CreditAdapter(val context: Context, val credits: ArrayList<Credit>) :
    RecyclerView.Adapter<CreditAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewType: TextView = view.findViewById(R.id.textViewType)
        val textViewName: TextView = view.findViewById(R.id.textViewName)
        val textViewCredits: TextView = view.findViewById(R.id.textViewCredits)
        val textViewTimestamp: TextView = view.findViewById(R.id.textViewTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_credit_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return credits.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = credits[position]
        holder.textViewName.text = getName(item.type, item.name)
        holder.textViewCredits.text = "$${UIUtil.formatBalance(item.credit)}"
        holder.textViewTimestamp.text = TimeUtil.timeAgo(item.timestamp)
        holder.textViewType.text = item.type
        if (item.type == Constants.DEBIT) {
            holder.textViewType.setTextColor(ContextCompat.getColor(context, R.color.colorRed))
            holder.textViewCredits.setTextColor(ContextCompat.getColor(context, R.color.colorRed))
        }
    }

    private fun getName(type: String, name: String): String {
        if (type == Constants.DEBIT) {
            return name
        }

        return when (name) {
            Constants.BONUS_CREDIT -> context.getString(R.string.bonus)
            Constants.CHECK_IN_CREDIT -> context.getString(R.string.daily_check_in)
            Constants.WATCH_VIDEOS_CREDIT -> context.getString(R.string.watch_ads)
            else -> context.getString(R.string.referral_program)
        }
    }
}