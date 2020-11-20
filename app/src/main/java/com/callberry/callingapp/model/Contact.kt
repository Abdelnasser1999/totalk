package com.callberry.callingapp.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
public class Contact() : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var contactId: Int = 0

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "phone_no")
    var phoneNo: String? = null

    @ColumnInfo(name = "display_picture")
    var displayPicture: String? = null

    @ColumnInfo(name = "dial_code")
    var dialCode: String? = null

    @ColumnInfo(name = "theme")
    var theme: String? = null

    constructor(parcel: Parcel) : this() {
        contactId = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(contactId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?> {
            return arrayOfNulls(size)
        }
    }

}