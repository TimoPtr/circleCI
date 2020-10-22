/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.NOT_READY_TRY_LATER
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.NO_ERROR
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.NO_VALID_FILE_FOR_UPDATE
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.TRANSMIT_ERROR
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.UNKNOWN_ERROR
import com.kolibree.android.sdk.plaqless.PushDspLastStatus.VALIDATION_ERROR
import com.kolibree.android.test.utils.randomByte
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PushDspStateTest : BaseUnitTest() {
    @Test
    fun `when payload is 0 0 0, state is not running, DSP_UPDATE_NO_ERROR and progress 0`() {
        val expectedState = PushDspState(
            isUpdateRunning = false,
            lastStatus = NO_ERROR,
            progress = 0
        )

        assertEquals(expectedState, PushDspState.fromPayload(payload(byteArrayOf(0, 0, 0))))
    }

    @Test
    fun `when payload is 1 0 0, state is running, NO_ERROR and progress 0`() {
        val expectedState = PushDspState(
            isUpdateRunning = true,
            lastStatus = NO_ERROR,
            progress = 0
        )

        assertEquals(expectedState, PushDspState.fromPayload(payload(byteArrayOf(1, 0, 0))))
    }

    @Test
    fun `when payload is 1 0 X, state is running, NO_ERROR and progress X`() {
        (0 until 100).forEach { byteProgress ->
            val expectedState = PushDspState(
                isUpdateRunning = true,
                lastStatus = NO_ERROR,
                progress = byteProgress
            )

            assertEquals(
                expectedState,
                PushDspState.fromPayload(payload(byteArrayOf(1, 0, byteProgress.toByte())))
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `when payload contains progress higher than 100, creation throws IllegalStateException`() {
        PushDspState.fromPayload(payload(byteArrayOf(1, 0, 101)))
    }

    @Test(expected = IllegalStateException::class)
    fun `when constructor contains progress higher than 100, creation throws IllegalStateException`() {
        PushDspState(
            isUpdateRunning = true,
            lastStatus = NO_ERROR,
            progress = 105
        )
    }

    @Test
    fun `when payload is 1 1 0, state is running, NOT_READY_TRY_LATER and progress 0`() {
        val expectedState = PushDspState(
            isUpdateRunning = true,
            lastStatus = NOT_READY_TRY_LATER,
            progress = 0
        )

        assertEquals(expectedState, PushDspState.fromPayload(payload(byteArrayOf(1, 1, 0))))
    }

    @Test
    fun `when payload is 1 2 0, state is running, NO_VALID_FILE_FOR_UPDATE and progress 0`() {
        val expectedState = PushDspState(
            isUpdateRunning = true,
            lastStatus = NO_VALID_FILE_FOR_UPDATE,
            progress = 0
        )

        assertEquals(expectedState, PushDspState.fromPayload(payload(byteArrayOf(1, 2, 0))))
    }

    @Test
    fun `when payload is 1 3 0, state is running, TRANSMIT_ERROR and progress 0`() {
        val expectedState = PushDspState(
            isUpdateRunning = true,
            lastStatus = TRANSMIT_ERROR,
            progress = 0
        )

        assertEquals(expectedState, PushDspState.fromPayload(payload(byteArrayOf(1, 3, 0))))
    }

    @Test
    fun `when payload is 1 4 0, state is running, VALIDATION_ERROR and progress 0`() {
        val expectedState = PushDspState(
            isUpdateRunning = true,
            lastStatus = VALIDATION_ERROR,
            progress = 0
        )

        assertEquals(expectedState, PushDspState.fromPayload(payload(byteArrayOf(1, 4, 0))))
    }

    @Test
    fun `when payload is 1 X 0, state is running, UNKNOWN_ERROR and progress 0`() {
        byteArrayOf(Byte.MAX_VALUE, randomByte()).forEach { updateStateByte ->
            val expectedState = PushDspState(
                isUpdateRunning = true,
                lastStatus = UNKNOWN_ERROR,
                progress = 0
            )

            assertEquals(
                expectedState,
                PushDspState.fromPayload(payload(byteArrayOf(1, updateStateByte, 0)))
            )
        }
    }

    fun payload(byteArray: ByteArray) = byteArrayOf(0x51) + byteArray
}
