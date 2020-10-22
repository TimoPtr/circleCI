/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.domain

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
sealed class HeadspaceMindfulMomentStatus {

    @VisibleForApp
    data class Available(val headspaceMindfulMoment: HeadspaceMindfulMoment) :
        HeadspaceMindfulMomentStatus()

    @VisibleForApp
    object NotAvailable : HeadspaceMindfulMomentStatus()
}
