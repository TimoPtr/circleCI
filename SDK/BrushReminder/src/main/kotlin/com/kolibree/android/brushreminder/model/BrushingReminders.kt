/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.model

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class BrushingReminders(
    val morningReminder: BrushingReminder,
    val afternoonReminder: BrushingReminder,
    val eveningReminder: BrushingReminder
) : Parcelable
