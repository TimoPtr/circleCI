/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import org.junit.Assert.assertEquals
import org.junit.Test

/** [AccountUtils] tests */
class AccountUtilsKtTest : BaseUnitTest() {

    @Test
    fun `getAgeFromBirthDate with birth date in the future returns Profile DEFAULT_AGE`() {
        assertEquals(
            Profile.DEFAULT_AGE,
            getAgeFromBirthDate(TrustedClock.getNowLocalDate().plusYears(2))
        )
    }

    @Test
    fun `getAgeFromBirthDate with birth date in the past returns difference in years`() {
        assertEquals(
            2,
            getAgeFromBirthDate(TrustedClock.getNowLocalDate().minusYears(2).minusDays(1))
        )
    }
}
