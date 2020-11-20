package com.callberry.callingapp.menu

import com.callberry.callingapp.R

class MenuUtil {
    companion object {
        const val MENU_HOME = 1
        const val MENU_CREDITS = 2
        const val MENU_CALL_RATES = 3
        const val MENU_SETTING = 4

        @JvmStatic
        fun getMenu(): ArrayList<Menu> {
            val menus = ArrayList<Menu>()
            menus.add(Menu(MENU_HOME, R.drawable.ic_home_outline, R.string.menu_home))
            menus.add(Menu(MENU_CREDITS, R.drawable.ic_home_outline, R.string.menu_credits))
            menus.add(Menu(MENU_CALL_RATES, R.drawable.ic_home_outline, R.string.menu_call_rates))
            menus.add(Menu(MENU_SETTING, R.drawable.ic_home_outline, R.string.menu_settings))
            return menus
        }
    }
}