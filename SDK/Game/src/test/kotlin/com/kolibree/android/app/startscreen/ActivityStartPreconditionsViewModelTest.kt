/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.startscreen

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Test

internal class ActivityStartPreconditionsViewModelTest : BaseUnitTest() {

    private val connectionProvider: KLTBConnectionProvider = mock()

    @Test
    fun `canStartActivityStream emits inverse vibrator status`() {
        val mac = "hello"
        val connection = KLTBConnectionBuilder.createAndroidLess().build()
        val checker = ActivityStartPreconditionsViewModel(ActivityStartPreconditionsViewState.initial(), connectionProvider, mac)

        val testObserver = checker.canStart.test()

        whenever(connection.vibrator().vibratorStream).thenReturn(Flowable.just(true, false))

        whenever(connectionProvider.existingActiveConnection(mac)).thenReturn(Single.just(connection))

        checker.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        testObserver.assertValueHistory(false, true)
    }
}
