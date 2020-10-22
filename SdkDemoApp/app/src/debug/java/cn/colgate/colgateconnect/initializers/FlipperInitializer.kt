/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package cn.colgate.colgateconnect.initializers

import android.app.Application
import cn.colgate.colgateconnect.BuildConfig
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import javax.inject.Inject

internal class FlipperInitializer @Inject constructor(
    private val networkFlipperPlugin: NetworkFlipperPlugin
) {

    fun initialize(application: Application) {
        SoLoader.init(application, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(application)) {
            val client = AndroidFlipperClient.getInstance(application)
            client.addPlugin(InspectorFlipperPlugin(application, DescriptorMapping.withDefaults()))
            client.addPlugin(networkFlipperPlugin)
            client.addPlugin(SharedPreferencesFlipperPlugin(application))
            client.addPlugin(DatabasesFlipperPlugin(application))
            client.start()
        }
    }
}
