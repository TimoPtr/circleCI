/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class GameProgressConflictResolutionStrategyTest : BaseUnitTest() {

    @Test
    fun `resolve returns remote result`() {
        val local = mock<SynchronizableItem>()
        val remote = mock<SynchronizableItem>()

        assertEquals(remote, GameProgressConflictResolutionStrategy.resolve(local, remote).remoteSynchronizable)
    }
}
