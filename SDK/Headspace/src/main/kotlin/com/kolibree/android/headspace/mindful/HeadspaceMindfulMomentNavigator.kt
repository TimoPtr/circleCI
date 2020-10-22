/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment

@VisibleForApp
interface HeadspaceMindfulMomentNavigator {
    fun showMindfulMomentScreen(mindfulMoment: HeadspaceMindfulMoment)
}
