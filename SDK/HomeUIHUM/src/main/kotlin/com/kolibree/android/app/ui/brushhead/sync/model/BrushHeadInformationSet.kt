/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync.model

import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly

internal data class BrushHeadInformationSet(
    private val data: Set<BrushHeadInformation>
) : Set<BrushHeadInformation> by data, SynchronizableReadOnly
