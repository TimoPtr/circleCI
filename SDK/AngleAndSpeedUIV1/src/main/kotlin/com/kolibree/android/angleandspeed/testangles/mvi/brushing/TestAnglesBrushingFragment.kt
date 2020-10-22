/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import androidx.databinding.ViewDataBinding
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.game.mvi.BaseGameFragment
import com.kolibree.android.tracker.NonTrackableScreen

internal abstract class TestAnglesBrushingFragment<
    VMF : BaseViewModel.Factory<TestAnglesBrushingViewState>,
    VM : TestAnglesBrushingViewModel,
    B : ViewDataBinding> :
    BaseGameFragment<TestAnglesBrushingViewState, VMF, VM, B>(),
    NonTrackableScreen
