package com.callberry.callingapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View

import com.callberry.callingapp.R
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.PrefUtils
import kotlinx.android.synthetic.main.fragment_more.*

class MoreFragment : Fragment(R.layout.fragment_more) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

    }

    private fun initViews() {
        switchBonusNotification.isChecked =
            PrefUtils.getBoolean(Constants.IS_BONUS_NOTIFICATION_ENABLED, true)
        switchBonusNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            PrefUtils.setBoolean(Constants.IS_BONUS_NOTIFICATION_ENABLED, isChecked)
        }
    }

}
