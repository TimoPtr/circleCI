/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AppConfigurationImplTest : BaseUnitTest() {

    private val appConfiguration = AppConfigurationImpl

    @Test
    fun `showPromotionsOptionAtSignUp returns false`() {
        assertFalse(appConfiguration.showPromotionsOptionAtSignUp)
    }

    @Test
    fun `allowDisablingDataSharing returns false`() {
        assertFalse(appConfiguration.allowDisablingDataSharing)
    }

    @Test
    fun `showHeadspaceRelatedContent returns true`() {
        assertTrue(appConfiguration.showHeadspaceRelatedContent)
    }
}
