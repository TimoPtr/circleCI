/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test

import android.content.Context
import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.reset
import org.junit.After
import org.junit.Before
import org.junit.Rule
import timber.log.Timber

abstract class BaseInstrumentationTest {

    // to make sure that Room executes all the database operations instantly.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val TEST_TREE = object : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            super.log(priority, tag, message, t)

            println(message)
        }
    }

    init {
        if (Timber.treeCount() == 0) {
            Timber.plant(TEST_TREE)
        }
    }

    protected abstract fun context(): Context

    @Before
    @CallSuper
    @Throws(Exception::class)
    open fun setUp() {
        AndroidThreeTen.init(context())
        TrustedClock.reset()
    }

    @After
    @CallSuper
    open fun tearDown() {
        // reserved
    }
}
