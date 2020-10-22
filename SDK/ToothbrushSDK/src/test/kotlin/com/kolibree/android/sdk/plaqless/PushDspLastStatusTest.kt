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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PushDspLastStatusTest : BaseUnitTest() {
    /*
    NO_ERROR
     */
    @Test
    fun `NO_ERROR value is 0`() {
        assertEquals(0, NO_ERROR.value)
    }

    @Test
    fun `NO_ERROR returns false to isError`() {
        assertFalse(NO_ERROR.isError())
    }

    @Test
    fun `NO_ERROR returns false to isRecoverableByPushError`() {
        assertFalse(NO_ERROR.isRecoverableByPushError())
    }

    @Test
    fun `NO_ERROR returns false to isUnrecoverableError`() {
        assertFalse(NO_ERROR.isUnrecoverableError())
    }

    /*
    NOT_READY_TRY_LATER
     */
    @Test
    fun `NOT_READY_TRY_LATER value is 1`() {
        assertEquals(1, NOT_READY_TRY_LATER.value)
    }

    @Test
    fun `NOT_READY_TRY_LATER returns true to isError`() {
        assertTrue(NOT_READY_TRY_LATER.isError())
    }

    @Test
    fun `NOT_READY_TRY_LATER returns true to isRecoverableByPushError`() {
        assertTrue(NOT_READY_TRY_LATER.isRecoverableByPushError())
    }

    @Test
    fun `NOT_READY_TRY_LATER returns false to isUnrecoverableError`() {
        assertFalse(NOT_READY_TRY_LATER.isUnrecoverableError())
    }

    /*
    NO_VALID_FILE_FOR_UPDATE
     */
    @Test
    fun `NO_VALID_FILE_FOR_UPDATE value is 2`() {
        assertEquals(2, NO_VALID_FILE_FOR_UPDATE.value)
    }

    @Test
    fun `NO_VALID_FILE_FOR_UPDATE returns true to isError`() {
        assertTrue(NO_VALID_FILE_FOR_UPDATE.isError())
    }

    @Test
    fun `NO_VALID_FILE_FOR_UPDATE returns false to isRecoverableByPushError`() {
        assertFalse(NO_VALID_FILE_FOR_UPDATE.isRecoverableByPushError())
    }

    @Test
    fun `NO_VALID_FILE_FOR_UPDATE returns true to isUnrecoverableError`() {
        assertTrue(NO_VALID_FILE_FOR_UPDATE.isUnrecoverableError())
    }

    /*
    TRANSMIT_ERROR
     */
    @Test
    fun `TRANSMIT_ERROR value is 3`() {
        assertEquals(3, TRANSMIT_ERROR.value)
    }

    @Test
    fun `TRANSMIT_ERROR returns true to isError`() {
        assertTrue(TRANSMIT_ERROR.isError())
    }

    @Test
    fun `TRANSMIT_ERROR returns true to isRecoverableByPushError`() {
        assertTrue(TRANSMIT_ERROR.isRecoverableByPushError())
    }

    @Test
    fun `TRANSMIT_ERROR returns false to isUnrecoverableError`() {
        assertFalse(TRANSMIT_ERROR.isUnrecoverableError())
    }

    /*
    VALIDATION_ERROR
     */
    @Test
    fun `VALIDATION_ERROR value is 4`() {
        assertEquals(4, VALIDATION_ERROR.value)
    }

    @Test
    fun `VALIDATION_ERROR returns true to isError`() {
        assertTrue(VALIDATION_ERROR.isError())
    }

    @Test
    fun `VALIDATION_ERROR returns true to isRecoverableByPushError`() {
        assertTrue(VALIDATION_ERROR.isRecoverableByPushError())
    }

    @Test
    fun `VALIDATION_ERROR returns false to isUnrecoverableError`() {
        assertFalse(VALIDATION_ERROR.isUnrecoverableError())
    }

    /*
    UNKNOWN_ERROR
     */
    @Test
    fun `UNKNOWN_ERROR value is 0xFF`() {
        assertEquals(0xFF.toByte(), UNKNOWN_ERROR.value)
    }

    @Test
    fun `UNKNOWN_ERROR returns true to isError`() {
        assertTrue(UNKNOWN_ERROR.isError())
    }

    @Test
    fun `UNKNOWN_ERROR returns true to isRecoverableByPushError`() {
        assertTrue(UNKNOWN_ERROR.isRecoverableByPushError())
    }

    @Test
    fun `UNKNOWN_ERROR returns false to isUnrecoverableError`() {
        assertFalse(UNKNOWN_ERROR.isUnrecoverableError())
    }
}
