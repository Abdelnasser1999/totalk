package com.callberry.callingapp.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.ContactAdapter
import com.callberry.callingapp.database.dao.ContactDao
import com.callberry.callingapp.materialdialog.MaterialProgressDialog
import com.callberry.callingapp.ui.fragment.ContactFragment
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var contactFragment: ContactFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        init()

        contactFragment = ContactFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.contactFragment, contactFragment)
            .commit()


    }

    private fun init() {
        editTextSearch.addTextChangedListener(getTextWatcher())
        btnBack.setOnClickListener { onBackPressed() }

    }

    private fun getTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                contactFragment.searchContact(s.toString())
            }
        }
    }

}
