/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.lifecycle

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.game.lifecycle.GameLifecycle.Background
import com.kolibree.android.game.lifecycle.GameLifecycle.Finished
import com.kolibree.android.game.lifecycle.GameLifecycle.Foreground
import com.kolibree.android.game.lifecycle.GameLifecycle.Idle
import com.kolibree.android.game.lifecycle.GameLifecycle.Paused
import com.kolibree.android.game.lifecycle.GameLifecycle.Restarted
import com.kolibree.android.game.lifecycle.GameLifecycle.Resumed
import com.kolibree.android.game.lifecycle.GameLifecycle.Started
import com.kolibree.android.game.lifecycle.GameLifecycle.Terminated
import org.junit.Test

internal class GameLifecycleTest : BaseUnitTest() {
    /*
    Idle
     */

    @Test(expected = AssertionError::class)
    fun `Idle fails on transition to Idle`() {
        Idle.validateTransition(Idle)
    }

    @Test
    fun `Idle can transition to Started`() {
        Idle.validateTransition(Started)
    }

    @Test
    fun `Idle can transition to Terminated`() {
        Idle.validateTransition(Terminated)
    }

    @Test
    fun `Idle can transition to Background`() {
        Idle.validateTransition(Background)
    }

    @Test(expected = AssertionError::class)
    fun `Idle fails on transition to Paused`() {
        Idle.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Idle fails on transition to Resumed`() {
        Idle.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Idle fails on transition to Restarted`() {
        Idle.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Idle fails on transition to Finished`() {
        Idle.validateTransition(Finished)
    }

    @Test(expected = AssertionError::class)
    fun `Idle fails on transition to Foreground`() {
        Idle.validateTransition(Foreground)
    }

    /*
    Started
     */

    @Test(expected = AssertionError::class)
    fun `Started fails on transition to Idle`() {
        Started.validateTransition(Idle)
    }

    @Test(expected = AssertionError::class)
    fun `Started fails on transition to Started`() {
        Started.validateTransition(Started)
    }

    @Test
    fun `Started can transition to Paused`() {
        Started.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Started fails on transition to Resumed`() {
        Started.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Started fails on transition to Restarted`() {
        Started.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Started fails on transition to Background`() {
        Started.validateTransition(Background)
    }

    @Test(expected = AssertionError::class)
    fun `Started fails on transition to Foreground`() {
        Started.validateTransition(Foreground)
    }

    @Test
    fun `Started can transition to Finished`() {
        Started.validateTransition(Finished)
    }

    @Test
    fun `Started can transition to Terminated`() {
        Started.validateTransition(Terminated)
    }

    /*
    Paused
     */

    @Test(expected = AssertionError::class)
    fun `Paused fails on transition to Idle`() {
        Paused.validateTransition(Idle)
    }

    @Test(expected = AssertionError::class)
    fun `Paused fails on transition to Started`() {
        Paused.validateTransition(Started)
    }

    @Test(expected = AssertionError::class)
    fun `Paused fails on transition to Foreground`() {
        Paused.validateTransition(Foreground)
    }

    @Test(expected = AssertionError::class)
    fun `Paused fails on transition to Paused`() {
        Paused.validateTransition(Paused)
    }

    @Test
    fun `Paused can transition to Resumed`() {
        Paused.validateTransition(Resumed)
    }

    @Test
    fun `Paused can transition to Restarted`() {
        Paused.validateTransition(Restarted)
    }

    @Test
    fun `Paused can transition to Finished`() {
        Paused.validateTransition(Finished)
    }

    @Test
    fun `Paused can transition to Terminated`() {
        Paused.validateTransition(Terminated)
    }

    @Test
    fun `Paused can transition to Background`() {
        Paused.validateTransition(Background)
    }

    /*
    Resumed
     */

    @Test(expected = AssertionError::class)
    fun `Resumed fails on transition to Idle`() {
        Resumed.validateTransition(Idle)
    }

    @Test(expected = AssertionError::class)
    fun `Resumed fails on transition to Started`() {
        Resumed.validateTransition(Started)
    }

    @Test
    fun `Resumed can transition to Paused`() {
        Resumed.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Resumed fails on transition to Resumed`() {
        Resumed.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Resumed fails on transition to Restarted`() {
        Resumed.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Resumed fails on transition to Background`() {
        Resumed.validateTransition(Background)
    }

    @Test(expected = AssertionError::class)
    fun `Resumed fails on transition to Foreground`() {
        Resumed.validateTransition(Foreground)
    }

    @Test
    fun `Resumed can transition to Finished`() {
        Resumed.validateTransition(Finished)
    }

    @Test

    fun `Resumed can transition to Terminated`() {
        Resumed.validateTransition(Terminated)
    }

    /*
    Restarted
     */

    @Test(expected = AssertionError::class)
    fun `Restarted fails on transition to Idle`() {
        Restarted.validateTransition(Idle)
    }

    @Test
    fun `Restarted can transition to Started`() {
        Restarted.validateTransition(Started)
    }

    @Test(expected = AssertionError::class)
    fun `Restarted fails on transition to Paused`() {
        Restarted.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Restarted fails on transition to Resumed`() {
        Restarted.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Restarted fails on transition to Restarted`() {
        Restarted.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Restarted fails on transition to Finished`() {
        Restarted.validateTransition(Finished)
    }

    @Test(expected = AssertionError::class)
    fun `Restarted fails on transition to Foreground`() {
        Restarted.validateTransition(Foreground)
    }

    @Test
    fun `Restarted can transition to Terminated`() {
        Restarted.validateTransition(Terminated)
    }

    @Test
    fun `Restarted can transition to Background`() {
        Restarted.validateTransition(Background)
    }

    /*
    Finished
     */

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Idle`() {
        Finished.validateTransition(Idle)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Started`() {
        Finished.validateTransition(Started)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Paused`() {
        Finished.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Resumed`() {
        Finished.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Restarted`() {
        Finished.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Background`() {
        Finished.validateTransition(Background)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Foreground`() {
        Finished.validateTransition(Foreground)
    }

    @Test(expected = AssertionError::class)
    fun `Finished fails on transition to Finished`() {
        Finished.validateTransition(Finished)
    }

    @Test
    fun `Finished can transition to Terminated`() {
        Finished.validateTransition(Terminated)
    }

    /*
    Terminated
     */

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Idle`() {
        Terminated.validateTransition(Idle)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Started`() {
        Terminated.validateTransition(Started)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Paused`() {
        Terminated.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Resumed`() {
        Terminated.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Restarted`() {
        Terminated.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Finished`() {
        Terminated.validateTransition(Finished)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Background`() {
        Terminated.validateTransition(Background)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Foreground`() {
        Terminated.validateTransition(Foreground)
    }

    @Test(expected = AssertionError::class)
    fun `Terminated fails on transition to Terminated`() {
        Terminated.validateTransition(Terminated)
    }

    /*
    Background
     */

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Idle`() {
        Background.validateTransition(Idle)
    }

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Started`() {
        Background.validateTransition(Started)
    }

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Paused`() {
        Background.validateTransition(Paused)
    }

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Resumed`() {
        Background.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Restarted`() {
        Background.validateTransition(Restarted)
    }

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Finished`() {
        Background.validateTransition(Finished)
    }

    @Test(expected = AssertionError::class)
    fun `Background fails on transition to Background`() {
        Background.validateTransition(Background)
    }

    @Test
    fun `Background can transition to Foreground`() {
        Background.validateTransition(Foreground)
    }

    @Test
    fun `Background can transition to Terminated`() {
        Background.validateTransition(Terminated)
    }

    /*
    Foreground
     */

    @Test(expected = AssertionError::class)
    fun `Foreground fails on transition to Started`() {
        Foreground.validateTransition(Started)
    }

    @Test(expected = AssertionError::class)
    fun `Foreground fails on transition to Resumed`() {
        Foreground.validateTransition(Resumed)
    }

    @Test(expected = AssertionError::class)
    fun `Foreground fails on transition to Finished`() {
        Foreground.validateTransition(Finished)
    }

    @Test(expected = AssertionError::class)
    fun `Foreground fails on transition to Terminated`() {
        Foreground.validateTransition(Terminated)
    }

    @Test(expected = AssertionError::class)
    fun `Foreground fails on transition to Foreground`() {
        Foreground.validateTransition(Foreground)
    }

    @Test(expected = AssertionError::class)
    fun `Foreground fails on transition to Background`() {
        Foreground.validateTransition(Background)
    }

    @Test
    fun `Foreground can transition to Paused`() {
        Foreground.validateTransition(Paused)
    }

    @Test
    fun `Foreground can transition to Idle`() {
        Foreground.validateTransition(Idle)
    }

    @Test
    fun `Foreground can transition to Restarted`() {
        Foreground.validateTransition(Restarted)
    }
}
