/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slide2

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SlideTwoViewState(
    val isDescriptionSelected1: Boolean = false,
    val isDescriptionSelected2: Boolean = false,
    val isDescriptionSelected3: Boolean = false
) : BaseViewState {

    companion object {
        fun initial() = SlideTwoViewState(true)
    }

    private fun noDescriptionSelected() = copy(
        isDescriptionSelected1 = false,
        isDescriptionSelected2 = false,
        isDescriptionSelected3 = false
    )

    fun onlyDescription1() = noDescriptionSelected().copy(isDescriptionSelected1 = true)

    fun onlyDescription2() = noDescriptionSelected().copy(isDescriptionSelected2 = true)

    fun onlyDescription3() = noDescriptionSelected().copy(isDescriptionSelected3 = true)
}
