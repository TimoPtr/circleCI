/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class AmazonDashCheckAlexaUseCaseImplTest : BaseUnitTest() {

    private val context: Context = mock()

    private lateinit var useCase: AmazonDashCheckAlexaUseCase

    @Test
    fun `returns true if alexa app is available`() {
        mockUseCase(
            sdkInt = Build.VERSION_CODES.P,
            packageInfo = packageInfo(versionCode = REQUIRED_MINIMUM_VERSION_CODE + 1)
        )

        val testObserver = useCase.isAlexaAppAvailable().test()
        testObserver.assertValue(true)
    }

    @Test
    fun `returns true if alexa app is available on older API`() {
        mockUseCase(
            sdkInt = Build.VERSION_CODES.O,
            packageInfo = packageInfo(versionCode = null)
        )

        val testObserver = useCase.isAlexaAppAvailable().test()
        testObserver.assertValue(true)
    }

    @Test
    fun `returns false if alexa app not available`() {
        mockUseCase(
            sdkInt = Build.VERSION_CODES.P,
            packageInfo = null
        )

        val testObserver = useCase.isAlexaAppAvailable().test()
        testObserver.assertValue(false)
    }

    @Test
    fun `returns false if alexa app not available on older API`() {
        mockUseCase(
            sdkInt = Build.VERSION_CODES.O,
            packageInfo = null
        )

        val testObserver = useCase.isAlexaAppAvailable().test()
        testObserver.assertValue(false)
    }

    @Test
    fun `returns false if exception`() {
        mockUseCase(
            sdkInt = Build.VERSION_CODES.O,
            packageInfo = null,
            throwException = true
        )

        val testObserver = useCase.isAlexaAppAvailable().test()
        testObserver.assertValue(false)
    }

    private fun mockUseCase(
        sdkInt: Int,
        packageInfo: PackageInfo? = null,
        throwException: Boolean = false
    ) {
        val packageManager: PackageManager = mock()

        if (throwException) {
            whenever(packageManager.getPackageInfo(ALEXA_PACKAGE_NAME, 0))
                .thenThrow(PackageManager.NameNotFoundException::class.java)
        } else {
            whenever(packageManager.getPackageInfo(ALEXA_PACKAGE_NAME, 0))
                .thenReturn(packageInfo)
        }

        whenever(context.packageManager).thenReturn(packageManager)
        useCase = AmazonDashCheckAlexaUseCaseImpl(context, sdkInt)
    }

    private fun packageInfo(versionCode: Long?): PackageInfo {
        val packageInfo: PackageInfo = mock()
        if (versionCode != null) {
            whenever(packageInfo.longVersionCode).thenReturn(versionCode)
        }
        return packageInfo
    }
}
