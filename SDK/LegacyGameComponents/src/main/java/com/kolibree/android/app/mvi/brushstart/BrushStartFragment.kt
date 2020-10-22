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
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.game.legacy.R
import com.kolibree.game.legacy.databinding.FragmentBrushStartBinding

@Keep
abstract class BrushStartFragment : BaseBrushStartFragment<
    BrushStartViewAction,
    BrushStartViewModel.Factory,
    BrushStartViewModel,
    FragmentBrushStartBinding>() {

    override fun getViewModelClass() = BrushStartViewModel::class.java

    override fun getLayoutId() = R.layout.fragment_brush_start

    override fun execute(action: BrushStartViewAction) {
        when (action) {
            is BrushStarted -> onBrushStarted(action.model, action.mac)
        }
    }

    abstract fun onBrushStarted(
        model: ToothbrushModel,
        mac: String
    )
}
