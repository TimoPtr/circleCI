/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.clock.TrustedClock
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

@Keep
fun getAgeFromBirthDate(birthDate: LocalDate) =
    ChronoUnit
        .YEARS
        .between(birthDate, TrustedClock.getNowZonedDateTime())
        .toInt()
        .let {
            if (it < 0) {
                Timber.e("Birth date in the future, falling back to default age")
                Profile.DEFAULT_AGE
            } else {
                it
            }
        }
