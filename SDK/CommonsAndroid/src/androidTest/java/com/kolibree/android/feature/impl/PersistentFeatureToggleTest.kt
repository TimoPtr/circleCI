/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.feature.impl

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.feature.Feature
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistentFeatureToggleTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private object InitiallyFalseFeature : Feature<Boolean> {
        override val initialValue: Boolean
            get() = false
        override val displayable: Boolean
            get() = true
        override val displayName: String
            get() = "InitiallyFalseFeature"
        override val requiresAppRestart: Boolean
            get() = false
    }

    private val initiallyFalseFeatureToggle = PersistentFeatureToggle(context(), InitiallyFalseFeature)

    private object InitiallyTrueFeature : Feature<Boolean> {
        override val initialValue: Boolean
            get() = true
        override val displayable: Boolean
            get() = true
        override val displayName: String
            get() = "InitiallyTrueFeature"
        override val requiresAppRestart: Boolean
            get() = false
    }

    private val initiallyTrueFeatureToggle = PersistentFeatureToggle(context(), InitiallyTrueFeature)

    @Test
    fun isEnabled_keepsTheInitialValueBeforeAnythingIsChanged() {
        assertFalse(initiallyFalseFeatureToggle.value)
        assertTrue(initiallyTrueFeatureToggle.value)
    }

    @Test
    fun isEnabled_reactsToChanges() {
        initiallyFalseFeatureToggle.value = true

        assertTrue(initiallyFalseFeatureToggle.value)
        assertTrue(initiallyTrueFeatureToggle.value)

        initiallyTrueFeatureToggle.value = false

        assertTrue(initiallyFalseFeatureToggle.value)
        assertFalse(initiallyTrueFeatureToggle.value)
    }
}
