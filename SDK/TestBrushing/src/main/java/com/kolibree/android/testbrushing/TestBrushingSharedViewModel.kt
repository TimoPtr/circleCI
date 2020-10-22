/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import androidx.lifecycle.LiveData
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error

@VisibleForApp
interface TestBrushingSharedViewModel {

    val sharedViewStateLiveData: LiveData<TestBrushingViewState>

    fun getSharedViewState(): TestBrushingViewState?

    fun showProgress(show: Boolean)

    fun showError(error: Error)

    fun resetState()
}
