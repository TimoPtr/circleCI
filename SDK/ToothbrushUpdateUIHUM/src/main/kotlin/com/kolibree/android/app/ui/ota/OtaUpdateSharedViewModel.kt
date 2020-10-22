/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import com.kolibree.android.app.Error

internal interface OtaUpdateSharedViewModel {

    fun showError(error: Error)

    fun hideError()

    fun showProgress(show: Boolean)
}
