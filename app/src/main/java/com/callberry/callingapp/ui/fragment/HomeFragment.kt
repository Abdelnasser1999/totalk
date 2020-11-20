package com.callberry.callingapp.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.PagerAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHomePagerAdapter()

        floatBtnDial.setOnClickListener {
            val dialFragment = DialFragment()
            dialFragment.show(childFragmentManager, "dialog")
        }
    }

    private fun setHomePagerAdapter() {
        val adapter = PagerAdapter(childFragmentManager, 0)
        adapter.addFragment(RecentFragment(), getString(R.string.home_recents))
        adapter.addFragment(ContactFragment(), getString(R.string.home_contacts))
        homePager.adapter = adapter
        tabs.setupWithViewPager(homePager)
    }


}
