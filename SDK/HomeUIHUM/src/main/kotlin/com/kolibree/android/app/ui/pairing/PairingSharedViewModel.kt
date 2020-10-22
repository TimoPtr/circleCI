/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import androidx.lifecycle.LiveData
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
interface PairingSharedViewModel {

    val pairingViewStateLiveData: LiveData<PairingViewState>

    fun getPairingViewState(): PairingViewState?

    fun showProgress(show: Boolean)

    fun resetState()

    fun onPairingFlowSuccess()
}
