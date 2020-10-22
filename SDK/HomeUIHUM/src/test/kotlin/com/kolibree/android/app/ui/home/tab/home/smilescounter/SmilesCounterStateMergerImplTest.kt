/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Idle
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Invisible
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.NoInternet
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayLanding
import com.kolibree.android.test.utils.randomInt
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import kotlin.random.Random.Default.nextInt
import org.junit.Test

class SmilesCounterStateMergerImplTest : BaseUnitTest() {
    private lateinit var merger: SmilesCounterStateMergerImpl

    private fun init() {
        merger = SmilesCounterStateMergerImpl()
    }

    private fun initWithPlayLandingEmitted() {
        init()

        forcePlayLandingEmission()
    }

    @Test
    fun `merger never returns Error`() {
        init()

        val nbOfPointsList = mutableListOf(0)
        repeat(10) {
            nbOfPointsList.add(randomInt(maxValue = 1000))
        }

        nbOfPointsList.forEach { nbOfPoints ->
            booleans.forEach { isCounterVisible ->
                booleans.forEach { syncPending ->
                    booleans.forEach { hasConnectivity ->
                        assertNotSame(
                            Error,
                            merger.apply(
                                isCounterVisible = isCounterVisible,
                                syncPending = syncPending,
                                hasConnectivity = hasConnectivity,
                                nbOfPoints = nbOfPoints
                            )
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `when hasConnectivity is false, merger always return NoInternet`() {
        val nbOfPoints = 43

        init()

        booleans.forEach { isVisible ->
            booleans.forEach { syncPending ->
                assertEquals(
                    NoInternet,
                    merger.apply(
                        isCounterVisible = isVisible,
                        syncPending = syncPending,
                        nbOfPoints = nbOfPoints,
                        hasConnectivity = false
                    )
                )
            }
        }
    }

    @Test
    fun `when hasConnectivity is true, PlayLanding is always emitted as first value`() {
        val nbOfPoints = 43
        val expectedState = PlayLanding(points = nbOfPoints)

        booleans.forEach { isVisible ->
            booleans.forEach { syncPending ->
                assertEquals(
                    expectedState,
                    SmilesCounterStateMergerImpl()
                        .apply(
                            isCounterVisible = isVisible,
                            syncPending = syncPending,
                            nbOfPoints = nbOfPoints,
                            hasConnectivity = true
                        )
                )
            }
        }
    }

    /*
    counter not visible
     */

    @Test
    fun `when PlayLanding was emitted, counter is not visible and it has never been visible before, return Invisible`() {
        initWithPlayLandingEmitted()

        val invisible = Invisible

        assertEquals(
            invisible,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = false,
                syncPending = true,
                nbOfPoints = 54
            )
        )

        assertEquals(
            invisible,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = false,
                syncPending = false,
                nbOfPoints = 54
            )
        )
    }

    @Test
    fun `when PlayLanding was emitted, counter is not visible but it was visible before, return Invisible`() {
        initWithPlayLandingEmitted()

        merger.apply(
            hasConnectivity = true,
            isCounterVisible = true,
            syncPending = true,
            nbOfPoints = 88
        )

        val expectedState = Invisible
        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = false,
                syncPending = true,
                nbOfPoints = 88
            )
        )

        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = false,
                syncPending = false,
                nbOfPoints = 88
            )
        )
    }

    /*
    counter is visible and PlayLanding has been emitted
     */
    @Test
    fun `when counter is visible and syncPending=true, always return Pending`() {
        initWithPlayLandingEmitted()

        assertEquals(
            Pending,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = true,
                nbOfPoints = randomPoints()
            )
        )

