/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import android.annotation.SuppressLint
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggle

/**
 * This class should be used when you want to test a piece of code where you need
 * a feature
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
class TestFeatureToggle<T : Any>(
    override val feature: Feature<T>,
    val initialValue: T = feature.initialValue
) : FeatureToggle<T> {

    override var value: T = initialValue
}
