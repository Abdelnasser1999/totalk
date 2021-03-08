package com.callberry.callingapp.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList
import kotlin.reflect.KClass


class PrefUtils {
    companion object {
        @JvmStatic
        fun setString(key: String, value: String): Boolean {
            val editor = sharedPref().edit()
            editor.putString(key, value)
            return editor.commit()
        }

        @JvmStatic
        fun getString(key: String, defaultValue: String? = null): String? {
            return if (contains(key)) sharedPref().getString(
                key,
                defaultValue
            ) else null
        }

        @JvmStatic
        fun setBoolean(key: String, value: Boolean): Boolean {
            val editor = sharedPref().edit()
            editor.putBoolean(key, value)
            return editor.commit()
        }

        @JvmStatic
        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return sharedPref().getBoolean(key, defaultValue)
        }

        @JvmStatic
        fun setLong(key: String, value: Long): Boolean {
            val editor = sharedPref().edit()
            editor.putLong(key, value)
            return editor.commit()
        }

        @JvmStatic
        fun getLong(key: String): Long? {
            return if (contains(key)) sharedPref().getLong(key, 0) else null
        }

        @JvmStatic
        fun setInt(key: String, value: Int): Boolean {
            val editor = sharedPref().edit()
            editor.putInt(key, value)
            return editor.commit()
        }

        @JvmStatic
        fun getInt(key: String, defaultValue: Int): Int {
            return sharedPref().getInt(key, defaultValue)
        }

        @JvmStatic
        fun setFloat(key: String, value: Float): Boolean {
            val editor = sharedPref().edit()
            editor.putFloat(key, value)
            return editor.commit()
        }

        @JvmStatic
        fun getFloat(key: String): Float {
            return sharedPref().getFloat(key, 0.0f)
        }

        @JvmStatic
        fun set(key: String, obj: Any): Boolean {
            val prefsEditor: SharedPreferences.Editor = sharedPref().edit()
            val gson = Gson()
            val json = gson.toJson(obj)
            prefsEditor.putString(key, json)
            return prefsEditor.commit()
        }

        @JvmStatic
        fun <T : Any> get(key: String, activity: KClass<T>): Any? {
            val gson = Gson()
            val json: String = sharedPref().getString(key, "")!!
            return if (contains(key)) gson.fromJson(json, activity.java) else null
        }

        @JvmStatic
        fun setList(key: String, list: ArrayList<Any>?) {
            val editor = sharedPref().edit()
            val gson = Gson()
            val json: String = gson.toJson(list)
            editor.putString(key, json)
            editor.apply()
        }

        @JvmStatic
        fun getList(key: String?): ArrayList<Any?>? {
            val gson = Gson()
            val json: String? = sharedPref().getString(key, null)
            val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
            return gson.fromJson(json, type)
        }

        @JvmStatic
        fun remove(key: String) {
            val editor = sharedPref().edit()
            editor.remove(key)
            editor.commit()
        }

        @JvmStatic
        fun contains(key: String): Boolean {
            return sharedPref().contains(key)
        }

        private fun sharedPref(): SharedPreferences {
            return App.context().getSharedPreferences("new_countries", Context.MODE_PRIVATE)
        }
    }
}