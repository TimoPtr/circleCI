/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pulsingdot.data

import android.content.Context
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.edit
import com.kolibree.android.extensions.observeForChanges
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Observable
import javax.inject.Inject

@VisibleForApp
internal interface PulsingDotProvider {
    fun getTimesShown(pulsingDotPersistence: PulsingDotPersistence): Int
    fun incTimesShown(pulsingDotPersistence: PulsingDotPersistence)
    fun isClicked(pulsingDotPersistence: PulsingDotPersistence): Observable<Boolean>
    fun setIsClicked(pulsingDotPersistence: PulsingDotPersistence)
    fun isExplanationShown(): Observable<Boolean>
    fun setExplanationShown()
}

internal class PulsingDotProviderImpl @Inject constructor(context: Context) :
    PulsingDotProvider, BasePreferencesImpl(context) {

    private val preferences = prefs

    override fun getTimesShown(pulsingDotPersistence: PulsingDotPersistence) =
        preferences.getInt(getTimesShownKey(pulsingDotPersistence), 0)

    override fun incTimesShown(pulsingDotPersistence: PulsingDotPersistence) {
        preferences.edit {
            putInt(
                getTimesShownKey(pulsingDotPersistence),
                getTimesShown(pulsingDotPersistence).inc()
            )
        }
    }

    override fun isClicked(pulsingDotPersistence: PulsingDotPersistence): Observable<Boolean> {
        val key = getIsClickedKey(pulsingDotPersistence)

        return preferences
            .observeForChanges(key) { getBoolean(it, false) }
            .startWith(preferences.getBoolean(key, false))
    }

    override fun setIsClicked(pulsingDotPersistence: PulsingDotPersistence) {
        preferences.edit {
            putBoolean(getIsClickedKey(pulsingDotPersistence), true)
        }
    }

    override fun isExplanationShown(): Observable<Boolean> {
        return preferences
            .observeForChanges(HAS_SHOWN_EXPLANATION) { getBoolean(it, false) }
            .startWith(preferences.getBoolean(HAS_SHOWN_EXPLANATION, false))
    }

    override fun setExplanationShown() {
        preferences.edit { putBoolean(HAS_SHOWN_EXPLANATION, true) }
    }

    private fun getTimesShownKey(pulsingDotPersistence: PulsingDotPersistence) =
        pulsingDotPersistence.key + TIMES_SHOWN

    private fun getIsClickedKey(pulsingDotPersistence: PulsingDotPersistence) =
        pulsingDotPersistence.key + IS_CLICKED

    companion object {
        const val TIMES_SHOWN = "_times_shown"
        const val IS_CLICKED = "_is_clicked"
        const val HAS_SHOWN_EXPLANATION = "has_shown_explanation"
    }
}

internal enum class PulsingDotPersistence(val key: String) {
    SMILE("dot_smile"),
    LAST_BRUSHING_SESSION("dot_last_brushing_session"),
    BRUSH_BETTER("dot_brush_better"),
    FREQUENCY_CHART("dot_frequency_chart")
}
