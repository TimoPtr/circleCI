/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.mvi

import androidx.annotation.Keep
import androidx.lifecycle.DefaultLifecycleObserver
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.sba.testbrushing.base.ViewAction

@Keep
abstract class BaseTestBrushingViewModel<VS : BaseTestBrushingViewState>(viewState: VS) :
    BaseViewModel<VS, ViewAction>(viewState), DefaultLifecycleObserver
