/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachPlusSensorConfigurationFactoryTest : BaseUnitTest() {

    /*
    NonPlaqlessSensorConfigurationWithKML
     */
    @Test
    fun `NonPlaqlessSensorConfigurationWithKML returns false for svm`() {
        assertFalse(NonPlaqlessSensorConfigurationWithKML.useSvm)
    }

    @Test
    fun `NonPlaqlessSensorConfigurationWithKML returns true for raw data`() {
        assertTrue(NonPlaqlessSensorConfigurationWithKML.useRawData)
    }

    @Test
    fun `NonPlaqlessSensorConfigurationWithKML returns false for plaqless raw data`() {
        assertFalse(NonPlaqlessSensorConfigurationWithKML.usePlaqless)
    }

    @Test
    fun `NonPlaqlessSensorConfigurationWithKML returns false for overpressure`() {
        assertFalse(NonPlaqlessSensorConfigurationWithKML.useOverpressure)
    }

    /*
    PlaqlessConfiguration
     */
    @Test
    fun `PlaqlessConfiguration returns false for svm`() {
        assertFalse(PlaqlessSensorConfiguration.useSvm)
    }

    @Test
    fun `PlaqlessConfiguration returns false for raw data`() {
        assertFalse(PlaqlessSensorConfiguration.useRawData)
    }

    @Test
    fun `PlaqlessConfiguration returns true for plaqless raw data`() {
        assertTrue(PlaqlessSensorConfiguration.usePlaqless)
    }

    @Test
    fun `PlaqlessConfiguration returns false for overpressure`() {
        assertFalse(PlaqlessSensorConfiguration.useOverpressure)
    }

    /*
    GlintSensorConfiguration
     */
    @Test
    fun `GlintSensorConfiguration returns false for svm`() {
        assertFalse(GlintSensorConfiguration.useSvm)
    }

    @Test
    fun `GlintSensorConfiguration returns true for raw data`() {
        assertTrue(GlintSensorConfiguration.useRawData)
    }

    @Test
    fun `GlintSensorConfiguration returns false for plaqless raw data`() {
        assertFalse(GlintSensorConfiguration.usePlaqless)
    }

    @Test
    fun `GlintSensorConfiguration returns true for overpressure`() {
        assertTrue(GlintSensorConfiguration.useOverpressure)
    }

    /*
    coachPlusSensorConfiguration
     */
    @Test
    fun `sensorConfiguration returns PlaqlessConfiguration for PLAQLESS connection whenever KML is activate`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(ToothbrushModel.PLAQLESS)
            .build()

        assertEquals(
            PlaqlessSensorConfiguration,
            CoachPlusSensorConfigurationFactory.configurationForConnection(connection)
        )

        assertEquals(
            PlaqlessSensorConfiguration,
            CoachPlusSensorConfigurationFactory.configurationForConnection(connection)
        )
    }

    @Test
    fun `sensorConfig returns GlintConfiguration for GLINT connection whenever KML is activate`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(ToothbrushModel.GLINT)
            .build()

        assertEquals(
            GlintSensorConfiguration,
            CoachPlusSensorConfigurationFactory.configurationForConnection(connection)
        )

        assertEquals(
            GlintSensorConfiguration,
            CoachPlusSensorConfigurationFactory.configurationForConnection(connection)
        )
    }

    @Test
    fun `sensorConfiguration returns NonPlaqlessSensorConfigurationWithKML for any other connection when KML enable`() {
        ToothbrushModel.values()
            .filterNot { it == ToothbrushModel.PLAQLESS }
            .filterNot { it == ToothbrushModel.GLINT }
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withModel(model)
                    .build()

                assertEquals(
                    NonPlaqlessSensorConfigurationWithKML,
                    CoachPlusSensorConfigurationFactory.configurationForConnection(connection)
                )
            }
    }
}
