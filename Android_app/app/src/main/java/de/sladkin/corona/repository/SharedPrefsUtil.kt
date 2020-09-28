package de.sladkin.corona.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedPrefsUtil(context: Context) {

    private val USER_UNIQ_ID_KEY = "user_uniq_id"
    private val SHARED_PREFERENCES_KEY = "shared_prefs"
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)

    fun writeUserId(userId: String?) {
        sharedPrefs.edit().putString(USER_UNIQ_ID_KEY, userId)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPrefs.getString(USER_UNIQ_ID_KEY, null)
    }

}