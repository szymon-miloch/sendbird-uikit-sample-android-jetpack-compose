package com.sendbird.uikit.compose.sample.pref

import android.content.Context
import android.content.SharedPreferences

private const val PREFERENCE_KEY_APP_ID = "PREFERENCE_KEY_APP_ID"
private const val PREFERENCE_KEY_USER_ID = "PREFERENCE_KEY_USER_ID"
private const val PREFERENCE_KEY_THEME_MODE = "PREFERENCE_KEY_THEME_MODE"
private const val PREFERENCE_KEY_DO_NOT_DISTURB = "PREFERENCE_KEY_DO_NOT_DISTURB"

/**
 * A class that manages the preferences of the Sendbird UIKit.
 */
class SendbirdUikitPref(context: Context, fileName: String = "sendbird-uikit-compose-sample") {
    var appId: String
        get() = getString(PREFERENCE_KEY_APP_ID) ?: "FEA2129A-EA73-4EB9-9E0B-EC738E7EB768"
        set(value) = putString(PREFERENCE_KEY_APP_ID, value)

    var userId: String
        get() = getString(PREFERENCE_KEY_USER_ID) ?: ""
        set(value) = putString(PREFERENCE_KEY_USER_ID, value)

    var themeMode: Boolean
        get() = getBoolean(PREFERENCE_KEY_THEME_MODE, false)
        set(themeMode) = putBoolean(PREFERENCE_KEY_THEME_MODE, themeMode)

    var doNotDisturb: Boolean
        get() = getBoolean(PREFERENCE_KEY_DO_NOT_DISTURB, false)
        set(doNotDisturb) = putBoolean(PREFERENCE_KEY_DO_NOT_DISTURB, doNotDisturb)

    private fun getString(key: String, defaultValue: String? = null): String? =
        pref.getString(key, defaultValue) ?: defaultValue

    private fun putString(key: String, value: String) = pref.edit().putString(key, value).apply()

    private fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = pref.getBoolean(key, defaultValue)

    private fun putBoolean(key: String, value: Boolean) = pref.edit().putBoolean(key, value).apply()

    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(
            fileName,
            Context.MODE_PRIVATE
        )
    }
}
