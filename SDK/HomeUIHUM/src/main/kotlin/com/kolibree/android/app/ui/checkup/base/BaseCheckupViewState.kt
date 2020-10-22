/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.base

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import org.threeten.bp.OffsetDateTime

/** Checkup [BaseViewState] implementation */
@VisibleForApp
interface BaseCheckupViewState : BaseViewState {

    val coverage: Float?

    val durationPercentage: Float

    val durationSeconds: Long

    val game: String?

    val date: OffsetDateTime
}
