package com.kolibree.android.network.retrofit

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.internal.RefreshTokenProvider
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.API_CLIENT_ACCESS_TOKEN
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode.ACCESS_TOKEN_HAS_EXPIRED
import com.kolibree.android.network.core.AccessTokenManager
import com.kolibree.android.network.token.TokenRefresher
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

internal class TokenAuthenticator
@Inject constructor(
    // Lazy is needed to avoid Dagger hanging up trying to compose the graph
    private val tokenRefresher: dagger.Lazy<TokenRefresher>,
    private val accessTokenManager: dagger.Lazy<AccessTokenManager>,
    private val accountDatastore: AccountDatastore
) : Authenticator {
    private companion object {
        const val MAX_RETRIES = 3
        const val ONE_KILOBYTE = 1024L
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code() == HTTP_UNAUTHORIZED) {
            return onAuthenticationError(response)
        }

        return null
    }

    @VisibleForTesting
    @Suppress("NestedBlockDepth")
    fun onAuthenticationError(response: Response): Request? {
        Timber.d(
            "TokenAuthenticator onError isAccessTokenError %s, should refresh %s",
            isAccessTokenExpiredError(response),
            shouldReattemptRequest(response)
        )
        if (shouldReattemptRequest(response) && isAccessTokenExpiredError(response)) {
            refreshToken()?.let { storedRefreshToken ->
                doRefreshTokenCall(storedRefreshToken)?.let { refreshedToken ->
                    accessTokenManager.get().updateTokens(refreshedToken).blockingAwait()

                    accessTokenManager.get().getAccessToken()?.let { accessToken ->
                        Timber.d(
                            "TokenAuthenticator performing request %s",
                            response.request().url().url()
                        )
                        return response.request().newBuilder()
                            .header(API_CLIENT_ACCESS_TOKEN, accessToken)
                            .build()
                    }
                }
            }
        }

        accessTokenManager.get().notifyUnableToRefreshToken()

        return null
    }

    @Suppress("ReturnCount")
    private fun doRefreshTokenCall(refreshToken: String): RefreshTokenProvider? {
        if (accessTokenManager.get().getAccessToken() != null) {
            return tokenRefresher.get().refreshToken(
                        accessToken = accessTokenManager.get().getAccessToken()!!,
                        refreshToken = refreshToken
                ).blockingGet()
        }

        FailEarly.fail("Cannot refresh token! Account doesn't have appId, phone number and email, " +
                " and access token is unavailable!")
        return null
    }

    private fun shouldReattemptRequest(response: Response): Boolean {
        var previousAttempts = 1
        var innerResponse = response

        while (innerResponse.priorResponse() != null) {
            previousAttempts++

            innerResponse = innerResponse.priorResponse()!!
        }

        return previousAttempts < MAX_RETRIES
    }

    private fun isAccessTokenExpiredError(response: Response): Boolean {
        // use peekBody to avoid consuming the body
        val body = response.peekBody(ONE_KILOBYTE)?.string() ?: return false

        val apiError = ApiError(body)

        return apiError.internalErrorCode == ACCESS_TOKEN_HAS_EXPIRED
    }

    private fun refreshToken(): String? = account()?.refreshToken

    private fun email(): String? = account()?.email

    private fun phoneNumber(): String? = account()?.phoneNumber

    private fun appId(): String? = account()?.appId

    private fun account(): AccountInternal? = accountDatastore.getAccountMaybe().blockingGet()
}
