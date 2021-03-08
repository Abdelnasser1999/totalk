package com.callberry.callingapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.callberry.callingapp.R
import com.callberry.callingapp.viewmodel.AccountViewModel
import com.callberry.callingapp.viewmodel.PreferenceViewModel
import com.ornach.nobobutton.NoboButton
import kotlinx.android.synthetic.main.activity_number_verification.*
import kotlin.reflect.KClass

fun isNetworkAvailable(activity: Activity): Boolean {
    val connectivityManager =
        activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun <T : Any> Context.route(activity: KClass<T>) {
    val intent = Intent(this, activity.java)
    startActivity(intent)
}

fun <T : Any> KClass<T>.run(activity: Context) {
    val intent = Intent(activity, this.java)
    activity.startActivity(intent)
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun View.clipView() {
    outlineProvider = ViewOutlineProvider.BACKGROUND
    clipToOutline = true
}

fun exchangeView(viewToVisible: View, viewToGone: View) {
    viewToVisible.visibility = View.VISIBLE
    viewToGone.visibility = View.GONE
}

fun exchangeView(viewToVisible: View, viewToGone: ArrayList<View>) {
    viewToVisible.visibility = View.VISIBLE
    viewToGone.forEach {
        it.visibility = View.GONE
    }
}

fun AppCompatActivity.setFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.homeContainer, fragment)
        .commit()
}

fun hideSoftInput(context: Context, view: View?) {
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (view != null) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun showSoftInput(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun log(message: String) {
    Log.d("@App", message)
}

fun <T : ViewModel> AppCompatActivity.getViewModel(viewModelClass: Class<T>) =
    ViewModelProviders.of(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
        .get(viewModelClass)

fun <T : ViewModel> Activity.viewModel(viewModelClass: Class<T>) =
    ViewModelProvider.AndroidViewModelFactory(application)
        .create(viewModelClass)

fun Context.verifyPhoneNo(dialCode: String, input: String): Boolean {
    if (input.isEmpty()) return false
    val phoneNo = dialCode + input
    return PhoneUtils.isPhoneNumberValid(this, phoneNo, dialCode)
}

fun showError(message: String) {
    Log.d(Constants.ERROR_LOGS, "ERROR: ${message}")
}

fun EditText.onTextChange(callback: (s: String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            callback.invoke(s.toString())
        }
    })
}

fun <E> java.util.ArrayList<E>.enable(enable: Boolean) {
    for (view in this) {
        val btn: ImageView = view as ImageView
        btn.isEnabled = enable
    }
}

fun ImageView.updateView(context: Context, active: Boolean) {
    if (active) {
        this.background = ContextCompat.getDrawable(context, R.drawable.bg_active)
        this.setColorFilter(ContextCompat.getColor(context, R.color.colorWhite))
    } else {
        this.background = ContextCompat.getDrawable(context, R.drawable.bg_non_active)
        this.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
    }
}

//fun Context.setEnable(button: MaterialButton, enable: Boolean) {
////    button.isEnabled = enable
////    button.backgroundColor = if (enable)
////        ContextCompat.getColor(this, R.color.colorPrimary)
////    else
////        ContextCompat.getColor(this, R.color.colorGray)
//}