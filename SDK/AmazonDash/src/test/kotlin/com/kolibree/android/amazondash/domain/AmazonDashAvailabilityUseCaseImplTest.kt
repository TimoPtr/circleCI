/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.test.utils.TestFeatureToggle
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class AmazonDashAvailabilityUseCaseImplTest : BaseUnitTest() {

    private val feature = TestFeatureToggle(AmazonDashFeature)

    private lateinit var useCase: AmazonDashAvailabilityUseCase

    override fun setup() {
        super.setup()
        useCase = AmazonDashAvailabilityUseCaseImpl(setOf(feature))
    }

    @Test
    fun `return false if feature disabled`() {
        feature.value = false

        val isAvailable = useCase.isAvailable().blockingFirst()
        assertFalse(isAvailable)
    }

    @Test
    fun `return true if feature enabled`() {
        feature.value = true

        val isAvailable = useCase.isAvailable().blockingFirst()
        assertTrue(isAvailable)
    }
}
