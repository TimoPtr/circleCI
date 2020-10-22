/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class NightsWatchBootBroadcastReceiverTest : BaseInstrumentationTest() {
    private val receiver = spy(NightsWatchBootBroadcastReceiver())

    private val nightsWatchScheduler: NightsWatchScheduler = mock()

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        doAnswer {
            receiver.nightsWatchScheduler = nightsWatchScheduler

            Unit
        }.whenever(receiver).injectSelf(context())
    }

    @Test
    fun onReceive_injectsSelf() {
        receiver.onReceive(context(), Intent())

        verify(receiver).injectSelf(context())
    }

    @Test
    fun onReceive_invokes_nightsWatchScheduler_scheduleImmediateJob() {
        receiver.onReceive(context(), Intent())

        verify(nightsWatchScheduler).scheduleImmediateJob(context())
    }
}
