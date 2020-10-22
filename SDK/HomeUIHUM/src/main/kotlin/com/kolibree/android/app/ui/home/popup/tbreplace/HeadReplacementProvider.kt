/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.tbreplace

import android.content.Context
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.LocalDate

@VisibleForApp
interface HeadReplacementProvider {
    fun getWarningHiddenDate(mac: String): Single<LocalDate>

    fun setWarningHiddenDate(mac: String, localDate: LocalDate): Completable
}

internal class HeadReplacementProviderImpl @Inject constructor(context: Context) :
    HeadReplacementProvider, BasePreferencesImpl(context) {

    private val preferences = prefs

    /**
     * Retrieve the [LocalDate] associated with this head Toothbrush
     * where the user have decided to hide the head replacement warning.
     */
    override fun getWarningHiddenDate(mac: String): Single<LocalDate> {
        return Single.fromCallable {
            LocalDate.ofEpochDay(preferences.getLong(getKey(mac), 0))
        }
    }

    /**
     * Set the [LocalDate] associated with this head Toothbrush when
     * the head replacement warning should not be shown for this date.
     */
    override fun setWarningHiddenDate(mac: String, localDate: LocalDate): Completable {
        return Completable.fromAction {
            preferences.edit { putLong(getKey(mac), localDate.toEpochDay()) }
        }
    }

    /**
     * Get the shared preferences key which is used to determine if
     * the head replacement warning has been hidden.
     * This key is associated with an unique Toothbrush.
     */
    private fun getKey(mac: String) = KEY_NEVER_SHOW_AGAIN_HEAD_REPLACE + mac
}

private const val KEY_NEVER_SHOW_AGAIN_HEAD_REPLACE = "hide_head_replacement_warning_"
