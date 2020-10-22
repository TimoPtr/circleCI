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
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

internal class RxErrorHandlerAppInitializer @Inject constructor(
    private val errorHandler: Consumer<Throwable>
) : AppInitializer {

    override fun initialize(application: Application) {
        RxJavaPlugins.setErrorHandler(errorHandler)
    }
}
