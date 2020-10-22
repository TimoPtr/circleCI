/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization

import android.content.Context
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import javax.inject.Inject

internal class StatsSynchronizedVersions @Inject constructor(context: Context) :
    BasePreferencesImpl(context), Truncable {

    companion object {
        const val KEY_IN_OFF_BRUSHINGS_COUNT = "in_off_brushings_count"
    }

    fun inOffBrushingsCountVersion() = prefs.getInt(KEY_IN_OFF_BRUSHINGS_COUNT, 0)

    fun setInOffBrushingsCountVersion(newVersion: Int) {
        prefsEditor.putInt(KEY_IN_OFF_BRUSHINGS_COUNT, newVersion).apply()
    }

    override fun truncate(): Completable = Completable.fromAction { clear() }
}
