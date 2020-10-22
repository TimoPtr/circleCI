/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.SmilesCounterAnimation.NONE
import com.kolibree.android.app.ui.home.tab.home.SmilesCounterAnimation.PENDING
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Idle
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Invisible
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.NoInternet
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayLanding
import com.kolibree.android.app.ui.home.tab.home.smilescounter.smilesBackgroundIncreaseAnimation
import com.kolibree.android.app.ui.home.tab.home.smilescounter.smilesBackgroundLaunchAnimation
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HomeViewStateTest : BaseUnitTest() {
    @Test
    fun `initialValue with default params is initialized with zero points, pointsLoaded=false, pulsingDotVisible=false and state=NoValue`() {
        val initialViewState = HomeViewState.initial()

        assertInitialState(initialViewState)
    }

    /*
    withSmilesCounterState
     */
    @Test
    fun `withSmilesCounterState stores smilesCounterState and sets smilesCounterAnimation=None when newState=Error`() {
        val viewState = HomeViewState.initial().copy(
            currentPoints = 33,
            pointsLoaded = true,
            smilesCounterAnimation = PENDING,
            smilesCounterState = Invisible
        )

        assertEquals(
            viewState.copy(
                smilesCounterState = Error,
                smilesCounterAnimation = NONE
            ),
            viewState.withSmilesCounterState(Error)
        )
    }

    @Test
    fun `withSmilesCounterState stores smilesCounterState and sets smilesCounterAnimation=None when newState=Invisible`() {
        val viewState = HomeViewState.initial().copy(
            currentPoints = 33,
            pointsLoaded = true,
            smilesCounterAnimation = PENDING,
            smilesCounterState = Idle(33)
        )

        assertEquals(
            viewState.copy(
                smilesCounterState = Invisible,
                smilesCounterAnimation = NONE
            ),
            viewState.withSmilesCounterState(Invisible)
        )
    }

    @Test
    fun `withSmilesCounterState stores points, smilesCounterState and sets pointsLoaded=true when newState=PlayLanding`() {
        val expectedPoints = 343
        val expectedState = PlayLanding(points = expectedPoints)

        val viewState = HomeViewState.initial().withSmilesCounterState(expectedState)

        assertEquals(expectedPoints, viewState.currentPoints)
        assertTrue(viewState.pointsLoaded)
        assertFalse(viewState.pulsingDotVisible)
        assertEquals(expectedState, viewState.smilesCounterState)
        assertFalse(viewState.restartAnimation)
        assertFalse(viewState.pendingAnimation)
        assertEquals(smilesBackgroundLaunchAnimation, viewState.smilesBackgroundAnimation)
    }

    @Test
    fun `withSmilesCounterState stores points, smilesCounterState and doesn't touch pointsLoaded when newState=Idle`() {
        val expectedPoints = 343
        val expectedState = Idle(points = expectedPoints)

        val viewState = HomeViewState.initial().withSmilesCounterState(expectedState)

        assertEquals(expectedPoints, viewState.currentPoints)
        assertFalse(viewState.pointsLoaded)
        assertFalse(viewState.pulsingDotVisible)
        assertEquals(expectedState, viewState.smilesCounterState)
        assertFalse(viewState.restartAnimation)
        assertFalse(viewState.pendingAnimation)
    }

    @Test
    fun `withSmilesCounterState stores points, smilesCounterState and doesn't touch pointsLoaded when newState=Pending`() {
        val expectedState = Pending

        val viewState = HomeViewState.initial().withSmilesCounterState(expectedState)

        assertEquals(0, viewState.currentPoints)
        assertFalse(viewState.pointsLoaded)
        assertFalse(viewState.pulsingDotVisible)
        assertEquals(expectedState, viewState.smilesCounterState)
        assertFalse(viewState.restartAnimation)
        assertTrue(viewState.pendingAnimation)
    }

    @Test
    fun `withSmilesCounterState stores smilesCounterState and doesn't touch points or pointsLoaded when newState=NoInternet`() {
        val expectedState = NoInternet

        val viewState = HomeViewState.initial().withSmilesCounterState(expectedState)

        assertEquals(0, viewState.currentPoints)
        assertFalse(viewState.pointsLoaded)
        assertFalse(viewState.pulsingDotVisible)
        assertEquals(expectedState, viewState.smilesCounterState)
        assertFalse(viewState.restartAnimation)
        assertFalse(viewState.pendingAnimation)
    }

    @Test
    fun `withSmilesCounterState stores points, smilesCounterState and doesn't touch pointsLoaded when newState=PlayIncrease`() {
        val expectedPoints = 343
        val expectedState = PlayIncrease(initialPoints = 1, finalPoints = expectedPoints)

        val viewState = HomeViewState.initial().withSmilesCounterState(expectedState)

        assertEquals(expectedPoints, viewState.currentPoints)
        assertFalse(viewState.pointsLoaded)
        assertFalse(viewState.pulsingDotVisible)
        assertEquals(expectedState, viewState.smilesCounterState)
        assertTrue(viewState.restartAnimation)
        assertFalse(viewState.pendingAnimation)
        assertEquals(smilesBackgroundIncreaseAnimation, viewState.smilesBackgroundAnimation)
    }

    /*
    Utils
     */

    private fun assertInitialState(initialViewState: HomeViewState) {
        assertEquals(0, initialViewState.currentPoints)
        assertFalse(initialViewState.pointsLoaded)
        assertFalse(initialViewState.pulsingDotVisible)
        assertEquals(Idle(0), initialViewState.smilesCounterState)
        assertFalse(initialViewState.restartAnimation)
        assertFalse(initialViewState.pendingAnimation)
        assertNull(initialViewState.smilesBackgroundAnimation)
    }
}
