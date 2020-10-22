/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.guidedbrushing

import com.kolibree.android.jaws.tilt.JawsTiltModule
import dagger.Binds
import dagger.Module

@Module(includes = [JawsTiltModule::class])
abstract class GuidedBrushingJawsModule {

    @Binds
    internal abstract fun bindGuidedBrushingJawsViewRenderer(
        impl: GuidedBrushingJawsViewRendererImpl
    ): GuidedBrushingJawsViewRenderer
}
