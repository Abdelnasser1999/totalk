package com.callberry.callingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.callberry.callingapp.database.dao.*
import com.callberry.callingapp.model.Account
import com.callberry.callingapp.model.Contact
import com.callberry.callingapp.model.Credit
import com.callberry.callingapp.model.Recent
import com.callberry.callingapp.model.remote.credits.RemoteCredit
import com.callberry.callingapp.model.remote.packages.RemotePackage

@Database(
    entities = [Contact::class, Account::class, Recent::class, Credit::class, RemoteCredit::class, RemotePackage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun creditDao(): CreditDao
    abstract fun accountDao(): AccountDao
    abstract fun recentDao(): RecentDao
    abstract fun packageDao(): PackageDao

    companion object {
        private const val DATABASE_NAME = "db_qtalk"

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context, AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}