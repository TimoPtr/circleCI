/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.data.persistence.model

import androidx.room.TypeConverter
import com.kolibree.android.commons.ShortTask

internal class ShortTaskConverters {

    @TypeConverter
    fun fromString(value: String): ShortTask {
        return checkNotNull(ShortTask.fromInternalValue(value)) { "$value doesn't match any short task" }
    }

    @TypeConverter
    fun toString(shortTask: ShortTask): String = shortTask.internalValue
}
