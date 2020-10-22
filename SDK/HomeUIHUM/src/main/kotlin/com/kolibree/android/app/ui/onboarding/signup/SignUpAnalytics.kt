/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.signup

import com.kolibree.android.tracker.AnalyticsEvent

internal object SignUpAnalytics {

    fun main() = AnalyticsEvent(name = "Onboarding")

    private fun checkboxClicked() = main() + "CheckBox"

    fun termsAndConditionsCheckboxClicked(checked: Boolean): AnalyticsEvent =
        checkboxClicked() + "Terms" + ("checked" to checked.toString())

    fun privacyPolicyCheckboxClicked(checked: Boolean): AnalyticsEvent =
        checkboxClicked() + "Policy" + ("checked" to checked.toString())

    fun promotionsAndUpdatesCheckboxClicked(checked: Boolean): AnalyticsEvent =
        checkboxClicked() + "ReceivePromo" + ("checked" to checked.toString())

    fun googleButtonClicked() = main() + "SignUpGoogle"

    fun emailButtonClicked() = main() + "SignUpEmail"
}
