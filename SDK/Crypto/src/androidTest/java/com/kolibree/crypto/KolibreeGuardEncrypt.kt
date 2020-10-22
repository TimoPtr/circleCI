/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.crypto.test.R
import junit.framework.TestCase.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KolibreeGuardEncrypt : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun encryptKey(keyNotEncrypted: ByteArray, tag: String) {
        if (keyNotEncrypted.isNotEmpty()) {
            val guard = KolibreeGuard()
            val iv = generateRandomIV()
            val encryptedKey = guard.encrypt(keyNotEncrypted, iv)

            // This step is just a verification step if it throw an exception something is wrong in KolibreeGuard
            guard.reveal(
                encryptedKey.toCompactStringHex().extractHexToByteArray(),
                iv.toCompactStringHex().extractHexToByteArray()
            )

            fail(
                """ 

                    ------------------------ ${tag.toUpperCase()} ENCRYPTED KEY ------------------------
                    
                                                ----- INFO ---
                This test failed only to display the encrypted keys
                Please save this IV and key into the res of the SDK near the binary file encrypted

                Binaries files are location inside Crypto/${tag}_files_enc

                IV = ${iv.toCompactStringHex()}
                Encrypted key = ${encryptedKey.toCompactStringHex()}"
                
                
                
                
            """.trimIndent()
            )
        }
    }

    @Test
    fun testGenerateKeyAndIvForAngleFiles() {
        val keyNotEncrypted = context().resources.openRawResource(R.raw.angle_encryption_key).extractHexToByteArray()
        encryptKey(keyNotEncrypted, "angles")
    }

    @Test
    fun testGenerateKeyAndIvForTransitionFiles() {
        val keyNotEncrypted =
            context().resources.openRawResource(R.raw.transition_encryption_key).extractHexToByteArray()
        encryptKey(keyNotEncrypted, "transition")
    }

    @Test
    fun testGenerateKeyAndIvForWeighFiles() {
        val keyNotEncrypted = context().resources.openRawResource(R.raw.weight_encryption_key).extractHexToByteArray()

        encryptKey(keyNotEncrypted, "weight")
    }

    @Test
    fun testGenerateKeyAndIvForThresholdFiles() {
        val keyNotEncrypted = context().resources.openRawResource(R.raw.threshold_encryption_key).extractHexToByteArray()

        encryptKey(keyNotEncrypted, "threshold")
    }

    @Test
    fun testGenerateKeyAndIvForOtherFiles() {
        val keyNotEncrypted = context().resources.openRawResource(R.raw.other_encryption_key).extractHexToByteArray()

        encryptKey(keyNotEncrypted, "other")
    }
}
