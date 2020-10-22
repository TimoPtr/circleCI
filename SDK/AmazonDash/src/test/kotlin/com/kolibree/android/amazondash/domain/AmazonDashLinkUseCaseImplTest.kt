/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.amazondash.domain.AmazonDashLinkUseCaseImpl.Companion.STATE_QUERY
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import retrofit2.Response

class AmazonDashLinkUseCaseImplTest : BaseUnitTest() {

    private val checkAlexaUseCase: AmazonDashCheckAlexaUseCase = mock()
    private val verifyStateUseCase: AmazonDashVerifyStateUseCase = mock()
    private val amazonDashApi: AmazonDashApi = mock()

    private lateinit var useCase: AmazonDashLinkUseCase

    private val testState = "state"

    override fun setup() {
        super.setup()
        useCase = AmazonDashLinkUseCaseImpl(
            checkAlexaUseCase,
            verifyStateUseCase,
            amazonDashApi
        )

        whenever(verifyStateUseCase.createNewState())
            .thenReturn(Single.just(testState))
    }

    @Test
    fun `return app link if alexa app is available`() {
        val testLinks = AmazonDashGetLinkResponse(
            appUrl = "https://appurl.com?client_id=1",
            fallbackUrl = "https://fallbackurl.com?client_id=1"
        )

        whenever(checkAlexaUseCase.isAlexaAppAvailable())
            .thenReturn(Single.just(true))

        whenever(amazonDashApi.getLinks())
            .thenReturn(Single.just(Response.success(testLinks)))

        val testObserver = useCase.getLink().test()
        testObserver.assertValue("${testLinks.appUrl}&$STATE_QUERY=$testState")
    }

    @Test
    fun `return fallback link if alexa app is not available`() {
        val testLinks = AmazonDashGetLinkResponse(
            appUrl = "https://appurl.com?client_id=1",
            fallbackUrl = "https://fallbackurl.com?client_id=1"
        )

        whenever(checkAlexaUseCase.isAlexaAppAvailable())
            .thenReturn(Single.just(false))

        whenever(amazonDashApi.getLinks())
            .thenReturn(Single.just(Response.success(testLinks)))

        val testObserver = useCase.getLink().test()
        testObserver.assertValue("${testLinks.fallbackUrl}&$STATE_QUERY=$testState")
    }
}
