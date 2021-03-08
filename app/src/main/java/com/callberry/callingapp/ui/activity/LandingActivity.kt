package com.callberry.callingapp.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.callberry.callingapp.R
import com.callberry.callingapp.materialdialog.MaterialProgressDialog
import com.callberry.callingapp.util.*
import com.callberry.callingapp.viewmodel.PreferenceViewModel
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : AppCompatActivity() {

    private lateinit var progressDialog: MaterialProgressDialog
    private lateinit var prefViewModel: PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        initViews()
    }

    private fun initViews() {
        progressDialog = MaterialProgressDialog(this)
        progressDialog.setMessage(getString(R.string.logingin))
        prefViewModel = getViewModel(PreferenceViewModel::class.java)
        btnGetStarted.setOnClickListener(getStartedClickListener())
    }

    private fun getStartedClickListener() = View.OnClickListener {
        val intent = Intent(this, NumberVerificationActivity::class.java)
        startActivityForResult(intent,
            RC_LOGIN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOGIN && resultCode == Activity.RESULT_OK) {
            data?.let {
                if (it.getBooleanExtra(Constants.IS_ACCOUNT_SETUP_SUCCESSFUL, false)) {
                    progressDialog.show()
                    loadPreferences()
                } else {
                    toast(getString(R.string.network_error))
                }
            }
        }
    }

    private fun loadPreferences() {
        prefViewModel.loadPref()
        prefViewModel.prefLoaded().observe(this, Observer {
            progressDialog.dismiss()
            App.startSync()
            PrefUtils.setBoolean(Constants.IS_SETUP_COMPLETE, true)
            route(MainActivity::class)
            finishAffinity()
        })
    }

    companion object {
        const val RC_LOGIN = 121
    }
}
