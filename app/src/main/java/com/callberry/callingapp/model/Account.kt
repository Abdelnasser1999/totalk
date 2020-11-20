package com.callberry.callingapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "account")
class Account {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @SerializedName("phone")
    @Expose
    @ColumnInfo(name = "phone_no")
    var phoneNo: String = ""

    @SerializedName("dial_code")
    @Expose
    @ColumnInfo(name = "dial_code")
    var dialCode: String = ""

    @SerializedName("credits")
    @Expose
    @ColumnInfo(name = "balance")
    var balance: String = ""

    @SerializedName("referral_code")
    @Expose
    @ColumnInfo(name = "ref_code")
    var refCode: String = ""
}
