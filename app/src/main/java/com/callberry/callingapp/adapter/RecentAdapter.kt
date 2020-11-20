package com.callberry.callingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ornach.nobobutton.NoboButton
import com.callberry.callingapp.R
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.model.RecentContact
import com.callberry.callingapp.util.TimeUtil
import com.callberry.callingapp.util.UIUtil
import com.callberry.callingapp.util.Utils

class RecentAdapter(val context: Context, val recentList: List<RecentContact>, var listener: RecentClickListener) :
    RecyclerView.Adapter<RecentAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtViewIcon: TextView = view.findViewById(R.id.txt_view_icon)
        val txtViewDisplayName: TextView = view.findViewById(R.id.txt_view_display_name)
        val txtViewTimestamp: TextView = view.findViewById(R.id.txt_view_timestamp)
        val btnCall: NoboButton = view.findViewById(R.id.btnCall)

        init {
            view.setOnClickListener { listener.onCallHistory(recentList[adapterPosition].contact!!) }
            btnCall.setOnClickListener { listener.onInitiateCall(recentList[adapterPosition].contact!!) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recentContact = recentList[position]
        holder.txtViewTimestamp.text = TimeUtil.timeAgo(recentContact.recent!!.timestamp)
        recentContact.contact?.let {
            holder.txtViewDisplayName.text = it.name
            UIUtil.setIcon(context, holder.txtViewIcon, it.name!!, it.theme!!)
            return
        }

        holder.txtViewDisplayName.text = recentContact.recent!!.phoneNo
        UIUtil.setIcon(context, holder.txtViewIcon, "#")
        val contact = Utils.createContact(
            context,
            recentContact.recent!!.phoneNo,
            recentContact.recent!!.dialCode
        )

        recentList[position].contact = contact
    }

    interface RecentClickListener {
        fun onCallHistory(contact: Contact)

        fun onInitiateCall(contact: Contact)
    }

}