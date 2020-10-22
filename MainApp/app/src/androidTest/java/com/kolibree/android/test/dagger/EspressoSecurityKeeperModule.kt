/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.crypto.SecurityKeeper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
object EspressoSecurityKeeperModule {

    private val mock = mock<SecurityKeeper>()

    init {
        doReturn(true).whenever(mock).isLoggingAllowed
        doReturn(false).whenever(mock).isAuditAllowed
    }

    @Provides
    @AppScope
    internal fun providesSecurityKeeper(): SecurityKeeper = mock
}
