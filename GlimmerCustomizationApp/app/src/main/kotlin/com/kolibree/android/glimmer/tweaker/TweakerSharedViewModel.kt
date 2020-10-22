/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker

import androidx.lifecycle.LiveData
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker

internal interface TweakerSharedViewModel {

    val modeTweaker: BrushingModeTweaker

    val connection: KLTBConnection

    val sharedViewStateLiveData: LiveData<TweakerViewState>

    fun getSharedViewState(): TweakerViewState?

    fun showProgress(show: Boolean)

    fun showError(error: Throwable)

    fun resetState()
}
