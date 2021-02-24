package com.callberry.callingapp.ui.fragment


import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.PackageAdapter
import com.callberry.callingapp.admob.AdHelper
import com.callberry.callingapp.admob.OnRewardedAdListener
import com.callberry.callingapp.admob.Rewarded
import com.callberry.callingapp.materialdialog.MaterialAlertDialog
import com.callberry.callingapp.model.TimeModel
import com.callberry.callingapp.model.prefmodel.Package
import com.callberry.callingapp.model.remote.credits.RemoteCredit
import com.callberry.callingapp.retrofit.ApiHelper
import com.callberry.callingapp.util.*
import com.callberry.callingapp.viewmodel.CreditViewModel
import com.callberry.callingapp.viewmodel.PackageViewModel
import com.callberry.callingapp.worker.WorkerUtil
import kotlinx.android.synthetic.main.content_buy_credits.*
import kotlinx.android.synthetic.main.content_earn_credits.*
import kotlinx.android.synthetic.main.fragment_credit.*
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreditFragment : Fragment(R.layout.fragment_credit), BillingProcessor.IBillingHandler,
        OnRewardedAdListener {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var creditViewModel: CreditViewModel
    private lateinit var packageViewModel: PackageViewModel
    private lateinit var billingProcessor: BillingProcessor
    private var packageValue: Float? = null
    private lateinit var materialAlertDialog: MaterialAlertDialog
    private lateinit var tempCredit: RemoteCredit
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        creditViewModel = CreditViewModel.getInstance(activity!!)
        packageViewModel = PackageViewModel.getInstance(activity!!)
        getBillingProcessor()
        getCurrentBalance()
        getBuyCredits()
        getEarnCredits()
    }

    /* private fun getServerTime():Long {
         var timeStamp:Long=0
         ApiHelper.apiService().getTime(Utils.getToken()).enqueue(object : retrofit2.Callback<TimeModel> {
             override fun onResponse(call: Call<TimeModel>, response: Response<TimeModel>) {
                 var time: Int? = response.body()?.timestamp
                 if (time != null) {
                     timeStamp = time.toLong()
                 }
                 Log.d("MYTAG", "onResponse: $time")
             }

             override fun onFailure(call: Call<TimeModel>, t: Throwable) {
                 Log.d("MYTAG", "onFailure: ${t.message}")
             }

         })
         Log.d("MYTAG", "onFailure: ${time}")
         return timeStamp
     }*/

    private fun getBillingProcessor() {
        //TODO: replace with your licence key
        billingProcessor =
                BillingProcessor(context, null, this);
        billingProcessor.initialize();
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
        //TODO: Replace test item with original items
        billingProcessor.purchase(activity, "android.test.purchased")
        packageValue = it.value?.toFloat()
        // context!!.toast("Buy ${it.name}")
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

            val diff = it.timestamp.plus((60 * 60 * 1000)).let { it1 -> TimeUtil.getTimeDiff(it1, TimeUtil.getTimestamp()) }
            if (diff.let { it1 -> TimeUtil.intoHours(it1) } >= 1) {
                enableBonus()
                return@getCreditHistoryByName
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
         //   AdHelper.showInterstitialAd(null)
        }
    }

    private fun getWatchVideos(credit: RemoteCredit) {

        textViewWatch.text = context!!.getString(R.string.watch)
        textViewWatch.setOnClickListener {
            showMaterialDialog()
            tempCredit = credit
        }
    }

    private fun showMaterialDialog() {
        materialAlertDialog = MaterialAlertDialog(activity)
        materialAlertDialog.setTitle(getString(R.string.title_ads))
        materialAlertDialog.setMessage(getString(R.string.message_ads))
        materialAlertDialog.setPositiveClick(getString(R.string.watch_ad)) {
            AdHelper.showRewardedAd(this)
            textViewWatch.text = getString(R.string.loading)
        }
        materialAlertDialog.setNegativeClick(getString(R.string.cancel)) {
            materialAlertDialog.dismiss()
        }
        materialAlertDialog.show()

    }

    private fun creditObserver(name: String, generatedCredits: Float, type: String, callback: () -> Unit) {
        // Request to get current timestamp
        //val timeStamp:Long=getServerTime()
        creditViewModel.updateCredits(name, generatedCredits, type)
        UIUtil.popReward(activity!!,
                resources.getString(R.string.string_reward_message, "$$generatedCredits"),
                object : UIUtil.DialogActionListener {
                    override fun onAction() {
                        getCurrentBalance()
                        callback.invoke()
                        AdHelper.showInterstitialAd(null)
                        // context?.toast("Ad Load")
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

    override fun onBillingInitialized() {

    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        //TODO: Replace the name in CreditAdapter
        packageValue?.let {
            creditObserver("test_name", it, Constants.CREDIT) {
            }
        }

    }


    override fun onBillingError(errorCode: Int, error: Throwable?) {
        when (errorCode) {
            -2 -> Toast.makeText(
                    context,
                    getString(R.string.requested_feature_not_supported),
                    Toast.LENGTH_LONG
            ).show()
            -3 -> Toast.makeText(context, getString(R.string.service_time_out), Toast.LENGTH_LONG)
                    .show()
            2 -> Toast.makeText(
                    context,
                    getString(R.string.network_connection_down),
                    Toast.LENGTH_LONG
            ).show()
            3 -> Toast.makeText(
                    context,
                    getString(R.string.billing_API_version_is_not_supported),
                    Toast.LENGTH_LONG
            ).show()
            5 -> Toast.makeText(context, getString(R.string.developer_error), Toast.LENGTH_LONG)
                    .show()
            6 -> Toast.makeText(context, getString(R.string.error_fatal), Toast.LENGTH_LONG).show()
            7 -> Toast.makeText(context, getString(R.string.already_owned), Toast.LENGTH_LONG)
                    .show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release()
        }
        super.onDestroy()
    }

    override fun onAdListener(isLoaded: Boolean, rewardedAd: Rewarded?) {
        textViewWatch.text = context!!.getString(R.string.watch)
        materialAlertDialog.dismiss()
        if (isLoaded) {
            if (rewardedAd == Rewarded.ON_REWARDED_AD_COMPLETED) {
                val generatedCredits =
                        getRandomCredits(getCreditLimit(tempCredit)[0], getCreditLimit(tempCredit)[1])
                creditObserver(tempCredit.name!!, generatedCredits, Constants.CREDIT) {
                    getWatchVideos(tempCredit)
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.ads_not_available), Toast.LENGTH_SHORT)
                    .show()

        }
    }
}

