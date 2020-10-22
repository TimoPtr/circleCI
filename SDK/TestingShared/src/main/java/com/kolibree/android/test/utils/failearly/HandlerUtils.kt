/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils.failearly

import android.os.Handler
import androidx.annotation.Keep
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever

@Keep
fun Handler.executeRunnablesImmediately() {
    whenever(post(any())).doAnswer {
        (it.getArgument(0) as Runnable).run()

        null
    }
}
