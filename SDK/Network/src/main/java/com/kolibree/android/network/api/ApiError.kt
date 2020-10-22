package com.kolibree.android.network.api

import androidx.annotation.Keep
import com.kolibree.android.network.api.ApiErrorCode.ACCOUNT_EMAIL_ALREADY_EXIST
import com.kolibree.android.network.api.ApiErrorCode.NETWORK_ERROR
import com.kolibree.android.network.api.ApiErrorCode.PARENTAL_CONSENT_REQUIRED
import com.kolibree.android.network.api.ApiErrorCode.UNKNOWN_ERROR
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by aurelien on 27/07/15.
 *
 * SDK WS error model
 */
@Keep
class ApiError : Exception {

    override var message: String? = null
        private set

    var internalErrorCode = 0
        private set

    var details: String? = null
        private set

    private var displayMessage: String? = null

    var httpCode = 0
        private set

    @Throws(JSONException::class)
    constructor(rawJson: String?) {
        if (rawJson != null) {
            val json = JSONObject(rawJson)
            message = json.optString(FIELD_MESSAGE)
            internalErrorCode = json.getInt(FIELD_INTERNAL_ERROR_CODE)
            details = json.optString(FIELD_DETAIL)
            displayMessage = json.optString(FIELD_DISPLAY_MESSAGE)
            httpCode = json.optInt(FIELD_HTTP_CODE)
        }
    }

    constructor(message: String?, internalErrorCode: Int, details: String?) {
        this.message = message
        this.internalErrorCode = internalErrorCode
        this.details = details
    }

    constructor(e: Exception) : this(e.message, UNKNOWN_ERROR, "Internal error")

    val isNetworkError: Boolean
        get() = internalErrorCode == NETWORK_ERROR

    val isUnknownError: Boolean
        get() = internalErrorCode == UNKNOWN_ERROR

    val isParentalConsentError: Boolean
        get() = internalErrorCode == PARENTAL_CONSENT_REQUIRED

    val isEmailAlreadyUsed: Boolean
        get() = internalErrorCode == ACCOUNT_EMAIL_ALREADY_EXIST

    val displayableMessage: String?
        get() = if (displayMessage != null && displayMessage!!.isNotEmpty()) {
            displayMessage
        } else message

    override fun toString(): String {
        return """
            Message: $message
            Display message: $displayMessage
            Details: $details
            Internal error code: $internalErrorCode
            HTTP code: $httpCode
            """.trimIndent()
    }

    companion object {
        private const val FIELD_MESSAGE = "message"
        private const val FIELD_INTERNAL_ERROR_CODE = "internal_error_code"
        private const val FIELD_DETAIL = "detail"
        private const val FIELD_DISPLAY_MESSAGE = "display_message"
        private const val FIELD_HTTP_CODE = "http_code"

        @JvmStatic
        fun generateNetworkError(): ApiError {
            return ApiError(
                "Network unavailable",
                NETWORK_ERROR,
                "Unable to establish a connection"
            )
        }

        @JvmStatic
        @JvmOverloads
        fun generateUnknownError(httpCode: Int = 0): ApiError {
            return ApiError(
                "Unknown error",
                UNKNOWN_ERROR,
                "An internal error occurred, please report to developers@kolibree.com"
            ).apply {
                this.httpCode = httpCode
            }
        }
    }
}
