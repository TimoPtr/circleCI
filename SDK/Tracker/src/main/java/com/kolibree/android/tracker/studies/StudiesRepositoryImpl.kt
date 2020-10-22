/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.studies

import android.content.Context
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

internal class StudiesRepositoryImpl @Inject constructor(
    context: Context
) : BasePreferencesImpl(context), StudiesRepository {

    override fun getStudy(mac: String) = prefs.getString(mac.studyKey(), NO_STUDY) ?: NO_STUDY

    override fun addStudy(mac: String, studyName: String?) {
        prefsEditor
            .putString(mac.studyKey(), studyName)
            .commit()
    }
}

internal const val NO_STUDY = ""

private fun String.studyKey() = "$KEY_STUDY$this"

private const val KEY_STUDY = "studyForMac"
