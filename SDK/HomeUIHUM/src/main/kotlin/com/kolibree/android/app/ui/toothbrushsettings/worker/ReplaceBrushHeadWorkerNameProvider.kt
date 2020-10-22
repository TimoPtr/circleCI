/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.worker

import com.kolibree.android.worker.MacWorkerNameProvider
import javax.inject.Inject

internal class ReplaceBrushHeadWorkerNameProvider @Inject constructor() : MacWorkerNameProvider() {
    override fun getWorkerTag() = WORKER_TAG
}

private const val WORKER_TAG = "ReplaceBrushHeadWorker"
