/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.zone

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.ZoneData
import com.kolibree.android.app.ui.widget.ZoneProgressData
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ZoneProgressDataTest : BaseUnitTest() {

    @Test
    fun `create creates exact number of zones`() {
        val data = ZoneProgressData.create(4)
        assertEquals(4, data.zones.size)
        assertEquals(ZoneData(false, 0f), data.zones[0])
        assertEquals(ZoneData(false, 0f), data.zones[1])
        assertEquals(ZoneData(false, 0f), data.zones[2])
        assertEquals(ZoneData(false, 0f), data.zones[3])
    }

    @Test
    fun `brushingFinished sets all zones as not ongoing`() {
        val data = ZoneProgressData(
            zones = listOf(
                ZoneData(false, 0.4f),
                ZoneData(true, 0.7f),
                ZoneData(false, 0f)
            )
        )

        val newData = data.brushingFinished()

        assertEquals(ZoneData(false, 0.4f), newData.zones[0])
        assertEquals(ZoneData(false, 0.7f), newData.zones[1])
        assertEquals(ZoneData(false, 0f), newData.zones[2])
    }

    @Test
    fun `updatesProgressOnZone updates progress`() {
        val data = ZoneProgressData(
            zones = listOf(
                ZoneData(false, 0.3f),
                ZoneData(true, 0.1f),
                ZoneData(false, 0f)
            )
        )

        val newData = data.updateProgressOnZone(1, 0.5f)

        assertEquals(ZoneData(false, 0.3f), newData.zones[0])
        assertEquals(ZoneData(true, 0.5f), newData.zones[1])
        assertEquals(ZoneData(false, 0f), newData.zones[2])
    }

    @Test
    fun `updatesProgressOnZone sets updated zone as ongoing`() {
        val data = ZoneProgressData(
            zones = listOf(
                ZoneData(false, 0.7f),
                ZoneData(true, 0.8f),
                ZoneData(false, 0f)
            )
        )

        val newData = data.updateProgressOnZone(2, 0.9f)

        assertEquals(ZoneData(false, 0.7f), newData.zones[0])
        assertEquals(ZoneData(false, 0.8f), newData.zones[1])
        assertEquals(ZoneData(true, 0.9f), newData.zones[2])
    }
}
