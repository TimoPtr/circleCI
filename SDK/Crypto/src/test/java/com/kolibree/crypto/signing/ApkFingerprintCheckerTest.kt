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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import java.lang.IllegalStateException
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ApkFingerprintCheckerTest : BaseUnitTest() {

    private val packageName = "com.kolibree.crypto.test"

    private val context: Context = mock()

    private val packageManager: PackageManager = mock()

    private lateinit var apkFingerprintChecker: ApkFingerprintChecker

    override fun setup() {
        super.setup()
        doReturn(packageName).whenever(context).packageName
        doReturn(packageManager).whenever(context).packageManager
        apkFingerprintChecker = spy(ApkFingerprintChecker(context))
    }

    @Test
    fun `checker returns PRODUCTION if signature byte array is empty`() {
        val signature = mock<Signature>()
        doReturn(ByteArray(0)).whenever(signature).toByteArray()
        val packageInfo = PackageInfo()
        packageInfo.signatures = arrayOf(signature)
        doReturn(packageInfo)
            .whenever(packageManager).getPackageInfo(eq(packageName), any())

        val fingerprint = apkFingerprintChecker.fingerprint

        assertEquals(ApkFingerprint.PRODUCTION, fingerprint)
    }

    @Test
    fun `checker returns PRODUCTION if signature is unknown`() {
        val unknownSignatureData = byteArrayOf(
            0x4A.toByte(),
            0xDA.toByte(),
            0xF4.toByte(),
            0x14.toByte(),
            0x06.toByte(),
            0xB4.toByte(),
            0x34.toByte(),
            0xAE.toByte()
        )
        val signature = mock<Signature>()
        doReturn(unknownSignatureData).whenever(signature).toByteArray()
        val packageInfo = PackageInfo()
        packageInfo.signatures = arrayOf(signature)
        doReturn(packageInfo)
            .whenever(packageManager).getPackageInfo(eq(packageName), any())

        val fingerprint = apkFingerprintChecker.fingerprint

        assertEquals(ApkFingerprint.PRODUCTION, fingerprint)
    }

    @Test
    fun `checker returns PRODUCTION in case of exception`() {
        doThrow(IllegalStateException("To be handled!"))
            .whenever(packageManager).getPackageInfo(eq(packageName), any())

        val fingerprint = apkFingerprintChecker.fingerprint

        assertEquals(ApkFingerprint.PRODUCTION, fingerprint)
    }
}
