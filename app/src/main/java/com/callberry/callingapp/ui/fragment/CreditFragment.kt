package com.callberry.callingapp.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.PackageAdapter
import com.callberry.callingapp.model.prefmodel.Package
import com.callberry.callingapp.model.remote.credits.RemoteCredit
import com.callberry.callingapp.model.remote.packages.RemotePackage
import com.callberry.callingapp.util.*
import com.callberry.callingapp.viewmodel.CreditViewModel
import com.callberry.callingapp.viewmodel.PackageViewModel
import com.callberry.callingapp.viewmodel.PreferenceViewModel
import com.callberry.callingapp.worker.WorkerUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_buy_credits.*
import kotlinx.android.synthetic.main.content_earn_credits.*
import kotlinx.android.synthetic.main.fragment_credit.*
import java.util.*
import kotlin.collections.ArrayList

class CreditFragment : Fragment(R.layout.fragment_credit) {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var creditViewModel: CreditViewModel
    private lateinit var packageViewModel: PackageViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        creditViewModel = CreditViewModel.getInstance(activity!!)
        packageViewModel = PackageViewModel.getInstance(activity!!)

        getCurrentBalance()
        getBuyCredits()
        getEarnCredits()

    }

    private fun getCurrentBalance() {
        txtViewCurrentBalance.text = "$${UIUtil.formatBalanceMain(Utils.getBalance())}"
    }

    private fun getBuyCredits() {
        val packages = PreferenceUtil.getPackages()
        val adapter = PackageAdapter(context!!, ArrayList(packages)) { buyPackage(it) }
        listPackages.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        listPackages.adapter = adapter
        exchangeView(layoutMain, layoutProgress)
    }

    private fun buyPackage(it: Package) {
        context!!.toast("Buy ${it.name}")
    }

    private fun getEarnCredits() {
        creditViewModel.getLocalCredits {
            getBonus(getRemoteCredit(it!!, Constants.BONUS_CREDIT)!!)
            getDailyCheckIn(getRemoteCredit(it, Constants.CHECK_IN_CREDIT)!!)
            getWatchVideos(getRemoteCredit(it, Constants.WATCH_VIDEOS_CREDIT)!!)
        }
    }

    private fun getBonus(credit: RemoteCredit) {
        textViewBonusValue.text = context!!.getString(R.string.loading)
        creditViewModel.getCreditHistoryByName(credit.name!!) {
            if (it == null) {
                enableBonus()
                return@getCreditHistoryByName
            }

            val diff =
                TimeUtil.getTimeDiff(it.timestamp + (60 * 60 * 1000), TimeUtil.getTimestamp())
            if (TimeUtil.intoHours(diff) >= 1) {
                enableBonus()
                return@getCreditHistoryByName
            }

            enableBonus(false)

            val timeInMS: Long = ((TimeUtil.intoMinutes(diff) * 60000) + (TimeUtil.intoSeconds(diff) * 1000)).toLong()
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
            val generatedCredits =
                getRandomCredits(getCreditLimit(credit)[0], getCreditLimit(credit)[1])
            creditObserver(credit.name!!, generatedCredits, Constants.CREDIT) {
                getCurrentBalance()
                getBonus(credit)
                WorkerUtil.startBonusAlarm(context!!)
            }
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

    private fun getDailyCheckIn(credit: RemoteCredit) {
        creditViewModel.getCreditHistoryByName(credit.name!!) {
            if (it == null) {
                btnCheckIn.isEnabled = true
                return@getCreditHistoryByName
            }

            val lastCheckIn = TimeUtil.dateOnly(it.timestamp)
            btnCheckIn.isEnabled = lastCheckIn != TimeUtil.dateOnly(TimeUtil.getTimestamp())
        }


        btnCheckIn.setOnClickListener {
            val generatedCredits =
                getRandomCredits(getCreditLimit(credit)[0], getCreditLimit(credit)[1])
            creditObserver(credit.name!!, generatedCredits, Constants.CREDIT) {
                getDailyCheckIn(credit)
            }
        }
    }

    private fun getWatchVideos(credit: RemoteCredit) {
        textViewWatch.text = context!!.getString(R.string.watch)
        textViewWatch.setOnClickListener {
            textViewWatch.text = getString(R.string.loading)
            Handler().postDelayed({
                val generatedCredits =
                    getRandomCredits(getCreditLimit(credit)[0], getCreditLimit(credit)[1])
                creditObserver(credit.name!!, generatedCredits, Constants.CREDIT) {
                    getWatchVideos(credit)
                }
            }, 2000)
        }
    }

    private fun creditObserver(
        name: String,
        generatedCredits: Float,
        type: String,
        callback: () -> Unit
    ) {
        creditViewModel.updateCredits(name, generatedCredits, type)
        UIUtil.popReward(activity!!,
            resources.getString(R.string.string_reward_message, "$$generatedCredits"),
            object : UIUtil.DialogActionListener {
                override fun onAction() {
                    getCurrentBalance()
                    callback.invoke()
                    context?.toast("Ad Load")
                }
            })
    }

    private fun getRandomCredits(max: Float, min: Float): Float {
        var credits = min + Random().nextFloat() * (max - min)
        if (credits.toString().length > 4) {
            credits = credits.toString().substring(0, 4).toFloat()
        }
        return credits
    }

    private fun getRemoteCredit(remotes: List<RemoteCredit>, type: String): RemoteCredit? {
        for (it in remotes) {
            if (it.name == type) {
                return it
            }
        }
        return null
    }

    private fun getCreditLimit(credit: RemoteCredit): List<Float> {
        val credits = credit.value!!.split(", ")
        return arrayListOf<Float>(credits[0].toFloat(), credits[1].toFloat())

    }

}
