package com.kolibree.android.network

import androidx.annotation.Keep
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

@Keep
object NetworkConstants {

    const val SERVICE_REQUEST_TOKEN_V2 = "/v2/accounts/request_token/"

    const val SERVICE_REFRESH_TOKEN_V3 = "/v3/accounts/refresh_token/"

    @JvmField
    val DEFAULT_HTTP_CONNECTION_TIMEOUT: Duration = Duration.of(1, ChronoUnit.MINUTES)

    @JvmField
    @Suppress("MagicNumber")
    val DEFAULT_HTTP_READ_TIMEOUT: Duration = Duration.of(30, ChronoUnit.SECONDS)

    @JvmField
    @Suppress("MagicNumber")
    val DEFAULT_HTTP_WRITE_TIMEOUT: Duration = Duration.of(30, ChronoUnit.SECONDS)
}
