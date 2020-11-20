package com.callberry.callingapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.callberry.callingapp.util.TimeUtil

@Entity(tableName = "credit")
class Credit {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "name")
    var name: String = ""

    @ColumnInfo(name = "credits")
    var credit: Float = 0.0f

    @ColumnInfo(name = "type")
    var type: String = ""

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = TimeUtil.getTimestamp()
}