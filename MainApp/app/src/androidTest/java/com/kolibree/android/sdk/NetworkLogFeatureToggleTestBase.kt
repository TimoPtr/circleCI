/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk

import androidx.fragment.app.FragmentActivity
import com.kolibree.BuildConfig
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.network.NetworkLogFeature
import com.kolibree.android.network.NetworkLogFeatureToggle
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test

abstract class NetworkLogFeatureToggleTestBase<T : FragmentActivity> : BaseEspressoTest<T>() {

    @Test
    fun testToggleWhenLoggingIsAllowed() {
        doReturn(true).whenever(component().securityKeeper()).isLoggingAllowed

        val networkLogFeatureToggle = launchAndGetToggle()

        networkLogFeatureToggle.value = !BuildConfig.DEBUG
        assertEquals(!BuildConfig.DEBUG, networkLogFeatureToggle.value)

        networkLogFeatureToggle.value = BuildConfig.DEBUG
        assertEquals(BuildConfig.DEBUG, networkLogFeatureToggle.value)
    }

    @Test
    fun testToggleWhenLoggingIsNotAllowed() {
        doReturn(false).whenever(component().securityKeeper()).isLoggingAllowed

        val networkLogFeatureToggle = launchAndGetToggle()

        assertFalse(networkLogFeatureToggle.value)

        networkLogFeatureToggle.value = true
        assertFalse(networkLogFeatureToggle.value)
    }

    private fun launchAndGetToggle(): NetworkLogFeatureToggle {
        val sdkBuilder = SdkBuilder.create().prepareForMainScreen()
        AppMocker.create().withSdkBuilder(sdkBuilder).prepareForMainScreen().mock()
        launchActivity()
        return component().featureToggles().toggleForFeature(NetworkLogFeature) as NetworkLogFeatureToggle
    }

    abstract fun launchActivity()
}
