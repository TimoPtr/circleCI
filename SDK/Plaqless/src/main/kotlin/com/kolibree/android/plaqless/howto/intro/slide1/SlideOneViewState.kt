/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide1

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SlideOneViewState(
    val isInfoSelected1: Boolean = false,
    val isInfoSelected2: Boolean = false,
    val isInfoSelected3: Boolean = false
) : BaseViewState {

    private fun noInfoSelected() = copy(
        isInfoSelected1 = false,
        isInfoSelected2 = false,
        isInfoSelected3 = false
    )

    fun onlyInfo1() = noInfoSelected().copy(isInfoSelected1 = true)

    fun onlyInfo2() = noInfoSelected().copy(isInfoSelected2 = true)

    fun onlyInfo3() = noInfoSelected().copy(isInfoSelected3 = true)
}
