package com.callberry.callingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.callberry.callingapp.R
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.util.UIUtil
import kotlin.collections.ArrayList

class ContactAdapter(
    val context: Context,
    val contacts: ArrayList<Contact>,
    val listener: ContactSelectListener
) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    private val contactsFullList = ArrayList<Contact>()

    init {
        contactsFullList.addAll(contacts)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgViewDisplayPicture: ImageView = view.findViewById(R.id.img_view_display_picture)
        val txtViewIcon: TextView = view.findViewById(R.id.txt_view_icon)
        val txtViewDisplayName: TextView = view.findViewById(R.id.txt_view_display_name)
        val txtViewPhoneNo: TextView = view.findViewById(R.id.txt_view_phone_no)

        init {
            view.setOnClickListener { listener.onContactSelect(contacts[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.txtViewDisplayName.text = contact.name
        holder.txtViewPhoneNo.text = contact.phoneNo
        UIUtil.setIcon(context, holder.txtViewIcon, contact.name!!, contact.theme!!)
    }

    fun getFilter(): Filter? {
        return searchContacts
    }

    private val searchContacts: Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val searchContacts = ArrayList<Contact>()
            if (charSequence.isEmpty()) {
                searchContacts.clear()
                searchContacts.addAll(contactsFullList)
            } else {
                val filterPattern =
                    charSequence.toString().toLowerCase()
                for (contact in contactsFullList) {
                    if (contact.name!!.toLowerCase().contains(filterPattern) || contact.phoneNo!!.toLowerCase()
                            .contains(filterPattern)
                    ) {
                        searchContacts.add(contact)
                    }
                }
            }
            val results = FilterResults()
            results.values = searchContacts
            return results
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            contacts.clear()
            contacts.addAll(filterResults.values as Collection<Contact>)
            notifyDataSetChanged()
        }
    }

    interface ContactSelectListener {
        fun onContactSelect(contact: Contact)
    }
}