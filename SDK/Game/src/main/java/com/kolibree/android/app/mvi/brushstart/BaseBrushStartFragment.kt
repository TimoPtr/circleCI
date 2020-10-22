/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.annotation.Keep
import androidx.databinding.ViewDataBinding
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.auditor.UserStep

@Keep
abstract class BaseBrushStartFragment<
    A : BaseAction,
    VMF : BaseViewModel.Factory<BrushStartViewState>,
    VM : BaseBrushStartViewModel<A>,
    B : ViewDataBinding> : BaseMVIFragment<BrushStartViewState, A, VMF, VM, B>(), UserStep
