/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet.requests

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.kolibree.android.shop.data.googlewallet.FakeGoogleWalletConfiguration
import com.kolibree.android.shop.data.googlewallet.GoogleWalletRequestProviderImpl
import com.kolibree.android.shop.data.googlewallet.exceptions.GooglePayIllegalResponseException
import com.kolibree.android.shop.data.googlewallet.taskWithSuccess
import com.kolibree.android.shop.googleWalletPayment
import com.kolibree.android.shop.price
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test

internal class PaymentDataRequestUseCaseImplTest : BaseInstrumentationTest() {
    @Rule
    @JvmField
    var activityRule: ActivityTestRule<FakeActivity> =
        ActivityTestRule(FakeActivity::class.java, false, false)

    lateinit var activity: FakeActivity

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val paymentsClient = mock<PaymentsClient>()
    private val configuration = FakeGoogleWalletConfiguration()
    private val walletRequestProvider = GoogleWalletRequestProviderImpl(configuration)

    lateinit var useCase: PaymentDataRequestUseCaseImpl

    override fun setUp() {
        super.setUp()

        activityRule.launchActivity(Intent())

        activity = activityRule.activity
        activity.test = this

        useCase = PaymentDataRequestUseCaseImpl(
            activity = activity,
            paymentsClient = paymentsClient,
            walletRequestProvider = walletRequestProvider,
            googleWalletConfiguration = configuration
        )
    }

    /*
    validatePayment
     */

    /*
    This test can be flaky because we depend on delay.

    I always saw it green with 100ms of delay, so 1s should be more than enough...
     */
    @Test
    fun validatePayment_emitsExpectedToken() {
        val token = "my token"

        val paymentData = mockPaymentDataWithToken(token)

        createPaymentDataTask(paymentData)

        val price = price()
        val expectedGooglePayToken = googleWalletPayment(
            price = price,
            token = token
        )

        taskWithSuccess(
            paymentData,
            onTaskReadyBlock = {
                whenever(paymentsClient.loadPaymentData(any())).thenReturn(it)

                useCase.validatePayment(price).test()
            },
            delay = 1000L,
            assertBlock = {
                it.assertValue(expectedGooglePayToken)
            }
        )
    }

    @Test
    fun validatePayment_nullifiesOngoingValidationOnTaskComplete() {
        val paymentData = mockPaymentDataWithToken("my token")

        createPaymentDataTask(paymentData)

        taskWithSuccess(
            paymentData,
            onTaskReadyBlock = {
                whenever(paymentsClient.loadPaymentData(any())).thenReturn(it)

                val observer = useCase.validatePayment(price()).test()

                assertNotNull(useCase.ongoingValidation)

                observer
            },
            delay = 1000L,
            assertBlock = {
                assertNull(useCase.ongoingValidation)
            }
        )
    }

    @Test
    fun validatePayment_emitsErrorIfOngoingValidationIsNotNull() {
        val price = price()
        useCase.ongoingValidation = OngoingValidation(price, mock())

        val task = TaskCompletionSource<PaymentData>().task
        whenever(paymentsClient.loadPaymentData(any())).thenReturn(task)

        useCase.validatePayment(price()).test().assertError(IllegalStateException::class.java)
    }

    /*
    If this test fails, it might be that the device screen is off
     */
    @Test
    fun maybeProcessActivityResult_emitsGooglePayIllegalResponseExceptionIfIntentIsNull() {
        mockPaymentDataTask()

        val observer = useCase.validatePayment(price()).test().assertNotComplete()

        activity.testOnActivityResult(
            requestCode = LOAD_PAYMENT_REQUEST_CODE,
            resultCode = Activity.RESULT_OK,
            data = null
        )

        observer.assertError(GooglePayIllegalResponseException::class.java)
    }

    @Test
    fun maybeProcessActivityResult_emitsGooglePayIllegalResponseExceptionIfPaymentDataCantBeReadFromIntent() {
        mockPaymentDataTask()

        val observer = useCase.validatePayment(price()).test().assertNotComplete()

        activity.testOnActivityResult(
            requestCode = LOAD_PAYMENT_REQUEST_CODE,
            resultCode = Activity.RESULT_OK,
            data = Intent()
        )

        observer.assertError(GooglePayIllegalResponseException::class.java)
    }

