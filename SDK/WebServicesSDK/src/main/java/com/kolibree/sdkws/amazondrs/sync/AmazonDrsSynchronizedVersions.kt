/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.amazondrs.sync

import android.content.Context
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import javax.inject.Inject

internal class AmazonDrsSynchronizedVersions @Inject constructor(context: Context) :
    BasePreferencesImpl(context), Truncable {

    companion object {
        const val KEY_DRS_SYNC_VERSION = "drs_sync_version"
    }

    fun getDrsVersion() = prefs.getInt(KEY_DRS_SYNC_VERSION, 0)

    fun setDrsVersion(newVersion: Int) {
        prefsEditor.putInt(KEY_DRS_SYNC_VERSION, newVersion).apply()
    }

    override fun truncate(): Completable = Completable.fromAction { clear() }
}
