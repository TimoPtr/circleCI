/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import android.app.Activity
import android.view.View
import com.facebook.testing.screenshot.RecordBuilder
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.internal.TestNameDetector

interface Paparazzi {

    fun snap(activity: Activity, snapName: String)

    fun snap(view: View, snapName: String)
}

object RealPaparazzi : Paparazzi {

    override fun snap(activity: Activity, snapName: String) {
        Screenshot.snapActivity(activity)
            .name(snapName)
            .record()
    }

    override fun snap(view: View, snapName: String) {
        Screenshot.snap(view)
            .name(snapName)
            .record()
    }
}

private fun RecordBuilder.name(snapName: String): RecordBuilder {
    val name = "${TestNameDetector.getTestClass()
        .replace('.', '_')}_${TestNameDetector.getTestName()}_$snapName"
    return setName(name)
}

object FakePaparazzi : Paparazzi {

    override fun snap(activity: Activity, snapName: String) {
        // no-op
    }

    override fun snap(view: View, snapName: String) {
        // no-op
    }
}
