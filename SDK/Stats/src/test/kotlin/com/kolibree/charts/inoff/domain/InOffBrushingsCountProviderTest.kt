/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Test

internal class InOffBrushingsCountProviderTest : BaseUnitTest() {

    private val dao: InOffBrushingsCountDao = mock()

    private lateinit var provider: InOffBrushingsCountProviderImpl

    override fun setup() {
        super.setup()

        provider = InOffBrushingsCountProviderImpl(dao)
    }

    @Test
    fun `brushingsCountStream returns mapped value from DAO`() {
        val profileId = 1010L
        val entity = InOffBrushingsCountEntity(profileId, 10, 10)

        whenever(dao.getByProfileStream(profileId)).thenReturn(
            Flowable.just(
                entity
            )
        )

        provider.brushingsCountStream(profileId).test().assertValue(entity.toInOffBrushingsCount())
    }
}
