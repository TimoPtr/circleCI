/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature.impl

import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.checkIfFeatureTypeIsSupported

/**
 * Prevents value changes for the feature. Can be useful when feature has to be hidden from secret settings.
 */
@Keep
class ConstantFeatureToggle<T : Any>(
    override val feature: Feature<T>,
    private val initialValue: T = feature.initialValue
) : FeatureToggle<T> {

    override var value: T
        get() = initialValue
        set(_) { FailEarly.fail("Value of ConstantFeatureToggle($feature) cannot be changed") }

    init {
        checkIfFeatureTypeIsSupported()
    }
}
