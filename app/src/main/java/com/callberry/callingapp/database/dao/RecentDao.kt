package com.callberry.callingapp.database.dao

import androidx.room.*

import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.model.RecentContact

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent")
    suspend fun getRecent(): List<Recent>

    @Query("SELECT * FROM recent WHERE recent_phone_no = :phoneNo")
    suspend fun getRecentByPhoneNo(phoneNo: String): List<Recent>

    @Delete
    suspend fun deleteRecent(recent: Recent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recent: Recent): Long

    @Query("SELECT contacts.*, recent.* FROM recent LEFT JOIN contacts ON contacts.phone_no = recent.recent_phone_no GROUP BY recent.recent_phone_no ORDER BY recent.recentId DESC")
    suspend fun getRecentContacts(): List<RecentContact>


}