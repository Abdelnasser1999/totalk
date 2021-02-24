package com.callberry.callingapp.retrofit

import com.callberry.callingapp.model.TimeModel
import com.callberry.callingapp.model.prefmodel.PreferenceModel
import com.callberry.callingapp.model.remote.account.RemoteAccounts
import com.callberry.callingapp.model.remote.callrates.CallRates
import com.callberry.callingapp.model.remote.credits.RemoteCredits
import com.callberry.callingapp.model.remote.packages.RemotePackages
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("api/register")
    @FormUrlEncoded
    fun login(
            @Field("phone") phoneNo: String,
            @Field("dial_code") dialCode: String,
            @Field("refer_code") referralCode: String
    ): Call<RemoteAccounts>

    @GET("api/callrates")
    fun getCallRates(
            @Header("authorization") token: String
    ): Call<CallRates>

    @GET("api/callrates/{dialCode}")
    fun getCallRateByCode(
            @Path("dialCode") dialCode: String,
            @Header("authorization") token: String
    ): Call<CallRates>

    @GET("api/setting")
    fun getRemoteCredits(@Header("authorization") token: String): Call<RemoteCredits>

    @GET("api/package")
    fun getPackages(@Header("authorization") token: String): Call<RemotePackages>

    @GET("api/multiApis")
    fun getPreference(@Header("authorization") token: String): Call<PreferenceModel>

    @GET("api/ServerTime")
    fun getTime(@Header("authorization") token: String): Call<TimeModel>
}