/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.inprogress

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.ota.R
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class InProgressOtaViewState(
    val progress: Int = 0,
    val isOtaSuccess: Boolean = false,
    val isOtaFailed: Boolean = false
) : BaseViewState {

    fun showResult(): Boolean = isOtaFailed || isOtaSuccess

    @DrawableRes
    fun resultIcon(): Int = if (isOtaFailed) R.drawable.ic_ota_fail else R.drawable.ic_ota_done

    @StringRes
    fun title(): Int = when {
            isOtaFailed -> R.string.ota_failure_title
            isOtaSuccess -> R.string.ota_done_title
            else -> R.string.in_progress_ota_title
        }

    @StringRes
    fun content(): Int = when {
        isOtaFailed -> R.string.ota_failure_content
        isOtaSuccess -> R.string.ota_done_content
        else -> R.string.in_progress_ota_content
    }

    companion object {
        fun initial() =
            InProgressOtaViewState()
    }
}
