/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.commons.ToothbrushModel

/**
 *   Created by guillaume agis on 22/5/18.
 */
@Entity(tableName = "models_available")
data class ModelAvailable(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "model") val model: ToothbrushModel,
    @ColumnInfo(name = "is_available") val isAvailable: Boolean = true
)
