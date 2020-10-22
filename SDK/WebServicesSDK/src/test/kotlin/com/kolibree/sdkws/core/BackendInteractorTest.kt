package com.kolibree.sdkws.core

import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.network.NetworkLogFeature
import com.kolibree.android.network.NetworkLogFeatureToggle
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.core.AccessTokenManager
import com.kolibree.android.network.core.capabilities.ACCEPT_CAPABILITIES
import com.kolibree.android.network.core.capabilities.AcceptCapabilitiesHeaderProvider
import com.kolibree.android.network.core.useragent.USER_AGENT
import com.kolibree.android.network.core.useragent.UserAgentHeaderProvider
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.environment.Endpoint
import com.kolibree.android.network.errorhandler.NetworkErrorHandler
import com.kolibree.android.network.retrofit.DeviceParameters
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.sdkws.KolibreeUtils
import com.kolibree.sdkws.api.request.Request
import com.kolibree.sdkws.networking.RequestMethod
import com.kolibree.sdkws.networking.Response
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock

@Suppress("DEPRECATION")
internal class BackendInteractorTest : BaseUnitTest() {
    @Mock
    lateinit var kolibreeUtils: KolibreeUtils

    @Mock
    lateinit var networkChecker: NetworkChecker

    @Mock
    lateinit var errorHandler: NetworkErrorHandler

    @Mock
    lateinit var synchronizationScheduler: SynchronizationScheduler

    private val userAgentHeaderProvider = object : UserAgentHeaderProvider() {
        override val userAgentValue: String
            get() = TEST_USER_AGENT
    }

    private val acceptCapabilitiesHeaderProvider = object : AcceptCapabilitiesHeaderProvider() {
        override val capabilityValues: List<String>
            get() = testCapabilities
    }

    @Mock
    lateinit var accessTokenManager: AccessTokenManager

    @Mock
    lateinit var deviceParameters: DeviceParameters

    @Mock
    lateinit var accountDatastore: AccountDatastore

    @Mock
    lateinit var forceAppUpdater: InternalForceAppUpdater

    private val networkLogFeatureToggle = NetworkLogFeatureToggle(ConstantFeatureToggle(NetworkLogFeature))

    private lateinit var interactor: BackendInteractor

    override fun setup() {
        super.setup()

        interactor = spy(
            BackendInteractor(
                kolibreeUtils,
                createCredentialsProvider(),
                createEnvironmentProvider(),
                networkChecker,
                synchronizationScheduler,
                userAgentHeaderProvider,
                acceptCapabilitiesHeaderProvider,
                accessTokenManager,
                deviceParameters,
                accountDatastore,
                forceAppUpdater,
                errorHandler,
                setOf(networkLogFeatureToggle)
            )
        )
    }

    /*
    call
     */

    @Test
    fun call_noNetwork_returnsApiErrorResponseWithoutInvokingDoCallAndRefreshToken() {
        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        val response = interactor.call(mock(), "bla")

        assertTrue(response.error.isNetworkError)

        verify(interactor, never()).doCallAndRefreshTokenIfNeeded(
            any(),
            anyString()
        )
    }

    @Test
    fun call_noNetwork_invokesScheduleSynchronization() {
        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        interactor.call(mock(), "bla")

        verify(synchronizationScheduler).syncWhenConnectivityAvailable()
    }

