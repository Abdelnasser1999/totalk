package com.callberry.callingapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.callberry.callingapp.R
import com.callberry.callingapp.ui.fragment.CallRateFragment
import com.callberry.callingapp.ui.fragment.CreditFragment
import com.callberry.callingapp.ui.fragment.HomeFragment
import com.callberry.callingapp.ui.fragment.MoreFragment
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.route
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        setupBottomNav()


    }

    private fun setupBottomNav() {
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> updateView(
                        1,
                        getString(R.string.app_name),
                        R.drawable.ic_search_main,
                        HomeFragment()
                )

                R.id.menu_credits -> updateView(
                        2,
                        getString(R.string.menu_credits),
                        R.drawable.ic_history,
                        CreditFragment()
                )

                R.id.menu_call_rates -> updateView(
                        3,
                        getString(R.string.menu_call_rates),
                        -1,
                        CallRateFragment()
                )

                R.id.menu_more -> updateView(
                        4,
                        getString(R.string.menu_more),
                        -1,
                        MoreFragment()
                )

            }
            true
        }
    }

    private fun isFromCredit(): Boolean {
        return intent.hasExtra(Constants.NOTIFICATION_INTENT) && intent.getBooleanExtra(
                Constants.NOTIFICATION_INTENT,
                false
        )
    }

    private fun init() {
        if (isFromCredit()) {
            updateView(2, getString(R.string.menu_credits), R.drawable.ic_history, CreditFragment())
        } else {
            updateView(1, getString(R.string.app_name), R.drawable.ic_search_main, HomeFragment())
        }

        btnOptionMenu.setOnClickListener {
            if (currentPage == 1)
                route(SearchActivity::class)

            if (currentPage == 2)
                route(CreditHistoryActivity::class)
        }
    }

    private fun updateView(pageNo: Int, title: String, icon: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.homeContainer, fragment)
                .commit()

        textViewTitle.text = title
        currentPage = pageNo
        if (icon == -1) {
            btnOptionMenu.visibility = View.GONE
        } else {
            btnOptionMenu.visibility = View.VISIBLE
            btnOptionMenu.setDrawableResource(icon)
        }
    }

    private fun setNonActive(menu: Menu) {
        menu.viewBar.visibility = View.INVISIBLE
        menu.imgViewIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorGray))
        menu.menuText.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
    }

    private fun setActive(menu: Menu) {
        menu.viewBar.visibility = View.VISIBLE
        menu.imgViewIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
        menu.menuText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    data class Menu(val viewBar: View, val imgViewIcon: ImageView, val menuText: TextView)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

}
