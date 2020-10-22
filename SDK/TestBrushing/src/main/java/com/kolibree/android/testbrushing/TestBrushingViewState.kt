/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.commons.ToothbrushModel
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class TestBrushingViewState(
    val progressVisible: Boolean = false,
    private val model: ToothbrushModel,
    private val mac: String
) : BaseViewState {

    fun toothbrushModel() = model

    fun toothbrushMac() = mac

    @VisibleForApp
    companion object {
        fun initial(
            model: ToothbrushModel,
            mac: String
        ) = TestBrushingViewState(model = model, mac = mac)
    }
}
