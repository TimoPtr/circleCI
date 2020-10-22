/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.content.Context
import android.content.Intent

fun getMainActivityIntentWithPushNotificationPayload(
    context: Context,
    payload: Map<String, String>
): Intent {
    val intent = Intent(context, HomeScreenActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    payload.entries.forEach { entry -> intent.putExtra(entry.key, entry.value) }
    return intent
}
