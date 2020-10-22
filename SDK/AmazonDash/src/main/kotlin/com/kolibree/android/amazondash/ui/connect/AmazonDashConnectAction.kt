/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseAction
import org.threeten.bp.OffsetDateTime

internal sealed class AmazonDashConnectAction : BaseAction {

    data class ShowError(val error: Error) : AmazonDashConnectAction()

    data class SetResult(val sendRequestTime: OffsetDateTime) : AmazonDashConnectAction()
}
