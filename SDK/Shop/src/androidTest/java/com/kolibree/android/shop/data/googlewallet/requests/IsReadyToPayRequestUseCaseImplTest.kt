/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet.requests

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.gms.wallet.PaymentsClient
import com.kolibree.android.shop.data.googlewallet.FakeGoogleWalletConfiguration
import com.kolibree.android.shop.data.googlewallet.GoogleWalletRequestProviderImpl
import com.kolibree.android.shop.data.googlewallet.taskWithException
import com.kolibree.android.shop.data.googlewallet.taskWithSuccess
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import org.junit.Test

internal class IsReadyToPayRequestUseCaseImplTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val paymentsClient = mock<PaymentsClient>()
    private val walletRequestProvider =
        GoogleWalletRequestProviderImpl(FakeGoogleWalletConfiguration())

    private val useCase = IsReadyToPayRequestUseCaseImpl(paymentsClient, walletRequestProvider)

    @Test
    fun isReadyToPayRequest_emitsApiException() {
        val apiException = ApiException(Status.RESULT_DEAD_CLIENT)

        taskWithException<Boolean>(
            apiException,
            onTaskReadyBlock = {
                whenever(paymentsClient.isReadyToPay(any())).thenReturn(it)

                useCase.isReadyToPayRequest().test()
            },
            assertBlock = { observer ->
                observer.assertError(ApiException::class.java)

                TestCase.assertEquals(apiException, observer.errors().single())
            }
        )
    }

    @Test
    fun isReadyToPayRequest_emitsRuntimeException() {
        val runtimeException = RuntimeException()

        taskWithException<Boolean>(
            runtimeException,
            onTaskReadyBlock = {
                whenever(paymentsClient.isReadyToPay(any())).thenReturn(it)

                useCase.isReadyToPayRequest().test()
            },
            assertBlock = { observer ->
                observer.assertError(RuntimeException::class.java)

                TestCase.assertEquals(runtimeException, observer.errors().single().cause)
            }
        )
    }

    @Test
    fun isReadyToPayRequest_emitsRuntimeExecutionExceptionOnAnyOtherError() {
        val exception = TestForcedException()

        taskWithException<Boolean>(
            exception,
            onTaskReadyBlock = {
                whenever(paymentsClient.isReadyToPay(any())).thenReturn(it)

                useCase.isReadyToPayRequest().test()
            },
            assertBlock = { observer ->
                observer.assertError(RuntimeExecutionException::class.java)

                TestCase.assertEquals(exception, observer.errors().single().cause)
            }
        )
    }

    @Test
    fun isReadyToPayRequest_emitsTrueOnSuccessTrue() {
        taskWithSuccess(
            true,
            onTaskReadyBlock = {
                whenever(paymentsClient.isReadyToPay(any())).thenReturn(it)

                useCase.isReadyToPayRequest().test()
            },
            assertBlock = { observer -> observer.assertValue(true) }
        )
    }

    @Test
    fun isReadyToPayRequest_emitsFalseOnSuccessFalse() {
        taskWithSuccess(
            false,
            onTaskReadyBlock = {
                whenever(paymentsClient.isReadyToPay(any())).thenReturn(it)

                useCase.isReadyToPayRequest().test()
            },
            assertBlock = { observer -> observer.assertValue(false) }
        )
    }
}
