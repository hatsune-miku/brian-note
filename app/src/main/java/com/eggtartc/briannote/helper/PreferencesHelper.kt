package com.eggtartc.briannote.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.eggtartc.briannote.R

class PreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    fun read(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun read(key: String, defaultValue: String?): String? {
        Log.d("PreferencesHelper", "read: $key")
        return sharedPreferences.getString(key, defaultValue)
    }

    fun read(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun writeAsync(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun writeAsync(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun writeAsync(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    @SuppressLint("ApplySharedPref")
    fun write(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).commit()
    }

    @SuppressLint("ApplySharedPref")
    fun write(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).commit()
    }
}
