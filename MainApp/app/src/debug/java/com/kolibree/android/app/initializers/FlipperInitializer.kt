/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import com.kolibree.android.app.initializers.base.AppInitializer
import javax.inject.Inject

internal class FlipperInitializer @Inject constructor(
    private val networkFlipperPlugin: NetworkFlipperPlugin
) : AppInitializer {

    override fun initialize(application: Application) {
        SoLoader.init(application, false)

        if (FlipperUtils.shouldEnableFlipper(application)) {
            AndroidFlipperClient.getInstance(application).apply {
                addPlugin(networkFlipperPlugin)
                addPlugin(SharedPreferencesFlipperPlugin(application))
                addPlugin(DatabasesFlipperPlugin(application))
                start()
            }
        }
    }
}
