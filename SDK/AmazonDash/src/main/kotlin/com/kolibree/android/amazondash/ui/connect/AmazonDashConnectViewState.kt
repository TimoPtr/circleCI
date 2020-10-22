/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import android.content.Context
import android.text.Spanned
import androidx.core.text.HtmlCompat
import com.kolibree.android.amazondash.R
import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class AmazonDashConnectViewState(
    val isLoading: Boolean,
    val isSuccess: Boolean
) : BaseViewState {

    @IgnoredOnParcel
    val congratulationsVisible = isSuccess

    @IgnoredOnParcel
    val confirmationButtonEnabled = !isLoading

    @IgnoredOnParcel
    val dismissButtonVisible = !isSuccess

    @IgnoredOnParcel
    val dismissButtonEnabled = !isLoading

    @IgnoredOnParcel
    val loadingVisible = isLoading

    @IgnoredOnParcel
    val logoRes = when {
        isSuccess -> R.drawable.amazon_alexa_logo
        else -> R.drawable.amazon_alexa_brushes_logo
    }

    @IgnoredOnParcel
    val titleRes = when {
        isLoading -> R.string.amazon_dash_connect_loading_title
        isSuccess -> R.string.amazon_dash_celebration_title
        else -> R.string.amazon_dash_connect_title
    }

    @IgnoredOnParcel
    val bodyRes = when {
        isLoading -> R.string.amazon_dash_connect_loading_body
        isSuccess -> R.string.amazon_dash_celebration_body
        else -> R.string.amazon_dash_connect_body
    }

    @IgnoredOnParcel
    val description: Function1<Context, Spanned> = { context ->
        val resource = when {
            isSuccess -> R.string.amazon_dash_celebration_description
            else -> R.string.amazon_dash_connect_description
        }

        HtmlCompat.fromHtml(context.getString(resource), 0)
    }

    @IgnoredOnParcel
    val confirmButtonRes = when {
        isSuccess -> R.string.amazon_dash_celebration_confirm_button
        else -> R.string.amazon_dash_connect_confirm_button
    }

    companion object {
        fun initial() = AmazonDashConnectViewState(
            isLoading = false,
            isSuccess = false
        )
    }
}
