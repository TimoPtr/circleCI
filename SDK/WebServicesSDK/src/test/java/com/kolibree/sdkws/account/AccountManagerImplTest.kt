package com.kolibree.sdkws.account

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.internal.AccountInternalAdapter
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.persistence.ProfileInternalAdapter
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.gson.LocalDateTypeAdapter
import com.kolibree.android.network.api.ApiError
import com.kolibree.retrofit.ParentalConsentTypeAdapter
import com.kolibree.sdkws.account.AccountManagerImpl.Companion.RESPONSE_CODE_UNKNOWN_WECHAT_ACCOUNT
import com.kolibree.sdkws.account.models.BrushSyncReminderRequest
import com.kolibree.sdkws.account.models.BrushSyncReminderResponse
import com.kolibree.sdkws.account.models.EmailNewsletterSubscriptionData
import com.kolibree.sdkws.account.models.GoogleLoginRequestBody
import com.kolibree.sdkws.account.models.GoogleSignUpRequestBody
import com.kolibree.sdkws.account.models.PhoneWeChatLinked
import com.kolibree.sdkws.account.models.VerifyUniqueNumberRequest
import com.kolibree.sdkws.account.models.VerifyUniqueNumberResponse
import com.kolibree.sdkws.api.response.VerificationTokenResponse
import com.kolibree.sdkws.data.model.PhoneNumberData
import com.kolibree.sdkws.data.request.BetaData
import com.kolibree.sdkws.data.request.CreateAccountData
import com.kolibree.sdkws.data.request.UpdateAccountV3Data
import com.kolibree.sdkws.exception.WeChatAccountNotRecognizedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import java.io.IOException
import junit.framework.TestCase.assertEquals
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.threeten.bp.LocalDate
import retrofit2.Response

const val PACKAGE_NAME = "com.package.name.under.test"

class AccountManagerImplTest : BaseUnitTest() {

    @Mock
    internal lateinit var context: Context

    @Mock
    internal lateinit var accountApi: AccountApi

    @Mock
    internal lateinit var accountDatastore: AccountDatastore

    private lateinit var accountManager: AccountManagerImpl

