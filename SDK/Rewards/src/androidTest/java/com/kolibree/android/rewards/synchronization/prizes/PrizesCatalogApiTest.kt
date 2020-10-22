/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.prizes

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.clock.TrustedClock
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrizesCatalogApiTest {

    @Test
    fun prizesCatalogAPIHashCodeMethod() {
        val catalog = PrizesCatalogApi(
            listOf(
                PrizeApi(
                    0L, "category", listOf(
                        PrizeDetailsApi(
                            0,
                            false,
                            0.0,
                            "description",
                            "title",
                            "company",
                            "http://google.com",
                            TrustedClock.getNowLocalDate(),
                            0L,
                            0
                        )
                    )
                )
            )
        )

        catalog.hashCode()
    }
}
