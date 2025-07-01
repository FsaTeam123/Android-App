package com.example.androidapprpg.data.repository

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs : SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_TOKEN = "TOKEN"
    }

    fun saveLogin(id: Long, token: String?) {
        prefs.edit()
            .putLong(KEY_USER_ID,id)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getUserId() : Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    fun getToken() : String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1L && getToken() != null
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

}