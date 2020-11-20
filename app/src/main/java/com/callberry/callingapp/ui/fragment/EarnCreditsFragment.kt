package com.callberry.callingapp.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.callberry.callingapp.R
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.database.dao.CreditDao
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.util.Constants
import com.callberry.callingapp.util.TimeUtil
import com.callberry.callingapp.viewmodel.CreditViewModel
import kotlinx.android.synthetic.main.fragment_earn_credits.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*

class EarnCreditsFragment : Fragment(R.layout.fragment_earn_credits) {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var creditDao: CreditDao
    private lateinit var creditViewModel: CreditViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        initBonus()

        initCheckInDaily()

        initCreditPackages()
    }

    private fun initCreditPackages() {
//        val packages = arrayListOf<Package>(
//            Package("Starter", 5),
//            Package("Bronze", 10),
//            Package("Silver", 15),
//            Package("Gold", 20)
//        )
//
//        val adapter = PackageAdapter(context!!, packages) {
//            context!!.toast("Buy ${it.name} at ${it.value}")
//        }
//        listPackages.layoutManager =
//            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
//        listPackages.adapter = adapter
    }

    private fun initBonus() {
        CoroutineScope(Main).launch {
            val lastBonus = creditDao.getLastCreditByName(Constants.BONUS_CREDIT)
            if (lastBonus == null) {
                enableBonus()
                return@launch
            }

            val diff = TimeUtil.getTimeDiff(
                lastBonus.timestamp + (60 * 60 * 1000),
                TimeUtil.getTimestamp()
            )
            if (TimeUtil.intoHours(diff) >= 1) {
                enableBonus()
                return@launch
            }

            enableBonus(false)

            val timeInMS: Long =
                ((TimeUtil.intoMinutes(diff) * 60000) + (TimeUtil.intoSeconds(diff) * 1000)).toLong()
            countDownTimer = object : CountDownTimer(timeInMS, 1000) {
                override fun onTick(l: Long) {
                    val hours = TimeUtil.intoHours(l)
                    val minutes = TimeUtil.intoMinutes(l)
                    val seconds = TimeUtil.intoSeconds(l)
                    var time = "0$hours"
                    time += if (minutes < 10) " : 0$minutes" else " : $minutes"
                    time += if (seconds < 10) " : 0$seconds" else " : $seconds"
                    textViewBonusValue?.let { it.text = time }
                }

                override fun onFinish() {
                    countDownTimer!!.cancel()
                }
            }.start()
        }

        textViewBonusValue.setOnClickListener {
            val generatedCredits = getRandomCredits(10f, 20f)
            val credit = Credit()
            credit.name = Constants.BONUS_CREDIT
            credit.credit = generatedCredits
            credit.type = Constants.CREDIT
//            creditViewModel.addCredit(credit)
            initBonus()
        }
    }

    private fun initCheckInDaily() {
        CoroutineScope(Main).launch {
            val credit = creditDao.getLastCreditByName(Constants.CHECK_IN_CREDIT)
            if (credit == null) {
                btnCheckIn.isEnabled = true
                return@launch
            }

            val lastCheckIn = TimeUtil.dateOnly(credit.timestamp)
            btnCheckIn.isEnabled = lastCheckIn != TimeUtil.dateOnly(TimeUtil.getTimestamp())
        }

        btnCheckIn.setOnClickListener {
            val generatedCredits = getRandomCredits(10f, 20f)
            val credit = Credit()
            credit.name = Constants.CHECK_IN_CREDIT
            credit.credit = generatedCredits
            credit.type = Constants.CREDIT
//            creditViewModel.addCredit(credit)
            initCheckInDaily()
        }
    }

    private fun enableBonus(isEnable: Boolean = true) {
        textViewBonusValue.isEnabled = isEnable
        textViewBonusValue.text = if (isEnable) getString(R.string.get_bonus) else "00:00:00"
        val color = if (isEnable) R.color.colorWhite else R.color.colorGray
        textViewBonusValue.setTextColor(ContextCompat.getColor(context!!, color))
        val background = if (isEnable) R.drawable.bg_active else R.drawable.bg_non_active
        textViewBonusValue.background = context!!.getDrawable(background)
    }

    private fun init() {
        creditDao = AppDatabase(context!!).creditDao()
        creditViewModel = ViewModelProviders.of(activity!!).get(CreditViewModel::class.java)
    }

    private fun getRandomCredits(max: Float, min: Float): Float {
        return min + Random().nextFloat() * (max - min)
    }


}
