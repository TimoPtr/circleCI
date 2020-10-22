/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.push

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.sdkws.KolibreeUtils
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.push.PushNotificationApi
import com.kolibree.sdkws.push.PushNotificationTokenRequestBody
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

private const val TOKEN = "just-some-token"
private const val ACCOUNT_ID = 123L
private const val DEVICE_ID = "sample-device-id"

class PushNotificationUseCaseImplTest : BaseUnitTest() {

    private val account = mock<AccountInternal>().also {
        doReturn(ACCOUNT_ID).whenever(it).id
    }

    private val api = mock<PushNotificationApi>()

    private val kolibreeConnector = mock<InternalKolibreeConnector>()

    private val kolibreeUtils = mock<KolibreeUtils>()

    private val useCase = spy(PushNotificationUseCaseImpl(api, kolibreeConnector, kolibreeUtils))

    override fun setup() {
        super.setup()
        doReturn(DEVICE_ID).whenever(kolibreeUtils).deviceId
    }

    @Test
    fun `uploadNewTokenForCurrentAccount uploads token to the backend when account is available`() {
        doReturn(account).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.just(Response.success(204))).whenever(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )

        val observer = useCase.uploadNewTokenForCurrentAccount(TOKEN).test()

        observer.assertComplete()
        verify(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )
    }

    @Test
    fun `uploadNewTokenForCurrentAccount returns error from the API if there was any`() {
        val errorMessage = "Something went wrong!"
        doReturn(account).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.just(Response.error<Void>(400, ResponseBody.create(null, errorMessage))))
            .whenever(api).updatePushNotificationToken(
                eq(ACCOUNT_ID),
                eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
            )

        val observer = useCase.uploadNewTokenForCurrentAccount(TOKEN).test()

        observer.assertError(ApiError::class.java)
    }

    @Test
    fun `uploadNewTokenForCurrentAccount returns error when account is not available during token upload`() {
        doReturn(null).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.just(Response.success(204))).whenever(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )

        val observer = useCase.uploadNewTokenForCurrentAccount(TOKEN).test()

        observer.assertError(IllegalStateException::class.java)
        observer.assertErrorMessage("Account is null, cannot proceed with Firebase token upload")
        verify(api, never()).updatePushNotificationToken(any(), any())
    }

    @Test
    fun `forceUploadCurrentTokenForCurrentAccount re-uploads current token to API if it's available`() {
        doReturn(Single.just(TOKEN)).whenever(useCase).retrieveCurrentToken()
        doReturn(account).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.just(Response.success(204))).whenever(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )

        val observer = useCase.forceUploadCurrentTokenForCurrentAccount().test()

        observer.assertComplete()
        verify(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )
    }

    @Test
    fun `forceUploadCurrentTokenForCurrentAccount returns error from the API if there was any`() {
        val errorMessage = "Something went wrong!"

        doReturn(Single.just(TOKEN)).whenever(useCase).retrieveCurrentToken()
        doReturn(account).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.just(Response.error<Void>(400, ResponseBody.create(null, errorMessage))))
            .whenever(api).updatePushNotificationToken(
                eq(ACCOUNT_ID),
                eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
            )

        val observer = useCase.forceUploadCurrentTokenForCurrentAccount().test()

        observer.assertError(ApiError::class.java)
    }

    @Test
    fun `forceUploadCurrentTokenForCurrentAccount returns error when account is not available during token upload`() {
        doReturn(null).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.just(TOKEN)).whenever(useCase).retrieveCurrentToken()
        doReturn(Single.just(Response.success(204))).whenever(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )

        val observer = useCase.forceUploadCurrentTokenForCurrentAccount().test()

        observer.assertError(IllegalStateException::class.java)
        observer.assertErrorMessage("Account is null, cannot proceed with Firebase token upload")
        verify(api, never()).updatePushNotificationToken(any(), any())
    }

    @Test
    fun `forceUploadCurrentTokenForCurrentAccount does not fire API if token retrieval failed`() {
        val errorMessage = "Token retrieval failed!"

        doReturn(account).whenever(kolibreeConnector).currentAccount()
        doReturn(Single.error<String>(IllegalStateException(errorMessage)))
            .whenever(useCase).retrieveCurrentToken()
        doReturn(Single.just(Response.success(204))).whenever(api).updatePushNotificationToken(
            eq(ACCOUNT_ID),
            eq(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID))
        )

        val observer = useCase.forceUploadCurrentTokenForCurrentAccount().test()

        observer.assertError(IllegalStateException::class.java)
        observer.assertErrorMessage(errorMessage)
        verify(api, never()).updatePushNotificationToken(any(), any())
    }
}
