package com.callberry.callingapp.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.callberry.callingapp.R

class MenuAdapter(val context: Context, val listener: MenuChangeListener) :
    RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    private var menus: ArrayList<Menu> = MenuUtil.getMenu()
    private var lastActiveMenu: Int = 0
    private var activeMenu: Int = menus[0].id

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menuItem: ConstraintLayout = view.findViewById(R.id.item_menu)
        val imgViewIcon: ImageView = view.findViewById(R.id.imgViewIcon)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)

        init {
            menuItem.setOnClickListener {
                lastActiveMenu = activeMenu
                activeMenu = menus[adapterPosition].id
                notifyDataSetChanged()
                listener.onMenuChange(activeMenu)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menus[position]
        holder.imgViewIcon.setImageDrawable(context.getDrawable(menu.icon))
        holder.textViewTitle.text = context.getString(menu.title)
        if (menu.id == activeMenu) {
            holder.menuItem.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight))
            holder.textViewTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            DrawableCompat.setTint(
                DrawableCompat.wrap(holder.imgViewIcon.drawable),
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
        }

        if (menu.id == lastActiveMenu) {
            holder.menuItem.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
            holder.textViewTitle.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimaryText
                )
            )
            DrawableCompat.setTint(
                DrawableCompat.wrap(holder.imgViewIcon.drawable),
                ContextCompat.getColor(context, R.color.colorPrimaryText)
            )
        }
    }


}