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
    fun `showPromotionsOptionAtSignUp returns true`() {
        assertTrue(appConfiguration.showPromotionsOptionAtSignUp)
    }

    @Test
    fun `allowDisablingDataSharing returns true`() {
        assertTrue(appConfiguration.allowDisablingDataSharing)
    }

    @Test
    fun `showHeadspaceRelatedContent returns false`() {
        assertFalse(appConfiguration.showHeadspaceRelatedContent)
    }
}
