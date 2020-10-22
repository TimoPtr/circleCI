/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.navigator

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * [ActivityResultContract] of Input and Output (`<I, O>`) where the Input `<I>` is an Intent delivered
 * by the [com.google.android.gms.auth.api.signin.GoogleSignInClient] and the Output `<O>`
 * is the data returned by the Google auth screen
 */
internal class GoogleAuthContract : ActivityResultContract<Intent, Intent?>() {

    override fun createIntent(context: Context, intent: Intent) = intent

    override fun parseResult(resultCode: Int, data: Intent?): Intent? {
        return if (resultCode == Activity.RESULT_OK) data else null
    }
}
