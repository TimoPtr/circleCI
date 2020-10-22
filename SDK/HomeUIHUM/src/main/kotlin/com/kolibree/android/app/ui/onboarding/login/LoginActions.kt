/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.login

import com.kolibree.android.app.ui.onboarding.OnboardingActions

internal sealed class LoginActions : OnboardingActions {

    object OpenCheckYourEmail : LoginActions()
}
