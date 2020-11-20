package com.callberry.callingapp.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.callberry.callingapp.R
import com.callberry.callingapp.menu.MenuAdapter
import com.callberry.callingapp.menu.MenuChangeListener
import com.callberry.callingapp.menu.MenuUtil
import com.callberry.callingapp.ui.fragment.CreditFragment
import com.callberry.callingapp.ui.fragment.HomeFragment
import com.callberry.callingapp.util.route
import com.callberry.callingapp.util.setFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_main.*

class HomeActivity : AppCompatActivity() {

    private lateinit var activeFragment: Fragment
    private lateinit var title: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()

        initMenus()

    }

    private fun initMenus() {
        val adapter = MenuAdapter(this, object : MenuChangeListener {
            override fun onMenuChange(menu: Int) {
                when (menu) {
                    MenuUtil.MENU_HOME -> {
                        activeFragment = HomeFragment()
                        title = getString(R.string.app_name)
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    MenuUtil.MENU_CREDITS -> {
                        activeFragment = CreditFragment()
                        title = getString(R.string.menu_credits)
                        drawerLayout.closeDrawer(GravityCompat.START)

                    }
                    MenuUtil.MENU_CALL_RATES -> {
                        route(DialActivity::class)
//                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
            }
        })
        listMenu.layoutManager = LinearLayoutManager(this)
        listMenu.adapter = adapter
    }

    private fun initViews() {
        activeFragment = HomeFragment()
        title = getString(R.string.app_name)
        textViewTitle.text = title
        setFragment(HomeFragment())
        drawerLayout.addDrawerListener(getDrawerListener())
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        btnOptionMenu.setOnClickListener { route(SearchActivity::class) }
    }

    private fun getDrawerListener(): DrawerLayout.DrawerListener {
        return object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {

            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerClosed(drawerView: View) {
                setFragment(activeFragment)
                textViewTitle.text = title

            }

            override fun onDrawerOpened(drawerView: View) {
//                setFragment(LoadingFragment())
            }

        }
    }

}
