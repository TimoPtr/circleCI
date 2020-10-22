/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.RawRes
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.game.legacy.R

@Keep
interface BrushStartResourceProvider {

    @RawRes
    fun provideToothbrushVideo(model: ToothbrushModel): Int = when (model) {
        ToothbrushModel.ARA -> R.raw.anim_step2_ara
        ToothbrushModel.CONNECT_E1 -> R.raw.anim_step2_e1
        ToothbrushModel.CONNECT_E2 -> R.raw.anim_step2_e2
        ToothbrushModel.CONNECT_M1 -> R.raw.anim_m1
        ToothbrushModel.CONNECT_B1 -> R.raw.anim_step2_b1
        ToothbrushModel.PLAQLESS -> R.raw.anim_step2_pql
        ToothbrushModel.HILINK -> R.raw.anim_step2_e2
        ToothbrushModel.HUM_ELECTRIC -> R.raw.anim_step2_e2
        ToothbrushModel.HUM_BATTERY -> R.raw.anim_step2_b1
        ToothbrushModel.GLINT -> R.raw.anim_step2_e2
    }

    @DrawableRes
    fun provideToothbrushPreviewImage(model: ToothbrushModel): Int = when (model) {
        ToothbrushModel.ARA -> R.drawable.ic_ara_preview
        ToothbrushModel.CONNECT_E1 -> R.drawable.ic_e1_preview
        ToothbrushModel.CONNECT_E2 -> R.drawable.ic_e2_preview
        ToothbrushModel.CONNECT_M1 -> R.drawable.ic_m1_preview
        ToothbrushModel.CONNECT_B1 -> R.drawable.ic_b1_preview
        ToothbrushModel.PLAQLESS -> R.drawable.ic_pql_start_session
        ToothbrushModel.HILINK -> R.drawable.ic_e2_preview
        ToothbrushModel.HUM_ELECTRIC -> R.drawable.ic_e2_preview
        ToothbrushModel.HUM_BATTERY -> R.drawable.ic_b1_preview
        ToothbrushModel.GLINT -> R.drawable.ic_e2_preview
    }
}