        assertEquals(
            Pending,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = true,
                nbOfPoints = randomPoints()
            )
        )
    }

    @Test
    fun `when counter is visible, syncPending=false and Pending state was emitted before, return PlayIncrease with initialPoints and finalPoints from nbOfPoints`() {
        initWithPlayLandingEmitted()

        val initialPoints = 0
        val expectedPoints = 54

        forcePendingEmission(expectedPoints)

        val expectedState = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = expectedPoints
            )
        )
    }

    @Test
    fun `when counter is visible, syncPending=false, and there were intermediate Invisible states with points higher, return PlayIncrease with initialPoints and finalPoints from nbOfPoints`() {
        initWithPlayLandingEmitted()

        val initialPoints = 0
        val expectedPoints = 54

        forcePendingEmission(3)

        forceInvisibleEmission(points = initialPoints + 10)

        val expectedState = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = expectedPoints
            )
        )
    }

    @Test
    fun `when counter is invisible with initialPoints to 0, it should return PlayIncrease with initialPoints = 0 and expectedPoints = 10`() {
        initWithPlayLandingEmitted()

        val initialPoints = 0

        forceInvisibleEmission(points = initialPoints + 10)

        val expectedPoints = 10
        val expectedState = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = expectedPoints
            )
        )
    }

    @Test
    fun `when first state is PlayLanding with point to 1, next state with more points should return PlayIncrease`() {

        val initialPoints = 1

        init()
        forcePlayLandingEmission(points = initialPoints)

        val expectedPoints = 3
        val expectedState = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        val newState = merger.apply(
            hasConnectivity = true,
            isCounterVisible = true,
            syncPending = false,
            nbOfPoints = expectedPoints
        )

        assertEquals(expectedState, newState)
    }

    @Test
    fun `when Invisible state with a point rise is emitted and followed by a Pending state, PlayIncrease should be the next state being emitted`() {

        val initialPoints = 0
        val expectedPoints = 3

        initWithPlayLandingEmitted()
        forceInvisibleEmission(expectedPoints)
        forcePendingEmission(expectedPoints)

        val expectedState = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        val newState = merger.apply(
            hasConnectivity = true,
            isCounterVisible = true,
            syncPending = false,
            nbOfPoints = expectedPoints
        )

        assertEquals(expectedState, newState)
    }

    @Test
    fun `when counter is idle with initialPoints to 0, the second merge should return PlayIncrease with initialPoints = 0 and expectedPoints = 10`() {
        initWithPlayLandingEmitted()

        val initialPoints = 0
        forcePendingEmission(points = initialPoints)

        val expectedState = Idle(points = initialPoints)

        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = initialPoints
            )
        )

        val expectedPoints = 10
        val expectedStatePlayIncrease = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        assertEquals(
            expectedStatePlayIncrease,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = expectedPoints
            )
        )
    }

    @Test
    fun `when counter is visible, syncPending=false and Pending has been emitted with same number of points, return PlayIncrease with nbOfPoints`() {
        initWithPlayLandingEmitted()

        val initialPoints = 0
        val expectedPoints = 3
        forcePendingEmission(points = expectedPoints)

        val expectedState = PlayIncrease(
            initialPoints = initialPoints,
            finalPoints = expectedPoints
        )

        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = expectedPoints
            )
        )
    }

    /*
    Simulate login

 isCounterVisible: false, syncPending: true, nbOfPoints: 0
 isCounterVisible: true, syncPending: true, nbOfPoints: 0
 isCounterVisible: true, syncPending: false, nbOfPoints: 0
 isCounterVisible: true, syncPending: false, nbOfPoints: 1

14:46:51.447 isCounterVisible: false, syncPending: true, nbOfPoints: 0
14:46:51.814 isCounterVisible: true, syncPending: true, nbOfPoints: 0
14:46:52.992 isCounterVisible: true, syncPending: false, nbOfPoints: 0
     */

    @Test
    fun `simulate login`() {
        init()

        assertEquals(
            PlayLanding(points = 0),
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = false,
                syncPending = true,
                nbOfPoints = 0
            )
        )
        assertEquals(
            Pending,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = true,
                nbOfPoints = 0
            )
        )

        assertEquals(
            PlayIncrease(0, 1521),
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = false,
                nbOfPoints = 1521
            )
        )
    }

    /*
    Utils
     */

    private fun forcePendingEmission(points: Int) {
        val expectedPendingState = Pending
        assertEquals(
            expectedPendingState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = true,
                nbOfPoints = points
            )
        )
    }

    private fun forcePlayLandingEmission(syncPending: Boolean = false, points: Int = 0) {
        val expectedState = PlayLanding(points = points)
        assertEquals(
            expectedState,
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = true,
                syncPending = syncPending,
                nbOfPoints = points
            )
        )
    }

    private fun forceInvisibleEmission(points: Int) {
        assertTrue(
            merger.apply(
                hasConnectivity = true,
                isCounterVisible = false,
                syncPending = false,
                nbOfPoints = points
            ) is Invisible
        )
    }

    private fun randomPoints() = nextInt(from = 0, until = 1000)
}

private val booleans = listOf(true, false)
