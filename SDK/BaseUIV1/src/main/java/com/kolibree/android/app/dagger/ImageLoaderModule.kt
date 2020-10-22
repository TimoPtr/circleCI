/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.imageloader.ImageLoader
import com.kolibree.android.app.imageloader.ImageLoaderImpl
import dagger.Binds
import dagger.Module

@Module
abstract class ImageLoaderModule {
    @Binds
    internal abstract fun bindsImageLoader(implementation: ImageLoaderImpl): ImageLoader
}
