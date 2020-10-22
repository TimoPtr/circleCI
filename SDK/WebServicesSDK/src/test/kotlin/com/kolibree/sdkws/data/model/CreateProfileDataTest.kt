/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.data.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CreateProfileData] tests */
class CreateProfileDataTest : BaseUnitTest() {

    /*
    setGender
     */

    @Test
    fun `setGender uses Gender's serializedName`() {
        val profileData = CreateProfileData()

        for (expectedGender in Gender.values()) {
            profileData.setGender(expectedGender)
            assertEquals(expectedGender.serializedName, profileData.gender)
        }
    }

    /*
    setHandedness
     */

    @Test
    fun `setHandedness uses Handedness's serializedName`() {
        val profileData = CreateProfileData()

        for (expectedHandedness in Handedness.values()) {
            profileData.setHandedness(expectedHandedness)
            assertEquals(expectedHandedness.serializedName, profileData.handedness)
        }
    }
}
