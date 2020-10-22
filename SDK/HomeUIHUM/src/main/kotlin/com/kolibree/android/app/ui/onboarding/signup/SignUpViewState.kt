/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.signup

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SignUpViewState(
    val termsAndConditionsAccepted: Boolean = false,
    val privacyPolicyAccepted: Boolean = false
) : BaseViewState {

    fun bothConsentsNotAccepted(): Boolean = !termsAndConditionsAccepted && !privacyPolicyAccepted

    fun bothConsentsAccepted(): Boolean = termsAndConditionsAccepted && privacyPolicyAccepted

    companion object {

        fun initial() = SignUpViewState()
    }
}
