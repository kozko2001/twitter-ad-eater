package com.acme.twitteradeater

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge

private const val TAG: String = "TwitterAdEater"

class Utils {
    companion object {
        lateinit var prefs: XSharedPreferences

        fun readPrefs() {
            prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)
            prefs.makeWorldReadable()
            prefs.reload()
        }
    }
}

fun log(message: String, vararg va: Any?) {
    XposedBridge.log(String.format(message, *va))
}

fun logcat(message: String, vararg va: Any?) {
    Log.i(TAG, String.format(message, *va))
}

fun logerr(message: String, vararg va: Any?) {
    Log.e(TAG, String.format(message, *va))
}