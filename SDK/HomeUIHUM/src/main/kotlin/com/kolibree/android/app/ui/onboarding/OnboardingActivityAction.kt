/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error

@VisibleForApp
internal sealed class OnboardingActivityAction : OnboardingActions {

    object ShowUnderConstructionMessage : OnboardingActivityAction()

    object OpenHomeScreen : OnboardingActivityAction()

    object RestartLoginFlow : OnboardingActivityAction()

    data class ShowError(val error: Error) : OnboardingActivityAction()
}
