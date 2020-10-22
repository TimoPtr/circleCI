/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.mvi

import androidx.databinding.ViewDataBinding
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.auditor.UserStep
import com.kolibree.android.sba.testbrushing.base.ViewAction

internal abstract class BaseTestBrushingFragment<
    VS : BaseTestBrushingViewState,
    VM : BaseTestBrushingViewModel<VS>,
    VMF : BaseViewModel.Factory<VS>,
    T : ViewDataBinding
    > : BaseMVIFragment<VS, ViewAction, VMF, VM, T>(), UserStep
