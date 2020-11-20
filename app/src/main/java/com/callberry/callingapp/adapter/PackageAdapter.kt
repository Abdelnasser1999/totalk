package com.callberry.callingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.callberry.callingapp.R
import com.callberry.callingapp.model.prefmodel.Package
import com.callberry.callingapp.model.remote.packages.RemotePackage

class PackageAdapter(
    val context: Context,
    val packages: ArrayList<Package>,
    val callBack: (pack: Package) -> Unit
) :
    RecyclerView.Adapter<PackageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutPackage: ConstraintLayout = view.findViewById(R.id.layoutPackage)
        val textViewPackage: TextView = view.findViewById(R.id.textViewPackageName)
        val textViewPackageValue: TextView = view.findViewById(R.id.textViewPackageValue)
        val btnBuy: TextView = view.findViewById(R.id.materialBtnBuy)

        init {
            btnBuy.setOnClickListener { callBack.invoke(packages[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_package, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pack = packages[position]
        holder.textViewPackage.text = pack.name
        holder.textViewPackageValue.text = "$${pack.value}"
    }
}