package com.callberry.callingapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.callberry.callingapp.model.remote.packages.RemotePackage

@Dao
interface PackageDao {

    @Query("SELECT * FROM packages ORDER BY value ASC")
    suspend fun getPackages(): List<RemotePackage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: RemotePackage)
}