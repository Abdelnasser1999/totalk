package com.callberry.callingapp.model.prefmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GoogleBilling {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("license_key")
    @Expose
    var licenseKey: String? = null

    @SerializedName("merchant_id")
    @Expose
    var merchantId: String? = null

    @SerializedName("product_id")
    @Expose
    var productId: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    constructor(licenseKey: String, merchantId: String, productId: String) {
        this.licenseKey = licenseKey
        this.merchantId = merchantId
        this.productId = productId
    }


}