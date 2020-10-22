/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.sync

import android.content.Context
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import javax.inject.Inject

internal class AccountSynchronizedVersions @Inject constructor(context: Context) :
    BasePreferencesImpl(context), Truncable {

    companion object {
        const val KEY_ACCOUNT_SYNC_VERSION = "account_sync_version"
    }

    fun getAccountVersion() = prefs.getInt(KEY_ACCOUNT_SYNC_VERSION, 0)

    fun setAccountVersion(newVersion: Int) {
        prefsEditor.putInt(KEY_ACCOUNT_SYNC_VERSION, newVersion).apply()
    }

    override fun truncate(): Completable = Completable.fromAction { clear() }
}
