package com.kolibree.android.sba.testbrushing.results.hint

import android.annotation.SuppressLint
import android.content.SharedPreferences
import javax.inject.Inject

@SuppressLint("DeobfuscatedPublicSdkClass")
interface ResultHintsPreferences {
    fun isChangeViewHintVisible(): Boolean
    fun removeChangeViewHint()
}

internal class ResultHintsPreferencesImpl
@Inject constructor(private val preferences: SharedPreferences) : ResultHintsPreferences {

    override fun isChangeViewHintVisible() = preferences.getBoolean(CHANGE_VIEW_HINT_KEY, true)

    override fun removeChangeViewHint() = preferences.edit()
        .putBoolean(CHANGE_VIEW_HINT_KEY, false)
        .apply()

    companion object {
        val TAG = ResultHintsPreferencesImpl::class.java.simpleName
        val CHANGE_VIEW_HINT_KEY = "${TAG}_CHANGE_VIEW_HINT_KEY"
    }
}
