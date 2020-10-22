/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.shop.test.R.raw.shopify_api_key
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.crypto.KolibreeGuard
import com.kolibree.crypto.extractHexToByteArray
import com.kolibree.crypto.extractStringFromStream
import com.kolibree.crypto.generateRandomIV
import com.kolibree.crypto.toCompactStringHex
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShopifyCredentialsGeneratorTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testCredentialsGeneration() {
        val keyNotEncrypted =
            context().resources.openRawResource(shopify_api_key).extractStringFromStream()

        encryptKey(keyNotEncrypted, "shopify_api_key")
    }

    private fun encryptKey(keyNotEncrypted: String, tag: String) {
        if (keyNotEncrypted.isNotEmpty()) {
            val guard = KolibreeGuard.createInstance()
            val iv = generateRandomIV()
            val encryptedKey = guard.encrypt(keyNotEncrypted, iv)

            // This step is just a verification step if it throw an exception something is wrong in KolibreeGuard
            val revealedKey = guard.reveal(
                encryptedKey,
                iv.toCompactStringHex().extractHexToByteArray()
            )

            assertEquals(revealedKey, keyNotEncrypted)

            fail(
                """

                    ------------------------ ${tag.toUpperCase()} ENCRYPTED KEY ------------------------

                                                ----- INFO ---
                This test failed only to display the encrypted keys
                Please save this IV and key into the res of the SDK near the binary file encrypted

                IV = ${iv.toCompactStringHex()}
                Encrypted key = $encryptedKey"




            """.trimIndent()
            )
        }
    }
}
