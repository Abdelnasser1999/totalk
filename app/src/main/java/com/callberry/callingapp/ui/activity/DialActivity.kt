package com.callberry.callingapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.callberry.callingapp.R
import com.callberry.callingapp.util.toast
import kotlinx.android.synthetic.main.activity_dial.*

class DialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dial)

        init()

    }

    private fun clearListener(): View.OnClickListener {
        return View.OnClickListener {
            toast(getString(R.string.clear_number))
        }
    }

    private fun init() {
        btnClear.setOnClickListener(clearListener())
        btnBack.setOnClickListener(clearListener())
    }
}
