/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import com.kolibree.android.app.base.BaseAction

internal sealed class CreateFakeBrushingActions : BaseAction {
    object DateClick : CreateFakeBrushingActions()
    object FutureDateSelected : CreateFakeBrushingActions()
    object InvalidAccount : CreateFakeBrushingActions()
}
