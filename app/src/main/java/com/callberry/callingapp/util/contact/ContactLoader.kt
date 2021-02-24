package com.callberry.callingapp.util.contact

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.database.AppDatabase
import com.callberry.callingapp.util.PhoneUtils
import com.callberry.callingapp.util.UIUtil
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber

class ContactLoader {
    companion object {

        private var phoneNoList = ArrayList<String>()

        @JvmStatic
        suspend fun syncContact(context: Context, callback: (isComplete: Boolean, msg: String) -> Unit) {

            val cursor = cursor(context)
            if (cursor == null) {
                callback.invoke(false, "Failed to Sync Contact")
                return
            }

            if (cursor.count <= 0) {
                callback.invoke(false, "No Contact Found")
                return
            }

            while (cursor.moveToNext()) {
                if (getInt(cursor, ContactsContract.Contacts.HAS_PHONE_NUMBER) <= 0) {
                    continue
                }
                val id: Long = getLong(cursor, ContactsContract.Contacts._ID)
                val cursorInfo = cursorInfo(context, id)
                val person: Uri =
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
                val displayPic: Uri = Uri.withAppendedPath(
                        person,
                        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                )
                while (cursorInfo!!.moveToNext()) {

                    var phoneNo =
                            getString(cursorInfo, ContactsContract.CommonDataKinds.Phone.NUMBER)
                    phoneNo = formatWithPlus(phoneNo)
                    if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNo)) {
                        if (phoneNoList.indexOf(phoneNo) == -1) {
                            val name = getString(cursor, ContactsContract.Contacts.DISPLAY_NAME)
                            val number: Phonenumber.PhoneNumber = parseNo(context, phoneNo)
                            phoneNo = format(context, number).replace(" ", "")
                            val contact = Contact()
                            contact.name = name
                            contact.phoneNo = phoneNo
                            contact.displayPicture = displayPic.toString()
                            contact.dialCode = number.countryCode.toString()
                            contact.theme = UIUtil.getTheme()
                            saveContact(context, contact)
                            phoneNoList.add(phoneNo)
                        }
                    }
                }
                cursorInfo.close()
            }
            cursor.close()
            callback.invoke(true, "Contact Loaded")
        }

        private suspend fun saveContact(context: Context, contact: Contact) {
            val contactDao = AppDatabase(context).contactDao()
            if (contactDao.isContactExist(contact.phoneNo.toString()) <= 0) {
                contactDao.insert(contact)
            } else {
                val id = contactDao.getContactId(contact.phoneNo!!)
                contactDao.update(contact.name!!, id)
            }
        }

        private fun formatWithPlus(phoneNo: String): String {
            if (phoneNo.startsWith("03"))
                return phoneNo.replaceFirst("0", "+92")
            return phoneNo
        }

        private fun format(context: Context, number: Phonenumber.PhoneNumber): String {
            return phoneUtil(context)!!.format(
                    number,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
            )
        }

        @JvmStatic
        fun parseNo(context: Context, phoneNo: String): Phonenumber.PhoneNumber {
            return phoneUtil(context)!!.parse(phoneNo, PhoneUtils.getCountryRegionFromPhone(context)!!)
        }

        private fun phoneUtil(context: Context): PhoneNumberUtil? {
            return PhoneNumberUtil.createInstance(context)
        }

        private fun cursorInfo(context: Context, id: Long): Cursor? {
            return resolver(context).query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id.toString()),
                    null
            )
        }

        private fun cursor(context: Context): Cursor? {
            return resolver(context).query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            )
        }

        private fun resolver(context: Context): ContentResolver {
            return context.contentResolver
        }

        private fun getLong(cursor: Cursor, key: String): Long {
            return cursor.getLong(cursor.getColumnIndex(key))
        }

        private fun getInt(cursor: Cursor, key: String): Int {
            return cursor.getInt(cursor.getColumnIndex(key))
        }

        private fun getString(cursor: Cursor, key: String): String {
            return cursor.getString(cursor.getColumnIndex(key))
        }

    }

    interface SyncContactListener {
        fun onSyncComplete(isComplete: Boolean, msg: String)
    }
}