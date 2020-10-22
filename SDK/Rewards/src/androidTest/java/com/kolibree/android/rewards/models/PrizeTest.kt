/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.clock.TrustedClock
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class PrizeTest {

    @Test
    fun prizeEntityHashCodeMethod() {
        val prize = PrizeEntity(
            1L,
            "category",
            "description",
            "title",
            TrustedClock.getNowLocalDate(),
            0,
            false,
            0.0,
            "kolibree",
            "http://google.com",
            0
        )

        prize.hashCode()
    }
}
