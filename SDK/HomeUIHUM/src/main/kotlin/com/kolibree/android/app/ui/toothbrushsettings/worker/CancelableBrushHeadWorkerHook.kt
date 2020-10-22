/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.worker

import com.kolibree.account.utils.ForgottenToothbrush
import com.kolibree.account.utils.ToothbrushForgottenHook
import com.kolibree.android.worker.LazyWorkManager
import com.kolibree.android.worker.MacWorkerNameProvider
import io.reactivex.Completable
import javax.inject.Inject

/**
 * This abstract class is responsible to cancel the Workers of each toothbrushes.
 * As this is implementing [ToothbrushForgottenHook], you must add its subclass @IntoSet.
 */
internal abstract class CancelableBrushHeadWorkerHook
constructor(
    private val workManager: LazyWorkManager,
    private val nameProvider: MacWorkerNameProvider
) : ToothbrushForgottenHook {

    override fun onForgottenCompletable(toothbrush: ForgottenToothbrush): Completable {
        return Completable.fromAction {
            workManager.get().cancelUniqueWork(nameProvider.provide(toothbrush.mac))
        }
    }
}

internal class CancelReplaceBrushHeadWorkerHook @Inject constructor(
    workManager: LazyWorkManager,
    nameProvider: ReplaceBrushHeadWorkerNameProvider
) : CancelableBrushHeadWorkerHook(workManager, nameProvider)

internal class CancelSyncBrushHeadWorkerHook @Inject constructor(
    workManager: LazyWorkManager,
    nameProvider: SyncBrushHeadWorkerNameProvider
) : CancelableBrushHeadWorkerHook(workManager, nameProvider)
