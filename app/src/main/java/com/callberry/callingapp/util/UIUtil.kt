package com.callberry.callingapp.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.callberry.callingapp.R
import com.callberry.callingapp.materialdialog.MaterialAlertDialog
import java.util.*

class UIUtil {
    companion object {
        @JvmStatic
        fun popReward(activity: Activity, message: String, listener: DialogActionListener) {
            val dialog = Dialog(activity)
            dialog.setCancelable(false)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.decorView.setBackgroundResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val contentView = inflater.inflate(R.layout.layout_reward_dialog, null);
            val txtRewardMsg: TextView = contentView.findViewById(R.id.txtRewardMessage)
            txtRewardMsg.text = message
            val txtGotIt: TextView = contentView.findViewById(R.id.txtViewGotIt)
            txtGotIt.setOnClickListener {
                dialog.dismiss()
                listener.onAction()
            }
            dialog.setContentView(contentView)
            dialog.show()
        }

        @JvmStatic
        fun popError(
            activity: Activity,
            title: String,
            message: String,
            callback: (isTryAgain: Boolean) -> Unit
        ) {
            val materialAlertDialog = MaterialAlertDialog(activity)
            materialAlertDialog.setTitle(title)
            materialAlertDialog.setMessage(message)
            materialAlertDialog.setPositiveClick(activity.getString(R.string.try_again)) {
                it.dismiss()
                callback.invoke(true)
            }
            materialAlertDialog.setNegativeClick(activity.getString(R.string.close)) {
                it.dismiss()
                callback.invoke(false)
            }
            materialAlertDialog.show()
        }

        @JvmStatic
        fun popErrorClose(
            activity: Activity,
            title: String,
            message: String
        ) {
            val materialAlertDialog = MaterialAlertDialog(activity)
            materialAlertDialog.setTitle(title)
            materialAlertDialog.setMessage(message)
            materialAlertDialog.setPositiveClick(activity.getString(R.string.close)) {
                it.dismiss()
            }
            materialAlertDialog.show()
        }

        @JvmStatic
        fun popNotSupported(
            activity: Activity,
            title: String,
            message: String
        ) {
            val materialAlertDialog = MaterialAlertDialog(activity)
            materialAlertDialog.setTitle(title)
            materialAlertDialog.setMessage(message)
            materialAlertDialog.setPositiveClick(activity.getString(R.string.close)) {
                it.dismiss()
            }
        }


        @JvmStatic
        fun popPermissionDenied(activity: Activity, callback: (grant: Boolean) -> Unit) {
            val materialAlertDialog = MaterialAlertDialog(activity)
            materialAlertDialog.setTitle("Permission Denied")
            materialAlertDialog.setMessage("To continue importing contact we need you contact permission")
            materialAlertDialog.setPositiveClick("Grant") {
                it.dismiss()
                callback.invoke(true)
            }
            materialAlertDialog.setNegativeClick("Cancel") {
                it.dismiss()
                callback.invoke(true)
            }
            materialAlertDialog.show()
        }

        @JvmStatic
        fun showInfoSnackbar(view: View, msgText: String) {
            val bar = Snackbar.make(view, msgText, Snackbar.LENGTH_SHORT)
            bar.setAction(android.R.string.ok) { bar.dismiss() }
            bar.show()
        }

        @JvmStatic
        fun getTheme(): String {
            val themes = arrayListOf<String>(
                "${R.color.colorRed}, ${R.color.colorRedBG}",
                "${R.color.colorPink}, ${R.color.colorPinkBG}",
                "${R.color.colorPurple}, ${R.color.colorPurpleBG}",
                "${R.color.colorDeepPurple}, ${R.color.colorDeepPurpleBG}",
                "${R.color.colorIndigo}, ${R.color.colorIndigoBG}",
                "${R.color.colorCyan}, ${R.color.colorCyanBG}",
                "${R.color.colorTeal}, ${R.color.colorTealBG}",
                "${R.color.colorGreenDark}, ${R.color.colorGreenDarkBG}",
                "${R.color.colorGreenLight}, ${R.color.colorGreenLightBG}",
                "${R.color.colorAmber}, ${R.color.colorAmberBG}",
                "${R.color.colorBrown}, ${R.color.colorBrownBG}"
            )

            val id = Random().nextInt(themes.size - 0) + 0;
            return themes[id]
        }

        @JvmStatic
        fun setIcon(
            context: Context,
            view: TextView,
            name: String,
            iconTheme: String = getTheme()
        ) {
            view.text = getIcon(name)
            val theme: List<String> = iconTheme.split(", ")
            view.setTextColor(ContextCompat.getColor(context, theme[0].toInt()))
            view.setBackgroundColor(ContextCompat.getColor(context, theme[1].toInt()))
        }

        @JvmStatic
        fun formatBalance(balance: Float): String {
            return if (balance.toString().length > 4)
                balance.toString().substring(0, 4)
            else
                balance.toString()
        }

        @JvmStatic
        fun formatBalanceMain(balance: Float): String {
            return if (balance.toString().length > 6)
                balance.toString().substring(0, 6)
            else
                balance.toString()
        }

        @JvmStatic
        fun getIcon(name: String): String {
            val nameIcon: List<String> = name.split(" ")
            val firstCapital = nameIcon[0].substring(0, 1)
            val secondCapital = if (nameIcon.size > 1) nameIcon[1].substring(0, 1) else ""
            return "$firstCapital$secondCapital"
        }

        @JvmStatic
        fun formatCallDuration(seconds: Int): String {
            var seconds = seconds
            val minutes = seconds % 3600 / 60
            seconds = seconds % 60
            return twoDigitString(minutes) + ":" + twoDigitString(seconds)
        }

        private fun twoDigitString(number: Int): String {
            if (number == 0) {
                return "00"
            }
            return if (number / 10 == 0) {
                "0$number"
            } else number.toString()
        }

        fun flag(unicode: String): String {
            Log.d("Flag", "Code $unicode")
            var unicode = unicode
            var flag = ""
            unicode = unicode.replace("U+", "0x")
            val codeArr = unicode.split(" ").toTypedArray()
            if (codeArr.size == 2) {
                val hex1 = codeArr[0].substring(2).toInt(16)
                val hex2 = codeArr[1].substring(2).toInt(16)
                flag = String(Character.toChars(hex1)) + String(Character.toChars(hex2))
            }
            return flag
        }

        fun getEstimatedDurationInSeconds(callRate: Float): Int {
            val rate = callRate / 60
            val balance = Utils.getBalance()
            return (balance / rate).toInt()
        }

        fun getCreditUsedOnCallEnded(timestamp: Long, callRate: Float): String {
            val rate = callRate / 60
            val diff = TimeUtil.getTimeDiff(timestamp, TimeUtil.getTimestamp())
            val callDurationInSecond = TimeUtil.intoSeconds(diff)
            val credit = (callDurationInSecond * rate).toString()
            if (credit.length > 4) {
                return credit.substring(0, 4)
            }
            return credit
        }

        fun calculateCreditsUsed(callDurationInSecond: Int, callRate: Float): String {
            val rate = callRate / 60
            val credit = (callDurationInSecond * rate).toString()
            if (credit.length > 4) {
                return credit.substring(0, 4)
            }
            return credit
        }

        fun calculateCredits(callDurationInSecond: Int, callRate: Float): Float {
            val rate = callRate / 60
            return (callDurationInSecond * rate)
        }

        fun getCallEndedDurationInSecond(timestamp: Long): String {
            val diff = TimeUtil.getTimeDiff(timestamp, TimeUtil.getTimestamp())
            val callDurationInSecond = TimeUtil.intoSeconds(diff)
            return formatCallDuration(callDurationInSecond)
        }

        fun formatTimestamp(totalSeconds: Int): String? {
            val minutes = totalSeconds / 60.toLong()
            val seconds = totalSeconds % 60.toLong()
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }

    }

    interface DialogActionListener {
        fun onAction()
    }
}