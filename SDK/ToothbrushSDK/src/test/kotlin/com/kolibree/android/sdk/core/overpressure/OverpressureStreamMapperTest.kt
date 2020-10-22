/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.overpressure

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadReader
import org.junit.Test

/** [OverpressureStreamMapper] unit tests */
class OverpressureStreamMapperTest : BaseUnitTest() {

    private lateinit var interactor: OverpressureStreamMapper

    override fun setup() {
        super.setup()

        interactor = OverpressureStreamMapper()
    }

    /*
    onOverpressureSensorData
     */

    @Test
    fun `onOverpressureSensorData parses and emits overpressure sensor's state`() {
        val payloadReader = PayloadReader(byteArrayOf(0x01, 0x01))

        val testObserver = interactor.overpressureStateFlowable().test()
        interactor.onOverpressureSensorData(payloadReader)

        testObserver.assertValue { it.detectorIsActive && it.uiNotificationIsActive }
    }
}
