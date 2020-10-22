/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import com.kolibree.android.app.Error

/**
 * Contract to be fulfilled by Host of PairingFlow
 */
internal interface PairingFlowHost {
    /**
     * Show [error] to the user
     */
    fun showError(error: Error)

    fun hideError()

    fun showHostBackNavigation(show: Boolean)

    fun isOnboardingFlow(): Boolean
}
