/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import androidx.lifecycle.MutableLiveData
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.OngoingStubbing

internal const val DEFAULT_COUNTRY = "France"
internal const val DEFAULT_BETA_ACCOUNT_STATE = true

internal fun initialSharedState() =
    OnboardingSharedViewState.initial(DEFAULT_COUNTRY, DEFAULT_BETA_ACCOUNT_STATE)

internal fun <T> OngoingStubbing<T>.thenUpdateSharedStateWith(
    sharedStateLiveData: MutableLiveData<OnboardingSharedViewState>,
    updateBlock: (OnboardingSharedViewState, InvocationOnMock) -> OnboardingSharedViewState
) = then { invocation ->
    sharedStateLiveData.value = updateBlock(
        sharedStateLiveData.value ?: initialSharedState(),
        invocation
    )
    Unit
}
