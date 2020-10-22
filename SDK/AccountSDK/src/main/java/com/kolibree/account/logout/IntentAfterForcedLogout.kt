/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK

/**
 * Intent to be started after a Force logout is triggered
 *
 * By default, it will clear current Task and become the root of a new task
 *
 * See [FLAG_ACTIVITY_CLEAR_TASK] and [FLAG_ACTIVITY_NEW_TASK]
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
class IntentAfterForcedLogout
private constructor(
    context: Context,
    activityClazz: Class<out Any>
) : Intent(context, activityClazz) {
    companion object {
        /**
         * @param context
         * @param activityClazz a Class of a descendant of [Activity]
         * @return IntentAfterForcedLogout for class [T] with flags [FLAG_ACTIVITY_CLEAR_TASK]
         * and [FLAG_ACTIVITY_NEW_TASK]
         */
        @JvmStatic
        fun <T : Activity> create(
            context: Context,
            activityClazz: Class<T>
        ): IntentAfterForcedLogout {
            val intent = IntentAfterForcedLogout(context, activityClazz)

            intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK)

            return intent
        }
    }
}
