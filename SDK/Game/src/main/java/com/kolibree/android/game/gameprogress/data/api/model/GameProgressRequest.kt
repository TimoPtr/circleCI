/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.api.model

import com.google.gson.annotations.SerializedName

internal data class GameProgressRequest(
    @SerializedName("progress") val progress: String
)
