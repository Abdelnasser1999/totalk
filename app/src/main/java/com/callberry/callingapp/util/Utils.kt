package com.callberry.callingapp.util

import android.content.Context
import android.util.Log
import com.callberry.callingapp.R
import com.callberry.callingapp.model.Account
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.model.remote.BillingPackage
import com.callberry.callingapp.model.remote.sinch.Sinch
import io.paperdb.Paper

class Utils {
    companion object {

        const val ACCOUNT = "USER_ACCOUNT"

        @JvmStatic
        fun getBalance(): Float {
            return SharedPrefUtil.getFloat(Constants.CURRENT_BALANCE)
        }

        @JvmStatic
        fun updateCredit(credits: Float, type: String) {
            var balance = SharedPrefUtil.getFloat(Constants.CURRENT_BALANCE)
            if (type == Constants.CREDIT) {
                var credit = credits.toString()
                if (credit.length > 4) {
                    credit = credit.substring(0, 4)
                }
                balance += credit.toFloat()
            } else {
                balance -= credits
                balance = if (balance < 0) 0.0f else balance
            }

            SharedPrefUtil.setFloat(Constants.CURRENT_BALANCE, balance)
        }

        @JvmStatic
        fun getEstimatedDurationInSeconds(callRate: Float): Long {
            val rate = callRate / 60
            val balance = getBalance()
            return (balance / rate).toLong()
        }

        @JvmStatic
        fun createContact(context: Context, phoneNo: String, dialCode: String): Contact {
            val contact = Contact()
            contact.name = context.getString(R.string.unknown_number)
            contact.phoneNo = phoneNo
            contact.dialCode = dialCode
            contact.theme = UIUtil.getTheme()
            return contact
        }

        @JvmStatic
        fun getToken(): String {
            return SharedPrefUtil.getString(Constants.TOKEN) ?: ""
        }

        @JvmStatic
        fun setToken(token: String) {
            SharedPrefUtil.setString(Constants.TOKEN, "Bearer $token")
        }

        @JvmStatic
        fun createAccount(account: Account) {
            Paper.book().write(ACCOUNT, account)
        }


    }
}