/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.homeui.hum.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileEnumMapperKtTest {

    @Test
    fun `getResourceId for Gender returns the correct resource Id for each Gender`() {
        assertEquals(Gender.MALE.getResourceId(), R.string.settings_profile_gender_male)
        assertEquals(Gender.FEMALE.getResourceId(), R.string.settings_profile_gender_female)
        assertEquals(Gender.PREFER_NOT_TO_ANSWER.getResourceId(), R.string.gender_prefer_not_to_answer)
        assertEquals(Gender.UNKNOWN.getResourceId(), R.string.gender_unknown)
    }

    @Test
    fun `getResourceId for Handedness returns the correct resource Id for each Handedness`() {
        assertEquals(
            Handedness.LEFT_HANDED.getResourceId(),
            R.string.settings_profile_handedness_left
        )
        assertEquals(
            Handedness.RIGHT_HANDED.getResourceId(),
            R.string.settings_profile_handedness_right
        )
        assertEquals(
            Handedness.UNKNOWN.getResourceId(),
            R.string.handedness_unknown
        )
    }
}
