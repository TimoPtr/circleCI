/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto.signing

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApkFingerprintCheckerTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun checkerReturnsDebugFingerprintForAppSignedWithDebugCertificate() {
        val apkFingerprintChecker = ApkFingerprintChecker(context())
        val fingerprint = apkFingerprintChecker.fingerprint
        assertEquals(ApkFingerprint.DEBUG, fingerprint)
    }
}
