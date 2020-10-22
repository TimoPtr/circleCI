/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.extensions

import android.os.Handler
import androidx.annotation.Keep
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.mockito.ArgumentMatchers.anyLong

@Keep
fun Handler.postDelayedImmediateRun() {
    whenever(postDelayed(any(), anyLong())).thenAnswer {
        it.getArgument(0, Runnable::class.java).run()
        true
    }
}

@Keep
fun Handler.postImmediateRun() {
    whenever(post(any())).thenAnswer {
        it.getArgument(0, Runnable::class.java).run()
        true
    }
}

@Keep
fun fakeImmediateHandler(): Handler = mock<Handler>().apply {
    postImmediateRun()
    postDelayedImmediateRun()
}