    @Test
    fun validatePayment_doesNothingIfResultIsNotLoadPayment() {
        mockPaymentDataTask()

        val observer = useCase.validatePayment(price()).test().assertNotComplete()

        activity.testOnActivityResult(
            requestCode = LOAD_PAYMENT_REQUEST_CODE + 1,
            resultCode = Activity.RESULT_OK,
            data = Intent()
        )

        observer.assertNotComplete()
    }

    @Test
    fun validatePayment_emitsOnCompleteIfResultCancelled() {
        mockPaymentDataTask()

        val observer = useCase.validatePayment(price()).test().assertNotComplete()

        activity.testOnActivityResult(
            requestCode = LOAD_PAYMENT_REQUEST_CODE,
            resultCode = Activity.RESULT_CANCELED,
            data = Intent()
        )

        observer.assertComplete()
    }

    @Test
    fun validatePayment_emitsIllegalStateExceptionIfUnknownResultError() {
        mockPaymentDataTask()

        val observer = useCase.validatePayment(price()).test().assertNotComplete()

        activity.testOnActivityResult(
            requestCode = LOAD_PAYMENT_REQUEST_CODE,
            resultCode = AutoResolveHelper.RESULT_ERROR,
            data = Intent()
        )

        observer.assertError(IllegalStateException::class.java)
    }

    @Test
    fun validatePayment_emitsApiExceptionIfIntentContainsStatus() {
        mockPaymentDataTask()

        val observer = useCase.validatePayment(price()).test().assertNotComplete()

        activity.testOnActivityResult(
            requestCode = LOAD_PAYMENT_REQUEST_CODE,
            resultCode = AutoResolveHelper.RESULT_ERROR,
            data = Intent().apply {
                putExtra(
                    "com.google.android.gms.common.api.AutoResolveHelper.status",
                    Status.RESULT_INTERNAL_ERROR
                )
            }
        )

        observer.assertError(ApiException::class.java)
    }

    /*
    Utils
     */

    private fun mockPaymentDataWithToken(expectedTolen: String): PaymentData {
        val tokenizationDataJson = JSONObject()
        tokenizationDataJson.put("token", expectedTolen)

        val billingAddressJson = JSONObject().apply {
            put("address1", "address1")
            put("address2", "address2")
            put("address3", "address3")
            put("sortingCode", "sortingCode")
            put("countryCode", "countryCode")
            put("postalCode", "postalCode")
            put("name", "name")
            put("locality", "locality")
            put("administrativeArea", "administrativeArea")
        }
        val cardInfoJson = JSONObject().apply {
            put("billingAddress", billingAddressJson)
        }

        val paymentMethodDataJson =
            JSONObject().apply {
                put("tokenizationData", tokenizationDataJson)
                put("info", cardInfoJson)
            }

        val shippingAddressJson = JSONObject().apply {
            put("address1", "address1")
            put("address2", "address2")
            put("address3", "address3")
            put("sortingCode", "sortingCode")
            put("countryCode", "countryCode")
            put("postalCode", "postalCode")
            put("name", "name")
            put("locality", "locality")
            put("administrativeArea", "administrativeArea")
        }

        val json = JSONObject().apply {
            put("paymentMethodData", paymentMethodDataJson)
            put("shippingAddress", shippingAddressJson)
        }

        return PaymentData.fromJson(json.toString())
    }

    private fun createPaymentDataTask(paymentData: PaymentData) {
        val task = TaskCompletionSource<PaymentData>().task
        whenever(paymentsClient.loadPaymentData(any())).thenReturn(task)
    }

    private fun mockPaymentDataTask() {
        val task = TaskCompletionSource<PaymentData>().task
        whenever(paymentsClient.loadPaymentData(any())).thenReturn(task)
    }
}

internal class FakeActivity : AppCompatActivity() {
    lateinit var test: PaymentDataRequestUseCaseImplTest

    fun testOnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        onActivityResult(requestCode, resultCode, data)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        test.useCase.maybeProcessActivityResult(requestCode, resultCode, data)
    }
}
