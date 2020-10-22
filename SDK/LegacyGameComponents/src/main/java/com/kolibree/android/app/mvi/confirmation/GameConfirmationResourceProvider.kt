/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.confirmation

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes

/**
 * UI configuration object for game confirmation fragment.
 */
@Keep
interface GameConfirmationResourceProvider {

    /**
     * @return ID of summary text
     */
    @StringRes
    fun summaryTextResId(): Int

    /**
     * @return ID of highlighted summary text
     */
    @StringRes
    fun summaryHighlightTextResId(): Int

    /**
     * @return ID of main drawable
     */
    @DrawableRes
    fun drawableResId(): Int
}
