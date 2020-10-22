/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.inprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.ota.R
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class InProgressOtaViewStateTest : BaseUnitTest() {

    @Test
    fun `showResult returns true when ota is fail or a success`() {
        assertTrue(InProgressOtaViewState(isOtaSuccess = true, isOtaFailed = false).showResult())
        assertTrue(InProgressOtaViewState(isOtaSuccess = false, isOtaFailed = true).showResult())
    }

    @Test
    fun `showResult returns false when ota does not fail nor a success`() {
        assertFalse(InProgressOtaViewState(isOtaSuccess = false, isOtaFailed = false).showResult())
    }

    @Test
    fun `resultIcon returns ic ota fail when ota fail`() {
        assertEquals(
            R.drawable.ic_ota_fail,
            InProgressOtaViewState(isOtaFailed = true).resultIcon()
        )
    }

    @Test
    fun `resultIcon returns ic ota done when ota did not fail`() {
        assertEquals(
            R.drawable.ic_ota_done,
            InProgressOtaViewState(isOtaFailed = false).resultIcon()
        )
    }

    @Test
    fun `title returns title done when ota succeed`() {
        assertEquals(R.string.ota_done_title, InProgressOtaViewState(isOtaSuccess = true).title())
    }

    @Test
    fun `title returns title fail when ota failed`() {
        assertEquals(R.string.ota_failure_title, InProgressOtaViewState(isOtaFailed = true).title())
    }

    @Test
    fun `title returns title in progress when ota did not failed or succeed`() {
        assertEquals(
            R.string.in_progress_ota_title,
            InProgressOtaViewState(isOtaFailed = false, isOtaSuccess = false).title()
        )
    }

    @Test
    fun `content returns content done when ota succeed`() {
        assertEquals(
            R.string.ota_done_content,
            InProgressOtaViewState(isOtaSuccess = true).content()
        )
    }

    @Test
    fun `content returns content fail when ota failed`() {
        assertEquals(
            R.string.ota_failure_content,
            InProgressOtaViewState(isOtaFailed = true).content()
        )
    }

    @Test
    fun `content returns content in progress when ota did not failed or succeed`() {
        assertEquals(
            R.string.in_progress_ota_content,
            InProgressOtaViewState(isOtaFailed = false, isOtaSuccess = false).content()
        )
    }
}
