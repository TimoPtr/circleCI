/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package cn.colgate.colgateconnect.dagger

import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.kolibree.android.app.dagger.AppScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor

@Module
internal object FlipperModule {

    @Provides
    @AppScope
    fun providesNetworkFlipperPlugin() = NetworkFlipperPlugin()

    @Provides
    @IntoSet
    fun providesFlipperOkhttpInterceptor(plugin: NetworkFlipperPlugin): Interceptor =
        FlipperOkhttpInterceptor(plugin)
}
