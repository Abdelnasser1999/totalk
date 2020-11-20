package com.callberry.callingapp.model

import androidx.room.*
import com.callberry.callingapp.util.TimeUtil

@Entity(tableName = "recent")
class Recent() {

    @PrimaryKey(autoGenerate = true)
    var recentId: Int = 0

    @ColumnInfo(name = "contact_id")
    var contactId: Int = 0

    @ColumnInfo(name = "recent_phone_no")
    var phoneNo: String = ""

    @ColumnInfo(name = "recent_dial_code")
    var dialCode: String = ""

    @ColumnInfo(name = "duration")
    var duration: String = ""

    @ColumnInfo(name = "credit")
    var credit: Float = 0.0f

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = TimeUtil.getTimestamp()



}