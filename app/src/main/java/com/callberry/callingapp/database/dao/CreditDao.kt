package com.callberry.callingapp.database.dao

import androidx.room.*
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.remote.credits.RemoteCredit

@Dao
interface CreditDao {
    @Query("SELECT * FROM credit ORDER BY id DESC")
    suspend fun getCredits(): List<Credit>

    @Query("SELECT * FROM remote_credit ORDER BY id DESC")
    suspend fun getLocalCredits(): List<RemoteCredit>

    @Insert
    suspend fun insert(credit: Credit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remote: RemoteCredit)

    @Query("SELECT * FROM credit WHERE name = :creditName ORDER BY id DESC LIMIT 1")
    suspend fun getLastCreditByName(creditName: String): Credit?

}