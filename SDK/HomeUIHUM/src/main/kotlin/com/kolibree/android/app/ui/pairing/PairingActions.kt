/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseAction

internal sealed class PairingActions : BaseAction {
    data class ShowError(val error: Error) : PairingActions()
}
