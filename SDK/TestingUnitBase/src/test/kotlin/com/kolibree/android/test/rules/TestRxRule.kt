/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.rules

import com.kolibree.android.app.test.BaseUnitTest
import io.reactivex.Observable
import java.io.IOException
import org.junit.Ignore
import org.junit.Test
import timber.log.Timber

class TestRxRule : BaseUnitTest() {

    @Test
    @Ignore("This test should always fail")
    fun test() {
        Observable.create<String> {
            it.onNext("test")
            it.onComplete()
            it.onError(IOException()) // added this line
        }.subscribe({
            Timber.d(it)
        }, {
            Timber.d(it)
        }, {
            Timber.d("onCompleted")
        })
    }
}
