/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.curve

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class CurveViewState(
    val curveSettings: BrushingModeCurveSettings
) : BaseViewState {

    companion object {

        fun initial() = CurveViewState(
            curveSettings = BrushingModeCurveSettings.default()
        )
    }
}
