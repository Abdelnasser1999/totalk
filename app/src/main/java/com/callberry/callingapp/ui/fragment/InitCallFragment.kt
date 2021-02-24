package com.callberry.callingapp.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.callberry.callingapp.R
import com.callberry.callingapp.materialdialog.MaterialAlertDialog
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.model.OutgoingCall
import com.callberry.callingapp.model.remote.callrates.CallRate
import com.callberry.callingapp.ui.activity.CallScreenActivity
import com.callberry.callingapp.util.*
import com.callberry.callingapp.viewmodel.CallRateViewModel
import kotlinx.android.synthetic.main.fragment_init_call.*

class InitCallFragment : BottomSheetDialogFragment() {

    private lateinit var contact: Contact
    private var callRate: Float = 0.0f
    private lateinit var viewModel: CallRateViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_init_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        getCallRate()


    }

//    private fun getCallRate() {
//        var dialCode = contact.dialCode
//        if (!(contact.dialCode?.contains("+")!!)) {
//            dialCode = "+$dialCode"
//        }
//
//        viewModel.getCallRates(dialCode!!)
//        viewModel.callRatesByCode().observe(this, Observer { remoteCallRate ->
//            if (remoteCallRate?.callRate == null) {
//                dismiss()
//                UIUtil.popErrorClose(
//                    activity!!,
//                    context!!.getString(R.string.oops), context!!.getString(R.string.network_error)
//                )
//                return@Observer
//            }
//
//            callRate = remoteCallRate.callRate!!.toFloat()
//            textViewName.text = contact.name
//            texViewPhoneNo.text = "${contact.phoneNo}"
//            UIUtil.setIcon(context!!, textViewIcon, contact.name!!, contact.theme!!)
//            val balance: Float = Utils.getBalance()
//            textViewCurrentBalance.text = "$${UIUtil.formatBalance(balance)}"
//            textViewCallRate.text = "$${callRate}"
//            materialBtnCall.isEnabled = balance > callRate
//            exchangeView(layoutContent, progressBar)
//        })
//    }

    private fun getCallRate() {
        viewModel.getCallRates()
        viewModel.callRates().observe(this, Observer {
            if (it == null) {
                dismiss()
                UIUtil.popErrorClose(
                    activity!!,
                    context!!.getString(R.string.oops), context!!.getString(R.string.network_error)
                )
                return@Observer
            }

            var dialCode = contact.dialCode
            if (!(contact.dialCode?.contains("+")!!)) {
                dialCode = "+$dialCode"
            }

            val rate = getCallRateByCode(it, dialCode!!)
            if (rate == "-1") {
                dismiss()
                UIUtil.popNotSupported(
                    activity!!,
                    context!!.getString(R.string.oops),
                    context!!.getString(R.string.country_not_supported)
                )
                return@Observer
            }

            callRate = rate.toFloat()
            textViewName.text = contact.name
            texViewPhoneNo.text = "${contact.phoneNo}"
            UIUtil.setIcon(context!!, textViewIcon, contact.name!!, contact.theme!!)
            val balance: Float = Utils.getBalance()
            textViewCurrentBalance.text = "$${UIUtil.formatBalance(balance)}"
            textViewCallRate.text = "$${callRate}"
            materialBtnCall.isEnabled = balance > callRate
            exchangeView(layoutContent, progressBar)
        })
    }

    private fun getCallRateByCode(callRates: List<CallRate>, dialCode: String): String {
        val callRate = callRates.find { it.dialcode == dialCode }
        if (callRate != null) {
            return callRate.callRate.toString()
        }
        return "-1";
    }


//    private fun getCallRate() {
//        Handler().postDelayed({
//            callRate = 1.5f
//            textViewName.text = contact.name
//            texViewPhoneNo.text = "${contact.phoneNo}"
//            UIUtil.setIcon(context!!, textViewIcon, contact.name!!, contact.theme!!)
//            val balance: Float = Utils.getBalance()
//            textViewCurrentBalance.text = "$${UIUtil.formatBalance(balance)}"
//            textViewCallRate.text = "$${callRate}"
//            materialBtnCall.isEnabled = balance > callRate
//            exchangeView(layoutContent, progressBar)
//        }, 1000)
//    }

    private fun init() {
        contact = arguments!!.getParcelable(ARG_CONTACT)!!
        viewModel = CallRateViewModel.getInstance(activity!!)
        materialBtnCall.setOnClickListener {
            // TODO: For production release
            dismiss()
            startCall()

            // TODO: For Demo Release
            //starDemoCall()
        }
        materialBtnCancel.setOnClickListener { dismiss() }

    }

    private fun starDemoCall() {
        //This function will be eliminated so no need to localise
        val materialAlertDialog = MaterialAlertDialog(activity!!)
        materialAlertDialog.setTitle("Demo Call")
        materialAlertDialog.setMessage("This app is for demo calls only, this call will redirect to this number +923027121483")
        materialAlertDialog.setPositiveClick("Call now") {
            callNow()
            it.dismiss()

        }
        materialAlertDialog.setNegativeClick("Maybe later") {
            it.dismiss()
        }
        materialAlertDialog.show()
    }

    private fun startCall() {
        if (!isNetworkAvailable(activity!!)) {
            context!!.toast(getString(R.string.network_not_avaliable))
            return
        }

        val outgoingCall = OutgoingCall()
        outgoingCall.contactId = contact.contactId
        outgoingCall.name = contact.name.toString()
        outgoingCall.phoneNo = contact.phoneNo.toString()
        outgoingCall.dialCode = contact.dialCode.toString()
        outgoingCall.theme = contact.theme!!
        outgoingCall.callRate = callRate
        SharedPrefUtil.set(Constants.OUTGOING_CALL, outgoingCall)
        context!!.route(CallScreenActivity::class)
    }

    private fun callNow() {
        val outgoingCall = OutgoingCall()
        outgoingCall.contactId = contact.contactId
        outgoingCall.name = contact.name.toString()
        outgoingCall.phoneNo = "+923027121483"
        outgoingCall.dialCode = "+92"
        outgoingCall.theme = contact.theme!!
        outgoingCall.callRate = callRate
        SharedPrefUtil.set(Constants.OUTGOING_CALL, outgoingCall)
        context!!.route(CallScreenActivity::class)
        dismiss()
    }


    companion object {
        private const val ARG_CONTACT = "ARG_CONTACT"

        @JvmStatic
        fun init(manager: FragmentManager, contact: Contact) {
            val bundle = Bundle()
            bundle.putParcelable(ARG_CONTACT, contact)
            val initCallFragment = InitCallFragment()
            initCallFragment.arguments = bundle
            initCallFragment.show(manager, "call_dialog")
        }
    }

}
