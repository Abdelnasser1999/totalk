package com.callberry.callingapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.callberry.callingapp.model.Account

@Dao
interface AccountDao {
    @Query("SELECT * FROM account LIMIT 1")
    suspend fun getAccounts(): List<Account>

    @Insert
    suspend fun insert(account: Account)

}