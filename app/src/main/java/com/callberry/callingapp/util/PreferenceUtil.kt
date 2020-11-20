package com.callberry.callingapp.util

import com.callberry.callingapp.model.prefmodel.*
import io.paperdb.Paper

object PreferenceUtil {
    const val KEY_SINCH = "SINCH"
    const val KEY_BILLING = "BILLING"
    const val KEY_ADMOB = "ADMOB"
    const val KEY_FACEBOOK = "FACEBOOK"
    const val KEY_REMOTE_CREDITS = "EMOTE_CREDITS"
    const val KEY_PACKAGES = "PACKAGES"

    @JvmStatic
    fun setSinchCred(sinch: Sinch) {
        set(KEY_SINCH, sinch)
    }

    @JvmStatic
    fun getSinchCred(): Sinch {
        val sinch: Sinch? = get(KEY_SINCH) as Sinch?
        return sinch ?: DefaultConfig.sinch
    }

    @JvmStatic
    fun setBillingCred(billing: GoogleBilling) {
        set(KEY_BILLING, billing)
    }

    @JvmStatic
    fun getBillingCred(): GoogleBilling {
        val billing: GoogleBilling? = get(KEY_BILLING) as GoogleBilling?
        return billing ?: DefaultConfig.billing
    }

    @JvmStatic
    fun setGoogleAdmob(googleAdmob: GoogleAdmob) {
        set(KEY_ADMOB, googleAdmob)
    }

    @JvmStatic
    fun getGoogleAdmob(): GoogleAdmob {
        val admob: GoogleAdmob? = get(KEY_ADMOB) as GoogleAdmob?
        return admob ?: DefaultConfig.googleAdmob
    }

    @JvmStatic
    fun setFacebookAd(fbAd: FacebookAd) {
        set(KEY_FACEBOOK, fbAd)
    }

    @JvmStatic
    fun getFacebookAd(): FacebookAd {
        val facebookAd: FacebookAd? = get(KEY_FACEBOOK) as FacebookAd?
        return facebookAd ?: DefaultConfig.facebookAd
    }

    @JvmStatic
    fun setRemoteCredits(remoteCredits: List<RemoteCredit>) {
        set(KEY_REMOTE_CREDITS, remoteCredits)
    }

    @JvmStatic
    fun getRemoteCredits(): List<RemoteCredit> {
        val remoteCredits: List<RemoteCredit>? = get(KEY_REMOTE_CREDITS) as List<RemoteCredit>?
        return remoteCredits ?: DefaultConfig.creditsLimits
    }

    @JvmStatic
    fun setPackages(packages: List<Package>) {
        set(KEY_PACKAGES, packages)
    }

    @JvmStatic
    fun getPackages(): List<Package> {
        val packages: List<Package>? = get(KEY_PACKAGES) as List<Package>?
        return packages ?: DefaultConfig.packages
    }

    private fun set(key: String, data: Any) {
        Paper.book().write(key, data)
    }

    private fun get(key: String): Any? {
        return Paper.book().read(key)
    }

}

