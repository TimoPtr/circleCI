/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.domain.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

/**
 * This class wrap the progress of a given gameID,
 * The gameID is known by the backend and the UnityGame not the app.
 * Progress is a raw JSON that Unity creates/updates
 */
@Keep
@Parcelize
data class GameProgress(
    val gameId: String,
    val progress: String,
    val updatedAt: ZonedDateTime
) : Parcelable
