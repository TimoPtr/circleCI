/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.core

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.junit.Test

class CancelHttpRequestsUseCaseImplTest : BaseUnitTest() {
    private val okHttpClient: OkHttpClient = mock()

    private val useCase = CancelHttpRequestsUseCaseImpl(okHttpClient)

    @Test
    fun `run cancels requests on okHttpClient`() {
        val dispatcher: Dispatcher = mock()
        whenever(okHttpClient.dispatcher()).thenReturn(dispatcher)

        useCase.cancelAll()

        verify(dispatcher).cancelAll()
    }
}
