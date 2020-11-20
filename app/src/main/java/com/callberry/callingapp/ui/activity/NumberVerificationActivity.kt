package com.callberry.callingapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.callberry.callingapp.R
import com.callberry.callingapp.countrypicker.Country
import com.callberry.callingapp.countrypicker.CountryPickerAdapter
import com.callberry.callingapp.countrypicker.JSONParser
import com.callberry.callingapp.materialdialog.MaterialProgress
import com.callberry.callingapp.materialdialog.MaterialProgressDialog
import com.callberry.callingapp.model.Account
import com.callberry.callingapp.util.*
import com.callberry.callingapp.util.contact.ContactLoader
import com.callberry.callingapp.viewmodel.AccountViewModel
import com.callberry.callingapp.viewmodel.PreferenceViewModel
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.mukesh.OnOtpCompletionListener
import io.michaelrocks.libphonenumber.android.Phonenumber
import kotlinx.android.synthetic.main.activity_number_verification.*
import kotlinx.android.synthetic.main.layout_enter_code.*
import kotlinx.android.synthetic.main.layout_enter_phone_number.*
import kotlinx.android.synthetic.main.layout_select_country.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class NumberVerificationActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "numberVerification"
    private lateinit var account: Account
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var prefViewModel: PreferenceViewModel
    private var countryPickerSheet: BottomSheetBehavior<*>? = null
    private var adapter: CountryPickerAdapter? = null
    private lateinit var progressDialog: MaterialProgressDialog
    private val CREDENTIAL_PICKER_REQUEST = 1210
    private var phoneNo: String? = null
    private val countries = ArrayList<Country>()
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val materialProgress = MaterialProgress(this)
    private var countDownTimer: CountDownTimer? = null
    val phoneNum = "6505554567"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_verification)

        initViews()
        initCountryPicker()
        requestHint()

    }

    private fun initCountryPicker() {
        countries.addAll(JSONParser.countries(this))
        countries.sortWith(Comparator { o1: Country, o2: Country -> o1.name.compareTo(o2.name) })
        adapter = CountryPickerAdapter(countries) {
            txtViewCountry.text = it.flag
            txtDialCode.text = it.dialCode
            verifyPhoneNo(editTextMobileNo.text.toString())
            countryPickerSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        listCountries.layoutManager = LinearLayoutManager(this)
        listCountries.adapter = adapter

        setCountry("+1")
        editTextMobileNo.setText(phoneNum)
    }

    private fun verifyCode(code: String) {
        materialProgress.show()
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (!it.isSuccessful) {
                materialProgress.dismiss()
                UIUtil.showInfoSnackbar(window.decorView.rootView, getString(R.string.invalid_code))
                return@addOnCompleteListener
            }

            setupAccount()
        }
    }

    private fun setupAccount() {
        accountViewModel.setupAccount(account)
        accountViewModel.setupAccount().observe(this, Observer {
            materialProgress.dismiss()
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.IS_ACCOUNT_SETUP_SUCCESSFUL, it)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        })
    }

    private fun sendVerificationCode() {
        materialProgress.show()
        val dialCode = txtDialCode.text.toString()
            .substring(txtDialCode.text.indexOf("+"), txtDialCode.length())
        val phoneNo = dialCode + editTextMobileNo.text.toString()
        account = Account()
        account.phoneNo = phoneNo
        account.dialCode = "${dialCode}000"

        Log.d(TAG, "Phone Number: ${account.phoneNo}")
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(account.phoneNo)
//            .setTimeout(60L, TimeUnit.SECONDS)
//            .setActivity(this)
//            .setCallbacks(callbacks)
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)

        PhoneAuthProvider.getInstance().verifyPhoneNumber(account.phoneNo, 60, TimeUnit.SECONDS, this, callbacks)

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")
            credential.smsCode?.let { verifyCode(it) }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.d(TAG, "onVerificationFailed:${e.message.toString()}")
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d(TAG, "onVerificationFailed: Invalid Request")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d(TAG, "onVerificationFailed: SMS quota for the project has been exceeded")
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG, "onCodeSent:$verificationId")
            materialProgress.dismiss()
            storedVerificationId = verificationId
            resendToken = token
            slideNext(layoutEnterNumber, layoutVerificationCode)
            txtCodeDesp.text = resources.getString(R.string.enter_code_descp, "${account.phoneNo}")
            startResendTimer()
        }
    }


    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val credentialsClient = Credentials.getClient(this)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        startIntentSenderForResult(intent.intentSender, CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CREDENTIAL_PICKER_REQUEST ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    phoneNo = credential.id
                    val number: Phonenumber.PhoneNumber = ContactLoader.parseNo(this, credential.id)
                    Log.d(
                        "numberLogs",
                        "Dial Code: ${number.countryCode}, Phone Number: ${number.nationalNumber}"
                    )
                    setCountry("+${number.countryCode}")
                    editTextMobileNo.setText(number.nationalNumber.toString())
                    sendVerificationCode()
                }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClosePicker -> {
                countryPickerSheet?.run {
                    if (state == BottomSheetBehavior.STATE_EXPANDED) {
                        state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }

            R.id.layoutSelectCountry -> {
                editTextSearch.text.clear()
                countryPickerSheet?.run {
                    if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }

            R.id.btnCont -> {
                sendVerificationCode()
            }

            R.id.textViewResendCode -> {
                PhoneAuthProvider
                    .getInstance()
                    .verifyPhoneNumber(
                        account.phoneNo,
                        60,
                        TimeUnit.SECONDS,
                        this,
                        callbacks,
                        resendToken
                    )
                toast("Code sent again")
                textViewResendCode.isEnabled = false
                textViewResendCode.setTextColor(
                    ContextCompat.getColor(
                        this@NumberVerificationActivity,
                        R.color.colorGray
                    )
                )
            }
        }
    }

    private fun initViews() {
        auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()
        accountViewModel = getViewModel(AccountViewModel::class.java)
        prefViewModel = getViewModel(PreferenceViewModel::class.java)
        countryPickerSheet = BottomSheetBehavior.from(bottomSelectCountry)
        editTextMobileNo.onTextChange {
            if (txtDialCode.text.toString().contains("+")) {
                verifyPhoneNo(it)
            }
        }
        editTextSearch.onTextChange { adapter?.filter?.filter(it) }
        btnCont.setOnClickListener(this)
        btnBack.setOnClickListener { onBackPressed() }
        layoutSelectCountry.setOnClickListener(this)
        textViewResendCode.setOnClickListener(this)
        btnClosePicker.setOnClickListener(this)
        otpView.setOtpCompletionListener { verifyCode(it) }
    }

    private fun startResendTimer() {
        val timeInMS: Long = 60000
        countDownTimer = object : CountDownTimer(timeInMS, 1000) {
            override fun onTick(l: Long) {
                val hours = TimeUtil.intoHours(l)
                val minutes = TimeUtil.intoMinutes(l)
                val seconds = TimeUtil.intoSeconds(l)
                var time = "0$hours"
                time += if (minutes < 10) " : 0$minutes" else " : $minutes"
                time += if (seconds < 10) " : 0$seconds" else " : $seconds"
                textViewResendCode?.let { it.text = time }
            }

            override fun onFinish() {
                countDownTimer!!.cancel()
                textViewResendCode.isEnabled = true
                textViewResendCode.text = "Resend code"
                textViewResendCode.setTextColor(
                    ContextCompat.getColor(
                        this@NumberVerificationActivity,
                        R.color.colorPrimary
                    )
                )
            }
        }.start()
    }

    private fun setCountry(dialCode: String) {
        val country = countries.filter { it.dialCode == dialCode }
        txtViewCountry.text = country[0].flag
        txtDialCode.text = country[0].dialCode
    }

    private fun verifyPhoneNo(input: String) {
        val dialCode = txtDialCode.text.toString()
            .substring(txtDialCode.text.indexOf("+"), txtDialCode.length())
        btnCont.isEnabled = verifyPhoneNo(dialCode, input)

    }
}
