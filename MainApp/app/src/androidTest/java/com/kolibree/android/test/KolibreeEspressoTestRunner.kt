/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.test

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.facebook.testing.screenshot.ScreenshotRunner
import com.kolibree.BuildConfig

// Do not move this class to the SDK as it was making the SDK tests very slow,
// even if not used.
@Suppress("unused")
class KolibreeEspressoTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, KolibreeEspressoTestApplication::class.qualifiedName, context)
    }

    override fun onCreate(args: Bundle) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.ENABLE_SCREENSHOT_TESTING) {
            ScreenshotRunner.onCreate(this, args)
        }
        super.onCreate(args)
    }

    override fun finish(resultCode: Int, results: Bundle) {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.ENABLE_SCREENSHOT_TESTING) {
            ScreenshotRunner.onDestroy()
        }
        super.finish(resultCode, results)
    }
}
