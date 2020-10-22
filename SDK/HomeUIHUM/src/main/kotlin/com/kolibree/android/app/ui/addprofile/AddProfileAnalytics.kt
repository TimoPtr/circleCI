/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.tracker.AnalyticsEvent

internal object AddProfileAnalytics {
    fun main() = AnalyticsEvent(name = "AddProfile")

    fun goBack() = main() + "GoBack"

    fun addPhoto() = main() + "AddPhoto"

    fun genderSelected(gender: Gender?) =
        main() + "Gender" + ("gender" to gender?.serializedName)

    fun handednessSelected(handedness: Handedness?) =
        main() + "Handedness" + ("handedness" to handedness?.serializedName)

    private fun checkboxClicked() = main() + "CheckBox"

    fun termsAndConditionsCheckboxClicked(checked: Boolean): AnalyticsEvent =
        checkboxClicked() + "Terms" + ("checked" to checked.toString())

    fun privacyPolicyCheckboxClicked(checked: Boolean): AnalyticsEvent =
        checkboxClicked() + "Policy" + ("checked" to checked.toString())

    fun promotionsAndUpdatesCheckboxClicked(checked: Boolean): AnalyticsEvent =
        checkboxClicked() + "ReceivePromo" + ("checked" to checked.toString())

    fun addProfileButtonClicked() = main() + "Add"
}
