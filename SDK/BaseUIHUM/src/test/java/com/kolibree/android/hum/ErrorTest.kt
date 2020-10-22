/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum

import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ErrorTest : BaseUnitTest() {

    @Test
    fun `if source exception is not ApiError, use its directly`() {
        val exception = RuntimeException("Some test exception")
        val error = Error.from(exception)
        assertNull(error.message)
        assertEquals(exception, error.exception)
        assertNull(error.messageId)
    }

    @Test
    fun `if source exception is ApiError, use its display message`() {
        val error = Error.from(ApiError.generateNetworkError())
        assertEquals("Network unavailable", error.message)
        assertNull(error.exception)
        assertNull(error.messageId)
    }
}
