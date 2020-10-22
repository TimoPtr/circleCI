/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature.impl

import androidx.annotation.CallSuper
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.checkIfFeatureTypeIsSupported

/**
 * In-memory feature toggle. Value is reset to feature's initial value upon app restart.
 */
@Keep
class TransientFeatureToggle<T : Any>(override val feature: Feature<T>) : FeatureToggle<T> {

    override var value: T = feature.initialValue

    init {
        checkIfFeatureTypeIsSupported()
    }

    /**
     * A dedicated companion class for [TransientFeatureToggle].
     * Very useful if we need to toggle the value that is stored inside repository/database and
     * we don't want to keep a copy of that value anywhere else.
     * In such case [TransientFeatureToggle] will act as a facade for this value.
     *
     * @param featureToggleSet all available feature toggles in the app
     * @param associatedFeature feature we want to associate the companion with
     */
    @Keep
    abstract class Companion<T : Any>(
        featureToggleSet: FeatureToggleSet,
        associatedFeature: Feature<T>
    ) : FeatureToggle.Companion<T>(featureToggleSet, associatedFeature) {

        init {
            FailEarly.failInConditionMet(
                toggle !is TransientFeatureToggle<T>,
                "TransientFeatureToggle.Companion can be associated only with TransientFeatureToggle," +
                    "please check your implementation"
            )
        }

        @CallSuper
        override fun initialize() {
            toggle.value = getInitialValue()
        }

        protected abstract fun getInitialValue(): T
    }
}
