/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.navigation

import androidx.annotation.StringRes
import com.kolibree.android.annotation.VisibleForApp

/**
 * Navigation Helper Cards can rely on for generic actions
 */
@VisibleForApp
interface NavigationHelper {

    fun openUrl(url: String)

    fun showSomethingWentWrong()

    fun showSnackbarError(@StringRes messageResId: Int)

    fun showSnackbarError(message: String)
}