    private var gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(ParentalConsent::class.java, ParentalConsentTypeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .create()

    @Throws(Exception::class)
    override fun setup() {
        super.setup()
        doReturn(PACKAGE_NAME).whenever(context).packageName
        doReturn(context).whenever(context).applicationContext

        gson = gson.newBuilder()
            .registerTypeAdapter(ProfileInternal::class.java, ProfileInternalAdapter(gson))
            .create()

        gson = gson.newBuilder()
            .registerTypeAdapter(AccountInternal::class.java, AccountInternalAdapter(gson))
            .create()

        accountManager = spy(AccountManagerImpl(context, accountApi, accountDatastore, gson))
    }

    /*
  CREATE ANONYMOUS ACCOUNT

  In the future this should take responsibility of persisting the account, notifying observers that
  the account has changed, etc.
   */

    @Test
    fun createAnonymousAccount_callsAccountApiCreateAnonymousAccount() {
        val data = mock(CreateAccountData::class.java)

        val expectedSingle = SingleSubject.create<Response<AccountInternal>>()
        whenever(accountApi.createAccount(data)).thenReturn(expectedSingle)

        accountManager.createAnonymousAccount(data)
        verify(accountApi).createAccount(data)
    }

    /*
  UPDATE ACCOUNT
   */

    @Test
    fun updateAccount_callsAccountApiUpdateAccount() {
        val data = mock(UpdateAccountV3Data::class.java)

        val accountId: Long = 84

        val expectedSingle = SingleSubject.create<Response<AccountInternal>>()
        whenever(accountApi.updateAccount(accountId, data)).thenReturn(expectedSingle)

        accountManager.updateAccount(accountId, data)
        verify(accountApi).updateAccount(accountId, data)
    }

    /*
  UPDATE BETA
   */
    @Test
    fun updateAccount_callsAccountApiUpdateBetaAccount() {
        val data = mock(BetaData::class.java)
        val id = 78L
        val expectedSingle = SingleSubject.create<Response<AccountInternal>>()
        whenever(accountApi.updateBetaAccount(id, data)).thenReturn(expectedSingle)

        accountManager.updateBetaAccount(id, data)
        verify(accountApi).updateBetaAccount(id, data)
    }

    @Test
    fun sendSmsCode() {
        val response = mock<Response<VerificationTokenResponse>>()
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(VerificationTokenResponse(""))
        whenever(accountApi.sendSmsCode(any())).thenReturn(
            Single.just<Response<VerificationTokenResponse>>(
                response
            )
        )
        val phoneNumber = "+100200300"
        accountManager.sendSmsCode(phoneNumber)
        verify(accountApi).sendSmsCode(phoneNumber)
    }

    @Test
    fun createAccountBySms() {
        val data = mock(CreateAccountData::class.java)
        val account = mock(AccountInternal::class.java)
        whenever(accountApi.createAccountBySms(any())).thenReturn(
            Single.just(
                Response.success(
                    account
                )
            )
        )
        accountManager.createAccountBySms(data)
        verify(accountApi).createAccountBySms(data)
    }

    @Test
    fun loginBySms() {
        val body = mock(AccountInternal::class.java)
        val phone = "+101202303"
        val code = "123456"
        val token = "pH0n3_T0kEn"
        whenever(accountApi.loginBySms(phone, code, token, null, null))
            .thenReturn(Single.just(Response.success(body)))
        val data = PhoneNumberData(phone, token, code)

        accountManager.loginBySms(data).blockingGet()

        verify(accountApi).loginBySms(phone, code, token, null, null)
    }

    @Test
    fun createEmailAccount() {
        val data = mock<CreateAccountData>()
        val body = AccountInternal()
        val response = mock<Response<AccountInternal>>()
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(body)
        whenever(accountApi.createEmailAccount(data)).thenReturn(Single.just(response))

        accountManager.createEmailAccount(data).blockingGet()

        verify(accountApi).createEmailAccount(data)
    }

    /*
    registerWithGoogle
    */
    @Test
    fun `registerWithGoogle with valid data invokes API and ends with success`() {
        val data = CreateAccountData.builder()
            .setFirstName("Paul")
            .setEmail("paul@atreides.cl")
            .setCountry("Caladan")
            .setIsBetaAccount(false)
            .setParentalConsentGiven(true)
            .setGoogleId("muad'dib")
            .setGoogleIdToken("kwisatz.haderach")
            .build()
        val requestBody = GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
        val responseBody = AccountInternal()
        val response = mock<Response<AccountInternal>>()
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(responseBody)
        whenever(accountApi.registerWithGoogle(requestBody)).thenReturn(Single.just(response))

        accountManager.registerWithGoogle(data).blockingGet()

        verify(accountApi).registerWithGoogle(requestBody)
    }

    /*
    loginByGoogle
    */
    @Test
    fun `loginWithGoogle with valid data invokes API and ends with success`() {
        val data = CreateAccountData.builder()
            .setEmail("paul@atreides.cl")
            .setGoogleId("muad'dib")
            .setGoogleIdToken("kwisatz.haderach")
            .build()
        val requestBody = GoogleLoginRequestBody.createFrom(PACKAGE_NAME, data)
        val responseBody = AccountInternal()
        val response = mock<Response<AccountInternal>>()
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(responseBody)
        whenever(accountApi.loginByGoogle(requestBody)).thenReturn(Single.just(response))

        accountManager.loginByGoogle(data).blockingGet()

        verify(accountApi).loginByGoogle(requestBody)
    }

    /*
  attemptLoginWithWechat
   */

    @Test
    @Throws(IOException::class)
    fun attemptLoginWithWechat_emitsInternalAccount_whenHttpCodeIs200() {
        val responseBody = mock(ResponseBody::class.java)
        val body = AccountInternal()
        val gson = Gson()
        whenever(responseBody.string()).thenReturn(gson.toJson(body))
        whenever(accountApi.attemptLoginWithWechat(any())).thenReturn(
            Single.just(
                Response.success(
                    responseBody
                )
            )
        )

        accountManager
            .internalAttemptLoginWithWechat("")
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue(body)
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun attemptLoginWithWechat_emitsToken_whenHttpCodeIs202() {
        val responseBody = mock(ResponseBody::class.java)
        val jsonObject = JsonObject()
        val expectedToken = "1234"
        jsonObject.addProperty("token", expectedToken)
        whenever(responseBody.string()).thenReturn(jsonObject.toString())

        whenever(accountApi.attemptLoginWithWechat(any())).thenReturn(
            Single.just(
                Response.success(
                    RESPONSE_CODE_UNKNOWN_WECHAT_ACCOUNT,
                    responseBody
                )
            )
        )

        accountManager
            .internalAttemptLoginWithWechat("")
            .test()
            .await()
            .assertError { error ->
                error is WeChatAccountNotRecognizedException && error
                    .loginAttemptToken == expectedToken
            }
    }

    /*
    verifyUniqueNumber
     */
    @Test
    fun `checkPhoneNumberAssociation returns a valid PhoneWeChatLined`() {
        val testPhoneNumber = "phone number"
        val testVerificationToken = "verification token"
        val testVerificationCode = "verification code"
        val verifyUniqueNumberResponse = VerifyUniqueNumberResponse(true, true)
        val response = mock<Response<VerifyUniqueNumberResponse>>()

        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(verifyUniqueNumberResponse)
        whenever(
            accountApi.checkPhoneNumberAssociation(
                VerifyUniqueNumberRequest(
                    testPhoneNumber,
                    testVerificationToken,
                    testVerificationCode
                )
            )
        )
            .thenReturn(Single.just(response))

        accountManager.checkPhoneNumberAssociation(
            testPhoneNumber,
            testVerificationToken,
            testVerificationCode
        )
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(
                PhoneWeChatLinked(true, true)
            )
    }

    @Test
    fun `checkPhoneNumberAssociation emits ApiError`() {
        val testPhoneNumber = "phone number"
        val testVerificationToken = "verification token"
        val testVerificationCode = "verification code"
        val response = mock<Response<VerifyUniqueNumberResponse>>()

        whenever(response.isSuccessful).thenReturn(false)
        whenever(
            accountApi.checkPhoneNumberAssociation(
                VerifyUniqueNumberRequest(
                    testPhoneNumber,
                    testVerificationToken,
                    testVerificationCode
                )
            )
        )
            .thenReturn(Single.just(response))

        accountManager.checkPhoneNumberAssociation(
            testPhoneNumber,
            testVerificationToken,
            testVerificationCode
        )
            .test()
            .assertError {
                it is ApiError
            }
    }

    @Test
    fun `isEmailNewsletterSubscriptionEnabled returns value from internal account object`() {
        val account = mock<AccountInternal>()
        whenever(account.emailNewsletterSubscription)
            .thenReturn(true)
        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(account))

        accountManager.isEmailNewsletterSubscriptionEnabled()
            .test()
            .assertValue(true)
        verify(accountDatastore).getAccountMaybe()
    }

    @Test
    fun `emailNewsletterSubscription executes an appropriate API call`() {
        val response = Response.success(ResponseBody.create(null, "body"))
        whenever(accountApi.emailNewsletterSubscription(any(), any()))
            .thenReturn(Single.just(response))

        accountManager.emailNewsletterSubscription(18L, true)
            .test()
            .assertComplete()

        verify(accountApi).emailNewsletterSubscription(18L, EmailNewsletterSubscriptionData(true))
    }

    @Test
    fun `emailNewsletterSubscription updates newsletterSubscription field on success`() {
        val response = Response.success(ResponseBody.create(null, "body"))
        whenever(accountApi.emailNewsletterSubscription(any(), any()))
            .thenReturn(Single.just(response))

        accountManager.emailNewsletterSubscription(997, false).test()

        verify(accountDatastore).updateNewsletterSubscription(false)
    }

    @Test
    fun `setBrushSyncReminderEnabled starts with update request`() {
        val accountId = 1L
        val profileId = 1L
        val enabled = false

        val response = Response.success(ResponseBody.create(null, "body"))

        whenever(accountApi.setBrushSyncReminderEnabled(any(), any(), any()))
            .thenReturn(Single.just(response))

        val observer = accountManager
            .setBrushSyncReminderEnabled(accountId, profileId, enabled)
            .test()

        verify(accountApi).setBrushSyncReminderEnabled(
            accountId,
            profileId,
            BrushSyncReminderRequest(isActive = enabled)
        )
        observer.assertComplete()
        verifyNoMoreInteractions(accountApi)
    }

    @Test
    fun `setBrushSyncReminderEnabled sends create request if update fails`() {
        val accountId = 1L
        val profileId = 1L
        val enabled = false

        val errorResponse = Response.error<ResponseBody>(409, ResponseBody.create(null, ""))
        val successResponse = Response.success(ResponseBody.create(null, "body"))

        whenever(accountApi.setBrushSyncReminderEnabled(any(), any(), any()))
            .thenReturn(Single.just(errorResponse))

        whenever(accountApi.createBrushSyncReminder(any(), any(), any()))
            .thenReturn(Single.just(successResponse))

        val observer = accountManager
            .setBrushSyncReminderEnabled(accountId, profileId, enabled)
            .test()

        verify(accountApi).setBrushSyncReminderEnabled(
            accountId,
            profileId,
            BrushSyncReminderRequest(isActive = enabled)
        )
        verify(accountApi).createBrushSyncReminder(
            accountId,
            profileId,
            BrushSyncReminderRequest(isActive = enabled)
        )
        observer.assertComplete()
        verifyNoMoreInteractions(accountApi)
    }

    @Test
    fun `getBrushSyncReminderEnabled returns isActive value`() {
        val accountId = 1L
        val profileId = 1L

        fun checkResponse(isActive: Boolean) {
            whenever(accountApi.getBrushSyncReminderEnabled(accountId, profileId))
                .thenReturn(Single.just(Response.success(BrushSyncReminderResponse(isActive))))

            val observer = accountManager
                .getBrushSyncReminderEnabled(accountId, profileId)
                .test()

            observer.assertValue(isActive)
            observer.assertComplete()
        }

        checkResponse(isActive = true)
        checkResponse(isActive = false)
    }

    /*
  Companion
   */

    @Test
    fun valueOf_RESPONSE_CODE_UNKNOWN_WECHAT_ACCOUNT_is202() {
        assertEquals(202, RESPONSE_CODE_UNKNOWN_WECHAT_ACCOUNT)
    }
}
