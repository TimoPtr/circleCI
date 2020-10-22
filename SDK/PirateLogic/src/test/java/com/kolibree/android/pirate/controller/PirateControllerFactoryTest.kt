/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.controller

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.pirate.controller.kml.World1KmlController
import com.kolibree.android.pirate.controller.kml.World2KmlController
import com.kolibree.android.pirate.controller.kml.World3KmlController
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.kml.PirateHelper
import com.kolibree.kml.SupervisedBrushingAppContext12
import com.kolibree.kml.SupervisedBrushingAppContext16
import com.kolibree.kml.SupervisedBrushingAppContext8
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import javax.inject.Provider
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PirateControllerFactoryTest : BaseUnitTest() {

    private lateinit var pirateControllerFactory: PirateControllerFactory

    private val checkupCalculator = mock<CheckupCalculator>()
    private val supervisedAppContext8Provider = mock<Provider<SupervisedBrushingAppContext8>>()
    private val supervisedAppContext12Provider = mock<Provider<SupervisedBrushingAppContext12>>()
    private val supervisedAppContext16Provider = mock<Provider<SupervisedBrushingAppContext16>>()
    private val pirateHelperProvider = mock<Provider<PirateHelper>>()

    private fun setupMocks() {

        whenever(supervisedAppContext8Provider.get()).thenReturn(mock())
        whenever(supervisedAppContext12Provider.get()).thenReturn(mock())
        whenever(supervisedAppContext16Provider.get()).thenReturn(mock())
        whenever(pirateHelperProvider.get()).thenReturn(mock())

        pirateControllerFactory = PirateControllerFactory(
            checkupCalculator,
            supervisedAppContext8Provider,
            supervisedAppContext12Provider,
            supervisedAppContext16Provider,
            pirateHelperProvider
        )
    }

    @Test
    fun `getWorldController returns null when not valid worldId`() {
        setupMocks()
        assertNull(pirateControllerFactory.getWorldController(true, -1))
    }

    @Test
    fun `getWorldController returns World1KmlController when worldId equals WORLD_1_ID`() {
        setupMocks()
        assertTrue(pirateControllerFactory.getWorldController(true, WORLD_1_ID) is World1KmlController)
    }

    @Test
    fun `getWorldController returns World2KmlController when worldId equals WORLD_2_ID`() {
        setupMocks()
        assertTrue(pirateControllerFactory.getWorldController(true, WORLD_2_ID) is World2KmlController)
    }

    @Test
    fun `getWorldController returns World3KmlController when worldId equals WORLD_3_ID`() {
        setupMocks()
        assertTrue(pirateControllerFactory.getWorldController(true, WORLD_3_ID) is World3KmlController)
    }

    @Test
    fun `getWorldController returns World3KmlController when worldId equals WORLD_4_ID`() {
        setupMocks()
        assertTrue(pirateControllerFactory.getWorldController(true, WORLD_4_ID) is World3KmlController)
    }
}
