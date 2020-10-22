/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class HeadspaceMindfulMomentViewState(
    val mindfulMoment: HeadspaceMindfulMoment
) : BaseViewState
