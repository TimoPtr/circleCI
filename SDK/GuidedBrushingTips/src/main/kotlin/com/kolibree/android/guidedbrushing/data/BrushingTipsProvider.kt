/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.data

import android.content.Context
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@VisibleForApp
internal interface BrushingTipsProvider {
    fun isScreenDisplayable(): Single<Boolean>
    fun setNoShowAgain(): Completable
}

internal class BrushingTipsProviderImpl @Inject constructor(context: Context) :
    BrushingTipsProvider, BasePreferencesImpl(context) {

    private val preferences = prefs

    override fun isScreenDisplayable(): Single<Boolean> {
        return Single.fromCallable {
            preferences.getBoolean(KEY_BRUSHING_TIPS_DISPLAYABLE, true)
        }
    }

    override fun setNoShowAgain(): Completable {
        return Completable.fromAction {
            preferences.edit { putBoolean(KEY_BRUSHING_TIPS_DISPLAYABLE, false) }
        }
    }

    companion object {
        const val KEY_BRUSHING_TIPS_DISPLAYABLE = "brushing_tips_displayable"
    }
}
