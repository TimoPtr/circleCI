/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.appversion.AppVersionProvider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class VersionCodeAppInitializerTest : BaseUnitTest() {

    private val appVersionProvider: AppVersionProvider = mock()
    private lateinit var versionCodeAppInitializer: AppInitializer

    override fun setup() {
        versionCodeAppInitializer = VersionCodeAppInitializer(appVersionProvider)
    }

    @Test
    fun `initialize should update the version code from the provider`() {
        versionCodeAppInitializer.initialize(mock())

        verify(appVersionProvider).updateLastVersionCode()
    }
}
