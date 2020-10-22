/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.signup

import com.kolibree.android.app.ui.onboarding.OnboardingActions

internal sealed class SignUpActions : OnboardingActions {

    object HideSoftInput : SignUpActions()

    object OpenTermsAndConditions : SignUpActions()

    object OpenPrivacyPolicy : SignUpActions()

    object OpenEnterEmail : SignUpActions()
}
