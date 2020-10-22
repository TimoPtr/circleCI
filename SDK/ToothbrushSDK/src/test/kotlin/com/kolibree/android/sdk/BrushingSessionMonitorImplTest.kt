/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.BrushingSessionMonitorImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class BrushingSessionMonitorImplTest : BaseUnitTest() {
    private val mainHandler = mock<Handler>()

    private val sessionMonitor = BrushingSessionMonitorImpl(mainHandler)

    override fun setup() {
        super.setup()
        whenever(mainHandler.post(any())).thenAnswer {
            it.getArgument<Runnable>(0).run()
            true
        }
    }

    /*
    onBrushingSessionStateChanged
     */

    @Test
    fun `onBrushingSessionStateChanged emits value on the stream`() {
        val expectedState = true

        val testObserver = sessionMonitor.sessionMonitorStream.test()

        sessionMonitor.onBrushingSessionStateChanged(expectedState)

        testObserver.assertValue(expectedState)
    }
}
