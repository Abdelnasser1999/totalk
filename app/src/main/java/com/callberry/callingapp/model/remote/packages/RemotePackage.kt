package com.callberry.callingapp.model.remote.packages

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "packages")
class RemotePackage() {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @ColumnInfo(name = "name")
    @SerializedName("name")
    @Expose
    var name: String? = null

    @ColumnInfo(name = "value")
    @SerializedName("value")
    @Expose
    var value: String? = null

    @Ignore
    constructor(name: String, value: String) : this() {
        this.name = name
        this.value = value
    }

}