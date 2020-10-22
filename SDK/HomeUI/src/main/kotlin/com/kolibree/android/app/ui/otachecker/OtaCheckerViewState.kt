/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.otachecker

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
data class OtaCheckerViewState(
    val showMandatoryUpdateNeeded: Boolean = false,
    val showEnableInternet: Boolean = false
) {
    @VisibleForApp
    companion object {
        @JvmField
        val EMPTY = OtaCheckerViewState()
    }

    fun withMandatoryUpdateNeeded(mandatoryUpdateNeeded: Boolean): OtaCheckerViewState =
        copy(showMandatoryUpdateNeeded = mandatoryUpdateNeeded)

    fun withShowEnableInternet(enableInternet: Boolean): OtaCheckerViewState =
        copy(showEnableInternet = enableInternet)
}
