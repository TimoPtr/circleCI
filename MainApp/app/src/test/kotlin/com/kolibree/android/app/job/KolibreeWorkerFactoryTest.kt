/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class KolibreeWorkerFactoryTest : BaseUnitTest() {
    private val context: Context = mock()

    private val workerParams: WorkerParameters = mock()

    @Test
    fun `factory returns worker instance when class exists`() {
        val fakeFactory = FakeFactory()
        val map =
            mapOf<Class<out ListenableWorker>, Factory>(FakeListenableWorker::class.java to fakeFactory)

        assertEquals(
            FakeListenableWorker,
            KolibreeWorkerFactory(map).createWorker(
                context,
                "com.kolibree.android.app.job.FakeListenableWorker",
                workerParams
            )
        )
    }

    @Test
    fun `factory returns null when class is not on the map`() {
        try {
            FailEarly.overrideDelegateWith(NoopTestDelegate)

            assertNull(
                KolibreeWorkerFactory(mapOf()).createWorker(
                    context,
                    "com.kolibree.android.app.job.FakeListenableWorker",
                    workerParams
                )
            )
        } finally {
            FailEarly.overrideDelegateWith(TestDelegate)
        }
    }

    @Test
    fun `factory returns null when class does not exist`() {
        try {
            FailEarly.overrideDelegateWith(NoopTestDelegate)

            assertNull(
                KolibreeWorkerFactory(mapOf()).createWorker(
                    context,
                    "com.kolibree.android.app.job.IDontExist",
                    workerParams
                )
            )
        } finally {
            FailEarly.overrideDelegateWith(TestDelegate)
        }
    }
}

private object FakeListenableWorker : ListenableWorker(mock(), mock()) {
    override fun startWork(): ListenableFuture<Result> {
        TODO("Not yet implemented")
    }
}

private class FakeFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return FakeListenableWorker
    }
}
