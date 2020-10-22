/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.binding

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SettingsBindingModelTest : BaseUnitTest() {
    @Test
    fun `VibrationItemBindingModel has default visibility=false`() {
        assertFalse(VibrationLevelsItemBindingModel().isVisible())
    }

    @Test
    fun `AboutItemBindingModel has visibility=true`() {
        assertTrue(AboutItemBindingModel.isVisible())
    }

    @Test
    fun `TermsAndConditionsBindingModel has visibility=true`() {
        assertTrue(TermsAndConditionsBindingModel.isVisible())
    }

    @Test
    fun `PrivacyPolicyItemBindingModel has visibility=true`() {
        assertTrue(PrivacyPolicyItemBindingModel.isVisible())
    }

    @Test
    fun `HelpItemBindingModel has visibility=true`() {
        assertTrue(HelpItemBindingModel.isVisible())
    }

    @Test
    fun `SecretSettingsBindingModel has visibility=true`() {
        assertTrue(SecretSettingsBindingModel.isVisible())
    }
}
