/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core.feature

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.MarkAccountAsBetaFeature
import com.kolibree.android.feature.impl.TransientFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
object MarkAccountAsBetaFeatureToggleModule {

    @Provides
    @AppScope
    @IntoSet // this toggle is transient so that's the only way to preserve in-mem value
    fun provideMarkAccountAsBetaFeatureToggleIntoSet(): FeatureToggle<*> {
        return TransientFeatureToggle(MarkAccountAsBetaFeature)
    }

    @Provides
    @IntoSet
    fun provideMarkAccountAsBetaFeatureToggleCompanionIntoSet(
        companion: MarkAccountAsBetaFeatureToggleCompanion
    ): FeatureToggle.Companion<*> = companion
}
