/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.crypto.signing.ApkFingerprint
import com.kolibree.crypto.signing.ApkFingerprintChecker
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SecurityKeeperImplTest : BaseUnitTest() {

    private val checker: ApkFingerprintChecker = mock()

    @Test
    fun `checker returns true for DEBUG fingerprint`() {
        doReturn(ApkFingerprint.DEBUG).whenever(checker).fingerprint
        val keeper = SecurityKeeperImpl(checker)
        assertTrue(keeper.isLoggingAllowed)
    }

    @Test
    fun `checker returns true for BETA fingerprint`() {
        doReturn(ApkFingerprint.BETA).whenever(checker).fingerprint
        val keeper = SecurityKeeperImpl(checker)
        assertTrue(keeper.isLoggingAllowed)
    }

    @Test
    fun `checker returns false for PROD fingerprint`() {
        doReturn(ApkFingerprint.PRODUCTION).whenever(checker).fingerprint
        val keeper = SecurityKeeperImpl(checker)
        assertFalse(keeper.isLoggingAllowed)
    }
}
