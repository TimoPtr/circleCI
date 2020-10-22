/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.intro

import androidx.annotation.Keep
import androidx.annotation.RawRes
import androidx.annotation.StringRes

/**
 * UI configuration object for game intro fragment.
 */
@Keep
interface GameIntroResourceProvider {

    /**
     * @return ID of fragment header text
     */
    @StringRes
    fun headerTextResId(): Int

    /**
     * @return ID of fragment body text
     */
    @StringRes
    fun bodyTextResId(): Int

    /**
     * @return ID of animated GIF raw resource
     */
    @RawRes
    fun animatedGifResId(): Int

    /**
     * @return ID of fragment start button text
     */
    @StringRes
    fun startButtonTextResId(): Int
}
