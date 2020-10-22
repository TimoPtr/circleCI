/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.e1

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertTrue
import org.junit.Test

class ToothbrushShutdownValveModuleTest : BaseUnitTest() {
    @Test
    fun `module returns NoOpShutdownValue if mac is null`() {
        assertTrue(
            ToothbrushShutdownValveModule.providesToothbrushShutdownValve(
                mac = null,
                connectionProvider = mock(),
                timeoutScheduler = Schedulers.io()
            ) is NoOpShutdownValue
        )
    }

    @Test
    fun `module returns E1ShutdownValve if mac is not null`() {
        assertTrue(
            ToothbrushShutdownValveModule.providesToothbrushShutdownValve(
                mac = "",
                connectionProvider = mock(),
                timeoutScheduler = Schedulers.io()
            ) is ToothbrushShutdownValveImpl
        )
    }
}
