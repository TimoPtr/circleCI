package com.kolibree.android.network.retrofit

import android.util.Base64
import com.kolibree.android.extensions.getAcceptedLanguageHeader
import com.kolibree.android.network.ACCEPT_LANGUAGE
import com.kolibree.android.network.API_CLIENT_ACCESS_TOKEN
import com.kolibree.android.network.API_CLIENT_ID_HEADER
import com.kolibree.android.network.API_CLIENT_SIG_HEADER
import com.kolibree.android.network.API_DATA_TYPE
import com.kolibree.android.network.API_DEVICE_PARAMETERS
import com.kolibree.android.network.core.AccessTokenManager
import com.kolibree.android.network.core.capabilities.AcceptCapabilitiesHeaderProvider
import com.kolibree.android.network.core.useragent.UserAgentHeaderProvider
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.prepareUrlForSignatureCalculation
import dagger.Lazy
import java.io.IOException
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Provider
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * Adds client and id and client signature headers to all retrofit requests
 *
 * In a future version, it should also add an access token if it exists. Or, we could delegate
 * access token header to a different class.
 */
internal class HeaderInterceptor @Inject
internal constructor(
    private val credentialsProvider: Provider<Credentials>,
    private val deviceParameters: DeviceParameters,
    private val accessTokenManager: Lazy<AccessTokenManager>,
    private val userAgentHeaderProvider: UserAgentHeaderProvider,
    private val acceptCapabilitiesHeaderProvider: AcceptCapabilitiesHeaderProvider
) : Interceptor {

    private val accessToken: String?
        get() = accessTokenManager.get().getAccessToken()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        val (userAgentHeaderKey, userAgentHeaderValue) = userAgentHeaderProvider.userAgent
        builder.addHeader(userAgentHeaderKey, userAgentHeaderValue)
        val (acceptCapabilitiesHeaderKey, acceptCapabilitiesHeaderValue) =
            acceptCapabilitiesHeaderProvider.capabilities
        builder.addHeader(acceptCapabilitiesHeaderKey, acceptCapabilitiesHeaderValue)

        builder.addHeader(ACCEPT_HEADER, API_DATA_TYPE)
        builder.addHeader(ACCEPT_LANGUAGE, getAcceptedLanguageHeader())
        builder.addHeader(API_DEVICE_PARAMETERS, deviceParameters.encrypt())
        builder.addHeader(API_CLIENT_ID_HEADER, credentialsProvider.get().clientId())

        val accessToken = accessToken
        if (accessToken != null) {
            builder.addHeader(API_CLIENT_ACCESS_TOKEN, accessToken)
        }

        try {
            builder.addHeader(API_CLIENT_SIG_HEADER, clientSignature(chain))
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
        } catch (e: InvalidKeyException) {
            Timber.e(e)
        }

        return chain.proceed(builder.build())
    }

    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    private fun clientSignature(chain: Interceptor.Chain): String {
        val sha256HMAC = Mac.getInstance(ALGORITHM)

        val secretKeySpec = SecretKeySpec(
            String(
                credentialsProvider.get().clientSecret().toByteArray(),
                CHARSET
            ).toByteArray(),
            ALGORITHM
        )

        sha256HMAC.init(secretKeySpec)

        val urlUTF8 = String(
            chain.request().url().url().toString()
                .prepareUrlForSignatureCalculation()
                .toByteArray(),
            CHARSET
        )
        sha256HMAC.update(urlUTF8.toByteArray())
        return Base64.encodeToString(
            sha256HMAC.doFinal(),
            Base64.NO_WRAP
        )
    }

    companion object {
        private const val ACCEPT_HEADER = "Accept"

        private const val ALGORITHM = "HmacSHA256"

        private val CHARSET = Charset.forName("UTF-8")
    }
}
