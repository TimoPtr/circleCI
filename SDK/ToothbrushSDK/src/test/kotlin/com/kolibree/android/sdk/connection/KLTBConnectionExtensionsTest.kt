/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KLTBConnectionExtensionsTest : BaseUnitTest() {
    @Test
    fun `callSafelyIfActive invokes block if connection is active`() {
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build()

        var invoked = false
        connection.callSafelyIfActive { invoked = true }

        assertTrue(invoked)
    }

    @Test
    fun `callSafelyIfActive doesn't invoke block nor crash if connection is null`() {
        var invoked = false

        val connection: KLTBConnection? = null

        connection.callSafelyIfActive { invoked = true }

        assertFalse(invoked)
    }

    @Test
    fun `callSafelyIfActive doesn't invoke block if connection is not active`() {
        var invoked = false

        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                val connection =
                    KLTBConnectionBuilder.createAndroidLess().withState(state)
                        .build()

                connection.callSafelyIfActive { invoked = true }
            }

        assertFalse(invoked)
    }

    @Test
    fun `callSafelyIfActive doesn't propagate exception`() {
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE).build()

        connection.callSafelyIfActive { throw TestForcedException() }
    }
}
