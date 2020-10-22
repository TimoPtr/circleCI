/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.persistence

import android.content.Context
import com.kolibree.android.extensions.edit
import com.kolibree.android.persistence.BasePreferencesImpl
import java.lang.RuntimeException
import javax.inject.Inject
import timber.log.Timber

/**
 * Manages flags that should never been reset. Typically, they handle cases when
 * something should happen once after installation.
 */
@Deprecated("If you want to handle a case when something should happen only once after " +
    "installation, please use a `Migration` object instead")
class InstallationFlags @Inject constructor(context: Context) : BasePreferencesImpl(context) {

    enum class Flag(val key: String) {
        SHOULD_WIPE_BRUSHINGS_BEFORE_SHOWING_CALENDAR(
            "should_wipe_brushings_before_showing_calendar"
        ),
        SHOULD_WIPE_BRUSHINGS_TO_MAKE_SURE_DATA_ARE_NOT_CORRUPTED(
            "should_wipe_brushings_to_make_sure_data_are_not_corrupted"
        ),
        SHOULD_CONFIGURE_FEATURE_TOGGLES(
            "should_configure_feature_toggles"
        ),
        MAKE_ARA_VISIBLE_BY_DEFAULT(
            "make_ara_visible_by_default"
        )
    }

    fun needsToBeHandled(flag: Flag) =
        prefs.getBoolean(flag.key, true)

    fun setHandled(flag: Flag) {
        prefs.edit { putBoolean(flag.key, false) }
    }

    inline fun handleFlagIfNeeded(flag: Flag, handle: () -> Unit) {
        if (needsToBeHandled(flag)) {
            try {
                handle()
                setHandled(flag)
            } catch (e: RuntimeException) {
                Timber.e(e, "Handling installation flag $flag failed")
            }
        }
    }
}
