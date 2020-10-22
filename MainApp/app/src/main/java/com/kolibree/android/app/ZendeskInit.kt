/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app

import android.content.Context
import com.kolibree.R
import com.kolibree.android.extensions.takeIfNotBlank
import com.kolibree.android.failearly.FailEarly
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.Support

internal fun Context.initZendesk() {
    getString(R.string.help_center_url).takeIfNotBlank()?.let { helpCenterUrl ->
        getString(R.string.zendesk_app_id).takeIfNotBlank()?.let { appId ->
            getString(R.string.zendesk_client_id).takeIfNotBlank()?.let { clientId ->

                Zendesk.INSTANCE.init(this, helpCenterUrl, appId, clientId)
                Support.INSTANCE.init(Zendesk.INSTANCE)
                Zendesk.INSTANCE.setIdentity(AnonymousIdentity())
            } ?: FailEarly.fail("Zendesk clientId is not set correctly")
        } ?: FailEarly.fail("Zendesk appId is not set correctly")
    } ?: FailEarly.fail("Zendesk url is not set correctly")

    FailEarly.failInConditionMet(!Support.INSTANCE.isInitialized, "Zendesk is not initialized")
}
