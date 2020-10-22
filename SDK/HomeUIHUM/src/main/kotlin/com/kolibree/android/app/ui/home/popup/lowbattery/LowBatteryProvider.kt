/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.lowbattery

import android.content.Context
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

@VisibleForApp
internal interface LowBatteryProvider {
    fun isWarningShown(): Boolean

    fun setWarningShown(warningShown: Boolean)
}

internal class LowBatteryProviderImpl @Inject constructor(context: Context) :
    LowBatteryProvider, BasePreferencesImpl(context) {

    private val preferences = prefs

    override fun isWarningShown(): Boolean {
        return preferences.getBoolean(WARNING_SHOWN, false)
    }

    override fun setWarningShown(warningShown: Boolean) {
        preferences.edit { putBoolean(WARNING_SHOWN, warningShown) }
    }

    companion object {
        private const val WARNING_SHOWN = "low_battery_warning_shown"
    }
}
