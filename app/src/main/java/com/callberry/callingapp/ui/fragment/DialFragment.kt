package com.callberry.callingapp.ui.fragment

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.callberry.callingapp.R
import com.callberry.callingapp.adapter.ContactAdapter
import com.callberry.callingapp.countrypicker.*
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.util.PhoneUtils
import com.callberry.callingapp.viewmodel.ContactViewModel
import kotlinx.android.synthetic.main.activity_number_verification.*
import kotlinx.android.synthetic.main.fragment_dial.*
import kotlinx.android.synthetic.main.fragment_dial.btnBack
import kotlinx.android.synthetic.main.fragment_dial.layoutSelectCountry
import kotlinx.android.synthetic.main.fragment_dial.txtViewCountry
import kotlinx.android.synthetic.main.layout_select_country.*

class DialFragment : BottomSheetDialogFragment(), ContactAdapter.ContactSelectListener {

    private lateinit var adapter: ContactAdapter
    private lateinit var contactViewMode: ContactViewModel
    private var isContactImported = false
    private var countryPickerSheet: BottomSheetBehavior<*>? = null
    private var countryPickerAdapter: CountryPickerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactViewMode = ContactViewModel.getInstance(activity!!)

        initViews()
        initCountryPicker()
        Handler().postDelayed({ getContacts() }, 500)
    }

    private fun initCountryPicker() {
        val countries = JSONParser.countries(activity)
        countries.sortWith(Comparator { o1: Country, o2: Country -> o1.name.compareTo(o2.name) })
        countryPickerAdapter = CountryPickerAdapter(countries) {
            txtViewCountry.text = it.flag
            textViewDialCode.text = it.dialCode
            countryPickerSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        listCountries.layoutManager = LinearLayoutManager(activity)
        listCountries.adapter = countryPickerAdapter

    }

    private fun getContacts() {
        contactViewMode.getContacts()
        contactViewMode.contacts().observe(this, Observer {
            if (it == null) {
                return@Observer
            }

            isContactImported = true
            adapter = ContactAdapter(context!!, ArrayList(it), this@DialFragment)
            listContacts.layoutManager = LinearLayoutManager(context)
            listContacts.adapter = adapter
        })
    }

    override fun onContactSelect(contact: Contact) {
        InitCallFragment.init(childFragmentManager, contact)
    }

    private fun initCall() {
        val dialCode = textViewDialCode.text.toString()
        val phoneNo = dialCode + textViewPhoneNo.text.toString()
        contactViewMode.getContactsByPhoneNo(phoneNo, dialCode) {
            val permissions =
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)
            Permissions.check(context!!, permissions, null, null, object : PermissionHandler() {
                override fun onGranted() {
                    InitCallFragment.init(childFragmentManager, it)
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        val bottomSheet =
            dialog!!.findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        val view = view
        view!!.post {
            val parent = view.parent as View
            val params =
                parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            bottomSheetBehavior!!.peekHeight = view.measuredHeight
        }

    }

    private fun enterPhoneNo(input: String) {
        if (textViewPhoneNo.text.isEmpty()) {
            textViewPhoneNo.text = input
        } else {
            val currentNo: String = textViewPhoneNo.text.toString()
            textViewPhoneNo.text = currentNo + input
        }

        onPhoneNumberEnter()
    }

    private fun onPhoneNumberEnter() {
        if (isContactImported) {
            adapter.getFilter()!!.filter(textViewPhoneNo.text.toString())
        }

        if (textViewDialCode.text.length <= 1 || textViewPhoneNo.text.isEmpty()) {
            return
        }

        val dialCode = textViewDialCode.text.toString()
        val phoneNo = dialCode + textViewPhoneNo.text.toString()
        val enable = PhoneUtils.isPhoneNumberValid(context!!, phoneNo, dialCode)
        btnCall.isEnabled = enable
    }

    private fun backSpace() {
        if (textViewPhoneNo.text.toString().isNotEmpty()) {
            var phoneNo: String = textViewPhoneNo.text.toString()
            phoneNo = phoneNo.substring(0, phoneNo.length - 1)
            textViewPhoneNo.text = phoneNo
        }

        onPhoneNumberEnter()
    }

    private fun initViews() {
        dial_0.setOnClickListener { enterPhoneNo("0") }
        dial_1.setOnClickListener { enterPhoneNo("1") }
        dial_2.setOnClickListener { enterPhoneNo("2") }
        dial_3.setOnClickListener { enterPhoneNo("3") }
        dial_4.setOnClickListener { enterPhoneNo("4") }
        dial_5.setOnClickListener { enterPhoneNo("5") }
        dial_6.setOnClickListener { enterPhoneNo("6") }
        dial_7.setOnClickListener { enterPhoneNo("7") }
        dial_8.setOnClickListener { enterPhoneNo("8") }
        dial_9.setOnClickListener { enterPhoneNo("9") }
        btnBack.setOnClickListener { dismiss() }
        btnBackspace.setOnClickListener { backSpace() }
        countryPickerSheet = BottomSheetBehavior.from(bottomSelectCountry)
        btnCall.setOnClickListener { initCall() }
        btnClosePicker.setOnClickListener {
            countryPickerSheet?.run {
                if (state == BottomSheetBehavior.STATE_EXPANDED) {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        btnClear.setOnClickListener {
            textViewPhoneNo.text = ""
            onPhoneNumberEnter()
        }
        layoutSelectCountry.setOnClickListener {
//            editTextSearch.text.clear()
//            countryPickerSheet?.run {
//                if (state == BottomSheetBehavior.STATE_COLLAPSED) {
//                    state = BottomSheetBehavior.STATE_EXPANDED
//                }
//            }

            val countryPickerFragment = CountryPickerFragment(CountrySelectListener {
                txtViewCountry.text = "${it.flag} ${it.name}"
                textViewDialCode.text = it.dialCode
                onPhoneNumberEnter()
            })
            countryPickerFragment.show(childFragmentManager, "dialog")
        }
    }


}
