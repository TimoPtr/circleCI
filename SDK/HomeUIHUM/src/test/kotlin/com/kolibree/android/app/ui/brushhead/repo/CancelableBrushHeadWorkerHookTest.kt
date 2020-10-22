/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo

import com.kolibree.account.utils.ForgottenToothbrush
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.FakeWorkManager
import com.kolibree.android.app.ui.toothbrushsettings.worker.CancelableBrushHeadWorkerHook
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorkerNameProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.worker.LazyWorkManager
import org.junit.Test

class CancelableBrushHeadWorkerHookTest : BaseUnitTest() {
    private val workManager = FakeWorkManager()

    private val nameProvider = ReplaceBrushHeadWorkerNameProvider()

    private val hook = object :
        CancelableBrushHeadWorkerHook(
            object : LazyWorkManager {
                override fun get() = workManager
            }, nameProvider
        ) {}

    @Test
    fun `hook invokes cancelUniqueWork with provided name on subscription`() {
        val mac = KLTBConnectionBuilder.DEFAULT_MAC
        val accountId = 1L

        workManager.assertNoWorkCanceled()

        val forgottenToothbrush = ForgottenToothbrush(
            mac,
            KLTBConnectionBuilder.DEFAULT_SERIAL,
            accountId,
            KLTBConnectionBuilder.DEFAULT_OWNER_ID
        )
        val completable = hook.onForgottenCompletable(forgottenToothbrush)

        workManager.assertNoWorkCanceled()

        completable.test().assertComplete()

        workManager.assertWorkCanceledOnce(nameProvider.provide(mac))
    }
}
