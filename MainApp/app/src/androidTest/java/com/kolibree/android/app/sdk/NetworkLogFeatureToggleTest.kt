/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.sdk

import android.content.Intent
import com.kolibree.android.sdk.NetworkLogFeatureToggleTestBase
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule

class NetworkLogFeatureToggleTest : NetworkLogFeatureToggleTestBase<InjectionTestActivity>() {

    override fun createRuleForActivity(): KLBaseActivityTestRule<InjectionTestActivity> {
        return KolibreeActivityTestRule.Builder(InjectionTestActivity::class.java)
            .launchActivity(false)
            .build()
    }

    override fun launchActivity() {
        activityTestRule.launchActivity(Intent(context(), InjectionTestActivity::class.java))
    }
}
