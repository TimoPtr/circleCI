/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.content.Intent

fun getMainActivityIntentWithPushNotificationPayload(
    payload: Map<String, String>
): Intent {
    val intent = Intent()
    payload.entries.forEach { entry -> intent.putExtra(entry.key, entry.value) }
    return intent
}
