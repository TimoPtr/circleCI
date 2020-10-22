/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk

import com.kolibree.android.app.test.BaseUnitTest
import java.util.zip.CRC32
import org.junit.Assert
import org.junit.Test

/** com.kolibree.android.sdk.Ext tests */
class ExtKtTest : BaseUnitTest() {

    /*
    ByteArray.computeCrc()
     */

    @Test
    fun `ByteArray computeCrc() computes right CRC value`() {
        Assert.assertEquals(
            380987386L,
            byteArrayOf(0x65, 0x31, 0x54, 0x00, 0x23).computeCrc()
        )
    }

    @Test
    fun `ByteArray computeCrc() uses Java's CRC32 default checksum implementation, not Emmanuel's`() {
        val array = "Je suis une phrase accentuée.".toByteArray()
        val expectedCrc = CRC32().run {
            update(array)
            value
        }

        Assert.assertEquals(expectedCrc, array.computeCrc())
    }
}
