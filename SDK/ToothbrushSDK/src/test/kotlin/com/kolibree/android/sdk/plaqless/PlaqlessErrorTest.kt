/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.plaqless.PlaqlessError.NONE
import com.kolibree.android.sdk.plaqless.PlaqlessError.OUT_OF_MOUTH
import com.kolibree.android.sdk.plaqless.PlaqlessError.REPLACE_BRUSH_HEAD
import com.kolibree.android.sdk.plaqless.PlaqlessError.RINSE_BRUSH_HEAD
import com.kolibree.android.sdk.plaqless.PlaqlessError.UNKNOWN
import com.kolibree.android.sdk.plaqless.PlaqlessError.WRONG_HANDLE
import org.junit.Assert.assertEquals
import org.junit.Test

internal class PlaqlessErrorTest : BaseUnitTest() {
    @Test
    fun `error code NONE_CODE returns NONE`() {
        assertEquals(NONE, fromErrorCode(NONE_CODE))
        assertEquals(NONE.code, NONE_CODE)
    }

    @Test
    fun `error code OUT_OF_MOUTH_CODE returns OUT_OF_MOUTH`() {
        assertEquals(OUT_OF_MOUTH, fromErrorCode(OUT_OF_MOUTH_CODE))
        assertEquals(OUT_OF_MOUTH.code, OUT_OF_MOUTH_CODE)
    }

    @Test
    fun `error code REPLACE_BRUSH_HEAD_CODE returns REPLACE_BRUSH_HEAD`() {
        assertEquals(REPLACE_BRUSH_HEAD, fromErrorCode(REPLACE_BRUSH_HEAD_CODE))
        assertEquals(REPLACE_BRUSH_HEAD.code, REPLACE_BRUSH_HEAD_CODE)
    }

    @Test
    fun `error code RINSE_BRUSH_HEAD_CODE returns RINSE_BRUSH_HEAD`() {
        assertEquals(RINSE_BRUSH_HEAD, fromErrorCode(RINSE_BRUSH_HEAD_CODE))
        assertEquals(RINSE_BRUSH_HEAD.code, RINSE_BRUSH_HEAD_CODE)
    }

    @Test
    fun `error code WRONG_HANDLE_CODE returns WRONG_HANDLE`() {
        assertEquals(WRONG_HANDLE, fromErrorCode(WRONG_HANDLE_CODE))
        assertEquals(WRONG_HANDLE.code, WRONG_HANDLE_CODE)
    }

    @Test
    fun `error code unknown returns UNKNOWN`() {
        assertEquals(UNKNOWN, fromErrorCode(4325432.toByte()))
        assertEquals(UNKNOWN.code, UNKNOWN_CODE)
    }
}
