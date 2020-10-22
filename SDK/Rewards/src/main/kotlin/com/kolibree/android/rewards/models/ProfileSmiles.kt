/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly

/**
 * Interface to be consumed by non-persistence clients
 */
@Keep
interface ProfileSmiles {
    val profileId: Long
    val smiles: Int
}

@Keep
@Entity(tableName = "profile_smiles")
internal data class ProfileSmilesEntity(
    @PrimaryKey override val profileId: Long,
    override val smiles: Int
) : SynchronizableReadOnly, ProfileSmiles
