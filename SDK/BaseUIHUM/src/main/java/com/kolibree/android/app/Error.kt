/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import android.os.Parcelable
import androidx.annotation.StringRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error.ErrorStyle.Indefinite
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.api.ApiError
import kotlinx.android.parcel.Parcelize

/**
 * An Error object used to configure error messages inside the application
 */
@VisibleForApp
@Parcelize
data class Error(
    val exception: Throwable? = null,
    val message: String? = null,
    @StringRes val messageId: Int? = null,
    val style: ErrorStyle,
    @StringRes val buttonTextId: Int? = null
) : Parcelable {
    init {
        if (message == null && messageId == null && exception == null) {
            FailEarly.fail("At least one field in Error has to be set")
        }
    }

    @VisibleForApp
    companion object {

        fun from(exception: Throwable, style: ErrorStyle = Indefinite) =
            when (exception) {
                is ApiError -> Error(
                    message = exception.displayableMessage,
                    style = style
                )
                else -> Error(
                    exception = exception,
                    style = style
                )
            }

        fun from(message: String, style: ErrorStyle = Indefinite) =
            Error(message = message, style = style)

        fun from(@StringRes messageId: Int, style: ErrorStyle = Indefinite, @StringRes buttonTextId: Int? = null) =
            Error(
                messageId = messageId,
                style = style,
                buttonTextId = buttonTextId
            )
    }

    @VisibleForApp
    sealed class ErrorStyle : Parcelable {

        /**
         * Specify the error to stay on the screen until user interact with it
         */
        @VisibleForApp
        @Parcelize
        object Indefinite : ErrorStyle()

        /**
         * Specify the error should be dismissed automatically without interactions
         */
        @VisibleForApp
        @Parcelize
        object AutoDismiss : ErrorStyle()
    }
}
