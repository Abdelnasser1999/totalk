package com.callberry.callingapp.database.dao

import androidx.room.*
import com.callberry.callingapp.model.Contact

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    suspend fun getContacts(): List<Contact>

    @Query("SELECT * FROM contacts WHERE phone_no = :phoneNo")
    suspend fun getContactByPhoneNo(phoneNo: String): Contact?

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Insert
    suspend fun insert(contact: Contact)

    @Query("UPDATE contacts SET name =:name WHERE contactId = :id")
    suspend fun update(name: String, id: Int)

    @Query("SELECT COUNT(contactId) FROM contacts WHERE phone_no = :phoneNo")
    suspend fun isContactExist(phoneNo: String): Int

    @Query("SELECT contactId FROM contacts WHERE phone_no = :phoneNo")
    suspend fun getContactId(phoneNo: String): Int

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun countContact(): Int

}