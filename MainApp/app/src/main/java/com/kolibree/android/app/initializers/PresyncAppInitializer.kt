/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Application
import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.app.ui.welcome.ExecutePresyncInstallationFlagsUseCase
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class PresyncAppInitializer @Inject constructor(
    private val executePresyncInstallationFlagsUseCase: ExecutePresyncInstallationFlagsUseCase
) : AppInitializer {

    override fun initialize(application: Application) {
        val throwable = executePresyncInstallationFlagsUseCase.executeInstallationFlags()
            .subscribeOn(Schedulers.io())
            .blockingGet()
        throwable?.let { Timber.e(it, "executePresyncInstallationFlagsUseCase failed") }
    }
}
