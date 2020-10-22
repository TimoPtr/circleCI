/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.feature.impl.TransientFeatureToggle
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

/*
Collection of sample feature toggle configuration.
Add it to the Dagger graph if you want to put them into action
 */
@Module
class SampleFeatureToggleModule {

    @Provides
    @IntoSet
    fun provideSampleStringBasedFeatureToggle(): FeatureToggle<*> {
        return ConstantFeatureToggle(SampleStringBasedFeature)
    }

    @AppScope
    @Provides
    @IntoSet
    fun provideSampleLongBasedFeatureToggle(): FeatureToggle<*> {
        return TransientFeatureToggle(SampleLongBasedFeature)
    }

    @Provides
    @IntoSet
    fun provideSampleBooleanBasedFeatureToggle(context: Context): FeatureToggle<*> {
        return PersistentFeatureToggle(context, SampleBooleanBasedFeature)
    }
}

@Keep
object SampleLongBasedFeature : Feature<Long> {

    private const val MIN_VALUE = 100L
    private const val MAX_VALUE = 1000L

    override val initialValue: Long = MIN_VALUE

    override fun validate(newValue: Long): Boolean {
        return newValue in MIN_VALUE..MAX_VALUE
    }

    override val displayable: Boolean
        get() = true

    override val displayName: String
        get() = "Sample Long Based Feature with Constraints"

    override val requiresAppRestart: Boolean
        get() = false
}

@Keep
object SampleStringBasedFeature : Feature<String> {

    override val initialValue: String
        get() = "Initial value"

    override val displayable: Boolean
        get() = true

    override val displayName: String
        get() = "Sample String Based Feature"

    override val requiresAppRestart: Boolean
        get() = false
}

@Keep
object SampleBooleanBasedFeature : Feature<Boolean> {

    override val initialValue: Boolean
        get() = true

    override val displayable: Boolean
        get() = true

    override val displayName: String
        get() = "Sample Boolean Based Feature"

    override val requiresAppRestart: Boolean
        get() = true
}
