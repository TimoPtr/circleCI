
/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.commons.ToothbrushModel
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BrushStartViewState(
    val model: ToothbrushModel,
    val packageName: String,
    val mac: String
) : BaseViewState {

    @IgnoredOnParcel
    val isManualToothbrush: Boolean = model.isManual

    @IgnoredOnParcel
    val isStaticPreview: Boolean = model.isPlaqless
}
