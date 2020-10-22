/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.test.espresso.intent.Intents.init
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
inline fun runAndCheckIntent(intentMatcher: Matcher<Intent>, resultData: Intent = Intent(), test: () -> Unit) = try {
    init()
    intending(intentMatcher)
        .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

    test()

    intended(intentMatcher)
} finally {
    release()
}

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun webViewIntentWithData(context: Context, @StringRes url: Int): Matcher<Intent> =
    webViewIntentWithData(context.getString(url))

@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
fun webViewIntentWithData(url: String): Matcher<Intent> = allOf<Intent>(
    hasAction(Intent.ACTION_VIEW),
    hasData(Uri.parse(url))
)
