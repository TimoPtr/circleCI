/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui.card

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus
import com.kolibree.android.headspace.mindful.ui.shared.SAMPLE_MINDFUL_MOMENT
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class HeadspaceMindfulMomentCardViewStateTest : BaseUnitTest() {
    private lateinit var viewState: HeadspaceMindfulMomentCardViewState

    override fun setup() {
        super.setup()
        viewState = HeadspaceMindfulMomentCardViewState.initial(
            position = DynamicCardPosition.EIGHT
        )
    }

    @Test
    fun `withNotAvailableStatus sets visibility to false`() {
        val nextState = viewState.withNotAvailableStatus(HeadspaceMindfulMomentStatus.NotAvailable)

        assertFalse(nextState.visible)
    }

    @Test
    fun `withAvailableStatus sets visibility to true and updates mindfulMoment`() {
        val nextState = viewState.withAvailableStatus(
            HeadspaceMindfulMomentStatus.Available(
                SAMPLE_MINDFUL_MOMENT
            )
        )

        assertTrue(nextState.visible)
        assertNotNull(nextState.mindfulMoment)
    }
}
