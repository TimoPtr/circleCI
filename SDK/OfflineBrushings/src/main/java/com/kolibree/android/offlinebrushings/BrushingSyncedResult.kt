/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import androidx.annotation.Keep
import org.threeten.bp.OffsetDateTime

@Keep
sealed class BrushingSyncedResult(val profileId: Long, val mac: String, val dateTime: OffsetDateTime)

@Keep
class OrphanBrushingSyncedResult(profileId: Long, mac: String, dateTime: OffsetDateTime) :
    BrushingSyncedResult(profileId, mac, dateTime)
@Keep
class OfflineBrushingSyncedResult(profileId: Long, mac: String, dateTime: OffsetDateTime) :
    BrushingSyncedResult(profileId, mac, dateTime)