    /*
  CREATE URL CONNECTION
   */
    @Test
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class, IOException::class)
    fun createHttpUrlConnection_hasUserAgentHeader() {
        val mockedRequest = mock<Request>()
        whenever(mockedRequest.url).thenReturn("en/")
        whenever(mockedRequest.method).thenReturn(RequestMethod.GET)
        whenever(mockedRequest.data).thenReturn(null)

        val connection = interactor.createHttpUrlConnection(mockedRequest, "bla")

        assertEquals(TEST_USER_AGENT, connection.getRequestProperty(USER_AGENT))
    }

    @Test
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class, IOException::class)
    fun createHttpUrlConnection_hasAcceptCapabilitiesHeader() {
        val mockedRequest = mock<Request>()
        whenever(mockedRequest.url).thenReturn("en/")
        whenever(mockedRequest.method).thenReturn(RequestMethod.GET)
        whenever(mockedRequest.data).thenReturn(null)

        val connection = interactor.createHttpUrlConnection(mockedRequest, "bla")

        assertEquals(
            testCapabilities.joinToString(","),
            connection.getRequestProperty(ACCEPT_CAPABILITIES)
        )
    }

    /*
    ACCOUNT DOES NOT EXIST
     */

    /*
    LOGIN, SIGN UP AND MAGIC LINK
     */

    @Test
    fun call_withNetwork_doCallAndRefreshTokenReturnsNetworkError_invokesDoCallAndRefreshTokenTwice() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        val request = mock<Request>()
        val token = "tpoken"

        val firstResponse = mock<Response>()
        whenever(firstResponse.error).thenReturn(ApiError.generateNetworkError())
        val secondResponse = mock<Response>()
        doReturn(firstResponse, secondResponse)
            .whenever(interactor)
            .doCallAndRefreshTokenIfNeeded(request, token)

        assertEquals(secondResponse, interactor.call(request, token))
    }

    @Test
    fun call_withNetwork_doCallAndRefreshTokenReturnsUnknownError_returnsResponse() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        val request = mock<Request>()
        val token = "tpoken"

        val firstResponse = mock<Response>()
        whenever(firstResponse.error).thenReturn(ApiError.generateUnknownError())
        doReturn(firstResponse).whenever(interactor).doCallAndRefreshTokenIfNeeded(request, token)

        assertEquals(firstResponse, interactor.call(request, token))
    }

    @Test
    fun call_withNetwork_doCallAndRefreshTokenReturnsAccountDoesNotExistError_returnsResponseWithApiError() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        val request = mock<Request>()
        val token = "tpoken"

        val response = mock<Response>()
        val apiError = ApiError("mes", 4, "details")
        whenever(response.error).thenReturn(apiError)
        doReturn(response).whenever(interactor).doCallAndRefreshTokenIfNeeded(request, token)

        assertEquals(response, interactor.call(request, token))
    }

    @Test
    fun call_errorHandlerReturnsApiErrorNonNetwork_returnsResponse() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        val request = mock<Request>()
        val token = "da"
        val callResponse = mock<Response>()
        whenever(callResponse.succeeded()).thenReturn(false)
        val apiError = ApiError("mes", 4, "details")
        whenever(callResponse.error).thenReturn(apiError)

        doReturn(callResponse).whenever(interactor).doCall(request, token)

        val response = interactor.call(request, token)

        assertEquals(callResponse, response)
    }

    /*
    utils
     */

    private fun createEnvironmentProvider(): Provider<Endpoint> {
        val environmentProvider = mock<Provider<Endpoint>>()
        val mockedEnvironment = mock<Endpoint>()
        whenever(mockedEnvironment.url()).thenReturn(URL)
        whenever(environmentProvider.get()).thenReturn(mockedEnvironment)
        return environmentProvider
    }

    private fun createCredentialsProvider(): Provider<Credentials> {
        val credentialsProvider = mock<Provider<Credentials>>()
        val mockedCredentials = mock<Credentials>()

        whenever(mockedCredentials.clientId()).thenReturn(CLIENT_ID)
        whenever(mockedCredentials.clientSecret()).thenReturn(CLIENT_SECRET)
        whenever(mockedCredentials.validateCredentials()).thenReturn(true)

        whenever(credentialsProvider.get()).thenReturn(mockedCredentials)
        return credentialsProvider
    }

    private companion object {
        private const val CLIENT_ID = "clientId"
        private const val CLIENT_SECRET = "secret"
        private const val URL = "http://kolibree.com/"
        private const val TEST_USER_AGENT = "TestUserAgent"
        private val testCapabilities = listOf("capability1", "capability2")
    }
}
