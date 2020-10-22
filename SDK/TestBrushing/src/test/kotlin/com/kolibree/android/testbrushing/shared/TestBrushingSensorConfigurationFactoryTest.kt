/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.shared

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestBrushingSensorConfigurationFactoryTest : BaseUnitTest() {

    @Test
    fun `factory returns PLAQLESS config for PLAQLESS regardless of KML`() {
        val connection = mockConnectionFor(ToothbrushModel.PLAQLESS)
        val configWithKml =
            TestBrushingSensorConfigurationFactory.configurationForConnection(connection)
        val configWithoutKml =
            TestBrushingSensorConfigurationFactory.configurationForConnection(connection)
        assertEquals(configWithKml, configWithoutKml)
        assertEquals(PlaqlessSensorConfiguration, configWithKml)
        assertEquals(PlaqlessSensorConfiguration, configWithoutKml)
    }

    @Test
    fun `factory returns non-PLAQLESS and non-glint config for other types regardless of KML`() {
        ToothbrushModel.values()
            .filter { it != ToothbrushModel.PLAQLESS && it != ToothbrushModel.GLINT }
            .forEach { type ->
                val connection = mockConnectionFor(type)
                val configWithKml =
                    TestBrushingSensorConfigurationFactory.configurationForConnection(
                        connection
                    )
                val configWithoutKml =
                    TestBrushingSensorConfigurationFactory.configurationForConnection(
                        connection
                    )
                assertEquals(configWithKml, configWithoutKml)
                assertEquals(NonPlaqlessSensorConfiguration, configWithKml)
                assertEquals(NonPlaqlessSensorConfiguration, configWithoutKml)
            }
    }

    @Test
    fun `factory returns Glint config for Glint regardless of KML`() {
        val connection = mockConnectionFor(ToothbrushModel.GLINT)
        val configWithKml =
            TestBrushingSensorConfigurationFactory.configurationForConnection(connection)
        val configWithoutKml =
            TestBrushingSensorConfigurationFactory.configurationForConnection(connection)
        assertEquals(configWithKml, configWithoutKml)
        assertEquals(GlintSensorConfiguration, configWithKml)
        assertEquals(GlintSensorConfiguration, configWithoutKml)
    }

    @Test
    fun `PlaqlessSensorConfiguration is monitored and listens only to plaqless data`() {
        assertTrue(PlaqlessSensorConfiguration.isMonitoredBrushing)
        assertTrue(PlaqlessSensorConfiguration.usePlaqless)
        assertFalse(PlaqlessSensorConfiguration.useSvm)
        assertFalse(PlaqlessSensorConfiguration.useRawData)
    }

    @Test
    fun `NonPlaqlessSensorConfiguration is monitored and listens only to raw data`() {
        assertTrue(NonPlaqlessSensorConfiguration.isMonitoredBrushing)
        assertTrue(NonPlaqlessSensorConfiguration.useRawData)
        assertFalse(NonPlaqlessSensorConfiguration.useSvm)
        assertFalse(NonPlaqlessSensorConfiguration.usePlaqless)
    }

    @Test
    fun `GlintSensorConfiguration is monitored and listens only to raw data and overpressure`() {
        assertTrue(GlintSensorConfiguration.isMonitoredBrushing)
        assertTrue(GlintSensorConfiguration.useRawData)
        assertFalse(GlintSensorConfiguration.useSvm)
        assertFalse(GlintSensorConfiguration.usePlaqless)
    }

    private fun mockConnectionFor(model: ToothbrushModel): KLTBConnection {
        val connection: KLTBConnection = mock()
        val toothbrush: Toothbrush = mock()
        doReturn(toothbrush).whenever(connection).toothbrush()
        doReturn(model).whenever(toothbrush).model
        return connection
    }
}
